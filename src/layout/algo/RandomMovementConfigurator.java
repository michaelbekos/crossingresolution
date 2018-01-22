package layout.algo;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.util.ArrayList;

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

  @Override
  public ArrayList<AbstractLayoutInterfaceItem> getAbstractLayoutInterfaceItems() {
    ArrayList<AbstractLayoutInterfaceItem> parameterList = new ArrayList<>();
    parameterList.add(numIterations);
    parameterList.add(minStepSize);
    parameterList.add(maxStepSize);
    return parameterList;
  }

}
