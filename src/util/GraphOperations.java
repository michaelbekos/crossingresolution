package util;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.view.ISelectionModel;

import java.util.*;

public class GraphOperations {


    /**
     * Removes numVertices number of  nodes with the highest degree from graph g
     * @param g
     * @param numVertices
     * @return
     */
    public static VertexStack removeVertices(IGraph g, int numVertices, ISelectionModel<INode> selection, VertexStack removedVertices) {

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
            INode minDegVertex = g.getNodes().first();
            for (INode u: g.getNodes()) {
                if (u.getPorts().size() > minDegVertex.getPorts().size()) {
                    minDegVertex = u;
                }
            }
            Arrays.fill(maxDegVertex, minDegVertex);   //init with smallest

            //fill array maxDegVertex with n largest degree vertices (largest->smallest)
            for (INode u : g.getNodes()) {
                if (u.getPorts().size() > maxDegVertex[maxDegVertex.length-1].getPorts().size()) {
                    maxDegVertex[maxDegVertex.length-1] =  u;
                    Arrays.sort(maxDegVertex, (a,b) -> Integer.compare(a.getPorts().size(), b.getPorts().size()));  //todo check sorting large->small
                }
            }
        }



        if (removedVertices == null) {
            removedVertices = new VertexStack();
        }

        for (int i = 0; i < numVertices; i++) {
            removedVertices.push(maxDegVertex[i], g);
        }

        int[][] testReinsert = new int[numVertices][numVertices];

        INode [][] verticesAndEdges = new INode[numVertices][]; //First element in each subarray is the removed node, subsequent nodes are endpoints for edges to the removed node

        //initialize verticesAndEdges
        for (int i = 0; i < numVertices; i++) {

            verticesAndEdges[i] = new INode[maxDegVertex[i].getPorts().size()+1];
            verticesAndEdges[i][0] = maxDegVertex[i];
        }

        for (int i = numVertices-1 ; i >= 0; i--) { //remove first thr Vert. with the highest degree
            int j = 1;
            for (IPort p : verticesAndEdges[i][0].getPorts()) {
                for (IEdge e : g.edgesAt(p)) {
                    if (e.getSourceNode().equals(verticesAndEdges[i][0])) {
                        verticesAndEdges[i][j] = e.getTargetNode();
                    } else {
                        verticesAndEdges[i][j] = e.getSourceNode();
                    }
                    boolean deletNode = false;
                    for(int k=i; k >= 0; k--){  //check if the vertices before wich have edges to this one
                        if(verticesAndEdges[k][0].equals(verticesAndEdges[i][j])){
                            verticesAndEdges[i][j] = null;
                            testReinsert[i][k] = 1;
                            deletNode = true;
                        }
                    }
                    if(!deletNode){
                        j++;
                    }
                }
            }
            g.remove(verticesAndEdges[i][0]);
        }


        removedVertices.temp = testReinsert;
        removedVertices.verticesAndEdges_tmp = verticesAndEdges;

        return removedVertices;

    }

    /**
     * Reinserts the previously removed nodes
     * @param g
     * @param removedVertices
     */
    public static VertexStack reinsertVertices(IGraph g, VertexStack removedVertices) {

        INode[] reinsertedNodes = new INode[removedVertices.size()];
        int[][] testReinsert = removedVertices.temp;
        for (int i = 0; i < removedVertices.size(); i++) {
            reinsertedNodes[i] =  g.createNode(removedVertices.get(i).vertex.getLayout().toRectD(), removedVertices.get(i).vertex.getStyle(), removedVertices.get(i).vertex.getTag());
        }
        INode[][] vertices = removedVertices.verticesAndEdges_tmp;
        for (int i = 0; i < vertices.length; i++) {                     //edges to old nodes
            for (int j = 1; j < vertices[i].length; j++) {
                if (!(vertices[i][j]==null)) {
                    g.createEdge(reinsertedNodes[i], vertices[i][j]);
                }
            }
            for(int j = 0; j < testReinsert[i].length; j++){            //edges to new nodes
                if (testReinsert[i][j] == 1){
                    g.createEdge(reinsertedNodes[i], reinsertedNodes[j]);
                }
            }
        }
        removedVertices.temp = null;
        return null;
    }


    /**
     * Multiply the Coord. from each Node with the factor scaleValue
     * @return scaled grid points with scaleValue factor
     */
    public static  Mapper<INode, PointD> scaleUpProcess(IGraph g, Mapper<INode,PointD> nodePose, double scaleValue){
        for(INode u : g.getNodes()){
            nodePose.setValue(u, new PointD(u.getLayout().getCenter().getX() * scaleValue, u.getLayout().getCenter().getY() * scaleValue));
        }
        return nodePose;
    }

}