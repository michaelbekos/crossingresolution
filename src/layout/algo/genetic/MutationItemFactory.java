package layout.algo.genetic;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.VoidItemFactory;

import java.util.LinkedList;
import java.util.List;

public class MutationItemFactory extends VoidItemFactory {
  List<AbstractLayoutInterfaceItem> weights = new LinkedList<>();

  @Override
  public AbstractLayoutInterfaceItem<Double> doubleParameter(String name, double minValue, double maxValue, double threshold) {
    AbstractLayoutInterfaceItem<Double> item = super.doubleParameter(name, minValue, maxValue, threshold);
    weights.add(item);
    return item;
  }

  @Override
  public AbstractLayoutInterfaceItem<Integer> intParameter(String name, int minValue, int maxValue, int threshold) {
    throw new UnsupportedOperationException("Integer parameters are not supported by the genetic algorithm");
  }

  @Override
  public AbstractLayoutInterfaceItem<Boolean> booleanParameter(String name) {
    AbstractLayoutInterfaceItem<Boolean> item = super.booleanParameter(name);
    weights.add(item);
    return item;
  }
}
