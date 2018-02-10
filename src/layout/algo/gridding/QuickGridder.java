package layout.algo.gridding;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.ILayout;
import layout.algo.utils.PositionMap;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuickGridder implements ILayout {
  private IGraph graph;
  private Mapper<INode, PointD> positions;
  private Random random;
  private Set<PointD> reservedPositions;
  private HashSet<INode> griddedNodes;
  private QuickGridderConfigurator configurator;

  public QuickGridder(IGraph graph, QuickGridderConfigurator configurator) {
    this.graph = graph;
    this.configurator = configurator;
  }

  @Override
  public void init() {
    positions = PositionMap.FromIGraph(graph);
    random = new Random();
    reservedPositions = new HashSet<>();
    griddedNodes = new HashSet<>();
  }

  @Override
  public boolean executeStep(int iteration, int maxIterations) {
    for (INode node : graph.getNodes()) {
      if (griddedNodes.contains(node)) {
        continue;
      }

      PointD oldPosition = positions.getValue(node);
      double oldAngle = MinimumAngle.getMinimumAngleForNode(positions, node, graph);

      Stream<PointD> samplePositions = getNeighborGridPositions(oldPosition, iteration).stream()
          .filter(position -> !reservedPositions.contains(position));

      if (configurator.respectMinimumAngle.getValue()) {
        samplePositions = samplePositions.filter(position -> {
          positions.setValue(node, position);
          return MinimumAngle.getMinimumAngleForNode(positions, node, graph) >= oldAngle;
        });
      }

      List<PointD> goodPositions = samplePositions.collect(Collectors.toList());

      if (goodPositions.size() == 0) {
        positions.setValue(node, oldPosition);
        continue;
      }

      int number = random.nextInt(goodPositions.size());
      PointD newPosition = goodPositions.get(number);
      positions.setValue(node, newPosition);
      reservedPositions.add(newPosition);
      griddedNodes.add(node);
    }

    boolean success = griddedNodes.size() == graph.getNodes().size();
    if (success) {
      configurator.statusMessage.setValue("Success! (" + (iteration + 1) + " iterations)");
    }

    if (configurator.respectMinimumAngle.getValue()) {
      return success;
    } else {
      // max two iterations
      return iteration >= 1 || success;
    }
  }

  private List<PointD> getNeighborGridPositions(PointD position, int window) {
    int w = 2 * window + 1;
    ArrayList<PointD> positions = new ArrayList<>(w * w);

    long px = Math.round(position.getX());
    long py = Math.round(position.getY());
    for (long x = px - window; x <= px + window; x++) {
      for (long y = py - window; y <= py + window; y++) {
        positions.add(new PointD(x, y));
      }
    }

    return positions;
  }

  @Override
  public Mapper<INode, PointD> getNodePositions() {
    return positions;
  }

  @Override
  public void showDebug() {}

  @Override
  public void clearDebug() {}
}
