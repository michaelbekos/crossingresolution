package graphoperations;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.utils.IListEnumerable;
import layout.algo.utils.PositionMap;
import util.graph2d.Intersection;

import java.util.*;

public class GraphOperations {
  private static Mapper<INode, PointD> positions;

  public static double aspect_ratio(IGraph g){
    IEdge sEdge = getSmallstEdge(g);
    INode sSource = sEdge.getSourceNode();
    INode sTarget = sEdge.getTargetNode();
    double sLength  = euclidDist(sSource.getLayout().getX(), sSource.getLayout().getY(), sTarget.getLayout().getX(), sTarget.getLayout().getY());
    IEdge lEdge = getLongestEdge(g);
    INode lSource = lEdge.getSourceNode();
    INode lTarget = lEdge.getTargetNode();
    double lLength  = euclidDist(lSource.getLayout().getX(), lSource.getLayout().getY(), lTarget.getLayout().getX(), lTarget.getLayout().getY());
    return lLength / sLength;
  }

  public static IEdge getSmallstEdge(IGraph g){
    IListEnumerable<IEdge> edgeList = g.getEdges();

    if(edgeList.size() >= 1) {
      IEdge edge = edgeList.getItem(0);
      INode source = edge.getSourceNode();
      INode target = edge.getTargetNode();
      double length  = euclidDist(source.getLayout().getX(), source.getLayout().getY(), target.getLayout().getX(), target.getLayout().getY());
      double smallLength = length;
      IEdge smallEdge = edge;

      for (int i = 1; i < edgeList.size(); i++) {
        edge = edgeList.getItem(i);
        source = edge.getSourceNode();
        target = edge.getTargetNode();
        length = euclidDist(source.getLayout().getX(), source.getLayout().getY(), target.getLayout().getX(), target.getLayout().getY());
        if(length < smallLength){
          smallLength = length;
          smallEdge = edge;
        }
      }

      return smallEdge;
    }
    else{
      System.out.println("The graph has zero edges");
      return null;
    }
  }

  public static IEdge getLongestEdge(IGraph g){
    IListEnumerable<IEdge> edgeList = g.getEdges();

    if(edgeList.size() >= 1) {
      IEdge edge = edgeList.getItem(0);
      INode source = edge.getSourceNode();
      INode target = edge.getTargetNode();
      double length  = euclidDist(source.getLayout().getX(), source.getLayout().getY(), target.getLayout().getX(), target.getLayout().getY());
      double smallLength = length;
      IEdge smallEdge = edge;

      for (int i = 1; i < edgeList.size(); i++) {
        edge = edgeList.getItem(i);
        source = edge.getSourceNode();
        target = edge.getTargetNode();
        length = euclidDist(source.getLayout().getX(), source.getLayout().getY(), target.getLayout().getX(), target.getLayout().getY());
        if(length > smallLength){
          smallLength = length;
          smallEdge = edge;
        }
      }

      return smallEdge;
    }
    else{
      System.out.println("The graph has zero edges");
      return null;
    }
  }

  public static double euclidDist(double x1, double y1, double x2, double y2){
    return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 -y1), 2));
  }

  public static VertexStack reinsertChain(IGraph g, VertexStack removedVertices) {
    positions = PositionMap.FromIGraph(g);
    int numVertices = 0;
    numVertices += removedVertices.componentStack.get(removedVertices.componentStack.size() - 1);
    removedVertices.componentStack.remove(removedVertices.componentStack.size() - 1);

    INode[] outside = new INode[2];
    INode[] temp = new INode[g.getNodes().size()];
    int temp_int = 0;
    for (INode u2 : g.getNodes()) {
      temp[temp_int] = u2;
      temp_int++;
    }


    INode[] reinsertedNodes = new INode[numVertices];
    for (int i = 0; i < numVertices; i++) {
      INode removedNode = removedVertices.pop().vertex;
      reinsertedNodes[(numVertices - 1) - i] = g.createNode(removedNode.getLayout().toPointD(), removedNode.getStyle(), removedNode.getTag());
    }
    Mapper<Integer, INode> tagMap = new Mapper<>(new WeakHashMap<>());
    for (INode n : g.getNodes()) {
      tagMap.setValue(Integer.parseInt(n.getTag().toString()), n);
    }
    for (INode u : reinsertedNodes) {
      int tag = Integer.parseInt(u.getTag().toString());
      for (int i = 0; i < removedVertices.edgeList.length; i++) {
        INode connection = null;
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
        if (connection != null && check_non_membership(connection, temp)) {
          if (outside[0] == null) {
            outside[0] = connection;
          } else {
            outside[1] = connection;
          }
        }
        connection = null;
      }
    }
    IListEnumerable<IEdge> all_edges_1 = g.edgesAt(outside[0]);
    IListEnumerable<IEdge> all_edges_2 = g.edgesAt(outside[1]);
//	        List<INode> path=findpath(outside[0], outside[1], g);
//	        System.out.println(path);

    Optional<Intersection> minAngleCr = MinimumAngle.getMinimumAngleCrossing(g);
    double angle = minAngleCr.get().angle;
    System.out.println("Angle: " + angle);
    for (INode u : reinsertedNodes) {
      int tag = Integer.parseInt(u.getTag().toString());
      for (int i = 0; i < removedVertices.edgeList.length; i++) {
        INode connection = null;
        if (tag == removedVertices.edgeList[i][0]) {    //u = source node
          connection = tagMap.getValue(removedVertices.edgeList[i][1]);  //find target node with tag
          if (connection != null && g.getEdge(u, connection) == null) {
            g.createEdge(u, connection);
          }
        } else if (tag == removedVertices.edgeList[i][1]) { //u = target node
          connection = tagMap.getValue(removedVertices.edgeList[i][0]);  //find source node with tag
          if (connection != null && g.getEdge(connection, u) == null) {
            g.createEdge(connection, u);
          }
        }
        connection = null;
      }
    }
    minAngleCr = MinimumAngle.getMinimumAngleCrossing(g);
    double temp_angle = minAngleCr.get().angle;
    System.out.println(temp_angle);
    positions = getNodePositions();
    PointD temp_pos = new PointD();
    if (numVertices == 1) {
      for (IEdge e : all_edges_1) {
        INode target = e.getSourceNode();
        if (target == outside[0]) {
          target = e.getTargetNode();
        }
        double[] source_pos = {positions.getValue(outside[0]).x, positions.getValue(outside[0]).y};
        double[] target_pos = {positions.getValue(target).x, positions.getValue(target).y};

        double x = target_pos[0] - (sign(target_pos[0], source_pos[0]) * 0.5);
        double y = target_pos[1] - (sign(target_pos[1], source_pos[1]) * 0.5);
        PointD new_pos = new PointD(x, y);
        System.out.println(new_pos);
        positions.setValue(reinsertedNodes[0], new_pos);

        PositionMap.applyToGraph(g, positions);


        minAngleCr = MinimumAngle.getMinimumAngleCrossing(g);
        temp_angle = minAngleCr.get().angle;
        System.out.println(temp_angle);
        if (temp_angle >= angle * 0.99) {
          temp_pos = new_pos;
        }
        temp_angle = 0.0;
      }
    }

//	        List<Intersection> crossings = yFilesSweepLine.getCrossings(g, true,  PositionMap.FromIGraph(g));
//	        System.out.println(crossings);
    positions.setValue(reinsertedNodes[0], temp_pos);
    PositionMap.applyToGraph(g, positions);
    return removedVertices;
  }

  private static double sign(double d, double e) {
    if (d > e) {
      return 1;
    }
    if (d < e) {
      return -1;
    }
    return 0;
  }


  private static boolean check_non_membership(INode connection, INode[] temp) {
    for (INode u : temp) {
      if (connection == u) {
        return true;
      }
    }
    return false;
  }

  public static Mapper<INode, PointD> getNodePositions() {
    return positions;
  }
}
