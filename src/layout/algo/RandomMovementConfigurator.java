package layout.algo;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

public class RandomMovementConfigurator implements ILayoutConfigurator {
  AbstractLayoutInterfaceItem<Double> minStepSize;
  AbstractLayoutInterfaceItem<Double> maxStepSize;
  AbstractLayoutInterfaceItem<Integer> numIterations;

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    minStepSize = itemFactory.doubleParameter("Minimum step size", 0.1, 50, 1);
    maxStepSize = itemFactory.doubleParameter("Maximum step size", 0.1, 50, 1);
    numIterations = itemFactory.intParameter("Number of Iterations", -1,1000, 1);
    minStepSize.setValue(0.1);
    maxStepSize.setValue(10.);
    numIterations.setValue(100);
  }

}
