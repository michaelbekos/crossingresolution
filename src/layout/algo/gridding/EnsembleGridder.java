package layout.algo.gridding;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.execution.ILayout;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnsembleGridder implements IGridder {
  private IGraph graph;
  private GridderConfigurator configurator;
  private Supplier<IGridder> gridderFactory;
  private Set<IGridder> gridders;

  public EnsembleGridder(IGraph graph, GridderConfigurator configurator, Supplier<IGridder> gridderFactory) {
    this.graph = graph;
    this.configurator = configurator;
    this.gridderFactory = gridderFactory;
  }

  @Override
  public void init() {
    gridders = Stream.generate(gridderFactory)
        .limit(configurator.numberOfParallelExecutions.getValue())
        .collect(Collectors.toSet());

    gridders.forEach(IGridder::init);
  }

  @Override
  public void setFixNodes(Set<INode> fixNodes) {
    gridders.forEach(quickGridder -> setFixNodes(fixNodes));
  }

  @Override
  public boolean executeStep(int iteration) {
    return gridders.stream()
        .anyMatch(quickGridder -> quickGridder.executeStep(iteration));
  }

  @Override
  public void finish(int lastIteration) {
    gridders.forEach(gridder -> gridder.finish(lastIteration));
  }

  @Override
  public Mapper<INode, PointD> getNodePositions() {
    return gridders.stream()
        .map(IGridder::getNodePositions)
        .max(Comparator.comparingDouble(nodePositions ->
            MinimumAngle.getMinimumAngle(graph, nodePositions).orElse(Double.POSITIVE_INFINITY)))
        .orElse(null); // should not occur...
  }

  @Override
  public void showDebug() {
    gridders.forEach(ILayout::showDebug);
  }

  @Override
  public void clearDebug() {
    gridders.forEach(ILayout::clearDebug);
  }
}
