package layout.algo.randommovement;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

public class RandomMovementConfigurator implements ILayoutConfigurator {
  AbstractLayoutInterfaceItem<Double> minStepSize;
  AbstractLayoutInterfaceItem<Double> maxStepSize;
  AbstractLayoutInterfaceItem<Boolean> jumpOnLocalMaximum;
  AbstractLayoutInterfaceItem<Boolean> useGaussianDistribution;
  AbstractLayoutInterfaceItem<Integer> iterationsForLocalMaximum;
  AbstractLayoutInterfaceItem<Integer> numSamplesForJumping;
  AbstractLayoutInterfaceItem<Boolean> toggleNodeDistributions;
  AbstractLayoutInterfaceItem<Boolean> onlyGridPositions;

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    minStepSize = itemFactory.doubleParameter("Minimum step size", 0.1, 500, false);
    minStepSize.setValue(10.);

    maxStepSize = itemFactory.doubleParameter("Maximum step size", 0.1, 5_000_000, false);
    maxStepSize.setValue(150.);

    iterationsForLocalMaximum = itemFactory.intParameter("Failed iterations necessary to detect a local maximum", 1, 500);
    iterationsForLocalMaximum.setValue(50);

    numSamplesForJumping = itemFactory.intParameter("Number of test samples for local maximum resolving", 1, 500);
    numSamplesForJumping.setValue(50);

    jumpOnLocalMaximum = itemFactory.booleanParameter("Allow decreasing minimum angle at local maximum", false);
    jumpOnLocalMaximum.setValue(false);

    onlyGridPositions = itemFactory.booleanParameter("Only use grid coordinates", false);
    onlyGridPositions.setValue(true);

    useGaussianDistribution = itemFactory.booleanParameter("Focus on critical nodes", false);
    useGaussianDistribution.setValue(true);

    toggleNodeDistributions = itemFactory.booleanParameter("Automatically toggle focusing on critical nodes", false);
    toggleNodeDistributions.setValue(true);
  }

}
