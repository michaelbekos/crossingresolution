package algorithms.graphs;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.*;

import java.util.*;

import util.*;
import util.graph2d.*;

public class MinimumAngle{
  public static Maybe<Double> getMinimumAngle(IGraph graph){
    return getMinimumAngleCrossing(graph).bind(i -> Maybe.just(i.c.angle));
  }
  public static Maybe<Tuple3<LineSegment, LineSegment, Intersection>> getMinimumAngleCrossing(IGraph graph){
    List<Tuple3<LineSegment, LineSegment, Intersection>> crossings = getCrossings(graph);
    Comparator<Tuple3<LineSegment, LineSegment, Intersection>> byAngle = 
      (t1, t2) -> t1.c.angle.compareTo(t2.c.angle);
    Collections.sort(crossings, byAngle);
    if(crossings.size() > 0){
      return Maybe.just(crossings.get(0));
    }
    else{
      return Maybe.nothing();
    } 
  }

  public static List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossings(IGraph graph){
    return getCrossings(graph, true);
  }
  public static List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossings(IGraph graph, boolean edgesOnly){
    return getCrossingsNaiive(graph, edgesOnly);
  }

  public static List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossingsNaiive(IGraph graph, boolean edgesOnly){
    List<Tuple3<LineSegment, LineSegment, Intersection>> res = new LinkedList<>();
    for (IEdge e1 : graph.getEdges()){
      LineSegment l1 = new LineSegment(e1);
      for (IEdge e2 : graph.getEdges()){
        // same edge
        if(e1.equals(e2)) continue;
        LineSegment l2 = new LineSegment(e2);
        Maybe<Intersection> i = l1.intersects(l2, edgesOnly);
        if(i.hasValue()){
          Intersection i1 = i.get();
          res.add(new Tuple3<>(l1, l2, i1));
        }
      }
    }
    return res;
  }
}