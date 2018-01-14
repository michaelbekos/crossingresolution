package layout.algo;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import util.BoundingBox;

import java.util.*;
import java.util.stream.Stream;

public class RandomMovementLayout implements ILayout {
  private static final int NUM_SAMPLES = 50;
  private static final int NUM_SAMPLES_PER_TEST = 10;
  private final double minStepSize = 0.1;
  private final double maxStepSize = 10;

  private IGraph graph;
  private Mapper<INode, PointD> positions;
  private ArrayList<Sample> sampleDirections;
  private Random random;
  private RectD boundingBox;

  public RandomMovementLayout(IGraph graph) {
    this.graph = graph;
  }

  @Override
  public void init() {
    positions = LayoutUtils.positionMapFromIGraph(graph);
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
    Optional<INode> randomNode = selectRandomNode();
    if (!randomNode.isPresent()) {
      return true;
    }

    INode node = randomNode.get();

    Collection<Sample> samples = selectRandomSamples();

    double originalAngle = MinimumAngle.getMinimumAngleForNode(positions, node, graph);
    PointD originalPosition = positions.getValue(node);

    Sample[] goodSamples = samples.stream()
        .filter(sample -> {
          double stepSize = random.nextDouble() * (maxStepSize - minStepSize) + minStepSize;
          PointD newPosition = LayoutUtils.stepInDirection(originalPosition, sample.direction, stepSize);
          sample.position = newPosition;
          return boundingBox.contains(newPosition);
        })
        .filter(sample -> {
          positions.setValue(node, sample.position);
          double sampleMinAngle = MinimumAngle.getMinimumAngleForNode(positions, node, graph);
          sample.minimumAngle = sampleMinAngle;

          return sampleMinAngle > originalAngle;
        })
        .toArray(Sample[]::new);

    if (goodSamples.length > 0) {
      Sample sample = goodSamples[random.nextInt(goodSamples.length)];
      positions.setValue(node, sample.position);
    } else {
      jump(node, originalPosition);
    }

    // we're never finished...
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

  private Optional<INode> selectRandomNode() {
    // select random node (we might think about shuffling instead?)
    return graph.getNodes().stream()
        .skip(random.nextInt(graph.getNodes().size()))
        .findFirst();
  }

  @Override
  public Mapper<INode, PointD> getNodePositions() {
    return positions;
  }
}
