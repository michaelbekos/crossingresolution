package layout.algo.layoutinterface;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.view.IGraphSelection;

import java.util.Collection;
import java.util.List;

/**
 * A void implementation of {@link ILayoutInterfaceItemFactory} that returns {@link VoidItem}s. These provide only
 * limited functionality.
 */
public class VoidItemFactory implements ILayoutInterfaceItemFactory {

  @Override
  public AbstractLayoutInterfaceItem<Double> doubleParameter(String name, double minValue, double maxValue) {
    return new VoidItem<>(name);
  }

  @Override
  public AbstractLayoutInterfaceItem<Integer> intParameter(String name, int minValue, int maxValue) {
    return new VoidItem<>(name);
  }

  @Override
  public AbstractLayoutInterfaceItem<Boolean> booleanParameter(String name) {
    return new VoidItem<>(name);
  }

  @Override
  public AbstractLayoutInterfaceItem<Boolean> toggleableParameter(AbstractLayoutInterfaceItem<?> parameter) {
    return new VoidItem<>(parameter.getName());
  }

  @Override
  public AbstractLayoutInterfaceItem<Boolean> masterToggle(String name, Collection<AbstractLayoutInterfaceItem<Boolean>> parameters) {
    return new VoidItem<>(name);
  }

  @Override
  public AbstractLayoutInterfaceItem<IGraphSelection> selection(String name) {
    return new VoidItem<>(name);
  }

  @Override
  public AbstractLayoutInterfaceItem<Mapper<INode, PointD>> debugVectors(String name) {
    return new VoidItem<>(name);
  }

  @Override
  public AbstractLayoutInterfaceItem<String> statusMessage(String name) {
    return new VoidItem<>(name);
  }

  @Override
  public AbstractLayoutInterfaceItem<List<Double>> slopesParameter(String name) {
    return new VoidItem<>(name);
  }
}
