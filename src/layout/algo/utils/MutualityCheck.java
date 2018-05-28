package layout.algo.utils;

import java.util.Iterator;
import java.util.List;
import util.graph2d.Intersection;


import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.IMapper;
import com.yworks.yfiles.graph.INode;


public class MutualityCheck {
	public static class MutualityCheckHelper{
		
		public void detector (IGraph graph, IMapper<INode, PointD> nodePositions) {
			List<Intersection> getCrossings = algorithms.graphs.yFilesSweepLine.getCrossings(graph, true, nodePositions);
			int amountOfNodes = graph.getNodes().size();
			int neighborsWithCrossings[][]= new int [amountOfNodes][amountOfNodes];
			Iterator<Intersection> crossings= getCrossings.iterator();
			while (crossings.hasNext()) {
				Intersection crossing = crossings.next();
				neighborsWithCrossings[Integer.parseInt(crossing.segment1.n1.getTag().toString())][Integer.parseInt(crossing.segment1.n2.getTag().toString())]=1; // 1-2
				neighborsWithCrossings[Integer.parseInt(crossing.segment1.n1.getTag().toString())][Integer.parseInt(crossing.segment2.n1.getTag().toString())]=1; // 1-3
				neighborsWithCrossings[Integer.parseInt(crossing.segment1.n1.getTag().toString())][Integer.parseInt(crossing.segment2.n2.getTag().toString())]=1; // 1-4
				neighborsWithCrossings[Integer.parseInt(crossing.segment1.n2.getTag().toString())][Integer.parseInt(crossing.segment1.n2.getTag().toString())]=1; // 2-1
				neighborsWithCrossings[Integer.parseInt(crossing.segment1.n2.getTag().toString())][Integer.parseInt(crossing.segment2.n1.getTag().toString())]=1; // 2-3
				neighborsWithCrossings[Integer.parseInt(crossing.segment1.n2.getTag().toString())][Integer.parseInt(crossing.segment2.n2.getTag().toString())]=1; // 2-4
				neighborsWithCrossings[Integer.parseInt(crossing.segment2.n1.getTag().toString())][Integer.parseInt(crossing.segment1.n1.getTag().toString())]=1; // 3-1
				neighborsWithCrossings[Integer.parseInt(crossing.segment2.n1.getTag().toString())][Integer.parseInt(crossing.segment1.n2.getTag().toString())]=1; // 3-2
				neighborsWithCrossings[Integer.parseInt(crossing.segment2.n1.getTag().toString())][Integer.parseInt(crossing.segment2.n2.getTag().toString())]=1; // 3-4
				neighborsWithCrossings[Integer.parseInt(crossing.segment2.n2.getTag().toString())][Integer.parseInt(crossing.segment1.n1.getTag().toString())]=1; // 4-1
				neighborsWithCrossings[Integer.parseInt(crossing.segment2.n2.getTag().toString())][Integer.parseInt(crossing.segment1.n2.getTag().toString())]=1; // 4-2
				neighborsWithCrossings[Integer.parseInt(crossing.segment2.n2.getTag().toString())][Integer.parseInt(crossing.segment2.n2.getTag().toString())]=1; // 4-3
			}
			for(int i=1; i<amountOfNodes; i++){
				for(int j=0; j<i; j++){
					// TODO: weiter machen
				}
			}
		}
		
		
	}
}
