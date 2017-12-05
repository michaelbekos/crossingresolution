package layout.algo;

import algorithms.graphs.yFilesSweepLine;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;

import java.util.Map;
import java.util.WeakHashMap;

public class ClinchLayout {
  private static final double STEP_SIZE = 2.5;
  private static final int MAX_ITERATIONS = 2500;
  private static final double CLOSE_ENOUGH = STEP_SIZE * 1.5;

  private IGraph graph;
  private PointD anchor1;
  private PointD anchor2;

  public ClinchLayout(IGraph graph, PointD anchor1, PointD anchor2) {
    this.graph = graph;
    this.anchor1 = anchor1;
    this.anchor2 = anchor2;
  }

  public void apply() {
    Mapper<INode, PointD> positions = initPositions();
    Mapper<INode, PointD> projections = computeProjections(positions);

    boolean changed;
    int iterations = 0;
    do {
      changed = tryMoveNodesCloserToLine(positions, projections);
      System.out.println("Iteration: " + iterations);
    } while (iterations++ < MAX_ITERATIONS && changed);

    applyPositions(positions);
  }

  private Mapper<INode, PointD> initPositions() {
    Mapper<INode, PointD> positions = new Mapper<>(new WeakHashMap<>());

    for (INode node : graph.getNodes()) {
      positions.setValue(node, node.getLayout().getCenter());
    }

    return positions;
  }

  private Mapper<INode, PointD> computeProjections(Mapper<INode, PointD> positions) {
    Mapper<INode, PointD> projections = new Mapper<>(new WeakHashMap<>());

    for (INode node : graph.getNodes()) {
      PointD center = positions.getValue(node);
      PointD projection = center.getProjectionOnSegment(anchor1, anchor2);
      PointD direction =
          new PointD(projection.getX() - center.getX(), projection.getY() - center.getY()).getNormalized();

      projections.setValue(node, direction);
    }

    return projections;
  }

  private void applyPositions(Mapper<INode, PointD> positions) {
    for (Map.Entry<INode, PointD> entry : positions.getEntries()) {
      graph.setNodeCenter(entry.getKey(), entry.getValue());
    }
  }

  private boolean tryMoveNodesCloserToLine(Mapper<INode, PointD> positions, Mapper<INode, PointD> projections) {
    boolean changed = false;

    for (INode node : graph.getNodes()) {
      PointD oldPosition = positions.getValue(node);
      if (oldPosition.getProjectionOnSegment(anchor1, anchor2).distanceTo(oldPosition) < CLOSE_ENOUGH) {
        continue;
      }

      PointD direction = projections.getValue(node);
      long numberOfIntersections = getIntersectionsForNode(positions, node);

      PointD newPosition = stepInDirection(oldPosition, direction);
      positions.setValue(node, newPosition);

      // TODO: the number of intersections is not enough! -> must be the same intersections or less
      if (getIntersectionsForNode(positions, node) <= numberOfIntersections) {
        changed = true;
      } else {
        positions.setValue(node, oldPosition);
      }
    }

    return changed;
  }

  private long getIntersectionsForNode(Mapper<INode, PointD> positions, INode node) {
    return yFilesSweepLine.getCrossings(graph, true, positions).stream()
        .filter(tuple -> {
          IEdge e1 = tuple.a.e.get();
          IEdge e2 = tuple.b.e.get();
          return e1.getSourceNode() == node || e1.getTargetNode() == node
              || e2.getSourceNode() == node || e2.getTargetNode() == node;
        }).count();
  }

  private PointD stepInDirection(PointD oldPosition, PointD direction) {
    return new PointD(
        oldPosition.getX() + STEP_SIZE * direction.getX(),
        oldPosition.getY() + STEP_SIZE * direction.getY()
    );
  }
}
