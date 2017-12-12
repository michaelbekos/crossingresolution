package layout.algo;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.Matrix2D;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import util.Maybe;

import java.util.*;

public class ClinchLayout implements ILayout {
  private static final double INITIAL_STEP_SIZE = 2.5;
  private static final double STEP_SIZE_MULTIPLIER = 1.5;
  private static final double CLOSE_ENOUGH = INITIAL_STEP_SIZE * 1.5;

  private static final int NUMBER_OF_SAMPLES = 10;

  private static final double COMPARISON_EPSILON = 0.001;

  private IGraph graph;
  private PointD anchor1;
  private PointD anchor2;
  private Set<INode> fixNodes;
  private Mapper<INode, PointD> positions;
  private Mapper<INode, Collection<Sample>> sampleDirections;
  private Mapper<INode, Double> stepSizes;

  public ClinchLayout(IGraph graph, PointD anchor1, PointD anchor2, Set<INode> fixNodes) {
    this.graph = graph;
    this.anchor1 = anchor1;
    this.anchor2 = anchor2;
    this.fixNodes = fixNodes;
  }

  @Override
  public void init() {
    positions = initPositions();
    stepSizes = initStepSizes();
    sampleDirections = preComputeSamples();
  }


  @Override
  public boolean executeStep(int iteration) {
    return !tryMoveNodesCloserToLine();
  }

  @Override
  public Mapper<INode, PointD> getNodePositions() {
    return positions;
  }

  private Mapper<INode, PointD> initPositions() {
    Mapper<INode, PointD> positions = new Mapper<>(new WeakHashMap<>());

    for (INode node : graph.getNodes()) {
      positions.setValue(node, node.getLayout().getCenter());
    }

    return positions;
  }

  private Mapper<INode, Double> initStepSizes() {
    final Mapper<INode, Double> stepSizes = new Mapper<>(new WeakHashMap<>());

    for (INode node : graph.getNodes()) {
      stepSizes.setValue(node, INITIAL_STEP_SIZE);
    }

    return stepSizes;
  }

  private Mapper<INode, Collection<Sample>> preComputeSamples() {
    Mapper<INode, Collection<Sample>> projections = new Mapper<>(new WeakHashMap<>());

    PointD lineDirection = PointD.subtract(anchor2, anchor1).getNormalized();

    Collection<Sample> leftRotations = new ArrayList<>();
    Collection<Sample> rightRotations = new ArrayList<>();

    for (int i = 0; i < NUMBER_OF_SAMPLES; i++) {
      Matrix2D rotationMatrix = new Matrix2D();
      double angle = i * Math.PI / NUMBER_OF_SAMPLES;
      rotationMatrix.rotate(angle);
      rightRotations.add(new Sample(rotationMatrix.transform(lineDirection), angle));
    }

    for (int i = 0; i < NUMBER_OF_SAMPLES; i++) {
      Matrix2D rotationMatrix = new Matrix2D();
      double angle = -i * Math.PI / NUMBER_OF_SAMPLES;
      rotationMatrix.rotate(angle);
      leftRotations.add(new Sample(rotationMatrix.transform(lineDirection), angle));
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

  private boolean tryMoveNodesCloserToLine() {
    boolean changed = false;

    for (INode node : graph.getNodes()) {
      if (fixNodes.contains(node)) {
        continue;
      }

      PointD oldPosition = positions.getValue(node);
      final double distanceToLine = oldPosition.getProjectionOnSegment(anchor1, anchor2).distanceTo(oldPosition);
      if (distanceToLine < CLOSE_ENOUGH) {
        continue;
      }

      double minAngle = getMinimumAngleForNode(positions, node);
      final Double stepSize = stepSizes.getValue(node);
      Collection<Sample> samples = sampleDirections.getValue(node);

      @SuppressWarnings("ConstantConditions")
      Sample bestSample = samples.stream()
          .peek(sample -> {
            PointD newPosition = stepInDirection(oldPosition, sample.direction, stepSize);
            positions.setValue(node, newPosition);
            sample.position = newPosition;
            sample.minimumAngle = getMinimumAngleForNode(positions, node);
          })
          .max((s1, s2) -> {
            if (Math.abs(s1.minimumAngle - s2.minimumAngle) < COMPARISON_EPSILON) {
              return Double.compare(distanceTo90Degrees(s1.angle), distanceTo90Degrees(s2.angle));
            } else {
              return Double.compare(s1.minimumAngle, s2.minimumAngle);
            }
          })
          .get();

      if (bestSample.minimumAngle >= minAngle) {
        positions.setValue(node, bestSample.position);
        stepSizes.setValue(node, Math.min(stepSize * STEP_SIZE_MULTIPLIER, distanceToLine - stepSize));
        changed = true;
      } else {
        stepSizes.setValue(node, INITIAL_STEP_SIZE);
        positions.setValue(node, oldPosition);
      }
    }

    return changed;
  }

  private double distanceTo90Degrees(double angle) {
    return Math.abs(angle - Math.PI / 2);
  }

  private double getMinimumAngleForNode(Mapper<INode, PointD> positions, INode node) {
    return MinimumAngle.getMinimumAngleForNode(graph, node, positions).getDefault(Double.POSITIVE_INFINITY);
  }

  private PointD stepInDirection(PointD oldPosition, PointD direction, double stepSize) {
    return new PointD(
        oldPosition.getX() + stepSize * direction.getX(),
        oldPosition.getY() + stepSize * direction.getY()
    );
  }

  private static class Sample {
    PointD direction;
    double angle;
    double minimumAngle;
    PointD position;

    Sample(PointD direction, double angle) {
      this.direction = direction;
      this.angle = angle;
    }
  }
}
