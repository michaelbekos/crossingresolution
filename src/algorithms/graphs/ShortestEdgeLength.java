package algorithms.graphs;

import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import util.Tuple2;
import util.graph2d.LineSegment;

import java.util.*;

/**
 * Created by Jessica Wolz on 24.11.16.
 */
public class ShortestEdgeLength {

    /**
     * Compute the shortest edge length in an IGraph graph
     * @param graph - input graph
     * @return Shortest edge length, if there is an edge
     */
    public static Optional<Double> getShortestEdgeLength(IGraph graph){
        return getShortestEdge(graph).map(i -> i.b);
    }

    /**
     * Computes shortest edge in IGraph graph
     * @param graph - input graph
     * @return Tuple of Linesegment and edge length
     */
    public static Optional<Tuple2<LineSegment, Double>> getShortestEdge(IGraph graph){
        List<Tuple2<LineSegment, Double>> edges = getEdges(graph);
        Comparator<Tuple2<LineSegment, Double>> byLength = (l1, l2) -> l1.b.compareTo(l2.b);
        Collections.sort(edges, byLength);
        if(edges.size() > 0){
            return Optional.of(edges.get(0));
        } else {
            return Optional.empty();
        }

    }

    /**
     * Compute length of each edge
     * @param graph - input graph
     * @return List of edges with their length
     */
    public static List<Tuple2<LineSegment, Double>> getEdges(IGraph graph){
        List<Tuple2<LineSegment, Double>> result = new LinkedList<>();
        for(IEdge e1 : graph.getEdges()){
            LineSegment l1 = new LineSegment(e1);
            result.add(new Tuple2<>(l1, l1.ve.getVectorLength()));
        }
        return result;
    }
}
