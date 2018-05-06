package layout.algo.clinchlayout;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.Matrix2D;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.utils.IListEnumerable;
import com.yworks.yfiles.view.IGraphSelection;
import com.yworks.yfiles.view.ISelectionModel;
import layout.algo.execution.ILayout;
import layout.algo.utils.LayoutUtils;
import layout.algo.utils.PositionMap;
import layout.algo.utils.Sample;
import util.BoundingBox;
import util.Tuple2;
import util.Util;

import java.util.*;
import java.util.stream.Collectors;

public class ClinchLayout implements ILayout {
  private static final int NUMBER_OF_SAMPLES = 10;

  private static final double COMPARISON_EPSILON = 0.001;

  private ClinchLayoutConfigurator configurator;
  private IGraph graph;

  private INode anchor1;
  private INode anchor2;
  private PointD anchor1Location;
  private PointD anchor2Location;

  private Set<INode> fixNodes;
  private Mapper<INode, PointD> positions;
  private Mapper<INode, Collection<Sample>> sampleDirections;
  private Mapper<INode, Double> stepSizes;
  private RectD boundingBox;
  private PointD lineDirection;
  private Random random;

  public ClinchLayout(ClinchLayoutConfigurator configurator, IGraph graph) {
    this.configurator = configurator;
    this.graph = graph;
  }

  @Override
  public void init() {
    initLine();
    positions = PositionMap.FromIGraph(graph);
    stepSizes = initStepSizes();
    sampleDirections = preComputeSamples();
    boundingBox = BoundingBox.from(positions);

    random = new Random(System.currentTimeMillis());
  }

  @Override
  public void setFixNodes(Set<INode> fixNodes) {
    this.fixNodes.clear();
    this.fixNodes.addAll(fixNodes);
    this.fixNodes.add(anchor1);
    this.fixNodes.add(anchor2);
  }

  @Override
  public void setVarNodes(Set<INode> variableNodes) {
    throw new UnsupportedOperationException("setVarNodes is not yet supported");
  }

  @Override
  public boolean executeStep(int iteration) {
    return !tryMoveNodesCloserToLine();
  }

  @Override
  public Mapper<INode, PointD> getNodePositions() {
    return positions;
  }

  private void initLine() {
    IGraphSelection selection = configurator.selection.getValue();
    if (selection != null) {
      ISelectionModel<INode> selectedNodes = selection.getSelectedNodes();
      fixNodes = graph.getNodes().stream()
          .filter(node -> !selectedNodes.isSelected(node))
          .collect(Collectors.toSet());
    } else {
      fixNodes = new HashSet<>(2);
    }

    IListEnumerable<INode> nodes = graph.getNodes();
    Optional<Tuple2<INode, INode>> mostDistantNodes = Util.distinctPairs(nodes.stream(), nodes.stream())
        .filter(pair -> !fixNodes.contains(pair.a) && !fixNodes.contains(pair.b))
        .max(Comparator.comparingDouble(pair -> pair.a.getLayout().getCenter().distanceTo(pair.b.getLayout().getCenter())));

    if (!mostDistantNodes.isPresent()) {
      return;
    }

    anchor1 = mostDistantNodes.get().a;
    anchor2 = mostDistantNodes.get().b;
    anchor1Location = anchor1.getLayout().getCenter();
    anchor2Location = anchor2.getLayout().getCenter();

    fixNodes.add(anchor1);
    fixNodes.add(anchor2);
  }

  private Mapper<INode, Double> initStepSizes() {
    final Mapper<INode, Double> stepSizes = new Mapper<>(new WeakHashMap<>());

    for (INode node : graph.getNodes()) {
      stepSizes.setValue(node, configurator.initialStepSize.getValue());
    }

    return stepSizes;
  }

  private Mapper<INode, Collection<Sample>> preComputeSamples() {
    Mapper<INode, Collection<Sample>> sampleDirections = new Mapper<>(new WeakHashMap<>());

    lineDirection = PointD.subtract(anchor2Location, anchor1Location).getNormalized();

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
      PointD anchor1ToCenter = PointD.subtract(node.getLayout().getCenter(), anchor1Location);
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
      final double distanceToLine = oldPosition.getProjectionOnSegment(anchor1Location, anchor2Location).distanceTo(oldPosition);
      if (distanceToLine < configurator.initialStepSize.getValue() * 1.5) {
        continue;
      }

      double leftOrRight = Math.signum(crossProduct(PointD.subtract(oldPosition, anchor1Location), lineDirection));
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
          .filter(sample -> Math.signum(crossProduct(PointD.subtract(sample.position, anchor1Location), lineDirection)) == leftOrRight)
          .max((s1, s2) -> {
            if (Math.abs(s1.minimumAngle - s2.minimumAngle) < COMPARISON_EPSILON) {
              return Double.compare(random.nextDouble(), 0.5);
            } else {
              return Double.compare(s1.minimumAngle, s2.minimumAngle);
            }
          });

      if (bestSample.isPresent() && bestSample.get().minimumAngle >= minAngle) {
        positions.setValue(node, bestSample.get().position);
        stepSizes.setValue(node, Math.min(stepSize * configurator.stepSizeMultiplier.getValue(), distanceToLine - stepSize));
        changed = true;
      } else {
        stepSizes.setValue(node, configurator.initialStepSize.getValue());
        positions.setValue(node, oldPosition);
      }
    }

    return changed;
  }

}
