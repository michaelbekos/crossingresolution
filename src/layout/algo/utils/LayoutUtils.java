package layout.algo.utils;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.IPort;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.utils.IListEnumerable;
import graphoperations.GraphOperations;
import util.BoundingBox;

import static graphoperations.GraphOperations.euclidDist;

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

	public static Boolean overlap(PointD position, Mapper<INode, PointD> positions, INode node, IGraph graph) {		//TODO: actually returns NOT overlap
		Iterator<Entry<INode, PointD>> It = positions.getEntries().iterator();
		PointD tmp;
		while(It.hasNext()){
			tmp=It.next().getValue();
			if (position.hits(tmp, 0.5) || position.equals(tmp)) {
				return false;
			}
		}
		IListEnumerable<IPort> ports= node.getPorts();
		for(IPort port: ports){
			for(IEdge edge: graph.edgesAt(port)){
				It = positions.getEntries().iterator();
				while(It.hasNext()){
					tmp=It.next().getValue();
					try {
					if(tmp!=positions.getValue(edge.getSourceNode()) && tmp!=positions.getValue(edge.getTargetNode())
							&& tmp.hitsLineSegment(positions.getValue(edge.getSourceNode()), positions.getValue(edge.getTargetNode()), 0.5)){
						return false;
					}} catch (NullPointerException e) {
//						System.out.println(e);
						return true;
					}
				}
			}
		}
		return true;
	}

	public static Boolean overlapNodeEdge(PointD position, Mapper<INode, PointD> positions, IGraph graph) {
		for(IEdge edge: graph.getEdges()){
			INode src = edge.getSourceNode();
			INode dst = edge.getTargetNode();
			double src_node_dist, node_dst_dist, src_dst_dist;
			src_node_dist = node_dst_dist = src_dst_dist = -1;
			try {
				src_node_dist = GraphOperations.euclidDist(positions.getValue(src).getX(), positions.getValue(src).getY(), position.getX(), position.getY());
				node_dst_dist = GraphOperations.euclidDist(position.getX(), position.getY(), positions.getValue(dst).getX(), positions.getValue(dst).getY());
				src_dst_dist = GraphOperations.euclidDist(positions.getValue(src).getX(), positions.getValue(src).getY(), positions.getValue(dst).getX(), positions.getValue(dst).getY());
			} catch (NullPointerException e) {
//					System.out.println(e);
//				return false;
			}
			if (src_dst_dist == src_node_dist + node_dst_dist) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkNodeEdgeOverlap(IGraph graph, Mapper<INode, PointD> positions) {
		double edgeThreshold = 0.001;
		boolean nodeEdgeOverlap = false;
		for (INode u : graph.getNodes()) {
			if (u.getPorts().size() == 1) {
				for (IEdge e : graph.edgesAt(u.getPorts().first())) {
					INode src = e.getSourceNode();
					INode dst = e.getTargetNode();
					for (INode v : graph.getNodes()) {
						if ((u.hashCode() != v.hashCode()) &&
								v.hashCode() != src.hashCode() && v.hashCode() != dst.hashCode()) {
							try {
								double x0 = positions.getValue(v).getX();
								double y0 = positions.getValue(v).getY();
								double x1 = positions.getValue(src).getX();
								double y1 = positions.getValue(src).getY();
								double x2 = positions.getValue(dst).getX();
								double y2 = positions.getValue(dst).getY();
								double dist;
								if (y1 == y2) { //vertical
									dist = Math.abs(x0 - x1);
								} else if (x1 == x2) {  //horizontal
									dist = Math.abs(y0 - y1);
								} else {
									dist = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1) / Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
								}
								if (dist < edgeThreshold) {
									nodeEdgeOverlap = true;
								}
							} catch (NullPointerException ex) {

							}
						}
					}
				}
			}
		}
		return nodeEdgeOverlap;
	}

//	public static boolean anotherNodeEdgeOverlap(PointD position, IGraph graph, Mapper<INode, PointD> positions) {
//		double edgeThreshold = 0.001;
//		boolean nodeEdgeOverlap = false;
//		for (IEdge e : graph.edgesAt(u.getPorts().first())) {
//			INode src = e.getSourceNode();
//			INode dst = e.getTargetNode();
//			for (INode v : graph.getNodes()) {
//				if ((u.hashCode() != v.hashCode()) &&
//						v.hashCode() != src.hashCode() && v.hashCode() != dst.hashCode()) {
//					try {
//						double x0 = positions.getValue(v).getX();
//						double y0 = positions.getValue(v).getY();
//						double x1 = positions.getValue(src).getX();
//						double y1 = positions.getValue(src).getY();
//						double x2 = positions.getValue(dst).getX();
//						double y2 = positions.getValue(dst).getY();
//						double dist;
//						if (y1 == y2) { //vertical
//							dist = Math.abs(x0 - x1);
//						} else if (x1 == x2) {  //horizontal
//							dist = Math.abs(y0 - y1);
//						} else {
//							dist = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1) / Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
//						}
//						if (dist < edgeThreshold) {
//							nodeEdgeOverlap = true;
//						}
//					} catch (NullPointerException ex) {
//
//					}
//				}
//			}
//		}
//		return nodeEdgeOverlap;
//	}




	/**
	 * Calculates the area of the parallelogram of the three points.
	 * This is actually the same as the area of the triangle defined by the three points, multiplied by 2.
	 * @return 2 * triangleArea(a,b,c)
	 */
	static double perpDotProduct(PointD a, PointD b, PointD c)
	{
		return (a.x - c.x) * (b.y - c.y) - (a.y - c.y) * (b.x - c.x);
	}

	public static double getEpsilon(PointD v1, PointD v2)
	{
		double dx1 = v2.x - v1.x;
		double dy1 = v2.y - v1.y;
		double epsilon = 0.003 * (dx1 * dx1 + dy1 * dy1);
		return epsilon;
	}
	static boolean isPointOnLineviaPDP(PointD p, PointD v1, PointD v2)
	{
		return ( Math.abs(perpDotProduct(v1, v2, p)) < getEpsilon(v1, v2));
	}
	/**
	 * Check if the point is on the line segment.
	 */
	public static boolean isPointOnLineSegmentViaCrossProduct(PointD p, PointD v1, PointD v2)
	{
		if (!( (v1.x <= p.x && p.x <= v2.x) || (v2.x <= p.x && p.x <= v1.x) ))
		{
			// test point not in x-range
			return false;
		}
		if (!( (v1.y <= p.y && p.y <= v2.y) || (v2.y <= p.y && p.y <= v1.y) ))
		{
			// test point not in y-range
			return false;
		}
		return isPointOnLineviaPDP(p, v1, v2);
	}

	public static boolean suckit(PointD position, IGraph graph, Mapper<INode, PointD> positions) {
		for(IEdge edge: graph.getEdges()) {
			INode src = edge.getSourceNode();
			INode dst = edge.getTargetNode();
			if (isPointOnLineSegmentViaCrossProduct(new PointD(position.getX(), position.getY()), new PointD(positions.getValue(src).getX(), positions.getValue(src).getY()),
					new PointD(positions.getValue(dst).getX(), positions.getValue(dst).getY()))){
				return true;
			}
		}
		return false;
	}

	public static Boolean randomLayout(IGraph graph, int[] BOX_SIZE, Mapper<INode, PointD> positions) {
//		Mapper<INode, PointD> positions = PositionMap.FromIGraph(graph);
		Random random = new Random(System.currentTimeMillis());

		for (INode u : graph.getNodes()) {
//			System.out.println("node " + u.toString());
			boolean validPosition = false;
			PointD position = new PointD(0,0);
			int counter = 0;
			while (!validPosition) {
//				System.out.println("in da while ");
				if (counter >= 20) {
//					System.out.println("shuffle");
//					positions = shufflePositions(positions, BOX_SIZE, graph);
					return false;
				}
				position = new PointD(random.nextInt(BOX_SIZE[0]),random.nextInt(BOX_SIZE[1]));
				validPosition =
						position.getX() >= 0 && position.getY() >= 0
						&& LayoutUtils.overlap(position, positions, u, graph)
//								&& !LayoutUtils.overlapNodeEdge(position, positions, graph)
				&& !checkNodeEdgeOverlap(graph, positions);
//				System.out.println("pos " + position.toString() + " valid pos : " + validPosition + " " +
//						LayoutUtils.overlap(position, positions, u, graph) + " " +!LayoutUtils.overlapNodeEdge(position, positions, graph) + " " +(position.getX() >= 0 && position.getY() >= 0)
//				+ " Also NE ol: " + !checkNodeEdgeOverlap(graph, positions) );
				counter++;
			}

			positions.setValue(u, position);
		}

  	return true;
	}


}
