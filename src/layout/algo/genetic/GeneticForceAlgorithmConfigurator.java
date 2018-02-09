package layout.algo.genetic;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.util.ArrayList;

public class GeneticForceAlgorithmConfigurator implements ILayoutConfigurator {
  AbstractLayoutInterfaceItem<Integer> iterationsPerGeneration;
  AbstractLayoutInterfaceItem<Double> stepSize;

  private ArrayList<AbstractLayoutInterfaceItem> abstractLayoutInterfaceItems = new ArrayList<>();

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    iterationsPerGeneration = itemFactory.intParameter("Iterations per generation", 1, 500, 5);
    abstractLayoutInterfaceItems.add(iterationsPerGeneration);

    stepSize = itemFactory.doubleParameter("Mutation step size", 0.1, 1, 1);
    abstractLayoutInterfaceItems.add(stepSize);

    iterationsPerGeneration.setValue(100);
    stepSize.setValue(0.1);
  }

}