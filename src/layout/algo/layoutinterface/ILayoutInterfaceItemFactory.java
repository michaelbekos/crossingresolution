package layout.algo.layoutinterface;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;

public interface ILayoutInterfaceItemFactory {
  AbstractLayoutInterfaceItem<Double> doubleParameter(String name, double minValue, double maxValue);
  AbstractLayoutInterfaceItem<Boolean> booleanParameter(String name);

  AbstractLayoutInterfaceItem<Mapper<INode, PointD>> debugVectors(String name);
}
