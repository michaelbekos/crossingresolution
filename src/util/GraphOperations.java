package util;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.view.ISelectionModel;

import java.util.*;

public class GraphOperations {

	private static Comparator<INode> byPortSize = Comparator.<INode>comparingInt(p -> p.getPorts().size());

    /**
     * Removes numVertices number of nodes with the highest or lowest degree from graph g
     * @param g
     * @param numVertices
     * @return
     */
    public static VertexStack removeVertices(IGraph g, boolean removeHighestDegree, int numVertices, ISelectionModel<INode> selection, VertexStack removedVertices) {

        int tagNum = 0;
        for (INode u : g.getNodes()) {
            if (u.getTag() == null) {
               u.setTag(tagNum);
            }
            tagNum++;
        }
        if (numVertices == 0) {
            return removedVertices;
        }
        INode[] maxDegVertex = new INode[numVertices];


        if (selection != null) {
            int i = 0;
            for (INode u : g.getNodes()) {
                if (selection.isSelected(u)) {
                    maxDegVertex[i] = u;
                    i++;
                }
            }
        } else {
            int i = 0;
            for (INode u: g.getNodes()) {
                maxDegVertex[i] = u;
                i++;
                if(i >= numVertices){
                    break;
                }
            }
            if (removeHighestDegree) {
                //fill array maxDegVertex with n largest degree vertices (largest->smallest)
                Arrays.sort(maxDegVertex, byPortSize);
                while (i < g.getNodes().size()) {
                    if (g.getNodes().getItem(i).getPorts().size() > maxDegVertex[maxDegVertex.length - 1].getPorts().size()) {
                        maxDegVertex[maxDegVertex.length - 1] = g.getNodes().getItem(i);
                        Arrays.sort(maxDegVertex, byPortSize);
                    }
                    i++;
                }
            } else {
                //fill array maxDegVertex with n smallest degree vertices (smallest->largest), defaults degree 0,1,2 vertices
                Arrays.sort(maxDegVertex, byPortSize);
                while (i < g.getNodes().size()) {
                    if (g.getNodes().getItem(i).getPorts().size() < maxDegVertex[maxDegVertex.length - 1].getPorts().size()) {
                        maxDegVertex[maxDegVertex.length - 1] = g.getNodes().getItem(i);
                        Arrays.sort(maxDegVertex, byPortSize);
                    }
                    i++;
                }
            }
        }

        if (removedVertices == null) {
            removedVertices = new VertexStack(g);
        }

        for (int i = 0; i < numVertices; i++) {
            removedVertices.push(maxDegVertex[i], g);
            g.remove(maxDegVertex[i]);
        }
        removedVertices.componentStack.add(numVertices);

        return removedVertices;

    }

    /**
     * Reinserts the previously removed nodes
     * @param g
     * @param removedVertices
     */
    public static VertexStack reinsertVertices(IGraph g, boolean useVertices, int numVerticesComponents, VertexStack removedVertices) {
        int numVertices = 0;
        if (useVertices) {  //reinsert vertices
            numVertices = numVerticesComponents;
            int diff = -numVertices;
            for (int i = removedVertices.componentStack.size(); i >= 0 ; --i) {         //fix component stack for vertices removed
                diff += removedVertices.componentStack.get(removedVertices.componentStack.size() - 1);
                if (diff < 0) {
                    removedVertices.componentStack.remove(removedVertices.componentStack.size() - 1);
                } else if (diff == 0) {
                    removedVertices.componentStack.remove(removedVertices.componentStack.size() - 1);
                    break;
                } else {
                    removedVertices.componentStack.set(removedVertices.componentStack.size() - 1, diff);
                    break;
                }
            }
        } else {            //reinsert components
            for (int i = 0; i < numVerticesComponents; i++) {
                numVertices += removedVertices.componentStack.get(removedVertices.componentStack.size() - 1);
                removedVertices.componentStack.remove(removedVertices.componentStack.size() - 1);
            }
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
                if (tag == removedVertices.edgeList[i][0]) {    //u = source node
                    INode target = tagMap.getValue(removedVertices.edgeList[i][1]);  //find target node with tag
                    if (target != null && g.getEdge(u, target) == null){
                        g.createEdge(u, target);
                    }
                } else if (tag == removedVertices.edgeList[i][1]) { //u = target node
                    INode source = tagMap.getValue(removedVertices.edgeList[i][0]);  //find source node with tag
                    if (source != null && g.getEdge(source, u) == null ){
                        g.createEdge(source, u);
                    }
                }
            }
        }

        return removedVertices;
    }


    /**
     * Multiply the Coord. from each Node with the factor scaleValue
     * @return scaled grid points with scaleValue factor
     */
    public static  Mapper<INode, PointD> scaleUpProcess(IGraph g, Mapper<INode,PointD> nodePose, double scaleValue){
    	double minX=Double.POSITIVE_INFINITY, minY=Double.POSITIVE_INFINITY;
    	for(INode u : g.getNodes()){
    		if(u.getLayout().getCenter().getX()<minX){
        		minX=u.getLayout().getCenter().getX();
        	}
        	if(u.getLayout().getCenter().getY()<minY){
        		minY=u.getLayout().getCenter().getY();
        	}
        }  
        for(INode u : g.getNodes()){
        	nodePose.setValue(u, new PointD((u.getLayout().getCenter().getX()-minX) * scaleValue, (u.getLayout().getCenter().getY()-minY) * scaleValue));
            //g.setNodeLayout(u, new RectD(u.getLayout().getX()*scaleValue,u.getLayout().getY()*scaleValue,u.getLayout().getWidth()*scaleValue,u.getLayout().getHeight()*scaleValue));
        	g.setNodeLayout(u, new RectD(u.getLayout().getX(),u.getLayout().getY(),u.getLayout().getWidth(),u.getLayout().getHeight()));
        }
        return nodePose;
    }

}
