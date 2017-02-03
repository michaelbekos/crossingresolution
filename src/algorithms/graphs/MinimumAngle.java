package algorithms.graphs;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.graph.styles.*;
import com.yworks.yfiles.view.*;

import java.util.*;
import java.util.stream.*;

import layout.algo.ForceAlgorithmApplier;
import util.*;
import util.graph2d.*;

// to allow overriding for caching, we have an explicit singleton object, which implements all methods. Static calls just use the singleton.
// This is because you can't derive static methods to member-methods.
// since you can't have methods and functions with the same name, we need a helper class...

public class MinimumAngle {
  private static MinimumAngleHelper m = new MinimumAngleHelper();
  public static class MinimumAngleHelper{
    // singleton object mustn't be instantiated by others
    protected MinimumAngleHelper(){ }
    
    // return the worst angle
    public Maybe<Double> getMinimumAngle(IGraph graph, Maybe<IMapper<INode, PointD>> np){
      // Maybe (LS, LS, I) --fmap--> Maybe Double
      return this.getMinimumAngleCrossing(graph, np).fmap(i -> i.c.angle);
    }

    // return the worst crossing, if any
    public Maybe<Tuple3<LineSegment, LineSegment, Intersection>> getMinimumAngleCrossing(IGraph graph, Maybe<IMapper<INode, PointD>> np){
      List<Tuple3<LineSegment, LineSegment, Intersection>> crossings = getCrossingsSorted(graph, np);
      if(crossings.size() > 0){
        Tuple3<LineSegment, LineSegment, Intersection> crossing = crossings.get(0);
        highlightCrossing(crossing);
        return Maybe.just(crossing);
      }
      else{
        return Maybe.nothing();
      } 
    }

    // return a list of all crossings
    public List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossingsSorted(IGraph graph, Maybe<IMapper<INode, PointD>> np){
      List<Tuple3<LineSegment, LineSegment, Intersection>> crossings = getCrossings(graph, np);
      return sortCrossings(crossings);
    }
    /**
     * getCrossings: takes a graph and optional custom node positions. Also, you can specify that you want crossings on edge ends.
     */
    public List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossings(IGraph graph, Maybe<IMapper<INode, PointD>> np){
      return getCrossings(graph, true, np);
    }
    public List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossings(IGraph graph, boolean edgesOnly, Maybe<IMapper<INode, PointD>> np){
      // lazy values! :D
      IMapper<INode, PointD> actualMap = np.getDefault(() -> ForceAlgorithmApplier.initPositionMap(graph));
      return yFilesSweepLine.getCrossings(graph, edgesOnly, actualMap);
    }
  } 
  /* * * * * * * * * * * * * * * * * * * * * * * * * * * 
   * HELPER CLASS ENDS HERE (I always get confused)  *
   * * * * * * * * * * * * * * * * * * * * * * * * * * */

  // and now for some wrappers (or rappers?)
  public static Maybe<Double> getMinimumAngle(IGraph graph, Maybe<IMapper<INode, PointD>> np){
    return m.getMinimumAngle(graph, np);
  }
  public static Maybe<Tuple3<LineSegment, LineSegment, Intersection>> getMinimumAngleCrossing(IGraph graph, Maybe<IMapper<INode, PointD>> np){
    return m.getMinimumAngleCrossing(graph, np);
  }    
  
  public static List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossingsSorted(IGraph graph, Maybe<IMapper<INode, PointD>> np){
    return m.getCrossingsSorted(graph, np);
  }
  

  public static List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossings(IGraph graph, Maybe<IMapper<INode, PointD>> np){
    return m.getCrossings(graph, np);
  }
  
  public static List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossings(IGraph graph, boolean edgesOnly, Maybe<IMapper<INode, PointD>> np){
    return m.getCrossings(graph, edgesOnly, np);
  }
  
  // variant: use parallel map with maybe return values.
  public static List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossingsParallel(IGraph graph, boolean edgesOnly, IMapper<INode, PointD> nodePositions){
    return Util.distinctPairs(graph.getEdges()).parallel().map(e1e2 -> {
        IEdge e1 = e1e2.a,
              e2 = e1e2.b;
        LineSegment l1 = new LineSegment(e1, nodePositions),
                    l2 = new LineSegment(e2, nodePositions);
        Maybe<Intersection> i = l1.intersects(l2, edgesOnly);
        return i.bind(i1 -> Maybe.just(new Tuple3<LineSegment, LineSegment, Intersection>(l1, l2, i1)));
      }).filter(m -> m.hasValue())
      .map(m -> m.get()).collect(Collectors.toList());
  }

  // variant: use parallel flatmap, also maybes.
  public static List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossingsParallelFlat(IGraph graph, boolean edgesOnly, IMapper<INode, PointD> nodePositions){
    return Util.distinctPairs(graph.getEdges()).parallel().flatMap(e1e2 -> {
        IEdge e1 = e1e2.a,
              e2 = e1e2.b;
        LineSegment l1 = new LineSegment(e1, nodePositions),
                    l2 = new LineSegment(e2, nodePositions);
        Maybe<Intersection> i = l1.intersects(l2, edgesOnly);
        return i.bind(i1 -> Maybe.just(new Tuple3<LineSegment, LineSegment, Intersection>(l1, l2, i1))).stream();
      }).collect(Collectors.toList());
  }

  // variant: parallel, with synchronized list append
  public static List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossingsParallelSynch(IGraph graph, boolean edgesOnly, IMapper<INode, PointD> nodePositions){
    List<Tuple3<LineSegment, LineSegment, Intersection>> res = new LinkedList<>();
    Set<IEdge> seenEdges = new HashSet<>();
    Util.distinctPairs(graph.getEdges()).parallel().forEach(e1e2 -> {
        IEdge e1 = e1e2.a,
              e2 = e1e2.b;
        LineSegment l1 = new LineSegment(e1, nodePositions),
                    l2 = new LineSegment(e2, nodePositions);
        Maybe<Intersection> i = l1.intersects(l2, edgesOnly);
        i.andThen(i1 -> {
          synchronized(res){
            res.add(new Tuple3<LineSegment, LineSegment, Intersection>(l1, l2, i1));
          }
        });
      });
      return res;
  }

  // variant: serial 
  public static List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossingsNaiive(IGraph graph, boolean edgesOnly, IMapper<INode, PointD> nodePositions){
    List<Tuple3<LineSegment, LineSegment, Intersection>> res = new LinkedList<>();
    Set<IEdge> seenEdges = new HashSet<>();
    for (IEdge e1 : graph.getEdges()){
      LineSegment l1 = new LineSegment(e1, nodePositions);
      seenEdges.add(e1);
      for (IEdge e2 : graph.getEdges()){
        // same edge
        if(seenEdges.contains(e2)) continue;
        LineSegment l2 = new LineSegment(e2, nodePositions);
        Maybe<Intersection> i = l1.intersects(l2, edgesOnly);
        if(i.hasValue()){
          Intersection i1 = i.get();
          res.add(new Tuple3<>(l1, l2, i1));
        }
      }
    }
    return res;
  }

  /**
   * Sort the crossings ascending with comparator
   * @param crossings - Crossings list to sort
   * @return Sorted List of Crossings
   */
  public static List<Tuple3<LineSegment, LineSegment, Intersection>> sortCrossings(List<Tuple3<LineSegment, LineSegment, Intersection>> crossings){
    Comparator<Tuple3<LineSegment, LineSegment, Intersection>> byAngle = 
      (t1, t2) -> t1.c.angle.compareTo(t2.c.angle);
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
  public static void highlightCrossing(Tuple3<LineSegment, LineSegment, Intersection> crossing) {
    //System.out.println(crossing.a.e);
    crossing.a.e.andThen(e -> paintEdge(e, Pen.getRed()));
    crossing.b.e.andThen(e -> paintEdge(e, Pen.getRed()));
  }

  /**
   * Checks whether a particular edge e1 is crossing any other edges in Graph graph
   * @param edgesOnly
   * @return List of Crossings with e1
   */
  public static List<Tuple3<LineSegment, LineSegment, Intersection>> intersectsWith(IEdge e1, IGraph graph, IMapper<INode, PointD> nodePositions, boolean edgesOnly){
    List<Tuple3<LineSegment, LineSegment, Intersection>> res = new LinkedList<>();
    Set<IEdge> seenEdges = new HashSet<>();
    seenEdges.add(e1);
    LineSegment l1 = new LineSegment(e1, nodePositions);
    // for every other edge check if the line segment von e1 is crossing line segment of e2
    for(IEdge e2: graph.getEdges()){
      // do not consider e1 twice
      if(seenEdges.contains(e2)) continue;
      LineSegment l2 = new LineSegment(e2, nodePositions);
      Maybe<Intersection> i = l1.intersects(l2, edgesOnly);
      if(i.hasValue()){
        Intersection i1 = i.get();
        res.add(new Tuple3<>(l1,l2,i1));
      }
    }
    return res;
  }
}
