package layout.algo.layoutinterface;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;

public class VoidItemFactory implements ILayoutInterfaceItemFactory{

  @Override
  public AbstractLayoutInterfaceItem<Double> doubleParameter(String name, double minValue, double maxValue, double threshold) {
    return new VoidItem<>(name);
  }

  @Override
  public AbstractLayoutInterfaceItem<Integer> intParameter(String name, int minValue, int maxValue, int threshold) {
    return new VoidItem<>(name);
  }

  @Override
  public AbstractLayoutInterfaceItem<Boolean> booleanParameter(String name) {
    return new VoidItem<>(name);
  }

  @Override
  public AbstractLayoutInterfaceItem<Mapper<INode, PointD>> debugVectors(String name) {
    return new VoidItem<>(name);
  }
}
