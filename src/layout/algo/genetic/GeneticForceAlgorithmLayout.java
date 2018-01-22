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
import layout.algo.layoutinterface.VoidItemFactory;
import layout.algo.utils.PositionMap;

import java.util.LinkedList;
import java.util.Random;

public class GeneticForceAlgorithmLayout implements ILayout {
  private GeneForceAlgorithmConfigurator configurator;
  private IGraph graph;
  private GeneticAlgorithm<ForceAlgorithm> geneticAlgorithm;

  public GeneticForceAlgorithmLayout(GeneForceAlgorithmConfigurator configurator, IGraph graph) {
    this.configurator = configurator;
    this.graph = graph;
  }

  @Override
  public void init() {
    LinkedList<ForceAlgorithm> firstIndividuals = new LinkedList<>();
    VoidItemFactory itemFactory = new VoidItemFactory();
    firstIndividuals.add(InitForceAlgorithm.defaultForceAlgorithm(graph, itemFactory));
    LayoutUtilities.applyLayout(graph, new OrthogonalLayout());
    firstIndividuals.add(InitForceAlgorithm.defaultForceAlgorithm(graph, itemFactory));
    LayoutUtilities.applyLayout(graph, new OrganicLayout());
    firstIndividuals.add(InitForceAlgorithm.defaultForceAlgorithm(graph, itemFactory));

    firstIndividuals.forEach(ForceAlgorithm::init);

    Random rand = new Random();
    ForceAlgorithmObjective objective = new ForceAlgorithmObjective(configurator, graph, rand);
    geneticAlgorithm = new GeneticAlgorithm<>(objective, firstIndividuals, 5, rand);
  }

  @Override
  public boolean executeStep(int iteration) {
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
