package algorithms.graphs;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.view.ISelectionModel;
import util.Util;

public final class GridGraph {
  private GridGraph() {}

  public static boolean isGridGraph(IGraph graph) {
    return graph.getNodes().stream()
        .allMatch(node -> {
          PointD center = node.getLayout().getCenter();
          return Util.isInteger(center.getX()) && Util.isInteger(center.getY());
        });
  }

    /**
     * rounds value of selected nodes (i.e. with drag n' drop) in order for them to be gridded (Yfiles bug)
     * @param graph
     */
  public static void roundGraphToGrid(IGraph graph, ISelectionModel<INode> selectedNodes) {
      for (INode u : selectedNodes) {
          if (u.getLayout().getCenter().getX() % 1 != 0 || u.getLayout().getCenter().getY() %1 !=0) {
              graph.setNodeCenter(u, new PointD(Math.round(u.getLayout().getCenter().getX()), Math.round(u.getLayout().getCenter().getY())));
          }
      }
  }
}
