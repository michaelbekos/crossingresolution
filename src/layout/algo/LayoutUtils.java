package layout.algo;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;

import java.util.WeakHashMap;

public class LayoutUtils {
  public static Mapper<INode, PointD> positionMapFromIGraph(IGraph graph) {
    Mapper<INode, PointD> positions = new Mapper<>(new WeakHashMap<>());

    for (INode node : graph.getNodes()) {
      positions.setValue(node, node.getLayout().getCenter());
    }

    return positions;
  }

  public static PointD stepInDirection(PointD oldPosition, PointD direction, double stepSize) {
    return new PointD(
        oldPosition.getX() + stepSize * direction.getX(),
        oldPosition.getY() + stepSize * direction.getY()
    );
  }
}
