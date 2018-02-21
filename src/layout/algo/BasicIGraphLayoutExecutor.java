package layout.algo;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.ICompoundEdit;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;
import util.GraphModifier;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class BasicIGraphLayoutExecutor {
  private final IGraph graph;
  private final ILayout layout;
  final AbstractLayoutInterfaceItem<Integer> maxIterations;
  private final int numberOfCyclesBetweenViewUpdates;
  private volatile boolean running;
  private volatile boolean finished;
  private volatile int currentIteration;
  private ICompoundEdit compoundEdit;
  private PropertyChangeSupport propertyChange;

  private final ReentrantLock activeLock = new ReentrantLock();
  private final ExecutorService executorService;

  public BasicIGraphLayoutExecutor(ILayout layout,
                                   IGraph graph,
                                   int maxIterations,
                                   int numberOfCyclesBetweenGraphUpdates,
                                   ILayoutInterfaceItemFactory itemFactory) {
    this.layout = layout;
    this.graph = graph;
    this.numberOfCyclesBetweenViewUpdates = numberOfCyclesBetweenGraphUpdates;
    this.currentIteration = 0;
    this.running = false;
    this.finished = false;
    this.propertyChange = new PropertyChangeSupport(this);

    this.maxIterations = itemFactory.intParameter("Maximum number of iterations", -1, 10000);
    this.maxIterations.setValue(maxIterations);

    executorService = Executors.newSingleThreadExecutor();
  }

  public void start() {
    if (graph.getNodes().size() == 0) {
      return;
    }

    if (!running) {
      running = true;
      synchronized (this.graph) {
        compoundEdit = graph.beginEdit("Undo layout", "Redo layout");
      }
      run();
    }
  }

  public void stop() {
    if (running) {
      synchronized (graph) {
        compoundEdit.commit();
      }

      updateProgress(0);
      updateGraph(layout.getNodePositions());
      running = false;
      finished = true;
      propertyChange.firePropertyChange("finished", false, true);
    }
    reset();
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertyChange.addPropertyChangeListener(listener);
  }

  public void pause()
  {
    running = false;
  }

  public void unpause() {
    running = true;
    run();
  }

  private void reset() {
    running = false;
    finished = false;
    currentIteration = 0;
  }

  private void run() {
    //iterations < 0 infinite loop
    //iterations > 0 runs for # of iterations
    executorService.submit(() -> {
      try {
        activeLock.lock();

        if (currentIteration == 0) {
          layout.init();
        }

        while (running && !finished) {
          finished = layout.executeStep(currentIteration, maxIterations.getValue());

          synchronized (this) {
            currentIteration++;
          }

          if (finished || maxIterations.getValue() > 0 && currentIteration == maxIterations.getValue()) {
            stop();
            return;
          }

          if (currentIteration % numberOfCyclesBetweenViewUpdates == 0) {
            updateGraph(layout.getNodePositions());
          }

          updateProgress(currentIteration);
        }

        if (finished) {
          stop();
        }

      } finally {
        activeLock.unlock();
      }
    });
  }

  private void updateGraph(Mapper<INode, PointD> nodePositions) {
    synchronized (graph) {
      for (Map.Entry<INode, PointD> entry : nodePositions.getEntries()) {
        graph.setNodeCenter(entry.getKey(), entry.getValue());
      }
    }
  }

  protected void updateProgress(int iteration) {
    System.out.println(iteration + "/" + maxIterations.getValue());
  }

  public boolean isRunning() {
    return running;
  }

  public boolean isPaused() {
    return (!isRunning() && !isFinished() && currentIteration > 0);
  }

  public boolean isFinished() {
    return finished;
  }

  public void waitUntilFinished() throws InterruptedException {
    activeLock.lockInterruptibly();
    activeLock.unlock();
  }

  public void setMaxIterations(int maxIterations) {
    this.maxIterations.setValue(maxIterations);
    if (currentIteration >= maxIterations) {
      currentIteration = maxIterations;
      stop();
    }
  }

  public int getMaxIterations() {
    return maxIterations.getValue();
  }

  public ILayout getLayout() {
    return layout;
  }

  private final ReentrantLock lock = new ReentrantLock();
  public void modifyGraph(GraphModifier modifier) {
    lock.lock();
    boolean wasRunning = running && !finished;

    if (wasRunning) {
      running = false;
      try {
        waitUntilFinished();
      } catch (InterruptedException e) {
        throw new RuntimeException("Interrupted while waiting for executor to stop", e);
      }
      reset();
    }

    modifier.modify();

    if (wasRunning) {
      running = true;
      run();
    }

    lock.unlock();
  }

}
