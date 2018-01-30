package layout.algo;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.utils.LayoutUtils;
import layout.algo.utils.PositionMap;
import util.BoundingBox;
import util.graph2d.Intersection;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RandomMovementLayout implements ILayout {
  private static final int NUM_SAMPLES = 50;
  private static final int NUM_SAMPLES_PER_TEST = 10;

  private IGraph graph;
  private RandomMovementConfigurator configurator;
  private Mapper<INode, PointD> positions;
  private ArrayList<Sample> sampleDirections;
  private Random random;
  private RectD boundingBox;
  private int stepsSinceLastUpdate;

  public RandomMovementLayout(IGraph graph, RandomMovementConfigurator configurator) {
    this.graph = graph;
    this.configurator = configurator;
  }

  @Override
  public void init() {
    positions = PositionMap.FromIGraph(graph);
    sampleDirections = initSampleDirections();
    random = new Random(System.currentTimeMillis());
    boundingBox = BoundingBox.from(positions);
  }

  private ArrayList<Sample> initSampleDirections() {
    ArrayList<Sample> samples = new ArrayList<>(NUM_SAMPLES);

    for (int i = 0; i < NUM_SAMPLES; i++) {
      double alpha = i * Math.PI * 2 / NUM_SAMPLES;
      double x = Math.cos(alpha);
      double y = Math.sin(alpha);
      samples.add(new Sample(new PointD(x, y)));
    }

    return samples;
  }

  @Override
  public boolean executeStep(int iteration) {
    Optional<INode> randomNode = selectRandomNode(graph.getNodes());
    if (!randomNode.isPresent()) {
      return true;
    }

    INode node = randomNode.get();

    Collection<Sample> samples = selectRandomSamples();

    double originalAngle = MinimumAngle.getMinimumAngleForNode(positions, node, graph);
    PointD originalPosition = positions.getValue(node);

    Sample[] goodSamples = samples.stream()
        .peek(sample -> {
          double maxStepSize = configurator.maxStepSize.getValue();
          double minStepSize = configurator.minStepSize.getValue();
          double stepSize = random.nextDouble() * (maxStepSize - minStepSize) + minStepSize;
          sample.position = LayoutUtils.stepInDirection(originalPosition, sample.direction, stepSize);
        })
        .filter(sample -> boundingBox.contains(sample.position))
        .peek(sample -> {
          positions.setValue(node, sample.position);
          sample.minimumAngle = MinimumAngle.getMinimumAngleForNode(positions, node, graph);
        })
        .filter(sample -> sample.minimumAngle > originalAngle)
        .toArray(Sample[]::new);

    if (goodSamples.length > 0) {
      stepsSinceLastUpdate = 0;
      Sample sample = goodSamples[random.nextInt(goodSamples.length)];
      positions.setValue(node, sample.position);
    } else {
      stepsSinceLastUpdate++;
      positions.setValue(node, originalPosition);

      if (stepsSinceLastUpdate >= 50) {
        stepsSinceLastUpdate = 0;
        return resolveLocalMaximum();
      }
    }

    // we're never finished...
    return false;
  }

  private boolean resolveLocalMaximum() {
    Optional<Intersection> crossing = MinimumAngle.getMinimumAngleCrossing(graph, positions);
    if (!crossing.isPresent()) {
      return true;
    }

    HashSet<INode> nodesOfCrossing = new HashSet<>(4);
    nodesOfCrossing.add(crossing.get().segment1.n1);
    nodesOfCrossing.add(crossing.get().segment1.n2);
    nodesOfCrossing.add(crossing.get().segment2.n1);
    nodesOfCrossing.add(crossing.get().segment2.n2);
    selectRandomNode(nodesOfCrossing)
        .ifPresent(n -> jump(n, positions.getValue(n)));
    return false;
  }

  private void jump(INode node, PointD originalPosition) {
    // select some bounds around the node and limit them by the global bounding box
    double minX = Math.max(originalPosition.getX() - 50, boundingBox.getX());
    double minY = Math.max(originalPosition.getY() - 50, boundingBox.getY());
    RectD bounds = new RectD(
        minX,
        minY,
        Math.min(minX + 100, boundingBox.getX() + boundingBox.getWidth()) - minX,
        Math.min(minY + 100, boundingBox.getY() + boundingBox.getHeight()) - minY
    );

    Stream
        .generate(() -> {
          double x = random.nextDouble() * bounds.getWidth() + bounds.getX();
          double y = random.nextDouble() * bounds.getHeight() + bounds.getY();
          return new PointD(x, y);
        })
        .limit(NUM_SAMPLES_PER_TEST)
        .max(Comparator.comparingDouble(position -> {
          positions.setValue(node, position);
          return MinimumAngle.getMinimumAngleForNode(positions, node, graph);
        }))
        .ifPresent(pointD -> positions.setValue(node, pointD));
  }

  private Collection<Sample> selectRandomSamples() {
    HashSet<Sample> randomSamples = new HashSet<>(NUM_SAMPLES_PER_TEST);

    while (randomSamples.size() < NUM_SAMPLES_PER_TEST) {
      randomSamples.add(sampleDirections.get(random.nextInt(sampleDirections.size())));
    }

    return randomSamples;
  }

  private Optional<INode> selectRandomNode(Iterable<INode> nodes) {
    // select random node (we might think about shuffling instead?)
    return  StreamSupport.stream(nodes.spliterator(), false)
        .skip(random.nextInt(graph.getNodes().size()))
        .findFirst();
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
