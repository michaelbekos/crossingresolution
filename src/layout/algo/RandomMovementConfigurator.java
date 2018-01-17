package layout.algo;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

public class RandomMovementConfigurator implements ILayoutConfigurator {
  AbstractLayoutInterfaceItem<Double> minStepSize;
  AbstractLayoutInterfaceItem<Double> maxStepSize;

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    minStepSize = itemFactory.doubleParameter("Minimum step size", 0.1, 50);
    maxStepSize = itemFactory.doubleParameter("Maximum step size", 0.1, 50);
    minStepSize.setValue(0.1);
    maxStepSize.setValue(10.);
  }
}
