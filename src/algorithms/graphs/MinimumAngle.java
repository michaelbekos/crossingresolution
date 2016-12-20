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
    protected MinimumAngleHelper(){ }
    
    public Maybe<Double> getMinimumAngle(IGraph graph, Maybe<IMapper<INode, PointD>> np){
      return this.getMinimumAngleCrossing(graph, np).fmap(i -> i.c.angle);
    }

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

    public List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossingsSorted(IGraph graph, Maybe<IMapper<INode, PointD>> np){
      List<Tuple3<LineSegment, LineSegment, Intersection>> crossings = getCrossings(graph, np);
      return sortCrossings(crossings);
    }

    public List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossings(IGraph graph, Maybe<IMapper<INode, PointD>> np){
      return getCrossings(graph, true, np);
    }
    public List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossings(IGraph graph, boolean edgesOnly, Maybe<IMapper<INode, PointD>> np){
      IMapper<INode, PointD> actualMap = np.getDefault(() -> ForceAlgorithmApplier.initPositionMap(graph));
      return MinimumAngle.getCrossingsParallelFlat(graph, edgesOnly, actualMap);
    }
  } 
  /* * * * * * * * * * * * * * * * * * * * * * * * * * * 
   * HELPER CLASS ENDS HERE (I always get confused)  *
   * * * * * * * * * * * * * * * * * * * * * * * * * * */

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

  public static List<Tuple3<LineSegment, LineSegment, Intersection>> sortCrossings(List<Tuple3<LineSegment, LineSegment, Intersection>> crossings){
    Comparator<Tuple3<LineSegment, LineSegment, Intersection>> byAngle = 
      (t1, t2) -> t1.c.angle.compareTo(t2.c.angle);
    Collections.sort(crossings, byAngle);
    return crossings;
  }

  public static void resetHighlighting(IGraph graph){
    for(IEdge e: graph.getEdges()){
      paintEdge(e, Pen.getBlack());
    }
  }

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
    crossing.a.e.andThen(e -> paintEdge(e, Pen.getRed()));
    crossing.b.e.andThen(e -> paintEdge(e, Pen.getRed()));
  }
}
