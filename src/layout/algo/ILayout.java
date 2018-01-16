package layout.algo;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

public interface ILayout {
  void init(ILayoutInterfaceItemFactory interfaceItemFactory);
  boolean executeStep(int iteration);
  Mapper<INode, PointD> getNodePositions();
}
