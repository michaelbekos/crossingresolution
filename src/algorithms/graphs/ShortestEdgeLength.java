package algorithms.graphs;

import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import util.Maybe;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.yworks.yfiles.geometry.PointD;
import util.Tuple2;
import util.graph2d.LineSegment;

/**
 * Created by Jessica Wolz on 24.11.16.
 */
public class ShortestEdgeLength {

    public static Maybe<Double> getShortestEdgeLength(IGraph graph){
        return getShortestEdge(graph).bind(i -> Maybe.just(i.b));
    }

    public static Maybe<Tuple2<LineSegment, Double>> getShortestEdge(IGraph graph){
        List<Tuple2<LineSegment, Double>> edges = getEdges(graph);
        Comparator<Tuple2<LineSegment, Double>> byLength = (l1, l2) -> l1.b.compareTo(l2.b);
        Collections.sort(edges, byLength);
        if(edges.size() > 0){
            return Maybe.just(edges.get(0));
        } else {
            return Maybe.nothing();
        }

    }

    private static Double getDist(IEdge edge){
        return edge.getSourcePort().getLocation()
                .distanceTo( new PointD(edge.getTargetNode().getLayout().getCenter().getX(),
                        edge.getTargetNode().getLayout().getCenter().getY()));
    }

    public static List<Tuple2<LineSegment, Double>> getEdges(IGraph graph){
        List<Tuple2<LineSegment, Double>> result = new LinkedList<>();
        for(IEdge e1 : graph.getEdges()){
            LineSegment l1 = new LineSegment(e1);
            result.add(new Tuple2<>(l1, getDist(e1)));
        }
        return result;
    }
}
