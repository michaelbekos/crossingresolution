package algorithms.graphs;

import com.yworks.yfiles.algorithms.IntersectionAlgorithm;
import com.yworks.yfiles.algorithms.YList;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.geometry.PointD;

import util.*;
import util.graph2d.*;

import java.util.*;

public class yFilesSweepLine { 
  public static List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossings(IGraph graph, boolean edgesOnly, IMapper<INode, PointD> np) {
    YList elements = new YList(); //stores typed plane objects for sweep-line
    for (IEdge e: graph.getEdges()) {
      LineSegment segment = new LineSegment(e, np);
      elements.add(segment);
    }

    InterHandler handler = new InterHandler(edgesOnly);
    IntersectionAlgorithm.intersect(elements, handler);
    return handler.crossings;
  }

  static class InterHandler implements IntersectionAlgorithm.IIntersectionHandler {
    public List<Tuple3<LineSegment, LineSegment, Intersection>> crossings = new LinkedList<>();
    boolean eo;
    public InterHandler(boolean edgesOnly){
      eo = edgesOnly;
    }
    public void checkIntersection(Object o1, Object o2) {
      LineSegment s1 = (LineSegment) o1;
      LineSegment s2 = (LineSegment) o2;

      //check if we really have a intersection
      Optional<Intersection> mi = s1.intersects(s2, eo);
      mi.ifPresent(i -> crossings.add(new Tuple3<>(s1, s2, i)));
    }
  }
}