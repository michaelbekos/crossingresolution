package layout.algo.gridding;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

public class GridderConfigurator implements ILayoutConfigurator {
  AbstractLayoutInterfaceItem<Boolean> respectMinimumAngle;
  AbstractLayoutInterfaceItem<String> statusMessage;
  AbstractLayoutInterfaceItem<Integer> numberOfParallelExecutions;

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    numberOfParallelExecutions = itemFactory.intParameter("Number of parallel executions", 1, 100, 1);
    numberOfParallelExecutions.setValue(1);

    respectMinimumAngle = itemFactory.booleanParameter("Respect Minimum Angle");
    respectMinimumAngle.setValue(true);

    statusMessage = itemFactory.statusMessage("Status message");
    statusMessage.setValue("");
  }
}
