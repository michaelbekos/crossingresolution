package graphoperations;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.view.GraphComponent;

import java.util.Map;

public final class Scaling {
  private Scaling() {}

  /**
   * Move all node coordinates to positive values and multiply them by a factor
   */
  public static void scaleBy(double factor, Mapper<INode, PointD> nodePositions) {
    double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;

    for (Map.Entry<INode, PointD> entry : nodePositions.getEntries()) {
      INode node = entry.getKey();
      if (node.getLayout().getCenter().getX() < minX) {
        minX = node.getLayout().getCenter().getX();
      }
      if (node.getLayout().getCenter().getY() < minY) {
        minY = node.getLayout().getCenter().getY();
      }
    }

    for (Map.Entry<INode, PointD> entry : nodePositions.getEntries()) {
      INode node = entry.getKey();
      PointD center = node.getLayout().getCenter();
      nodePositions.setValue(node, new PointD(
          (center.getX() - minX) * factor,
          (center.getY() - minY) * factor)
      );
    }
  }

  public static void scaleNodeSizes(GraphComponent view) {
      IGraph graph = view.getGraph();
      double scaleValue = 1 / view.getZoom(); // scale reinserted nodes
      for (INode u : graph.getNodes()) {
          graph.setNodeLayout(u, new RectD(
              u.getLayout().getX(),
              u.getLayout().getY(),
              graph.getNodeDefaults().getSize().width * scaleValue,
              graph.getNodeDefaults().getSize().height * scaleValue
          ));
      }
  }
}
