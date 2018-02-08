package util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.utils.IEnumerable;

import algorithms.graphs.yFilesSweepLine;
import layout.algo.utils.PositionMap;
import util.graph2d.Intersection;

public class Chains {

	public static VertexStack reinsertChain(IGraph g, VertexStack removedVertices) {
		int numVertices = 0;
                numVertices += removedVertices.componentStack.get(removedVertices.componentStack.size() - 1);
                removedVertices.componentStack.remove(removedVertices.componentStack.size() - 1);

        	INode[] outside=new INode[2];
        	INode[] temp=new INode[g.getNodes().size()];
        	int temp_int=0;
        	for (INode u2: g.getNodes()){
        		temp[temp_int]= u2;
        		temp_int++;
        	}
        	
        	
        	INode[] reinsertedNodes = new INode[numVertices];
	        for (int i = 0; i < numVertices; i++) {
	            INode removedNode = removedVertices.pop().vertex;
	            reinsertedNodes[(numVertices - 1) - i] =  g.createNode(removedNode.getLayout().toPointD(), removedNode.getStyle(), removedNode.getTag());
	        }
	        Mapper<Integer, INode> tagMap = new Mapper<>(new WeakHashMap<>());
	        for (INode n : g.getNodes()) {
	            tagMap.setValue(Integer.parseInt(n.getTag().toString()), n);
	        }
	        for (INode u : reinsertedNodes) {
	            int tag = Integer.parseInt(u.getTag().toString());
	            for (int i = 0; i < removedVertices.edgeList.length; i++) {
	            	INode connection=null;
	                if (tag == removedVertices.edgeList[i][0]) {    //u = source node
	                    connection = tagMap.getValue(removedVertices.edgeList[i][1]);  //find target node with tag
//	                    if (connection != null && g.getEdge(u, connection) == null){
//	                        g.createEdge(u, connection);
//	                    }
	                } else if (tag == removedVertices.edgeList[i][1]) { //u = target node
	                    connection = tagMap.getValue(removedVertices.edgeList[i][0]);  //find source node with tag
//	                    if (connection != null && g.getEdge(connection, u) == null ){
//	                        g.createEdge(connection, u);
//	                    }
	                }
	                if (connection != null && check_non_membership(connection, temp)){
	                	if (outside[0]==null){
	                		outside[0]=connection;
	                	}
	                	else {outside[1]=connection;}
	                }
	                connection=null;
	            }
	        }
//	        List<INode> path=findpath(outside[0], outside[1], g);
//	        System.out.println(path);

	        List<Intersection> crossings = yFilesSweepLine.getCrossings(g, true,  PositionMap.FromIGraph(g));
	        System.out.println(crossings);
	        
	        for (INode u : reinsertedNodes) {
	            int tag = Integer.parseInt(u.getTag().toString());
	            for (int i = 0; i < removedVertices.edgeList.length; i++) {
	            	INode connection=null;
	                if (tag == removedVertices.edgeList[i][0]) {    //u = source node
	                    connection = tagMap.getValue(removedVertices.edgeList[i][1]);  //find target node with tag
	                    if (connection != null && g.getEdge(u, connection) == null){
	                        g.createEdge(u, connection);
	                    }
	                } else if (tag == removedVertices.edgeList[i][1]) { //u = target node
	                    connection = tagMap.getValue(removedVertices.edgeList[i][0]);  //find source node with tag
	                    if (connection != null && g.getEdge(connection, u) == null ){
	                        g.createEdge(connection, u);
	                    }
	                }
	                connection=null;
	            }
	        }
	        return removedVertices;
	}
	
	
	private static boolean check_non_membership(INode connection, INode[] temp) {
		for (INode u : temp){
			if (connection==u){
				return true;
			}
		}
		return false;
	}
	
	
/*
 * Dead code follows, as it isn't needed, but nice to have	
 */
/*	private static List<INode> findpath(INode iNode, INode iNode2, IGraph g) {
    	Hashtable<INode, INode> predecessor = new Hashtable<>();
    	Hashtable<INode, Integer> dijkstra= new Hashtable<>();
    	LinkedList<INode> unvisited= new LinkedList<INode>();
    	for(INode n: g.getNodes()){
    		dijkstra.put(n, Integer.MAX_VALUE);
    	}
    	dijkstra.put(iNode2, 0);
    	unvisited.push(iNode2);
    	while(dijkstra.get(iNode)==Integer.MAX_VALUE){
    		INode temp=unvisited.pop();
    		IEnumerable<INode> enumerator= g.neighbors(INode.class, temp);
    		int own_value=dijkstra.get(temp);
    		for (INode n: enumerator){
    			if (own_value+1<dijkstra.get(n)){
    				dijkstra.put(n, own_value+1);
    				predecessor.put(n, temp);
    				unvisited.add(n);
    			}
    		}
    	}
    	ArrayList<INode> path = new ArrayList<INode>();
    	path.add(iNode);
    	INode pre= predecessor.get(iNode);
    	while (pre != null){
    		path.add(pre);
    		pre=predecessor.get(pre);
    	}
		return path;
	}*/
}
