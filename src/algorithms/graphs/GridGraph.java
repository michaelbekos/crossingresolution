package algorithms.graphs;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import util.Util;

public final class GridGraph {
  private GridGraph() {}

  public static boolean isGridGraph(IGraph graph) {
    return graph.getNodes().stream()
        .allMatch(node -> {
          PointD center = node.getLayout().getCenter();
          return Util.isInteger(center.getX()) && Util.isInteger(center.getY());
//            return Util.isAlmostInteger(center.getX()) && Util.isAlmostInteger(center.getY());  //TODO: fix with yFiles bug
        });
  }
}
