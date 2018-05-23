package graphoperations;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.view.GraphComponent;
import util.BoundingBox;

import java.util.Map;

public final class Scaling {
  private Scaling() {}

  public static void scaleBy(double factor, Mapper<INode, PointD> nodePositions, double maxBoxSize) {
    RectD bounds = BoundingBox.from(nodePositions);
    double maxFactor = Math.min(maxBoxSize / bounds.getWidth(), maxBoxSize / bounds.getHeight());
    scaleByCore(Math.min(factor, maxFactor), nodePositions, bounds);
  }

    /**
     * Move all node coordinates to positive values and multiply them by a factor
     */
  public static void scaleBy(double factor, Mapper<INode, PointD> nodePositions) {
    RectD bounds = BoundingBox.from(nodePositions);
    scaleByCore(factor, nodePositions, bounds);
  }

  private static void scaleByCore(double factor, Mapper<INode, PointD> nodePositions, RectD bounds) {
    for (Map.Entry<INode, PointD> entry : nodePositions.getEntries()) {
      INode node = entry.getKey();
      PointD center = entry.getValue();
      nodePositions.setValue(node, new PointD(
          (center.getX() - bounds.getMinX()) * factor,
          (center.getY() - bounds.getMinY()) * factor)
      );
    }
  }

  public static void scaleNodeSizes(GraphComponent view) {
      IGraph graph = view.getGraph();
      double scaleValue = 1 / view.getZoom(); // scale reinserted nodes
      double width = graph.getNodeDefaults().getSize().width * scaleValue;
      double height = graph.getNodeDefaults().getSize().height * scaleValue;
      for (INode u : graph.getNodes()) {
          graph.setNodeLayout(u, new RectD(
              u.getLayout().getCenter().getX() - 0.5*width,
              u.getLayout().getCenter().getY() - 0.5*height,
              width,
              height
          ));
      }
  }
}
