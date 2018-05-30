package layout.algo.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import util.graph2d.Intersection;


import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.IMapper;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.utils.IListEnumerable;


public class MutualityCheck {
	
	
	public static class MutualityCheckHelper{
		
		public static void detector (IGraph graph, Mapper<INode, PointD> nodePositions) {
			List<Intersection> getCrossings = algorithms.graphs.yFilesSweepLine.getCrossings(graph, true, nodePositions);
			int amountOfEdges = graph.getEdges().size();
			PointD crossingEdges[][]= new PointD [amountOfEdges][amountOfEdges];
			Boolean[][] crossingEdgesvalid = new Boolean [amountOfEdges][amountOfEdges];
			//Arrays.fill(crossingEdgesvalid, Boolean.FALSE);
			IListEnumerable<IEdge> edges = graph.getEdges();
			Iterator<Intersection> crossings= getCrossings.iterator();
			while (crossings.hasNext()) {
				Intersection crossing = crossings.next();
				int i=0;
				IEdge edge= edges.getItem(i);
				while(!crossing.segment1.e.equals(edge)&& i<edges.size()){
					i++;
					edge= edges.getItem(i);
				}
				int j=0;
				edge= edges.getItem(j);
				while(!crossing.segment2.e.equals(edge)&& j<edges.size()){
					j++;
					edge= edges.getItem(j);
				}
				System.out.println(i +" "+ j);
				crossingEdgesvalid[i][j]=true;
				crossingEdgesvalid[j][i]=true;
				crossingEdges[i][j]=crossing.intersectionPoint;
				crossingEdges[j][i]=crossing.intersectionPoint;
			}
			
			for(int i=0; i<amountOfEdges; i++){           // Zeile in der Tabelle
				for(int j=i+1; j<amountOfEdges; j++){     // erster Index
					for(int k=j+1;k<amountOfEdges; k++){  // zweiter Index
						if(crossingEdgesvalid[i][j]&&crossingEdgesvalid[i][k]&&crossingEdgesvalid[j][k]){
							Random r = new Random();
							int rnd= r.nextInt(6 - 1 + 1) + 1;
							switch (rnd){
								case 1:
									nodePositions.setValue(edges.getItem(i).getSourceNode(), moveNode(nodePositions, edges.getItem(i).getSourceNode(), crossingEdges[i][j], crossingEdges[i][k]));
									break;
								case 2:
									nodePositions.setValue(edges.getItem(i).getTargetNode(), moveNode(nodePositions, edges.getItem(i).getTargetNode(), crossingEdges[i][j], crossingEdges[i][k]));
									break;
								case 3:
									nodePositions.setValue(edges.getItem(j).getSourceNode(), moveNode(nodePositions, edges.getItem(j).getSourceNode(), crossingEdges[j][k], crossingEdges[i][j]));
									break;
								case 4:
									nodePositions.setValue(edges.getItem(j).getTargetNode(), moveNode(nodePositions, edges.getItem(j).getTargetNode(), crossingEdges[j][k], crossingEdges[i][j]));
									break;
								case 5:
									nodePositions.setValue(edges.getItem(k).getSourceNode(), moveNode(nodePositions, edges.getItem(k).getSourceNode(), crossingEdges[i][k], crossingEdges[j][k]));
									break;
								case 6:
									nodePositions.setValue(edges.getItem(k).getTargetNode(), moveNode(nodePositions, edges.getItem(k).getTargetNode(), crossingEdges[i][k], crossingEdges[j][k]));
									break;
							}
						}
					}
				}
			}
		}
		
		
	}

	public INode[] getNodes(IEdge item, IEdge item2, IEdge item3) {
		//Random r = new Random();
		//int rnd= r.nextInt(6 - 1 + 1) + 1;
		INode tmp[] = {item.getSourceNode(), item.getTargetNode(), item2.getSourceNode(), item2.getTargetNode(), item3.getSourceNode(), item3.getTargetNode()};
		return tmp;
	}

	public static PointD moveNode(IMapper<INode, PointD> nodePositions, INode sourceNode, PointD pointD, PointD pointD2) {
		PointD sourcePos = nodePositions.getValue(sourceNode);
		double tmp_x= sourcePos.getX();
		double tmp_y= sourcePos.getY();
		if(sourcePos.distanceTo(pointD)<sourcePos.distanceTo(pointD2)){
			PointD tmp=new PointD(pointD.getX()+0.1*(pointD.getX()-tmp_x), pointD.getY()+0.1*(pointD.getY()-tmp_y));
			sourcePos = tmp;
		}
		else{
			PointD tmp=new PointD(pointD2.getX()+0.1*(pointD2.getX()-tmp_x), pointD2.getY()+0.1*(pointD2.getY()-tmp_y));
			sourcePos = tmp;
		}
		return sourcePos;
	}

}
