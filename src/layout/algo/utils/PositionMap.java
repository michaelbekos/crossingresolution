package layout.algo.utils;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;

import java.util.Map;
import java.util.WeakHashMap;

public final class PositionMap {
  private PositionMap() {}

  public static Mapper<INode, PointD> newPositionMap() {
    return new Mapper<>(new WeakHashMap<>());
  }

  public static Mapper<INode, PointD> copy(Mapper<INode, PointD> im) {
    Mapper<INode, PointD> res = newPositionMap();

    im.getEntries().forEach(e -> {
      INode n = e.getKey();
      PointD p = e.getValue();
      res.setValue(n, p);
    });

    return res;
  }

  public static Mapper<INode, PointD> FromIGraph(IGraph graph) {
    Mapper<INode, PointD> positions = new Mapper<>(new WeakHashMap<>());

    for (INode node : graph.getNodes()) {
      positions.setValue(node, node.getLayout().getCenter());
    }

    return positions;
  }

  public static IGraph applyToGraph(IGraph g, Mapper<INode, PointD> nodePositions) {
    for (Map.Entry<INode, PointD> e : nodePositions.getEntries()) {
      INode node = e.getKey();
      PointD position = e.getValue();
      g.setNodeCenter(node, position);
    }
    return g;
  }
}
