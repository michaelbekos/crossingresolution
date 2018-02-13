package layout.algo.gridding;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.ILayout;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnsembleGridder implements ILayout {
  private IGraph graph;
  private GridderConfigurator configurator;
  private Set<QuickGridder> quickGridders;

  public EnsembleGridder(IGraph graph, GridderConfigurator configurator) {
    this.graph = graph;
    this.configurator = configurator;
  }

  @Override
  public void init() {
    quickGridders = Stream.generate(() -> new QuickGridder(graph, configurator))
        .limit(configurator.numberOfParallelExecutions.getValue())
        .collect(Collectors.toSet());

    quickGridders.forEach(QuickGridder::init);
  }

  @Override
  public void setFixNodes(Set<INode> fixNodes) {
    quickGridders.forEach(quickGridder -> setFixNodes(fixNodes));
  }

  @Override
  public boolean executeStep(int iteration, int maxIterations) {
    return quickGridders.stream()
        .allMatch(quickGridder -> quickGridder.executeStep(iteration, maxIterations));
  }

  @Override
  public Mapper<INode, PointD> getNodePositions() {
    return quickGridders.stream()
        .map(QuickGridder::getNodePositions)
        .max(Comparator.comparingDouble(nodePositions ->
            MinimumAngle.getMinimumAngle(graph, nodePositions).orElse(Double.POSITIVE_INFINITY)))
        .orElse(null); // should not occur...
  }

  @Override
  public void showDebug() {}

  @Override
  public void clearDebug() {}
}
