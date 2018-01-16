package layout.algo;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.ICompoundEdit;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;

import java.util.Map;

public class BasicIGraphLayoutExecutor {
  private final IGraph graph;
  private final ILayout layout;
  final int maxIterations;
  private final int numberOfCyclesBetweenViewUpdates;

  public BasicIGraphLayoutExecutor(ILayout layout,
                                   IGraph graph,
                                   int maxIterations,
                                   int numberOfCyclesBetweenGraphUpdates) {
    this.layout = layout;
    this.graph = graph;
    this.maxIterations = maxIterations;
    this.numberOfCyclesBetweenViewUpdates = numberOfCyclesBetweenGraphUpdates;
  }

  public void run() {
    new Thread(() -> {
      ICompoundEdit compoundEdit;
      synchronized (graph) {
        compoundEdit = graph.beginEdit("Undo layout", "Redo layout");
      }

      layout.init();

      for (int i = 0; i < maxIterations || maxIterations == -1; i++) {
        boolean finished = layout.executeStep(i);
        if (finished) {
          break;
        }

        if (i % numberOfCyclesBetweenViewUpdates == 0) {
          updateGraph(layout.getNodePositions());
        }

        updateProgress(i);
      }

      updateProgress(0);
      updateGraph(layout.getNodePositions());

      synchronized (graph) {
        compoundEdit.commit();
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
}
