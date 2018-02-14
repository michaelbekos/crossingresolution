package layout.algo.forces;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

public interface IForce {
  void init(ILayoutInterfaceItemFactory itemFactory);
  Mapper<INode, PointD> calculate(Mapper<INode, PointD> forces, Mapper<INode, PointD> nodePositions);
  void toggleCheckbox(boolean value);
}