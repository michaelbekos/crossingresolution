package layout.algo.layoutinterface;

public class VoidItemFactory implements ILayoutInterfaceItemFactory{

  @Override
  public AbstractLayoutInterfaceItem<Double> doubleParameter(String name, double minValue, double maxValue) {
    return new VoidItem<>(name);
  }

  @Override
  public AbstractLayoutInterfaceItem<Boolean> booleanParameter(String name) {
    return new VoidItem<>(name);
  }
}
