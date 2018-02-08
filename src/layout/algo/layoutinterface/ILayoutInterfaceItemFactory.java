package layout.algo.layoutinterface;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.view.IGraphSelection;

public interface ILayoutInterfaceItemFactory {
  AbstractLayoutInterfaceItem<Double> doubleParameter(String name, double minValue, double maxValue, double threshold);
  AbstractLayoutInterfaceItem<Integer> intParameter(String name, int minValue, int maxValue, int threshold);
  AbstractLayoutInterfaceItem<Boolean> booleanParameter(String name);

  AbstractLayoutInterfaceItem<IGraphSelection> selection(String name);

  AbstractLayoutInterfaceItem<Mapper<INode, PointD>> debugVectors(String name);

  AbstractLayoutInterfaceItem<String> statusMessage(String name);
}
