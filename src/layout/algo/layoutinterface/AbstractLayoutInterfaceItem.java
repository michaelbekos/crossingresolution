package layout.algo.layoutinterface;

/**
 * A very generic class representing an abstract two-way parameter for algorithms. Values can be both set and retrieved
 * by both the algorithm and the caller-side. Setting and getting the value is thread-safe.
 *
 * @param <T> The type of the value that will be stored in this parameter.
 */
public abstract class AbstractLayoutInterfaceItem<T> {
  private volatile T value;
  private final String name;

  protected AbstractLayoutInterfaceItem(String name) {
    this.name = name;
  }

  public T getValue() {
    return value;
  }
  public void setValue(T value) {
    this.value = value;
  }

  public String getName() {
    return name;
  }
}
