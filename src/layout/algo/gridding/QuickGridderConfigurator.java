package layout.algo.gridding;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

public class QuickGridderConfigurator implements ILayoutConfigurator {
  AbstractLayoutInterfaceItem<Boolean> respectMinimumAngle;
  AbstractLayoutInterfaceItem<String> statusMessage;

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    respectMinimumAngle = itemFactory.booleanParameter("Respect Minimum Angle");
    respectMinimumAngle.setValue(true);

    statusMessage = itemFactory.statusMessage("Status message");
    statusMessage.setValue("");
  }
}
