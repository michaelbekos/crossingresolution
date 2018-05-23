package layout.algo.genetic;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.util.ArrayList;

public class GeneticForceAlgorithmConfigurator implements ILayoutConfigurator {
  AbstractLayoutInterfaceItem<Integer> iterationsPerGeneration;
  AbstractLayoutInterfaceItem<Double> stepSize;
  ArrayList<AbstractLayoutInterfaceItem> itemList;

  private ArrayList<AbstractLayoutInterfaceItem> abstractLayoutInterfaceItems = new ArrayList<>();

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    itemList = new ArrayList<>();

    iterationsPerGeneration = itemFactory.intParameter("Iterations per generation", 1, 500);
    iterationsPerGeneration.setValue(100);
    abstractLayoutInterfaceItems.add(iterationsPerGeneration);
    itemList.add(iterationsPerGeneration);

    stepSize = itemFactory.doubleParameter("Mutation step size", 0.1, 1);
    stepSize.setValue(0.1);
    itemList.add(stepSize);
    abstractLayoutInterfaceItems.add(stepSize);

  }
  @Override
  public ArrayList<AbstractLayoutInterfaceItem> getItems(){
    return itemList;
  }


}
