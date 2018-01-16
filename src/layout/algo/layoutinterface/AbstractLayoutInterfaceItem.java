package layout.algo.layoutinterface;

public abstract class AbstractLayoutInterfaceItem<T> {
  private T value;
  private String name;

  AbstractLayoutInterfaceItem(String name) {
    this.name = name;
  }

  public T getValue() {
    return value;
  }
  public void setValue(T value) {
    this.value = value;
  }
}
