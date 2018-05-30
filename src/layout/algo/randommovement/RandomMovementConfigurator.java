package layout.algo.randommovement;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RandomMovementConfigurator implements ILayoutConfigurator {
  AbstractLayoutInterfaceItem<Double> minStepSize;
  AbstractLayoutInterfaceItem<Double> maxStepSize;
  AbstractLayoutInterfaceItem<Boolean> jumpOnLocalMaximum;
  AbstractLayoutInterfaceItem<Boolean> useGaussianDistribution;
  AbstractLayoutInterfaceItem<Boolean> useReinsertChainNodes;
  AbstractLayoutInterfaceItem<Integer> iterationsForLocalMaximum;
  AbstractLayoutInterfaceItem<Integer> numSamplesForJumping;
  AbstractLayoutInterfaceItem<Boolean> toggleNodeDistributions;
  AbstractLayoutInterfaceItem<Boolean> onlyGridPositions;
  ArrayList<AbstractLayoutInterfaceItem> itemList;
  AbstractLayoutInterfaceItem<Boolean> allowIncreaseStepSize;
  AbstractLayoutInterfaceItem<Boolean> useCrossingResolution;
  AbstractLayoutInterfaceItem<Boolean> useAngularResolution;


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

    jumpOnLocalMaximum = itemFactory.booleanParameter("Allow decreasing minimum angle at local maximum");
    jumpOnLocalMaximum.setValue(false);
    itemList.add(jumpOnLocalMaximum);

    onlyGridPositions = itemFactory.booleanParameter("Only use grid coordinates");
    onlyGridPositions.setValue(false);
    itemList.add(onlyGridPositions);

    useGaussianDistribution = itemFactory.booleanParameter("Focus on critical nodes");
    useGaussianDistribution.setValue(false);
    itemList.add(useGaussianDistribution);

    useReinsertChainNodes = itemFactory.booleanParameter("Focus on nodes from reinserted chains");
    useReinsertChainNodes.setValue(true);
    itemList.add(useReinsertChainNodes);

    toggleNodeDistributions = itemFactory.booleanParameter("Automatically toggle focusing on critical nodes");
    toggleNodeDistributions.setValue(false);
    itemList.add(toggleNodeDistributions);

    allowIncreaseStepSize = itemFactory.booleanParameter("Automatically increase step size");
    allowIncreaseStepSize.setValue(false);
    itemList.add(allowIncreaseStepSize);

    useCrossingResolution = itemFactory.booleanParameter("Use Crossing Resolution (Default)");
    useCrossingResolution.setValue(true);
    itemList.add(useCrossingResolution);

    useAngularResolution = itemFactory.booleanParameter("Use Angular Resolution");
    useAngularResolution.setValue(false);
    itemList.add(useAngularResolution);

  }

  @Override
  public ArrayList<AbstractLayoutInterfaceItem> getItems() {
    System.out.println("size dsfksdaflsdfasdf" +itemList.size());return itemList;
  }



  public Optional<Map<String, AbstractLayoutInterfaceItem<Boolean>>> getBooleanParameters() {
      Map<String, AbstractLayoutInterfaceItem<Boolean>> paramaters = new HashMap<>();
      paramaters.put("useReinsertChainNodes", useReinsertChainNodes);
      paramaters.put("toggleNodeDistributions", toggleNodeDistributions);
      return Optional.of(paramaters);
  }

}
