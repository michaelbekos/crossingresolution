package layout.algo.randommovement;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;
import main.MainFrame;

import java.util.ArrayList;

public class RandomMovementConfigurator implements ILayoutConfigurator {
  AbstractLayoutInterfaceItem<Double> minStepSize;
  AbstractLayoutInterfaceItem<Double> maxStepSize;
  AbstractLayoutInterfaceItem<Integer> iterationsForLocalMaximum;
  AbstractLayoutInterfaceItem<Integer> numSamplesForJumping;
  AbstractLayoutInterfaceItem<Integer> maxAspectRatio;
  AbstractLayoutInterfaceItem<Boolean> jumpOnLocalMaximum;
  AbstractLayoutInterfaceItem<Boolean> useGaussianDistribution;
  AbstractLayoutInterfaceItem<Boolean> toggleNodeDistributions;
  AbstractLayoutInterfaceItem<Boolean> onlyGridPositions;
  AbstractLayoutInterfaceItem<Boolean> allowIncreaseStepSize;
  AbstractLayoutInterfaceItem<Boolean> useCrossingResolution;
  AbstractLayoutInterfaceItem<Boolean> useAngularResolution;
  AbstractLayoutInterfaceItem<Boolean> useAspectRatio;
  ArrayList<AbstractLayoutInterfaceItem> itemList;


  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    itemList = new ArrayList<>();

    minStepSize = itemFactory.doubleParameter("Minimum step size", 0.1, 500);
    minStepSize.setValue(10.);
    itemList.add(minStepSize);

    maxStepSize = itemFactory.doubleParameter("Maximum step size", 0.1, 5_000_000);
    maxStepSize.setValue(150.);
    itemList.add(maxStepSize);

    iterationsForLocalMaximum = itemFactory.intParameter("Failed iterations necessary to detect a local maximum", 1, 500);
    iterationsForLocalMaximum.setValue(50);
    itemList.add(iterationsForLocalMaximum);

    numSamplesForJumping = itemFactory.intParameter("Number of test samples for local maximum resolving", 1, 500);
    numSamplesForJumping.setValue(50);
    itemList.add(numSamplesForJumping);

    maxAspectRatio = itemFactory.intParameter("Maximum Legal Aspect Ratio", -1, 20, !MainFrame.CONTEST_MODE);
    maxAspectRatio.setValue(-1);
    itemList.add(maxAspectRatio);

    jumpOnLocalMaximum = itemFactory.booleanParameter("Allow decreasing minimum angle at local maximum");
    jumpOnLocalMaximum.setValue(false);
    itemList.add(jumpOnLocalMaximum);

    onlyGridPositions = itemFactory.booleanParameter("Only use grid coordinates");
    onlyGridPositions.setValue(true);
    itemList.add(onlyGridPositions);

    useGaussianDistribution = itemFactory.booleanParameter("Focus on critical nodes");
    useGaussianDistribution.setValue(true);
    itemList.add(useGaussianDistribution);

    toggleNodeDistributions = itemFactory.booleanParameter("Automatically toggle focusing on critical nodes");
    toggleNodeDistributions.setValue(true);
    itemList.add(toggleNodeDistributions);

    allowIncreaseStepSize = itemFactory.booleanParameter("Automatically increase step size");
    allowIncreaseStepSize.setValue(false);
    itemList.add(allowIncreaseStepSize);

    useCrossingResolution = itemFactory.booleanParameter("Use Crossing Resolution (Default)", !MainFrame.CONTEST_MODE);
    useCrossingResolution.setValue(true);
    itemList.add(useCrossingResolution);

    useAngularResolution = itemFactory.booleanParameter("Use Angular Resolution", !MainFrame.CONTEST_MODE);
    useAngularResolution.setValue(false);
    itemList.add(useAngularResolution);

    useAspectRatio = itemFactory.booleanParameter("Use Aspect Ratio", !MainFrame.CONTEST_MODE);
    useAspectRatio.setValue(false);
    itemList.add(useAspectRatio);
  }

  @Override
  public ArrayList<AbstractLayoutInterfaceItem> getItems() {
      return itemList;
  }


}
