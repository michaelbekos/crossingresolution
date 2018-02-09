package layout.algo;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.ICompoundEdit;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;

public class BasicIGraphLayoutExecutor {
  private final IGraph graph;
  private final ILayout layout;
  int maxIterations;
  private final int numberOfCyclesBetweenViewUpdates;
  private boolean running;
  private boolean finished;
  private int currentIteration;
  private ICompoundEdit compoundEdit;
  private PropertyChangeSupport propertyChange;

  public BasicIGraphLayoutExecutor(ILayout layout,
                                   IGraph graph,
                                   int maxIterations,
                                   int numberOfCyclesBetweenGraphUpdates) {
    this.layout = layout;
    this.graph = graph;
    this.maxIterations = maxIterations;
    this.numberOfCyclesBetweenViewUpdates = numberOfCyclesBetweenGraphUpdates;
    this.currentIteration = 0;
    this.running = false;
    this.finished = false;
    this.propertyChange = new PropertyChangeSupport(this);
  }

  public void start() {
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
    synchronized (BasicIGraphLayoutExecutor.this) {
      notify();
    }
  }

  private void reset() {
    running = false;
    finished = false;
    currentIteration = 0;
  }

  private void run() {
    //iterations < 0 infinite loop
    //iterations > 0 runs for # of iterations
    new Thread(() -> {
      layout.init();

      mainLoop:
      while (!finished) {
        while (running) {
          finished = layout.executeStep(currentIteration++);

          if (finished || maxIterations > 0 && currentIteration == maxIterations) {
            stop();
            break mainLoop;
          }

          if (currentIteration % numberOfCyclesBetweenViewUpdates == 0) {
            updateGraph(layout.getNodePositions());
          }

          updateProgress(currentIteration);
        }

        synchronized (BasicIGraphLayoutExecutor.this) {
          try {
            BasicIGraphLayoutExecutor.this.wait();
          } catch (InterruptedException e) {
            stop();
          }
        }
      }
      reset();
      synchronized (BasicIGraphLayoutExecutor.this) {
        BasicIGraphLayoutExecutor.this.notifyAll();
      }
    }).start();
  }

  private void updateGraph(Mapper<INode, PointD> nodePositions) {
    synchronized (graph) {
      for (Map.Entry<INode, PointD> entry : nodePositions.getEntries()) {
        graph.setNodeCenter(entry.getKey(), entry.getValue());
      }
    }
  }

  protected void updateProgress(int iteration) {
    System.out.println(iteration + "/" + maxIterations);
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

  public synchronized void waitUntilFinished() {
    try {
      wait();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void setMaxIterations(int maxIterations) {
    this.maxIterations = maxIterations;
    if (this.currentIteration >= maxIterations) {
      this.currentIteration = maxIterations;
      stop();
    }
  }
}