package layout.algo.layoutinterface;

public class VoidItem<T> extends AbstractLayoutInterfaceItem<T> implements Cloneable {
  VoidItem(String name) {
    super(name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public VoidItem<T> clone() {
    VoidItem<T> clone = null;
    try {
      clone = (VoidItem<T>) super.clone();
      clone.setValue(this.getValue());
    } catch (CloneNotSupportedException ignored) {}
    return clone;
  }
}
