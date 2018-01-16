package layout.algo;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.ICompoundEdit;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;

import javax.swing.*;
import java.util.Map;

public class IGraphLayoutExecutor extends BasicIGraphLayoutExecutor {
  protected final JProgressBar progressBar;

  public IGraphLayoutExecutor(ILayout layout,
                              IGraph graph,
                              JProgressBar progressBar,
                              int maxIterations,
                              int numberOfCyclesBetweenViewUpdates) {
    super(layout, graph, maxIterations, numberOfCyclesBetweenViewUpdates);
    this.progressBar = progressBar;
  }

  protected void updateProgress(int iteration) {
    synchronized (progressBar) {
      progressBar.setValue((int) (100 * iteration / (float) maxIterations));
    }
  }
}
