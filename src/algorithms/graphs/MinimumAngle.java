package algorithms.graphs;

import com.sun.istack.internal.Nullable;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.graph.styles.IEdgeStyle;
import com.yworks.yfiles.graph.styles.PolylineEdgeStyle;
import com.yworks.yfiles.view.Pen;
import layout.algo.utils.PositionMap;
import util.Util;
import util.graph2d.Intersection;
import util.graph2d.LineSegment;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// to allow overriding for caching, we have an explicit singleton object, which implements all methods. Static calls just use the singleton.
// This is because you can't derive static methods to member-methods.
// since you can't have methods and functions with the same name, we need a helper class...

public class MinimumAngle {
  private static MinimumAngleHelper m = new MinimumAngleHelper();
  public static class MinimumAngleHelper{
    // singleton object mustn't be instantiated by others
    protected MinimumAngleHelper(){ }
    
    // return the worst angle
    public Optional<Double> getMinimumAngle(IGraph graph, @Nullable IMapper<INode, PointD> np){
      return this.getMinimumAngleCrossing(graph, np).map(i -> i.angle);
    }

    Optional<Double> getMinimumAngleForNode(IGraph graph, INode node, @Nullable Mapper<INode, PointD> nodePositions) {
      return this.getMinimumAngleCrossingForNode(graph, node, nodePositions).map(i -> i.angle);
    }

    private Optional<Intersection> getMinimumAngleCrossingForNode(IGraph graph, INode node, @Nullable Mapper<INode, PointD> nodePositions) {
      if (nodePositions == null) {
        nodePositions = PositionMap.FromIGraph(graph);
      }
      List<Intersection> crossings = getCrossingsForNode(graph, node, nodePositions);

      return crossings.stream().min(Comparator.comparingDouble(crossing -> crossing.angle));
    }

    /**
     * This implementation does not use a sweep line but simply checks all combinations of neighbor edges of <code>node</code>
     * and all the edges of the whole graph.
     */
    private List<Intersection> getCrossingsForNode(IGraph graph, INode node, IMapper<INode, PointD> nodePositions) {
      final List<LineSegment> neighborEdges = graph.getEdges().stream()
          .filter(edge -> edge.getSourceNode() == node || edge.getTargetNode() == node)
          .map(e -> new LineSegment(e, nodePositions))
          .collect(Collectors.toList());

      return graph.getEdges().stream()
          .map(e -> new LineSegment(e, nodePositions))
          .flatMap(s1 -> neighborEdges.stream()
              .map(s2 -> s1.intersects(s2, true)))
              .filter(Optional::isPresent)
              .map(Optional::get)
          .collect(Collectors.toList());
    }

    // return the worst crossing, if any
    public Optional<Intersection> getMinimumAngleCrossing(IGraph graph, @Nullable IMapper<INode, PointD> np){
      List<Intersection> crossings = getCrossingsSorted(graph, np);
      if(crossings.size() > 0){
        Intersection crossing = crossings.get(0);
        //highlightCrossing(crossing);
        return Optional.of(crossing);
      }
      else{
        return Optional.empty();
      } 
    }

    public List<Intersection> getCrossingsSorted(IGraph graph, @Nullable IMapper<INode, PointD> np){
      List<Intersection> crossings = getCrossings(graph, np);
      return sortCrossings(crossings);
    }

    public List<Intersection> getCrossings(IGraph graph, @Nullable IMapper<INode, PointD> np){
      return getCrossings(graph, true, np);
    }

    public List<Intersection> getCrossings(IGraph graph, boolean edgesOnly, @Nullable IMapper<INode, PointD> np){
      if (np == null) {
        np = PositionMap.FromIGraph(graph);
      }
      return yFilesSweepLine.getCrossings(graph, edgesOnly, np);
    }
  } 
  /* * * * * * * * * * * * * * * * * * * * * * * * * * * 
   * HELPER CLASS ENDS HERE (I always get confused)  *
   * * * * * * * * * * * * * * * * * * * * * * * * * * */

  // and now for some wrappers (or rappers?)
  public static Optional<Double> getMinimumAngle(IGraph graph, @Nullable IMapper<INode, PointD> np){
    return m.getMinimumAngle(graph, np);
  }

  public static double getMinimumAngleForNode(Mapper<INode, PointD> positions, INode node, IGraph graph) {
    return m.getMinimumAngleForNode(graph, node, positions).orElse(Double.POSITIVE_INFINITY);
  }

  public static Optional<Double> getMinimumAngleForNode(IGraph graph, INode node, @Nullable Mapper<INode, PointD> nodePositions) {
    return m.getMinimumAngleForNode(graph, node, nodePositions);
  }

  public static Optional<Intersection> getMinimumAngleCrossing(IGraph graph) {
    return m.getMinimumAngleCrossing(graph, null);
  }

  public static Optional<Intersection> getMinimumAngleCrossing(IGraph graph, @Nullable IMapper<INode, PointD> np){
    return m.getMinimumAngleCrossing(graph, np);
  }

  public static List<Intersection> getCrossingsSorted(IGraph graph, @Nullable IMapper<INode, PointD> np){
    return m.getCrossingsSorted(graph, np);
  }


  public static List<Intersection> getCrossings(IGraph graph, @Nullable IMapper<INode, PointD> np){
    return m.getCrossings(graph, np);
  }

  // variant: use parallel map with maybe return values.
  public static List<Intersection> getCrossingsParallel(IGraph graph, boolean edgesOnly, IMapper<INode, PointD> nodePositions){
    return Util.distinctPairs(graph.getEdges())
        .parallel()
        .map(e1e2 -> {
          IEdge e1 = e1e2.a;
          IEdge e2 = e1e2.b;
          LineSegment l1 = new LineSegment(e1, nodePositions);
          LineSegment l2 = new LineSegment(e2, nodePositions);
          return l1.intersects(l2, edgesOnly);
        }).filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  // variant: use parallel flatmap, also maybes.
  public static List<Intersection> getCrossingsParallelFlat(IGraph graph, boolean edgesOnly, IMapper<INode, PointD> nodePositions){
    return Util.distinctPairs(graph.getEdges())
        .parallel()
        .flatMap(e1e2 -> {
        IEdge e1 = e1e2.a;
          IEdge e2 = e1e2.b;
          LineSegment l1 = new LineSegment(e1, nodePositions);
          LineSegment l2 = new LineSegment(e2, nodePositions);
          Optional<Intersection> i = l1.intersects(l2, edgesOnly);
          return i.map(Stream::of).orElseGet(Stream::empty);
      }).collect(Collectors.toList());
  }

  // variant: parallel, with synchronized list append
  public static List<Intersection> getCrossingsParallelSynch(IGraph graph, boolean edgesOnly, IMapper<INode, PointD> nodePositions){
    List<Intersection> res = new LinkedList<>();
    Set<IEdge> seenEdges = new HashSet<>();
    Util.distinctPairs(graph.getEdges()).parallel().forEach(e1e2 -> {
        IEdge e1 = e1e2.a,
              e2 = e1e2.b;
        LineSegment l1 = new LineSegment(e1, nodePositions),
                    l2 = new LineSegment(e2, nodePositions);
        Optional<Intersection> i = l1.intersects(l2, edgesOnly);
        i.ifPresent(i1 -> {
          synchronized(res){
            res.add(i1);
          }
        });
      });
      return res;
  }

  // variant: serial 
  public static List<Intersection> getCrossingsNaiive(IGraph graph, boolean edgesOnly, IMapper<INode, PointD> nodePositions){
    List<Intersection> res = new LinkedList<>();
    Set<IEdge> seenEdges = new HashSet<>();
    for (IEdge e1 : graph.getEdges()){
      LineSegment l1 = new LineSegment(e1, nodePositions);
      seenEdges.add(e1);
      for (IEdge e2 : graph.getEdges()){
        // same edge
        if(seenEdges.contains(e2)) continue;
        LineSegment l2 = new LineSegment(e2, nodePositions);
        Optional<Intersection> i = l1.intersects(l2, edgesOnly);
        i.ifPresent(res::add);
      }
    }
    return res;
  }

  /**
   * Sort the crossings ascending with comparator
   * @param crossings - Crossings list to sort
   * @return Sorted List of Crossings
   */
  public static List<Intersection> sortCrossings(List<Intersection> crossings){
    Comparator<Intersection> byAngle = Comparator.comparingDouble(intersection -> intersection.angle);
    Collections.sort(crossings, byAngle);
    return crossings;
  }

  /**
   * Removes highlight of all edges in input graph
   * @param graph - input graph
   */
  public static void resetHighlighting(IGraph graph){
    for(IEdge e: graph.getEdges()){
      paintEdge(e, Pen.getBlack());
    }
  }

  /**
   * Paints edge e certain
   * @param e - edge to color
   * @param p - use p for coloring
   */
  public static void paintEdge(IEdge e, Pen p){
    IEdgeStyle s = e.getStyle();
    if(s instanceof PolylineEdgeStyle) {
      ((PolylineEdgeStyle) s).setPen(p);
    } else {
      System.out.println(s.getClass());
    }
  }

  /**
   * Displays vectors for debugging purposes
   */
  public static void highlightCrossing(Intersection crossing) {
      paintEdge(crossing.segment1.e, Pen.getRed());
      paintEdge(crossing.segment2.e, Pen.getRed());
  }

  /**
   * Checks whether a particular edge e1 is crossing any other edges in Graph graph
   * @param edgesOnly
   * @return List of Crossings with e1
   */
  public static List<Intersection> intersectsWith(IEdge e1, IGraph graph, IMapper<INode, PointD> nodePositions, boolean edgesOnly){
    List<Intersection> res = new LinkedList<>();
    Set<IEdge> seenEdges = new HashSet<>();
    seenEdges.add(e1);
    LineSegment l1 = new LineSegment(e1, nodePositions);
    // for every other edge check if the line segment von e1 is crossing line segment of e2
    for(IEdge e2: graph.getEdges()){
      // do not consider e1 twice
      if(seenEdges.contains(e2)) continue;
      LineSegment l2 = new LineSegment(e2, nodePositions);
      Optional<Intersection> i = l1.intersects(l2, edgesOnly);
      i.ifPresent(res::add);
    }
    return res;
  }

}
