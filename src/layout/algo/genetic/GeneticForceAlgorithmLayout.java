package layout.algo.genetic;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.LayoutUtilities;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.layout.orthogonal.OrthogonalLayout;
import layout.algo.ForceAlgorithm;
import layout.algo.ILayout;
import layout.algo.InitForceAlgorithm;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.utils.PositionMap;

import java.util.*;

public class GeneticForceAlgorithmLayout implements ILayout {
  private GeneticForceAlgorithmConfigurator configurator;
  private IGraph graph;
  private GeneticAlgorithm<ForceAlgorithm> geneticAlgorithm;

  public GeneticForceAlgorithmLayout(GeneticForceAlgorithmConfigurator configurator, IGraph graph) {
    this.configurator = configurator;
    this.graph = graph;
  }

  @Override
  public void init() {
    LinkedList<ForceAlgorithm> firstIndividuals = new LinkedList<>();
    Map<ForceAlgorithm, List<AbstractLayoutInterfaceItem>> weightsMap = new WeakHashMap<>();

    spawnIndividual(firstIndividuals, weightsMap);
    LayoutUtilities.applyLayout(graph, new OrthogonalLayout());
    spawnIndividual(firstIndividuals, weightsMap);
    LayoutUtilities.applyLayout(graph, new OrganicLayout());
    spawnIndividual(firstIndividuals, weightsMap);


    firstIndividuals.forEach(ForceAlgorithm::init);

    Random rand = new Random();
    ForceAlgorithmObjective objective = new ForceAlgorithmObjective(configurator, graph, rand, weightsMap);
    geneticAlgorithm = new GeneticAlgorithm<>(objective, firstIndividuals, 5, rand);
  }

  @Override
  public void setFixNodes(Set<INode> fixNodes) {
    throw new UnsupportedOperationException("setFixNodes is not yet supported");
  }

  private void spawnIndividual(LinkedList<ForceAlgorithm> firstIndividuals, Map<ForceAlgorithm, List<AbstractLayoutInterfaceItem>> weightsMap) {
    MutationItemFactory itemFactory = new MutationItemFactory();
    ForceAlgorithm forceAlgorithm = InitForceAlgorithm.defaultForceAlgorithm(graph, itemFactory);
    firstIndividuals.add(forceAlgorithm);
    weightsMap.put(forceAlgorithm, itemFactory.weights);
  }

  @Override
  public boolean executeStep(int iteration, int maxIterations) {
    geneticAlgorithm.iterate();
    return false;
  }

  @Override
  public Mapper<INode, PointD> getNodePositions() {
    return geneticAlgorithm.getBestIndividual()
        .map(ForceAlgorithm::getNodePositions)
        .orElse(PositionMap.FromIGraph(graph)); // just in case...
  }

  @Override
  public void showDebug() {}

  @Override
  public void clearDebug() {}
}
