package layout.algo.genetic;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.util.ArrayList;

public class GeneForceAlgorithmConfigurator implements ILayoutConfigurator {
  AbstractLayoutInterfaceItem<Integer> iterationsPerGeneration;
  private ArrayList<AbstractLayoutInterfaceItem> abstractLayoutInterfaceItems = new ArrayList<>();

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    iterationsPerGeneration = itemFactory.intParameter("Iterations per generation", 1, 500, 5);
    abstractLayoutInterfaceItems.add(iterationsPerGeneration);

    iterationsPerGeneration.setValue(100);
  }

  @Override
  public ArrayList<AbstractLayoutInterfaceItem> getAbstractLayoutInterfaceItems() {
    return abstractLayoutInterfaceItems;
  }
}
