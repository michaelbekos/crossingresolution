package layout.algo;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

public class RandomMovementConfigurator implements ILayoutConfigurator {
  AbstractLayoutInterfaceItem<Double> minStepSize;
  AbstractLayoutInterfaceItem<Double> maxStepSize;
  AbstractLayoutInterfaceItem<Integer> numIterations;
  AbstractLayoutInterfaceItem<Boolean> doubleStepSizeOnLocalMaximum;
  AbstractLayoutInterfaceItem<Boolean> useGaussianDistribution;

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    minStepSize = itemFactory.doubleParameter("Minimum step size", 0.1, 500, 1);
    maxStepSize = itemFactory.doubleParameter("Maximum step size", 0.1, 50000, 1);
    numIterations = itemFactory.intParameter("Number of Iterations", -1,1000, 1);
    useGaussianDistribution = itemFactory.booleanParameter("Gaussian node distribution");
    doubleStepSizeOnLocalMaximum = itemFactory.booleanParameter("Double step size on local Minimum");

    useGaussianDistribution.setValue(true);
    minStepSize.setValue(10.);
    maxStepSize.setValue(150.);
    doubleStepSizeOnLocalMaximum.setValue(true);
    numIterations.setValue(100);
  }

}
