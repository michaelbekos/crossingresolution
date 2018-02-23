package layout.algo;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;

import java.util.Set;

public interface ILayout {
  void init();
  void setFixNodes(Set<INode> fixNodes);
  boolean executeStep(int iteration, int maxIterations);
  Mapper<INode, PointD> getNodePositions();
  default void showDebug() {}
  default void clearDebug() {}
}
