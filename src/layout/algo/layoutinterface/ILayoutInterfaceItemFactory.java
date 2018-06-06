package layout.algo.layoutinterface;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.view.IGraphSelection;

import java.util.Collection;
import java.util.List;


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
  AbstractLayoutInterfaceItem<Double> doubleParameter(String name, double minValue, double maxValue);

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
  AbstractLayoutInterfaceItem<Boolean> booleanParameter(String name);

  /**
   * Creates a wrapper for a parameter to make it toggleable. The parameter will NOT automatically be switched on or off
   * as it is impossible to determine what "on" and "off" mean for this specific parameter. The difference to
   * {@link #booleanParameter(String)} is that the implementation shall make this grouping explicit.
   * @param parameter a parameter that shall be toggleable
   * @return a boolean parameter that represents the state of the toggle
   */
  AbstractLayoutInterfaceItem<Boolean> toggleableParameter(AbstractLayoutInterfaceItem<?> parameter);

  /**
   * Creates a parameter that works as master toggle for a collection of boolean parameters. Setting its value will
   * automatically propagate to all parameters passed.
   * @param name the name of the parameter
   * @param parameters a collection of boolean parameters
   * @return a boolean parameter representing the state of the master toggle
   */
  AbstractLayoutInterfaceItem<Boolean> masterToggle(String name, Collection<AbstractLayoutInterfaceItem<Boolean>> parameters);

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

  /**
   * Creates a parameter that represents a list of uniformly distributed slopes/angles in radians.
   * @param name the name of the parameter
   * @return a parameter for slopes
   */
  AbstractLayoutInterfaceItem<List<Double>> slopesParameter(String name);
}
