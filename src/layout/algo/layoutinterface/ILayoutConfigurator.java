package layout.algo.layoutinterface;

/**
 * The interface for algorithm configurators. Classes implementing this interface should not contain any complex logic
 * but should instead serve as simple container for all parameters ({@link AbstractLayoutInterfaceItem}s) of the
 * algorithm. The parameters must be initialized within {@link #init(ILayoutInterfaceItemFactory)}.
 */
public interface ILayoutConfigurator {

  /**
   * Initializes all parameters of this algorithm. Note that after you created a parameter, you should also call
   * {@link AbstractLayoutInterfaceItem#setValue(Object)} to set the initial value of the parameter. Otherwise
   * {@link NullPointerException}s might be thrown.
   * @param itemFactory a factory to create the {@link AbstractLayoutInterfaceItem}s; it must not be stored as it will
   *                    become invalid after the invocation
   */
  void init(ILayoutInterfaceItemFactory itemFactory);
}
