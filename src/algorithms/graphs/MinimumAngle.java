package algorithms.graphs;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.graph.styles.*;
import com.yworks.yfiles.view.*;

import java.util.*;

import layout.algo.ForceAlgorithmApplier;
import util.*;
import util.graph2d.*;

public class MinimumAngle{
  public static Maybe<Double> getMinimumAngle(IGraph graph, Maybe<IMapper<INode, PointD>> np){
    return getMinimumAngleCrossing(graph, np).bind(i -> Maybe.just(i.c.angle));
  }
  public static Maybe<Tuple3<LineSegment, LineSegment, Intersection>> getMinimumAngleCrossing(IGraph graph, Maybe<IMapper<INode, PointD>> np){
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
  public static List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossingsSorted(IGraph graph, Maybe<IMapper<INode, PointD>> np){
    List<Tuple3<LineSegment, LineSegment, Intersection>> crossings = getCrossings(graph, np);
    Comparator<Tuple3<LineSegment, LineSegment, Intersection>> byAngle = 
      (t1, t2) -> t1.c.angle.compareTo(t2.c.angle);
    Collections.sort(crossings, byAngle);
    return crossings;
  }
  public static List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossings(IGraph graph, Maybe<IMapper<INode, PointD>> np){
    return getCrossings(graph, true, np);
  }
  public static List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossings(IGraph graph, boolean edgesOnly, Maybe<IMapper<INode, PointD>> np){
    IMapper<INode, PointD> defaultMap = ForceAlgorithmApplier.initPositionMap(graph);
    IMapper<INode, PointD> actualMap = np.getDefault(defaultMap);
    return getCrossingsNaiive(graph, edgesOnly, actualMap);
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
