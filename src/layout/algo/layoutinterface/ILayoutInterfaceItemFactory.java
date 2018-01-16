package layout.algo.layoutinterface;

public interface ILayoutInterfaceItemFactory {
  AbstractLayoutInterfaceItem<Double> doubleParameter(String name, double minValue, double maxValue);
  AbstractLayoutInterfaceItem<Boolean> booleanParameter(String name);
}
