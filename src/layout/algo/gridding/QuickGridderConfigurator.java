package layout.algo.gridding;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

public class QuickGridderConfigurator implements ILayoutConfigurator {
  AbstractLayoutInterfaceItem<Boolean> respectMinimumAngle;

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    respectMinimumAngle = itemFactory.booleanParameter("Respect Minimum Angle");
    respectMinimumAngle.setValue(true);
  }
}
