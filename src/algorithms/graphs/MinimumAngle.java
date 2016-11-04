package algorithms.graphs;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.*;

import java.util.*;

import util.*;

public class MinimumAngle{
  public static Maybe<Double> getMinimumAngle(IGraph graph){
    List<Tuple4<IEdge, IEdge, PointD, Double>> crossings = getCrossings(graph);
    Comparator<Tuple4<IEdge, IEdge, PointD, Double>> byAngle = 
      (Tuple4<IEdge, IEdge, PointD, Double> t1, Tuple4<IEdge, IEdge, PointD, Double> t2) ->t1.d.compareTo(t2.d);
    Collections.sort(crossings, byAngle);
    if(crossings.size() > 0){
      return new Just<Double>(crossings.get(0).d);
    }
    else{
      return new Nothing<Double>();
    }
  }

  public static List<Tuple4<IEdge, IEdge, PointD, Double>> getCrossings(IGraph graph){
    return getCrossingsNaiive(graph);
  }

  public static List<Tuple4<IEdge, IEdge, PointD, Double>> getCrossingsNaiive(IGraph graph){
    List<Tuple4<IEdge, IEdge, PointD, Double>> res = new LinkedList<>();
    for (IEdge e1 : graph.getEdges()){
      INode n1 = e1.getSourceNode();
      INode n2 = e1.getTargetNode();
      for (IEdge e2 : graph.getEdges()){
        // same edge
        if(e1 == e2) continue;
        INode n3 = e2.getSourceNode();
        INode n4 = e2.getTargetNode();
        // same nodes ==> no crossing
        if(n1 == n3 || n1 == n4 || 
           n2 == n3 || n2 == n4) continue;
        PointD p1, p2, p3, p4;
        p1 = n1.getLayout().getCenter();
        p2 = n2.getLayout().getCenter();
        p3 = n3.getLayout().getCenter();
        p4 = n4.getLayout().getCenter();
        // http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
        PointD r = PointD.subtract(p2, p1);
        PointD s = PointD.subtract(p4, p3);
        double rTimesS = crossProduct(r, s);
        // lines parallel
        if(rTimesS == 0) continue;
        double t, u;
        t = crossProduct(PointD.subtract(p3, p1), s) / rTimesS;
        u = crossProduct(PointD.subtract(p3, p1), r) / rTimesS;
        // intersection not on line segments
        if(t < 0 || u < 0 || t > 1 || u > 1) continue;
        PointD crossingPoint = PointD.add(p1, PointD.times(t, r));
        Double crossingsAngle = Math.toDegrees(Math.acos(PointD.scalarProduct(r, s) / (r.getVectorLength() * s.getVectorLength())));
        if(crossingsAngle > 90) crossingsAngle = 180 - crossingsAngle;
        res.add(new Tuple4<>(e1, e2, crossingPoint, crossingsAngle));
      }
    }
    return res;
  }

  public static double crossProduct(PointD p1, PointD p2){
    return p1.getX() * p2.getY() - p1.getY() * p2.getX();
  }

  public static List<Tuple4<IEdge, IEdge, PointD, Double>> getCrossingsScanline(IGraph graph){
    return getCrossingsNaiive(graph);
  }

}