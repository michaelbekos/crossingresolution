package layout.algo.gridding;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;

import java.util.Set;

public class CombinedGridder implements IGridder {
  private IGraph graph;
  private GridderConfigurator configurator;
  private IGridder gridder;

  public CombinedGridder(IGraph graph, GridderConfigurator configurator) {
    this.graph = graph;
    this.configurator = configurator;
  }

  @Override
  public void init() {
    gridder = new EnsembleGridder(graph, configurator, () -> new ScalingGridder(
        configurator,
        new QuickGridder(graph, configurator)
    ));

    gridder.init();
  }

  @Override
  public void setFixNodes(Set<INode> fixNodes) {
    gridder.setFixNodes(fixNodes);
  }

  @Override
  public boolean executeStep(int iteration) {
    return gridder.executeStep(iteration);
  }

  @Override
  public void finish(int lastIteration) {
    gridder.finish(lastIteration);
  }

  @Override
  public Mapper<INode, PointD> getNodePositions() {
    return gridder.getNodePositions();
  }

  @Override
  public void showDebug() {
    gridder.showDebug();
  }

  @Override
  public void clearDebug() {
    gridder.clearDebug();
  }
}
