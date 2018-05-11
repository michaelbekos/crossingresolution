package algorithms.graphs;

import com.yworks.yfiles.algorithms.IntersectionAlgorithm;
import com.yworks.yfiles.algorithms.YList;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.IMapper;
import com.yworks.yfiles.graph.INode;
import util.graph2d.Intersection;
import util.graph2d.LineSegment;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class yFilesSweepLine { 
  public static List<Intersection> getCrossings(IGraph graph, boolean edgesOnly, IMapper<INode, PointD> np) {
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
    public List<Intersection> crossings = new LinkedList<>();
    boolean eo;
    public InterHandler(boolean edgesOnly){
      eo = edgesOnly;
    }
    public void checkIntersection(Object o1, Object o2) {
      LineSegment s1 = (LineSegment) o1;
      LineSegment s2 = (LineSegment) o2;

      //check if we really have a intersection
      Optional<Intersection> optionalIntersection = s1.intersects(s2, eo);
      optionalIntersection.ifPresent(i -> crossings.add(i));
    }
  }
}