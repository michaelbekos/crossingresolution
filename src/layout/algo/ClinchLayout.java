package layout.algo;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.Matrix2D;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import util.Maybe;
import util.Tuple2;

import java.util.*;

public class ClinchLayout {
  private static final double STEP_SIZE = 2.5;
  private static final int MAX_ITERATIONS = 2500;
  private static final double CLOSE_ENOUGH = STEP_SIZE * 1.5;
  private static final int NUMBER_OF_SAMPLES = 10;

  private IGraph graph;
  private PointD anchor1;
  private PointD anchor2;
  private Set<INode> fixNodes;

  public ClinchLayout(IGraph graph, PointD anchor1, PointD anchor2, Set<INode> fixNodes) {
    this.graph = graph;
    this.anchor1 = anchor1;
    this.anchor2 = anchor2;
    this.fixNodes = fixNodes;
  }

  public void apply() {
    Mapper<INode, PointD> positions = initPositions();
    Mapper<INode, Collection<PointD>> projections = computeProjections();

    boolean changed;
    int iterations = 0;
    do {
      changed = tryMoveNodesCloserToLine(positions, projections);
      System.out.println("Iteration: " + iterations + ", minimum angle: " + getMinimumAngle(positions));
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

  private Mapper<INode, Collection<PointD>> computeProjections() {
    Mapper<INode, Collection<PointD>> projections = new Mapper<>(new WeakHashMap<>());

    PointD lineDirection = PointD.subtract(anchor2, anchor1).getNormalized();

    Collection<PointD> leftRotations = new ArrayList<>();
    Collection<PointD> rightRotations = new ArrayList<>();

    for (int i = 0; i < NUMBER_OF_SAMPLES; i++) {
      Matrix2D rotationMatrix = new Matrix2D();
      rotationMatrix.rotate(i * Math.PI / NUMBER_OF_SAMPLES);
      rightRotations.add(rotationMatrix.transform(lineDirection));
    }

    for (int i = 0; i < NUMBER_OF_SAMPLES; i++) {
      Matrix2D rotationMatrix = new Matrix2D();
      rotationMatrix.rotate(-i * Math.PI / NUMBER_OF_SAMPLES);
      leftRotations.add(rotationMatrix.transform(lineDirection));
    }

    for (INode node : graph.getNodes()) {
      PointD anchor1ToCenter = PointD.subtract(node.getLayout().getCenter(), anchor1);
      if (crossProduct(lineDirection, anchor1ToCenter) > 0) {
        projections.setValue(node, leftRotations);
      } else {
        projections.setValue(node, rightRotations);
      }
    }

    return projections;
  }

  private double crossProduct(PointD v1, PointD v2) {
    return v1.getX() * v2.getY() - v1.getY() * v2.getX();
  }

  private void applyPositions(Mapper<INode, PointD> positions) {
    for (Map.Entry<INode, PointD> entry : positions.getEntries()) {
      graph.setNodeCenter(entry.getKey(), entry.getValue());
    }
  }

  private boolean tryMoveNodesCloserToLine(Mapper<INode, PointD> positions, Mapper<INode, Collection<PointD>> projections) {
    boolean changed = false;

    for (INode node : graph.getNodes()) {
      if (fixNodes.contains(node)) {
        continue;
      }

      PointD oldPosition = positions.getValue(node);
      if (oldPosition.getProjectionOnSegment(anchor1, anchor2).distanceTo(oldPosition) < CLOSE_ENOUGH) {
        continue;
      }

      double minAngle = getMinimumAngle(positions);

      Collection<PointD> samples = projections.getValue(node);
      @SuppressWarnings("ConstantConditions")
      Tuple2<PointD, Double> bestPosition = samples.stream()
          .map(dir -> {
            PointD newPosition = stepInDirection(oldPosition, dir);
            positions.setValue(node, newPosition);
            return new Tuple2<>(newPosition, getMinimumAngle(positions));
          })
          // TODO: prefer directions that are orthogonal to the line
          .max(Comparator.comparingDouble(pos -> pos.b))
          .get();

      if (bestPosition.b >= minAngle) {
        positions.setValue(node, bestPosition.a);
        changed = true;
      } else {
        positions.setValue(node, oldPosition);
      }
    }

    return changed;
  }

  private double getMinimumAngle(Mapper<INode, PointD> positions) {
    return MinimumAngle.getMinimumAngle(graph, Maybe.just(positions)).getDefault(Double.POSITIVE_INFINITY);
  }

  private PointD stepInDirection(PointD oldPosition, PointD direction) {
    return new PointD(
        oldPosition.getX() + STEP_SIZE * direction.getX(),
        oldPosition.getY() + STEP_SIZE * direction.getY()
    );
  }
}
