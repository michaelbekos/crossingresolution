package layout.algo;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

public class RandomMovementConfigurator implements ILayoutConfigurator {
  AbstractLayoutInterfaceItem<Double> minStepSize;
  AbstractLayoutInterfaceItem<Double> maxStepSize;
  AbstractLayoutInterfaceItem<Boolean> doubleStepSizeOnLocalMaximum;
  AbstractLayoutInterfaceItem<Boolean> useGaussianDistribution;

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    minStepSize = itemFactory.doubleParameter("Minimum step size", 0.1, 500, 1);
    maxStepSize = itemFactory.doubleParameter("Maximum step size", 0.1, 50000, 1);
    useGaussianDistribution = itemFactory.booleanParameter("Focus on critical nodes");
    doubleStepSizeOnLocalMaximum = itemFactory.booleanParameter("Double step size on local Maximum");

    useGaussianDistribution.setValue(true);
    minStepSize.setValue(10.);
    maxStepSize.setValue(150.);
    doubleStepSizeOnLocalMaximum.setValue(true);
  }

}
