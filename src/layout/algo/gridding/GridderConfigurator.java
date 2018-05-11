package layout.algo.gridding;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

public class GridderConfigurator implements ILayoutConfigurator {
  AbstractLayoutInterfaceItem<Boolean> respectMinimumAngle;
  AbstractLayoutInterfaceItem<Double> allowDecreasingBy;
  AbstractLayoutInterfaceItem<String> statusMessage;
  AbstractLayoutInterfaceItem<Integer> numberOfParallelExecutions;
  AbstractLayoutInterfaceItem<Boolean> scaleUpIfNecessary;
  AbstractLayoutInterfaceItem<Integer> iterationsUntilScaleUp;
  AbstractLayoutInterfaceItem<Double> scaleBy;
  AbstractLayoutInterfaceItem<Boolean> forceGridAfterStop;

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    numberOfParallelExecutions = itemFactory.intParameter("Number of parallel executions", 1, 100);
    numberOfParallelExecutions.setValue(1);

    respectMinimumAngle = itemFactory.booleanParameter("Respect Minimum Angle");
    respectMinimumAngle.setValue(true);

    allowDecreasingBy = itemFactory.doubleParameter("Allow decreasing angle by", 0, 90);
    allowDecreasingBy.setValue(0.0);

    scaleUpIfNecessary = itemFactory.booleanParameter("Scale up if necessary");
    scaleUpIfNecessary.setValue(false);

    iterationsUntilScaleUp = itemFactory.intParameter("Iterations until scale-up", 1, 50);
    iterationsUntilScaleUp.setValue(20);

    scaleBy = itemFactory.doubleParameter("Scale by", 1.0, 5.0);
    scaleBy.setValue(2.0);

    statusMessage = itemFactory.statusMessage("Status message");
    statusMessage.setValue("");

    forceGridAfterStop = itemFactory.booleanParameter("Force grid after stop was pressed");
    forceGridAfterStop.setValue(true);
  }
}
