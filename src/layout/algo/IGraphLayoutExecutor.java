package layout.algo;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.ICompoundEdit;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;

import javax.swing.*;
import java.util.Map;

public class IGraphLayoutExecutor {
  private final IGraph graph;
  private final ILayout layout;
  private final JProgressBar progressBar;
  private final int maxIterations;
  private final int numberOfCyclesBetweenViewUpdates;

  public IGraphLayoutExecutor(ILayout layout, IGraph graph, JProgressBar progressBar, int maxIterations, int numberOfCyclesBetweenViewUpdates) {
    this.graph = graph;
    this.layout = layout;
    this.progressBar = progressBar;
    this.maxIterations = maxIterations;
    this.numberOfCyclesBetweenViewUpdates = numberOfCyclesBetweenViewUpdates;
  }

  public void run() {
    new Thread(() -> {
      ICompoundEdit compoundEdit;
      synchronized (graph) {
        compoundEdit = graph.beginEdit("Undo layout", "Redo layout");
      }

      layout.init();

      for (int i = 0; i < maxIterations; i++) {
        boolean finished = layout.executeStep(i);
        if (finished) {
          break;
        }

        if (i % numberOfCyclesBetweenViewUpdates == 0) {
          updateView(layout.getNodePositions());
        }

        updateProgressBar(i);
      }

      updateProgressBar(0);
      updateView(layout.getNodePositions());

      synchronized (graph) {
        compoundEdit.commit();
      }
    }).start();
  }

  private void updateView(Mapper<INode, PointD> nodePositions) {
    synchronized (graph) {
      for (Map.Entry<INode, PointD> entry : nodePositions.getEntries()) {
        graph.setNodeCenter(entry.getKey(), entry.getValue());
      }
    }
  }

  private void updateProgressBar(int iteration) {
    synchronized (progressBar) {
      progressBar.setValue((int) (100 * iteration / (float) maxIterations));
    }
  }
}
