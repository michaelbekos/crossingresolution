package layout.algo.genetic;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.VoidItemFactory;

import java.util.LinkedList;
import java.util.List;

public class MutationItemFactory extends VoidItemFactory {
  List<AbstractLayoutInterfaceItem> weights = new LinkedList<>();

  @Override
  public AbstractLayoutInterfaceItem<Double> doubleParameter(String name, double minValue, double maxValue, boolean enableCheckbox) {
    AbstractLayoutInterfaceItem<Double> item = super.doubleParameter(name, minValue, maxValue, enableCheckbox);
    weights.add(item);
    return item;
  }

  @Override
  public AbstractLayoutInterfaceItem<Integer> intParameter(String name, int minValue, int maxValue) {
    throw new UnsupportedOperationException("Integer parameters are not supported by the genetic algorithm");
  }

  @Override
  public AbstractLayoutInterfaceItem<Boolean> booleanParameter(String name, boolean enableMasterCheckbox) {
    AbstractLayoutInterfaceItem<Boolean> item = super.booleanParameter(name, enableMasterCheckbox);
    weights.add(item);
    return item;
  }
}
