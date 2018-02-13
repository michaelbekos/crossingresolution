package layout.algo.gridding;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

public class GridderConfigurator implements ILayoutConfigurator {
  AbstractLayoutInterfaceItem<Boolean> respectMinimumAngle;
  AbstractLayoutInterfaceItem<String> statusMessage;
  AbstractLayoutInterfaceItem<Integer> numberOfParallelExecutions;
  AbstractLayoutInterfaceItem<Boolean> scaleUpIfNecessary;
  AbstractLayoutInterfaceItem<Integer> iterationsUntilScaleUp;
  AbstractLayoutInterfaceItem<Double> scaleBy;

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    numberOfParallelExecutions = itemFactory.intParameter("Number of parallel executions", 1, 100, 1);
    numberOfParallelExecutions.setValue(1);

    respectMinimumAngle = itemFactory.booleanParameter("Respect Minimum Angle");
    respectMinimumAngle.setValue(true);

    scaleUpIfNecessary = itemFactory.booleanParameter("Scale up if necessary");
    scaleUpIfNecessary.setValue(false);

    iterationsUntilScaleUp = itemFactory.intParameter("Iterations until scale-up", 1, 50, 1);
    iterationsUntilScaleUp.setValue(20);

    scaleBy = itemFactory.doubleParameter("Scale by", 1.0, 5.0, 1.0, false);
    scaleBy.setValue(2.0);

    statusMessage = itemFactory.statusMessage("Status message");
    statusMessage.setValue("");
  }
}
