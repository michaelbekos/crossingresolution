package layout.algo;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;

public interface ILayout {
  void init();
  boolean executeStep(int iteration);
  Mapper<INode, PointD> getNodePositions();
}
