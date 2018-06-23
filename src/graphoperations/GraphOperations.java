package graphoperations;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.algorithms.Graph;
import com.yworks.yfiles.algorithms.INodeCursor;
import com.yworks.yfiles.algorithms.Node;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.layout.YGraphAdapter;
import com.yworks.yfiles.utils.IListEnumerable;
import layout.algo.utils.PositionMap;
import util.graph2d.Intersection;

import java.util.*;

public class GraphOperations {
  private static Mapper<INode, PointD> positions;

  public static double getMinimumAngleForNodes(IGraph g){

    double angle = Double.MAX_VALUE;
    for(int i = 0; i < g.getNodes().size(); i++){
      double alpha = getMinimumAngleForOneNode(g,g.getNodes().getItem(i));
     // System.out.println(angle);
      if(alpha < angle){
        angle = alpha;
        System.out.println("Neu: " +angle );
      }
    }
    return angle;
  }

  public static double getMinimumAngleForOneNode(IGraph g, INode n){
    YGraphAdapter graphAdapter = new YGraphAdapter(g);
    Graph graph = graphAdapter.getYGraph();
    Node node = graphAdapter.getCopiedNode(n);
    double angle = Double.MAX_VALUE;
    PointD pointA = n.getLayout().getCenter();
    INodeCursor neighborCur = node.getNeighborCursor();
    Node[] neighbors = new Node[neighborCur.size()];

    for(int i = 0; i < neighbors.length && neighborCur.ok(); i++){
      neighbors[i] = neighborCur.node();
      neighborCur.cyclicNext();
    }

    for(int i = 0; i < neighbors.length; i++){
      PointD pointC = graphAdapter.getOriginalNode(neighbors[i]).getLayout().getCenter();
      double b = euclidDist(pointA.getX(), pointA.getY(), pointC.getX(), pointC.getY());
      for(int j = i+1; j < neighbors.length; j++){
        PointD pointB = graphAdapter.getOriginalNode(neighbors[j]).getLayout().getCenter();
        double c = euclidDist(pointA.getX(), pointA.getY(), pointB.getX(), pointB.getY());
        double a = euclidDist(pointB.getX(), pointB.getY(), pointC.getX(), pointC.getY());
        double  alpha = lawOfCosines(a, b, c);
        alpha = Math.toDegrees(alpha);
        if(alpha < angle){
          angle = alpha;
          //System.out.println("Candidate: " +angle + " a = " + a + " b = " + b + " c = " + c );
        }
      }



    }

    return angle;
  }

  public static double lawOfCosines(double a, double b, double c){
    return Math.acos((b*b + c*c - a*a) / (2*b*c));
  }

  public static class AspectRatio {
    private double shortestEdgeLength;
    private IEdge shortestEdge;
    private double longestEdgeLength;
    private IEdge longestEdge;

    AspectRatio(double shortestEdgeLength, IEdge shortestEdge, double longestEdgeLength, IEdge longestEdge) {
      this.shortestEdgeLength = shortestEdgeLength;
      this.shortestEdge = shortestEdge;
      this.longestEdgeLength = longestEdgeLength;
      this.longestEdge = longestEdge;
    }

    public void setShortestEdgeLength(double shortestEdgeLength) {
      this.shortestEdgeLength = shortestEdgeLength;
    }

    public void setShortestEdge(IEdge shortestEdge) {
      this.shortestEdge = shortestEdge;
    }

    public void setLongestEdgeLength(double longestEdgeLength) {
      this.longestEdgeLength = longestEdgeLength;
    }

    public void setLongestEdge(IEdge longestEdge) {
      this.longestEdge = longestEdge;
    }

    public double getShortestEdgeLength() {
      return shortestEdgeLength;
    }

    public IEdge getShortestEdge() {
      return shortestEdge;
    }

    public double getLongestEdgeLength() {
      return longestEdgeLength;
    }

    public IEdge getLongestEdge() {
      return longestEdge;
    }

    public double getValue() {
      return longestEdgeLength/shortestEdgeLength;
    }

    public INode[] getCriticalNodes() {
          return new INode[] {shortestEdge.getSourceNode(), shortestEdge.getTargetNode(), longestEdge.getSourceNode(), longestEdge.getTargetNode()};
    }
  }

  public static AspectRatio getAspectRatio(IGraph g){
    if (g.getNodes().size() <= 0) {
      return null;
    }
    IEdge sEdge = getShortestEdge(g);
    INode sSource = sEdge.getSourceNode();
    INode sTarget = sEdge.getTargetNode();
    double sLength = euclidDist(sSource.getLayout().getCenter().getX(), sSource.getLayout().getCenter().getY(), sTarget.getLayout().getCenter().getX(), sTarget.getLayout().getCenter().getY());
    IEdge lEdge = getLongestEdge(g);
    INode lSource = lEdge.getSourceNode();
    INode lTarget = lEdge.getTargetNode();
    double lLength = euclidDist(lSource.getLayout().getCenter().getX(), lSource.getLayout().getCenter().getY(), lTarget.getLayout().getCenter().getX(), lTarget.getLayout().getCenter().getY());
    return new AspectRatio(sLength, sEdge, lLength, lEdge);
  }

  //returns false if moving the node to the position results in a worse aspect ratio
  public static boolean improvedAspectRatio(PointD position, INode node, IGraph graph, AspectRatio aspectRatio, double maxAspectRatio) {
      for (IPort p : node.getPorts()) {
          for (IEdge e : graph.edgesAt(p)) {
              INode endPoint;
              if (e.getSourceNode() == node) {
                  endPoint = e.getTargetNode();
              } else {
                  endPoint = e.getSourceNode();
              }
              double length = euclidDist(position.getX(), position.getY(), endPoint.getLayout().getCenter().getX(), endPoint.getLayout().getCenter().getY());
              if (length > aspectRatio.getLongestEdgeLength() && length/aspectRatio.getShortestEdgeLength() > maxAspectRatio) {
                  return false;
              } else if (length < aspectRatio.getShortestEdgeLength() && aspectRatio.getLongestEdgeLength()/length > maxAspectRatio) {
                  return false;
              }
          }
      }
      return true;
  }


  public static IEdge getShortestEdge(IGraph g){
    IListEnumerable<IEdge> edgeList = g.getEdges();

    if(edgeList.size() >= 1) {
      IEdge edge = edgeList.getItem(0);
      INode source = edge.getSourceNode();
      INode target = edge.getTargetNode();
      double length  = euclidDist(source.getLayout().getCenter().getX(), source.getLayout().getCenter().getY(), target.getLayout().getCenter().getX(), target.getLayout().getCenter().getY());
      double shortLength = length;
      IEdge shortEdge = edge;

      for (int i = 1; i < edgeList.size(); i++) {
        edge = edgeList.getItem(i);
        source = edge.getSourceNode();
        target = edge.getTargetNode();
        length = euclidDist(source.getLayout().getCenter().getX(), source.getLayout().getCenter().getY(), target.getLayout().getCenter().getX(), target.getLayout().getCenter().getY());
        if(length < shortLength){
          shortLength = length;
          shortEdge = edge;
        }
      }
      return shortEdge;
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
      double length  = euclidDist(source.getLayout().getCenter().getX(), source.getLayout().getCenter().getY(), target.getLayout().getCenter().getX(), target.getLayout().getCenter().getY());
      double smallLength = length;
      IEdge smallEdge = edge;

      for (int i = 1; i < edgeList.size(); i++) {
        edge = edgeList.getItem(i);
        source = edge.getSourceNode();
        target = edge.getTargetNode();
        length = euclidDist(source.getLayout().getCenter().getX(), source.getLayout().getCenter().getY(), target.getLayout().getCenter().getX(), target.getLayout().getCenter().getY());
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
    return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
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
