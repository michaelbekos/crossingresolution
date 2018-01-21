package layout.algo.genetic;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.ForceAlgorithm;
import layout.algo.ILayout;
import layout.algo.utils.PositionMap;

import java.util.Collection;
import java.util.Random;

public class GeneticForceAlgorithmLayout implements ILayout {
  private IGraph graph;
  private GeneticAlgorithm<ForceAlgorithm> geneticAlgorithm;
  private Collection<ForceAlgorithm> firstIndividuals;

  public GeneticForceAlgorithmLayout(IGraph graph, Collection<ForceAlgorithm> firstIndividuals) {
    this.graph = graph;
    this.firstIndividuals = firstIndividuals;
  }

  @Override
  public void init() {
    Random rand = new Random();
    ForceAlgorithmObjective objective = new ForceAlgorithmObjective(graph, rand);
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
