package layout.algo.randommovement;

import algorithms.graphs.AngularResolution;
import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.algorithms.Bfs;
import com.yworks.yfiles.algorithms.Node;
import com.yworks.yfiles.algorithms.NodeList;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.layout.YGraphAdapter;
import graphoperations.GraphOperations;
import layout.algo.execution.ILayout;
import layout.algo.utils.LayoutUtils;
import layout.algo.utils.PositionMap;
import layout.algo.utils.Sample;
import util.BoundingBox;
import util.graph2d.Intersection;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RandomMovementLayout implements ILayout {
  private static final int NUM_SAMPLES = 50;
  private static final int NUM_SAMPLES_PER_TEST = 10;
  private static final int MAX_NUMBER_OF_NODES_FOR_UNIFORM_DISTRIBUTION = 20;

  private IGraph graph;
  private RandomMovementConfigurator configurator;
  private Mapper<INode, PointD> positions;
  private ArrayList<Sample> sampleDirections;
  private Random random;
  private RectD boundingBox;
  private int successfulSteps, failedSteps;
  private Set<INode> fixNodes;
  public RandomMovementLayout(IGraph graph, RandomMovementConfigurator configurator) {
    this.graph = graph;
    this.configurator = configurator;
  }

  @Override
  public void init() {
    positions = PositionMap.FromIGraph(graph);
    sampleDirections = initSampleDirections();
    random = new Random(System.currentTimeMillis());
//    boundingBox = BoundingBox.from(positions);
    boundingBox = BoundingBox.maxBox();
    double maxStepSize = Math.max(boundingBox.getWidth(), boundingBox.getHeight()) * 0.5;
    configurator.maxStepSize.setValue(maxStepSize);
    configurator.minStepSize.setValue(maxStepSize * 0.01);

    if (graph.getNodes().size() <= MAX_NUMBER_OF_NODES_FOR_UNIFORM_DISTRIBUTION) {
      configurator.useGaussianDistribution.setValue(false);
    }

    fixNodes = new HashSet<>();
  }

  @Override
  public void setFixNodes(Set<INode> fixNodes) {
    this.fixNodes = fixNodes;
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
    Optional<INode> randomNode;
    boolean usingAspectRatio = false;
    GraphOperations.AspectRatio currentAspectRatio = GraphOperations.getAspectRatio(graph);
    if (configurator.useAspectRatio.getValue()
            && configurator.maxAspectRatio.getValue() > 0   //If slider = -1, dont correct illegal, just allow curr a.r. > prev a.r.
            && (currentAspectRatio.getValue() > configurator.maxAspectRatio.getValue())) {
      System.out.println("ILLEGAL Aspect Ratio, correcting ...");
      randomNode = aspectRatioNodeSelection();
      usingAspectRatio = true;
    } else {
      if (configurator.useGaussianDistribution.getValue()) {
        randomNode = gaussianNodeSelection();
      } else {
        randomNode = selectRandomNode(graph.getNodes(), graph.getNodes().size());
      }
    }

    if (!randomNode.isPresent()) {
      return true;
    }

    INode node = randomNode.get();

    Collection<Sample> samples = selectRandomSamples();

    double originalAngle = getAngleForNode(positions, node, graph);
    PointD originalPosition = positions.getValue(node);
    final boolean usingAR = usingAspectRatio;
    Sample[] goodSamples = samples.stream()
        .peek(sample -> {
          double maxStepSize = configurator.maxStepSize.getValue();
          double minStepSize = configurator.minStepSize.getValue();
          double stepSize = random.nextDouble() * (maxStepSize - minStepSize) + minStepSize;
          sample.position = LayoutUtils.stepInDirection(originalPosition, sample.direction, stepSize);

          if (configurator.onlyGridPositions.getValue()) {
            sample.position = LayoutUtils.round(sample.position);
          }
        })
        .filter(sample -> boundingBox.contains(sample.position))
        .filter(sample -> !configurator.onlyGridPositions.getValue() || LayoutUtils.overlapFree(sample.position, positions, node, graph))
//        .filter(sample -> !configurator.useAspectRatio.getValue() || GraphOperations.improvedAspectRatio(sample.position, node, graph, currentAspectRatio, configurator.maxAspectRatio.getValue()) ) //filter out non-ok aspect ratio
        .filter(sample -> !configurator.useAspectRatio.getValue() ||
                GraphOperations.improvedAspectRatio(sample.position, node, graph, currentAspectRatio,
                        configurator.maxAspectRatio.getValue() < 0 ? currentAspectRatio.getValue() : configurator.maxAspectRatio.getValue()) ) //filter out non-ok aspect ratio (if slider = -1, allow all moves that dont worsen it)
        .peek(sample -> {
          this.positions.setValue(node, sample.position);

          sample.minimumAngle = getAngleForNode(positions, node, graph);
        })
            .filter(sample -> sample.minimumAngle > originalAngle)
//            .filter(sample -> usingAR ||  sample.minimumAngle > originalAngle)        //Ignore crossing angle when correcting illegal a.r. (much faster)
        .toArray(Sample[]::new);

    if (goodSamples.length > 0) {
      failedSteps = 0;
      successfulSteps++;
      Sample sample = goodSamples[random.nextInt(goodSamples.length)];

      positions.setValue(node, sample.position);

      if (configurator.toggleNodeDistributions.getValue()
          && successfulSteps >= configurator.iterationsForLocalMaximum.getValue()) {
        configurator.useGaussianDistribution.setValue(true);
      }

    } else {
      failedSteps++;
      positions.setValue(node, originalPosition);


      if (failedSteps >= configurator.iterationsForLocalMaximum.getValue()) {
        failedSteps = 0;
        successfulSteps = 0;

        if (configurator.toggleNodeDistributions.getValue()) {
          configurator.useGaussianDistribution.setValue(false);
        }

        return resolveLocalMaximum();
      }
    }

    // we're never finished...
    return false;
  }

  private boolean resolveLocalMaximum() {
    if (configurator.allowIncreaseStepSize.getValue()) {
      double boxDiagonal =
              Math.sqrt(boundingBox.getWidth() * boundingBox.getWidth() + boundingBox.getHeight() * boundingBox.getHeight());
      configurator.maxStepSize.setValue(Math.min(configurator.maxStepSize.getValue() * 2, boxDiagonal));
    }
    if (!configurator.jumpOnLocalMaximum.getValue()) {
      return false;
    }

    Optional<Intersection> crossing = MinimumAngle.getMinimumAngleCrossing(graph, positions); //TODO angular resolution
    if (!crossing.isPresent()) {
      return true;
    }

    HashSet<INode> nodesOfCrossing = new HashSet<>(4);
    nodesOfCrossing.add(crossing.get().segment1.n1);
    nodesOfCrossing.add(crossing.get().segment1.n2);
    nodesOfCrossing.add(crossing.get().segment2.n1);
    nodesOfCrossing.add(crossing.get().segment2.n2);
    selectRandomNode(nodesOfCrossing, graph.getNodes().size())
        .ifPresent(n -> jump(n, positions.getValue(n)));
    return false;
  }

  private void jump(INode node, PointD originalPosition) {
    // select some bounds around the node and limit them by the global bounding box
    double windowSize = configurator.maxStepSize.getValue();
    double halfWindowSize = windowSize / 2;
    double minX = Math.max(originalPosition.getX() - halfWindowSize, boundingBox.getMinX());
    double minY = Math.max(originalPosition.getY() - halfWindowSize, boundingBox.getMinY());
    RectD bounds = new RectD(
        minX,
        minY,
        Math.min(minX + windowSize, boundingBox.getMaxX()) - minX,
        Math.min(minY + windowSize, boundingBox.getMaxY()) - minY
    );

    Stream
      .generate(() -> {
          double x = random.nextDouble() * bounds.getWidth() + bounds.getX();
          double y = random.nextDouble() * bounds.getHeight() + bounds.getY();

        PointD point = new PointD(x, y);
        if (configurator.onlyGridPositions.getValue()) {
          return LayoutUtils.round(point);
        } else {
          return point;
        }
      })
        .filter(position -> !configurator.onlyGridPositions.getValue() || LayoutUtils.overlapFree(position, positions, node, graph))
        .limit(configurator.numSamplesForJumping.getValue())
        .max(Comparator.comparingDouble(position -> {
          positions.setValue(node, position);
          return getAngleForNode(positions, node, graph);
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

  private Optional<INode> gaussianNodeSelection() {

    if (configurator.useAngularResolution.getValue() ||
            (configurator.useAngularResolution.getValue() && configurator.useCrossingResolution.getValue()
                    && MinimumAngle.getMinimumAngle(graph, positions).orElse(Double.MAX_VALUE) < AngularResolution.getAngularResolution(graph))) {
      YGraphAdapter graphAdapter = new YGraphAdapter(graph);

      NodeList nodesOfCrossing = new NodeList();
      INode[] tmpList = AngularResolution.getCriticalNodes(graph);
      for (INode u : tmpList) {
        nodesOfCrossing.add(graphAdapter.getCopiedNode(u));
      }

      NodeList[] layers = Bfs.getLayers(graphAdapter.getYGraph(), nodesOfCrossing);

      // select a layer according to a normal distribution with a variance that is equal to half of the number
      // of layers. The reason for this number is that more than 95% of all values are within the interval
      // [-2 * sigma; 2 * sigma]:
      //     2 * sigma = #layers
      // <=>     sigma = #layers / 2
      int layerIndex = Math.min((int) Math.floor(Math.abs(random.nextGaussian() * layers.length / 2d)), layers.length - 1);

      Set<INode> nodesOfLayer = layers[layerIndex].stream()
              .map(node -> graphAdapter.getOriginalNode((Node) node))
              .collect(Collectors.toSet());

      return selectRandomNode(nodesOfLayer, nodesOfLayer.size());

    } else {
      return MinimumAngle.getMinimumAngleCrossing(graph, positions)
              .flatMap(crossing -> {
                YGraphAdapter graphAdapter = new YGraphAdapter(graph);

                NodeList nodesOfCrossing = new NodeList();
                nodesOfCrossing.add(graphAdapter.getCopiedNode(crossing.segment1.n1));
                nodesOfCrossing.add(graphAdapter.getCopiedNode(crossing.segment1.n2));
                nodesOfCrossing.add(graphAdapter.getCopiedNode(crossing.segment2.n1));
                nodesOfCrossing.add(graphAdapter.getCopiedNode(crossing.segment2.n2));

                NodeList[] layers = Bfs.getLayers(graphAdapter.getYGraph(), nodesOfCrossing);

                // select a layer according to a normal distribution with a variance that is equal to half of the number
                // of layers. The reason for this number is that more than 95% of all values are within the interval
                // [-2 * sigma; 2 * sigma]:
                //     2 * sigma = #layers
                // <=>     sigma = #layers / 2
                int layerIndex = Math.min((int) Math.floor(Math.abs(random.nextGaussian() * layers.length / 2d)), layers.length - 1);

                Set<INode> nodesOfLayer = layers[layerIndex].stream()
                        .map(node -> graphAdapter.getOriginalNode((Node) node))
                        .collect(Collectors.toSet());

                return selectRandomNode(nodesOfLayer, nodesOfLayer.size());
              });
    }
  }

  private Optional<INode> aspectRatioNodeSelection() {

      YGraphAdapter graphAdapter = new YGraphAdapter(graph);

      NodeList nodesOfCrossing = new NodeList();
      INode[] tmpList = GraphOperations.getAspectRatio(graph).getCriticalNodes();
      for (INode u : tmpList) {
        nodesOfCrossing.add(graphAdapter.getCopiedNode(u));
      }

      NodeList[] layers = Bfs.getLayers(graphAdapter.getYGraph(), nodesOfCrossing);

      // select a layer according to a normal distribution with a variance that is equal to half of the number
      // of layers. The reason for this number is that more than 95% of all values are within the interval
      // [-2 * sigma; 2 * sigma]:
      //     2 * sigma = #layers
      // <=>     sigma = #layers / 2
      int layerIndex = Math.min((int) Math.floor(Math.abs(random.nextGaussian() * layers.length / 2d)), layers.length - 1);

      Set<INode> nodesOfLayer = layers[layerIndex].stream()
              .map(node -> graphAdapter.getOriginalNode((Node) node))
              .collect(Collectors.toSet());

      return selectRandomNode(nodesOfLayer, nodesOfLayer.size());
  }

  private Optional<INode> selectRandomNode(Iterable<INode> nodes, int size) {
    return  StreamSupport.stream(nodes.spliterator(), false)
        .filter(node -> !fixNodes.contains(node))
        .skip(random.nextInt(size))
        .findFirst();
  }

  @Override
  public Mapper<INode, PointD> getNodePositions() {
    return positions;
  }

  private double getAngleForNode(Mapper<INode, PointD> positions, INode node, IGraph graph) {
    if (configurator.useCrossingResolution.getValue() && configurator.useAngularResolution.getValue()) {
      return Math.min(MinimumAngle.getMinimumAngleForNode(positions, node, graph), AngularResolution.getAngularResolutionForNode(positions, node, graph));
    } else if (configurator.useAngularResolution.getValue()) {
      return AngularResolution.getAngularResolutionForNode(positions, node, graph);
    } else {
     return MinimumAngle.getMinimumAngleForNode(positions, node, graph);
    }
  }

}
