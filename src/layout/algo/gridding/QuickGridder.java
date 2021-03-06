package layout.algo.gridding;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.utils.LayoutUtils;
import layout.algo.utils.PositionMap;
import util.BoundingBox;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuickGridder implements IGridder {
  private IGraph graph;
  private Mapper<INode, PointD> positions;
  private Random random;
  private Set<PointD> reservedPositions;
  private HashSet<INode> griddedNodes;
  private GridderConfigurator configurator;
  private RectD boundingBox;

  public QuickGridder(IGraph graph, GridderConfigurator configurator) {
    this.graph = graph;
    this.configurator = configurator;
  }

  @Override
  public void init() {
    positions = PositionMap.FromIGraph(graph);
    random = new Random();
    reservedPositions = new HashSet<>();
    griddedNodes = new HashSet<>();
    boundingBox = BoundingBox.maxBox();
  }

  @Override
  public void setFixNodes(Set<INode> fixNodes) {
    throw new UnsupportedOperationException("setFixNodes make no sense for Quickgridder");
  }

  @Override
  public boolean executeStep(int iteration) {
    for (INode node : graph.getNodes()) {
      if (griddedNodes.contains(node)) {
        continue;
      }

      PointD oldPosition = positions.getValue(node);
      double oldAngle = MinimumAngle.getMinimumAngleForNode(positions, node, graph);
      double allowedAngle = oldAngle - configurator.allowDecreasingBy.getValue();

      Stream<PointD> samplePositions = getNeighborGridPositions(oldPosition, iteration).stream()
          .filter(position -> !reservedPositions.contains(position))
          .filter(position -> boundingBox.contains(position))
          .filter(position -> LayoutUtils.overlapFree(position, positions, node, graph))
          .filter(position -> position.x > 0 && position.y > 0);

      if (configurator.respectMinimumAngle.getValue()) {
        samplePositions = samplePositions.filter(position -> {
          positions.setValue(node, position);
          return MinimumAngle.getMinimumAngleForNode(positions, node, graph) >= allowedAngle;
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

    configurator.statusMessage.setValue("Gridded " + griddedNodes.size() + "/" + graph.getNodes().size() + " nodes");

    boolean success = griddedNodes.size() == graph.getNodes().size();
    if (configurator.respectMinimumAngle.getValue()) {
      return success;
    } else {
      // max two iterations
      return iteration >= 1 || success;
    }
  }

  @Override
  public void finish(int lastIteration) {
    boolean success = griddedNodes.size() == graph.getNodes().size();
    if (success) {
      configurator.statusMessage.setValue("Success! (" + (lastIteration + 1) + " iterations)");
    } else if (!configurator.forceGridAfterStop.getValue()) {
      configurator.statusMessage.setValue("Gridding failed after " + (lastIteration + 1) + " iterations");
    } else {
      configurator.statusMessage.setValue("Forced grid positions\n(may result in suboptimal solutions)");
      graph.getNodes().stream()
          .filter(node -> !griddedNodes.contains(node))
          .forEach(node -> positions.setValue(node, getNeighborGridPositions(positions.getValue(node), 1).stream()
              .filter(position -> !reservedPositions.contains(position))
              .findFirst()
              .orElseGet(() -> {
                configurator.statusMessage.setValue("Force gridding failed!\n(node overlaps with another node)");
                return LayoutUtils.round(positions.getValue(node));
              })
          ));
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
}
