package layout.algo.execution;

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

/**
 * This class executes algorithms that implement the {@link ILayout} interface.
 *
 * The algorithm itself will run in it's own thread. However, all calls to methods of {@link ILayout} are guaranteed to
 * run only in this thread and this class itself takes care of all necessary synchronization. Thus, neither the algorithm
 * nor the caller-side of this class need to care about synchronization.
 */
public class BasicIGraphLayoutExecutor {
  private final IGraph graph;
  private final ILayout layout;
  private final AbstractLayoutInterfaceItem<Integer> maxIterations;
  private final int numberOfCyclesBetweenViewUpdates;
  private volatile boolean running;
  private volatile boolean finished;
  private volatile int currentIteration;
  private ICompoundEdit compoundEdit;
  private final PropertyChangeSupport propertyChange;

  private final ReentrantLock activeLock = new ReentrantLock();
  private final ReentrantLock stoppingLock = new ReentrantLock();
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

    this.executorService = Executors.newSingleThreadExecutor();
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
    if (!stoppingLock.tryLock()) {
      return;
    }
    if (running) {
      finished = true;
      stopAndWait();
      finish();
    }
    reset();
    stoppingLock.unlock();
  }

  private void stopAndWait() {
    running = false;
    try {
      waitUntilFinished();
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while waiting for executor to stop", e);
    }
  }

  private void finish() {
    updateGraph(layout.getNodePositions());

    synchronized (graph) {
      compoundEdit.commit();
    }

    updateProgress(0);
    finished = true;
    propertyChange.firePropertyChange("finished", false, true);
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
          boolean finished = layout.executeStep(currentIteration);

          if (finished || maxIterations.getValue() > 0 && currentIteration == maxIterations.getValue()) {
            layout.finish(currentIteration);
            finish();
            return;
          }

          synchronized (this) {
            currentIteration++;
          }

          if (currentIteration % numberOfCyclesBetweenViewUpdates == 0) {
            updateGraph(layout.getNodePositions());
          }

          updateProgress(currentIteration);
        }

        if (finished) {
          layout.finish(currentIteration - 1);
          finish();
        }

      } finally {
        activeLock.unlock();
      }
    });
  }

  private void updateGraph(Mapper<INode, PointD> nodePositions) {
    propertyChange.firePropertyChange("removeListeners", false, true);
    synchronized (graph) {
      for (Map.Entry<INode, PointD> entry : nodePositions.getEntries()) {
        graph.setNodeCenter(entry.getKey(), entry.getValue());
      }
    }
    propertyChange.firePropertyChange("removeListeners", true, false);
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

  /**
   * This method allows for modification of the graph while the algorithm is running.
   *
   * Internally, the algorithm will be stopped (if it's actually running), then the callback will be called and
   * afterwards the algorithm will be started again (if it was running). Currently, this will result in resetting the
   * iterations count and calling {@link ILayout#init()} method again. Some algorithms may have problems with this.
   * @param modifier the function that modifies the graph
   */
  public void modifyGraph(GraphModifier modifier) {
    if (!stoppingLock.tryLock()) {
      return;
    }
    boolean wasRunning = running && !finished;

    if (wasRunning) {
      stopAndWait();
      reset();
    }

    modifier.modify();

    if (wasRunning) {
      running = true;
      run();
    }

    stoppingLock.unlock();
  }

}
