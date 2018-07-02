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

	public static Boolean overlapFree(PointD position, Mapper<INode, PointD> positions, INode node, IGraph graph) {
		Iterator<Entry<INode, PointD>> It2 = positions.getEntries().iterator();
		PointD tmp;
		while(It2.hasNext()){					//Check new position does not overlap with nodes
			tmp=It2.next().getValue();
			if (position.hits(tmp, 0.999) || position.equals(tmp)) {
				return false;
			}
		}
		for (IEdge edge : graph.getEdges()) {	//Check new position does not overlap with edges
			double x0 = position.getX();
			double y0 = position.getY();
			double x1 = positions.getValue(edge.getSourceNode()).getX();
			double y1 = positions.getValue(edge.getSourceNode()).getY();
			double x2 = positions.getValue(edge.getTargetNode()).getX();
			double y2 = positions.getValue(edge.getTargetNode()).getY();
			if (x0 == x1 && x1 == x2 || y0 == y1 && y1 == y2) {	//preemptive horiz/vert check
				return false;
			}
			double dist = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1) / Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));

			if(position!=positions.getValue(edge.getSourceNode()) && position!=positions.getValue(edge.getTargetNode())
//						&& tmp.hitsLineSegment(positions.getValue(edge.getSourceNode()), positions.getValue(edge.getTargetNode()), 0.5)){			//ERROR HERE
					&& dist < 0.01){
				return false;
			}
		}
		for(IPort port: node.getPorts()){		//Check new edges at new position do not overlap with nodes	//TODO: merge above
			for(IEdge edge: graph.edgesAt(port)){
				Iterator<Entry<INode, PointD>> It = positions.getEntries().iterator();
				while(It.hasNext()){
					tmp=It.next().getValue();
					double x0 = position.getX();
					double y0 = position.getY();
					double x1 = positions.getValue(edge.getSourceNode()).getX();
					double y1 = positions.getValue(edge.getSourceNode()).getY();
					double x2 = positions.getValue(edge.getTargetNode()).getX();
					double y2 = positions.getValue(edge.getTargetNode()).getY();
					if (x0 == x1 && x1 == x2 || y0 == y1 && y1 == y2) {	//preemptive horiz/vert check
						return false;
					}
					double dist = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1) / Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));

					if(It.next() != node && tmp!=positions.getValue(edge.getSourceNode()) && tmp!=positions.getValue(edge.getTargetNode())
//						&& tmp.hitsLineSegment(positions.getValue(edge.getSourceNode()), positions.getValue(edge.getTargetNode()), 0.5)){			//ERROR HERE
							&& dist < 0.01){
						return false;
					}
				}
			}
		}
		return true;
	}
}
