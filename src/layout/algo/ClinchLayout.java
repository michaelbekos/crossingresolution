package layout.algo;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.Matrix2D;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.utils.LayoutUtils;
import layout.algo.utils.PositionMap;
import util.BoundingBox;

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
  private RectD boundingBox;
  private PointD lineDirection;
  private Random random;

  public ClinchLayout(IGraph graph, PointD anchor1, PointD anchor2, Set<INode> fixNodes) {
    this.graph = graph;
    this.anchor1 = anchor1;
    this.anchor2 = anchor2;
    this.fixNodes = fixNodes;
  }

  @Override
  public void init() {
    positions = PositionMap.FromIGraph(graph);
    stepSizes = initStepSizes();
    sampleDirections = preComputeSamples();
    boundingBox = BoundingBox.from(positions);

    random = new Random(System.currentTimeMillis());
  }

  @Override
  public boolean executeStep(int iteration) {
    return !tryMoveNodesCloserToLine();
  }

  @Override
  public Mapper<INode, PointD> getNodePositions() {
    return positions;
  }

  @Override
  public void showDebug() {}

  @Override
  public void clearDebug() {}

  private Mapper<INode, Double> initStepSizes() {
    final Mapper<INode, Double> stepSizes = new Mapper<>(new WeakHashMap<>());

    for (INode node : graph.getNodes()) {
      stepSizes.setValue(node, INITIAL_STEP_SIZE);
    }

    return stepSizes;
  }

  private Mapper<INode, Collection<Sample>> preComputeSamples() {
    Mapper<INode, Collection<Sample>> sampleDirections = new Mapper<>(new WeakHashMap<>());

    lineDirection = PointD.subtract(anchor2, anchor1).getNormalized();

    Collection<Sample> leftRotations = new ArrayList<>();
    Collection<Sample> rightRotations = new ArrayList<>();

    for (int i = 0; i < NUMBER_OF_SAMPLES; i++) {
      Matrix2D rotationMatrix = new Matrix2D();
      double angle = i * Math.PI / NUMBER_OF_SAMPLES;
      rotationMatrix.rotate(angle);
      rightRotations.add(new Sample(rotationMatrix.transform(lineDirection)));
    }

    for (int i = 0; i < NUMBER_OF_SAMPLES; i++) {
      Matrix2D rotationMatrix = new Matrix2D();
      double angle = -i * Math.PI / NUMBER_OF_SAMPLES;
      rotationMatrix.rotate(angle);
      leftRotations.add(new Sample(rotationMatrix.transform(lineDirection)));
    }

    for (INode node : graph.getNodes()) {
      PointD anchor1ToCenter = PointD.subtract(node.getLayout().getCenter(), anchor1);
      if (crossProduct(lineDirection, anchor1ToCenter) > 0) {
        sampleDirections.setValue(node, leftRotations);
      } else {
        sampleDirections.setValue(node, rightRotations);
      }
    }

    return sampleDirections;
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

      double leftOrRight = Math.signum(crossProduct(PointD.subtract(oldPosition, anchor1), lineDirection));
      double minAngle = MinimumAngle.getMinimumAngleForNode(positions, node, graph);
      final Double stepSize = stepSizes.getValue(node);
      Collection<Sample> samples = sampleDirections.getValue(node);

      Optional<Sample> bestSample = samples.stream()
          .peek(sample -> {
            PointD newPosition = LayoutUtils.stepInDirection(oldPosition, sample.direction, stepSize);
            positions.setValue(node, newPosition);
            sample.position = newPosition;
            sample.minimumAngle = MinimumAngle.getMinimumAngleForNode(positions, node, graph);
          })
          .filter(sample -> boundingBox.contains(sample.position))
          // check if sample is still on the same side of the line
          .filter(sample -> Math.signum(crossProduct(PointD.subtract(sample.position, anchor1), lineDirection)) == leftOrRight)
          .max((s1, s2) -> {
            if (Math.abs(s1.minimumAngle - s2.minimumAngle) < COMPARISON_EPSILON) {
              return Double.compare(random.nextDouble(), 0.5);
            } else {
              return Double.compare(s1.minimumAngle, s2.minimumAngle);
            }
          });

      if (bestSample.isPresent() && bestSample.get().minimumAngle >= minAngle) {
        positions.setValue(node, bestSample.get().position);
        stepSizes.setValue(node, Math.min(stepSize * STEP_SIZE_MULTIPLIER, distanceToLine - stepSize));
        changed = true;
      } else {
        stepSizes.setValue(node, INITIAL_STEP_SIZE);
        positions.setValue(node, oldPosition);
      }
    }

    return changed;
  }

}
