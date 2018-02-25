package layout.algo.execution;

import com.yworks.yfiles.graph.IGraph;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import javax.swing.*;

/**
 * Same as {@link BasicIGraphLayoutExecutor} with the addition of a progress bar.
 */
public class IGraphLayoutExecutor extends BasicIGraphLayoutExecutor {
  private final JProgressBar progressBar;

  public IGraphLayoutExecutor(ILayout layout,
                              IGraph graph,
                              JProgressBar progressBar,
                              int maxIterations,
                              int numberOfCyclesBetweenViewUpdates,
                              ILayoutInterfaceItemFactory itemFactory) {
    super(layout, graph, maxIterations, numberOfCyclesBetweenViewUpdates, itemFactory);
    this.progressBar = progressBar;
  }

  protected void updateProgress(int iteration) {
    synchronized (progressBar) {
      progressBar.setValue((int) (100 * iteration / (float) getMaxIterations()));
    }
  }
}
