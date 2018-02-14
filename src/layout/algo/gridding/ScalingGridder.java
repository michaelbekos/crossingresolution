package layout.algo.gridding;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import util.GraphOperations;

import java.util.Set;

public class ScalingGridder implements IGridder {
  private IGridder gridder;
  private GridderConfigurator configurator;

  public ScalingGridder(GridderConfigurator configurator, IGridder gridder) {
    this.gridder = gridder;
    this.configurator = configurator;
  }

  @Override
  public void init() {
    gridder.init();
  }

  @Override
  public void setFixNodes(Set<INode> fixNodes) {
    gridder.setFixNodes(fixNodes);
  }

  @Override
  public boolean executeStep(int iteration, int maxIterations) {
    if (!configurator.scaleUpIfNecessary.getValue()) {
      return gridder.executeStep(iteration, maxIterations);
    }

    int iterationsPerScale = configurator.iterationsUntilScaleUp.getValue();

    boolean success = gridder.executeStep(iteration % iterationsPerScale, iterationsPerScale);
    if (success) {
      return true;
    }

    if (iteration % iterationsPerScale == 0 && iteration > 0) {
      GraphOperations.scaleUpProcess(gridder.getNodePositions(), configurator.scaleBy.getValue());
    }

    return false;
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