package layout.algo.genetic;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.BasicIGraphLayoutExecutor;
import layout.algo.ForceAlgorithm;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.VoidItem;
import util.G;
import util.graph2d.Intersection;

import java.util.*;

class ForceAlgorithmObjective implements IObjective<ForceAlgorithm> {
  private GeneticForceAlgorithmConfigurator configurator;
  private IGraph graph;
  private Random rand;
  private Map<ForceAlgorithm, List<AbstractLayoutInterfaceItem>> weights;

  ForceAlgorithmObjective(GeneticForceAlgorithmConfigurator configurator, IGraph graph, Random rand, Map<ForceAlgorithm, List<AbstractLayoutInterfaceItem>> weights) {
    this.configurator = configurator;
    this.graph = graph;
    this.rand = rand;
    this.weights = weights;
  }

  @Override
  public ForceAlgorithm advance(ForceAlgorithm forceAlgorithm) {
    int iterations = configurator.iterationsPerGeneration.getValue();
    final BasicIGraphLayoutExecutor executor = new BasicIGraphLayoutExecutor(forceAlgorithm, graph, iterations, iterations);
    executor.start();
    executor.waitUntilFinished();
    return forceAlgorithm;
  }

  @Override
  public ForceAlgorithm mutate(ForceAlgorithm forceAlgorithm) {
    ForceAlgorithm mutationFA = forceAlgorithm.clone();

    List<AbstractLayoutInterfaceItem> weights = this.weights.get(forceAlgorithm);
    LinkedList<AbstractLayoutInterfaceItem> mutationWeights = new LinkedList<>();
    for (AbstractLayoutInterfaceItem weight : weights) {
      if (weight instanceof VoidItem) { // as it always is...
        mutationWeights.add(((VoidItem) weight).clone());
      }
    }

    this.weights.put(mutationFA, mutationWeights);


    Mapper<INode, PointD> nodePositions = mutationFA.getNodePositions();
    List<Intersection> crossings = MinimumAngle.getCrossingsSorted(graph, nodePositions);

    if (crossings.size() == 0) {
      return mutationFA;
    }

    List<Intersection> mostInteresting = crossings.subList(0, (int) Math.ceil(crossings.size() / 50.0));

    //random crossing
    int crossingIndex = rand.nextInt(mostInteresting.size());
    Intersection nodeCrossing = mostInteresting.get(crossingIndex);
    int whichNode = rand.nextInt(4);
    INode[] nodes = new INode[]{
        nodeCrossing.segment1.n1,
        nodeCrossing.segment1.n2,
        nodeCrossing.segment2.n1,
        nodeCrossing.segment2.n2
    };
    INode node = nodes[whichNode];

    PointD pos = nodeCrossing.intersectionPoint;
    PointD direction = new PointD(0, 0);
    switch (whichNode) {
      case 0:
        direction = PointD.negate(nodeCrossing.segment2.ve);
        break;
      case 1:
        direction = nodeCrossing.segment2.ve;
        break;
      case 2:
        direction = PointD.negate(nodeCrossing.segment1.ve);
        break;
      case 3:
        direction = nodeCrossing.segment1.ve;
        break;
    }
    if (nodeCrossing.orientedAngle > 90) {
      direction = PointD.negate(direction);
    }

    if (direction.getVectorLength() <= G.Epsilon) {
      return mutationFA;
    }
    direction = direction.getNormalized();
    pos = PointD.add(pos, PointD.times(configurator.stepSize.getValue(), direction));

    nodePositions.setValue(node, pos);
    mutationFA.setNodePositions(nodePositions);

    // russian roulette to change a modifier
    if (rand.nextDouble() > 0.75) {
      // randomly modify one spring threshold
      weights = this.weights.get(mutationFA);
      int indexToModify = rand.nextInt(weights.size());
      AbstractLayoutInterfaceItem weightToModify = weights.get(indexToModify);
      // smallest double > 0
      double minVal = Math.nextAfter(0, Double.POSITIVE_INFINITY);
      // value should remain 0 < val <= 1

      // this is a little bit ugly, I know...
      Object value = weightToModify.getValue();
      if (value instanceof Double) {
        //noinspection unchecked
        weightToModify.setValue(Math.min(1, Math.max(minVal, (double) value * rand.nextDouble() * 2)));
      } else if (value instanceof Boolean) {
        //noinspection unchecked
        weightToModify.setValue(!(boolean) value);
      }
    }

    return mutationFA;
  }

  @Override
  public int compare(ForceAlgorithm fa1, ForceAlgorithm fa2) {
    Optional<Double> ma1 = fa1.getMinimumAngle();
    Optional<Double> ma2 = fa2.getMinimumAngle();

    if (ma1.isPresent() && !ma2.isPresent()) {
      return -1;
    }

    if (!ma1.isPresent() && ma2.isPresent()) {
      return 1;
    }

    //noinspection ConstantConditions
    if (!ma1.isPresent() && !ma2.isPresent()) {
      return 0;
    }

    return ma1.get().compareTo(ma2.get());
  }
}
