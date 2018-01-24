package layout.algo.forces;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.util.ArrayList;

public interface IForce {
  void init(ILayoutInterfaceItemFactory itemFactory);
  Mapper<INode, PointD> calculate(Mapper<INode, PointD> forces, Mapper<INode, PointD> nodePositions);
  ArrayList<AbstractLayoutInterfaceItem> getAbstractLayoutInterfaceItems();

}