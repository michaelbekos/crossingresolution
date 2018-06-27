package layout.algo.utils;

import java.util.Iterator;
import java.util.Map.Entry;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.IPort;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.utils.IListEnumerable;

public class LayoutUtils {
  public static PointD stepInDirection(PointD oldPosition, PointD direction, double stepSize) {
    return new PointD(
        oldPosition.getX() + stepSize * direction.getX(),
        oldPosition.getY() + stepSize * direction.getY()
    );
  }

  public static PointD round(PointD p) {
    return new PointD(Math.round(p.getX()), Math.round(p.getY()));
  }

public static Boolean overlapFree(PointD position, Mapper<INode, PointD> positions, INode node, IGraph graph, double th) {
	Iterator<Entry<INode, PointD>> It = positions.getEntries().iterator();
	PointD tmp;
	while(It.hasNext()){
		tmp=It.next().getValue();
		if (position.hits(tmp, th) || position.equals(tmp)) {
			return false;
		}
	}
	IListEnumerable<IPort> ports= node.getPorts();
	for(IPort port: ports){
		for(IEdge edge: graph.edgesAt(port)){
			It = positions.getEntries().iterator();
			while(It.hasNext()){
				tmp=It.next().getValue();
				if(tmp!=positions.getValue(edge.getSourceNode()) && tmp!=positions.getValue(edge.getTargetNode())
						&& tmp.hitsLineSegment(positions.getValue(edge.getSourceNode()), positions.getValue(edge.getTargetNode()), 0.5)){
					return false;
				}
			}
		}
	}
	return true;
}
	public static Boolean overlapFree(PointD position, Mapper<INode, PointD> positions, INode node, IGraph graph) {
		return overlapFree(position, positions,node, graph, 0.5);
	}
}
