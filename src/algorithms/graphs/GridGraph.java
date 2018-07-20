package algorithms.graphs;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
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
     * rounds value of nodes to in order for it to be gridded (Yfiles bug)
     * @param graph
     */
  public static void roundGraphToGrid(IGraph graph) {
      for (INode u : graph.getNodes()) {
          if (u.getLayout().getCenter().getX() % 1 != 0 || u.getLayout().getCenter().getY() %1 !=0) {
              System.out.println("before " +u.getLayout().getCenter());
              graph.setNodeCenter(u, new PointD(Math.round(u.getLayout().getCenter().getX()), Math.round(u.getLayout().getCenter().getY())));
              System.out.println("after " + u.getLayout().getCenter());
          }
      }
  }
}
