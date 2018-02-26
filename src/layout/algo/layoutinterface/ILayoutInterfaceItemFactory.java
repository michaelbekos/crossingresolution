package layout.algo.layoutinterface;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.view.IGraphSelection;

/**
 * An abstract factory interface for {@link AbstractLayoutInterfaceItem}s. An implementation of this interface will be
 * passed to {@link ILayoutConfigurator#init(ILayoutInterfaceItemFactory)}.
 */
public interface ILayoutInterfaceItemFactory {

  /**
   * Creates a double parameter.
   * @param name the name of the parameter
   * @param minValue the minimum value of the parameter
   * @param maxValue the maximum value of the parameter
   * @return an {@link AbstractLayoutInterfaceItem<Double>}
   */
  AbstractLayoutInterfaceItem<Double> doubleParameter(String name, double minValue, double maxValue, boolean enableCheckbox);

  /**
   * Creates an integer parameter.
   * @param name the name of the parameter
   * @param minValue the minimum value of the parameter
   * @param maxValue the maximum value of the parameter
   * @return an {@link AbstractLayoutInterfaceItem<Integer>}
   */
  AbstractLayoutInterfaceItem<Integer> intParameter(String name, int minValue, int maxValue);

  /**
   * Creates a boolean parameter.
   * @param name the name of the parameter
   * @return an {@link AbstractLayoutInterfaceItem<Boolean>}
   */
  AbstractLayoutInterfaceItem<Boolean> booleanParameter(String name, boolean useAsMasterCheckbox);

  /**
   * Creates a parameter for a selection of the graph. It may be used to obtain nodes or edges the user has selected.
   * @param name the name of the parameter
   * @return an {@link AbstractLayoutInterfaceItem<IGraphSelection>}
   */
  AbstractLayoutInterfaceItem<IGraphSelection> selection(String name);

  /**
   * Creates an output parameter for debug vectors. Call {@link AbstractLayoutInterfaceItem#setValue(Object)} to set the
   * debug vectors or pass {@code null} to clear them.
   * @param name the name of the parameter
   * @return an output parameter to set or clear debug vectors.
   */
  AbstractLayoutInterfaceItem<Mapper<INode, PointD>> debugVectors(String name);

  /**
   * Creates an output parameter for status messages.
   * @param name the name of the parameter
   * @return an output parameter for status messages.
   */
  AbstractLayoutInterfaceItem<String> statusMessage(String name);
}
