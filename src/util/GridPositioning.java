package util;

import com.yworks.yfiles.geometry.*;
import com.yworks.yfiles.graph.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yworks.yfiles.layout.CopiedLayoutGraph;
import com.yworks.yfiles.layout.LayoutGraphAdapter;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.layout.organic.OrganicRemoveOverlapsStage;
import com.yworks.yfiles.layout.organic.RemoveOverlapsStage;
import layout.algo.ForceAlgorithmApplier;
import util.*;
import algorithms.graphs.MinimumAngle;
import util.graph2d.Intersection;
import util.graph2d.LineSegment;

import javax.sound.sampled.Line;

import static layout.algo.ForceAlgorithmApplier.newNodePointMap;

/**
 * Created by Jessica Wolz on 01.12.16.
 */

public class GridPositioning {

    private static Comparator<Tuple2<PointD, Double>> byAngle = (p1, p2) -> p1.b.compareTo(p2.b);
    private static Comparator<Tuple3<PointD, PointD, Double>> byAngles = (p1, p2) -> p1.c.compareTo(p2.c);
    private static Comparator<Tuple3<LineSegment, LineSegment, Double>> byCrossingAngle = (p1, p2) -> p1.c.compareTo(p2.c);


    /**
     * Gridding respective to smalles angle crossing.
     * @param g
     */
    public static void gridGraph(IGraph g){
        Mapper<INode, PointD> nodePositions = ForceAlgorithmApplier.initPositionMap(g);

        while (GridPositioning.isGridded(g) == false) {
            ForceAlgorithmApplier.applyNodePositionsToGraph(g, GridPositioning.getGridNodesRespectively(g, nodePositions));
            GridPositioning.removeOverlaps(g, 0.0001);

        }
        
    }

    /**
     * Fast gridding. Considers single nodes.
     * @param g
     */
    public static void simpleGridGraph(IGraph g){
        Mapper<INode, PointD> nodePositions = ForceAlgorithmApplier.initPositionMap(g);

        while (GridPositioning.isGridded(g) == false) {
            ForceAlgorithmApplier.applyNodePositionsToGraph(g, GridPositioning.getGridNodes(g, nodePositions));
            GridPositioning.removeOverlaps(g, 0.1);
        }

    }

    /**
     * Computes integer grid points respectively by crossing angle if such exists
     * Otherwise respectively to the minimum angle of the graph
     * @return nodePositions - integer grid node positions
     */
    private static Mapper<INode, PointD> getGridNodesRespectively(IGraph graph, Mapper<INode, PointD> nodePos){
        // take minimum crossing
        // try not to break it
        Mapper<INode, PointD> temp = nodePos;
        Maybe<Tuple3<LineSegment, LineSegment, Intersection>> minCrossing = MinimumAngle.getMinimumAngleCrossing(graph, Maybe.just(nodePos));
        List<Tuple3<LineSegment, LineSegment, Intersection>> crossings = MinimumAngle.getCrossings(graph, Maybe.just(nodePos));
        int crossingSize = crossings.size();
        List<Tuple2<INode, PointD>> griddedCrossingNodes;
        Set<INode> crossingNodes = new HashSet<>();
        if(minCrossing.hasValue()) {
            crossingNodes.add(minCrossing.get().a.n1.get());
            crossingNodes.add(minCrossing.get().a.n2.get());
            crossingNodes.add(minCrossing.get().b.n1.get());
            crossingNodes.add(minCrossing.get().b.n2.get());



            // compute new positions of each node from the minimum crossing
            List<LineSegment> lines = getGridSegments(minCrossing.get().a, nodePos);
            List<LineSegment> otherLines = getGridSegments(minCrossing.get().b, nodePos);
            // compute new crossing angles, choose best
            griddedCrossingNodes = getResultingAngle(lines, otherLines);
            if (!griddedCrossingNodes.isEmpty()) {
                for (Tuple2<INode, PointD> tup : griddedCrossingNodes) {
                    nodePos.setValue(tup.a, tup.b);
                }
            }

            // consider rest of nodes not with crossing
            nodePos = getGridNodes(graph, nodePos, crossingNodes);

        } else {
            nodePos = getGridNodes(graph, nodePos, crossingNodes);
        }





        return nodePos;
    }

    /**
     * Computes resulting angles and chooses the maximal angle
     * Returns then a Tuple with the containing coordinates of this crossing angle
     * @param l1 - List of new LineSegments
     * @param l2 - List of new LineSegments
     * @return Tuple with the containing coordinates of this crossing angle
     */
    private static List<Tuple2<INode, PointD>> getResultingAngle(List<LineSegment> l1, List<LineSegment> l2){
        Intersection inter;
        List<Tuple3<LineSegment, LineSegment, Double>> coordCrossing = new ArrayList<>();
        List<Tuple2<INode, PointD>> res = new ArrayList<>();
        for(LineSegment l: l1){
            for(LineSegment j: l2){
                if(l.intersects(j,true).hasValue()){
                    inter = l.intersects(j,true).get();
                    coordCrossing.add(new Tuple3<>(l,j, inter.angle));
                }
                if(j.intersects(l,true).hasValue()){
                    inter = j.intersects(l,true).get();
                    coordCrossing.add(new Tuple3<>(j,l, inter.angle));
                }
            }
        }

        Collections.sort(coordCrossing, byCrossingAngle);
        if (!coordCrossing.isEmpty()) {
            res.add(new Tuple2(coordCrossing.get(coordCrossing.size() - 1).a.n1.get(), coordCrossing.get(coordCrossing.size() - 1).a.p1));
            res.add(new Tuple2(coordCrossing.get(coordCrossing.size()-1).a.n2.get(), coordCrossing.get(coordCrossing.size()-1).a.p2));
            res.add(new Tuple2(coordCrossing.get(coordCrossing.size() - 1).b.n1.get(), coordCrossing.get(coordCrossing.size() - 1).b.p1));
            res.add(new Tuple2(coordCrossing.get(coordCrossing.size()-1).b.n2.get(), coordCrossing.get(coordCrossing.size()-1).b.p2));
        }

        return res;
    }


    /**
     * Creates 8 new LineSegments with only gridded Nodes
     * @param l
     * @param nodePos
     * @return
     */
    private static List<LineSegment> getGridSegments(LineSegment l,  Mapper<INode, PointD> nodePos){
        assert(l.n1.hasValue() && l.n2.hasValue());
        INode n1 = l.n1.get(),
                n2 = l.n2.get();
        List<PointD> n1ps = getGridPoints(n1, nodePos);
        List<PointD> n2ps = getGridPoints(n2, nodePos);
        List<LineSegment> segments = new ArrayList<>();
        for(PointD v : n1ps){
            for(PointD u : n2ps){
                LineSegment ls = new LineSegment(n1, n2);
                ls.p1 = v;
                ls.p2 = u;
                segments.add(ls);
            }
        }

        return segments;
    }


    /**
     * Computes integer grid points node per node
     * @return nodePositions - holds new positions of all nodes
     */
    public static Mapper<INode, PointD> getGridNodes(IGraph graph, Mapper<INode, PointD> nodePositions) {

        for (INode u : graph.getNodes()) {
                nodePositions.setValue(u, new PointD(Math.floor(u.getLayout().getX()), Math.floor(u.getLayout().getY())));
            }
        return nodePositions;
    }

    /**
     * Computes integer grid points for each node not already contained.
     * @return nodePositions - holds new positions of all nodes
     */
    public static Mapper<INode, PointD> getGridNodes(IGraph graph, Mapper<INode, PointD> nodePositions, Set<INode> containedNodes) {

        Mapper<INode, PointD> temp = ForceAlgorithmApplier.initPositionMap(graph);
        List<Tuple2<PointD, Double>> coord = new ArrayList<>();
        if(containedNodes.isEmpty()) {
            for (INode u : graph.getNodes()) {
                coord.addAll(addCoordinates(graph, u, getGridPoints(u, nodePositions), temp));

                Collections.sort(coord, byAngle);
                if (coord.size() > 0) {
                    nodePositions.setValue(u, coord.get(coord.size() - 1).a);
                }
            }
        } else {
            for (INode u : graph.getNodes()) {
                if (containedNodes.contains(u)) {
                    continue;
                }
                coord.clear();
                coord.addAll(addCoordinates(graph, u, getGridPoints(u, nodePositions), temp));

                Collections.sort(coord, byAngle);
                if (coord.size() > 0) {
                    nodePositions.setValue(u, coord.get(coord.size() - 1).a);
                }
            }
        }

        return nodePositions;
    }

    /**
     * Iterates over all PointD's in gridPointsSingle and computes minimum angle
     * Adds this to output coordinates
     * @param u - original node
     * @param gridPoints - contains surrounding grid points
     * @param pos - node positions of entire graph
     * @return coordinates - contains all minimum angles with node positions
     */
    private static List<Tuple2<PointD, Double>> addCoordinates(IGraph graph, INode u, List<PointD> gridPoints, Mapper<INode, PointD> pos) {
        List<Tuple2<PointD, Double>> coordinates = new ArrayList<>();
        for(PointD p : gridPoints) {
            coordinates.add(new Tuple2<>(p, getResultingAngle(graph, pos, u, p)));
        }
        return coordinates;
    }

    /**
     * Iterates over all PointD's in coords and computes minimum angle
     * Adds this to output coordinates
     * @param  coords - contains surrounding grid points and original nodes
     * @param pos - node positions of entire graph
     * @return coordinates - contains all minimum angles with node positions
     */
    private static List<Tuple3<PointD, PointD, Double>> addCoordinates(IGraph graph,
                                                                       List<Tuple4<INode, PointD,INode, PointD>> coords, Mapper<INode, PointD> pos) {
        List<Tuple3<PointD, PointD, Double>> coordinates = new ArrayList<>();

        for(Tuple4<INode, PointD,INode, PointD> tup : coords){
            PointD p_u = tup.b,
                    p_v = tup.d;
            INode i_u = tup.a,
                    i_v = tup.c;
            coordinates.add(new Tuple3<>(p_u, p_v, getResultingAngle(graph, pos, i_u, p_u, i_v, p_v)));
        }

        return coordinates;
    }

    /**
     * Updates node positions and computes new minimum Angle of several nodes
     * @param map   - input node positions
     * @param u,v   - nodes to be updated
     * @param posU, posV - new position of nodes
     * @return Double - computes minimum angle of new positions
     */
    public static Double getResultingAngle(IGraph graph, Mapper<INode, PointD> map, INode u, PointD posU, INode v, PointD posV) {
        map.setValue(u, posU);
        map.setValue(v, posV);
        return getResultingAngle(graph, map);
    }


    /**
     * Updates node positions and computes new minimum Angle
     * @param map  - input node positions
     * @param node - node to be updated
     * @param p    - new position of node
     * @return Double - computes minimum angle of new positions
     */
    public static Double getResultingAngle(IGraph graph, Mapper<INode, PointD> map, INode node, PointD p) {
        map.setValue(node, p);
        return getResultingAngle(graph, map);
    }

    /**
     * Computes minimum angle of positions
     * @param map - Input node positions
     * @return Double - minimum angle of graph
     */
    public static Double getResultingAngle(IGraph graph, Mapper<INode, PointD> map) {

        Maybe<Double> tempAngle = MinimumAngle.getMinimumAngle(graph, Maybe.just(map));
        if (tempAngle.hasValue()) {
            return tempAngle.get();
        }
        return 0.0;
    }



    /**
     * Removes node overlaps.
     */
    public static void removeOverlapsOrganic(IGraph graph){
        OrganicRemoveOverlapsStage removal = new OrganicRemoveOverlapsStage();
        LayoutGraphAdapter adap = new LayoutGraphAdapter(graph);
        CopiedLayoutGraph g2 = adap.createCopiedLayoutGraph();
        removal.applyLayout(g2);
        LayoutUtilities.applyLayout(graph, removal);
    }


    /**
     * Removes node overlaps.
     */
    public static void removeOverlaps(IGraph graph, double dist){
        RemoveOverlapsStage removal = new RemoveOverlapsStage(dist);
        LayoutGraphAdapter adap = new LayoutGraphAdapter(graph);
        CopiedLayoutGraph g2 = adap.createCopiedLayoutGraph();
        removal.applyLayout(g2);
        LayoutUtilities.applyLayout(graph, removal);
    }

    /**
     * Checks if the nodes of the graph are on integer grid points
     * @param graph - Input graph
     * @return true/false - depending on whether the nodes are integer
     */
    public static boolean isGridded(IGraph graph) {
        for (INode u : graph.getNodes()) {
            if ((u.getLayout().getCenter().getX() % 1) != 0) {
                return false;
            }
            if ((u.getLayout().getCenter().getY() % 1) != 0) {
                return false;
            }
        }
        return true;
    }


    /**
     * Returns combination of surrounding grid points of two points
     *
     * @param u - non-grid node
     * @param v - non-grid node
     * @return gridPoints - combination of u and v grid points
     */
    public static List<Tuple4<INode, PointD, INode, PointD>> getGridPoints(INode u, INode v, Mapper<INode, PointD> nodePositions) {
        List<Tuple4<INode, PointD, INode, PointD>> gridPoints = new ArrayList<>();
        List<PointD> gridU = getGridPoints(u, nodePositions);
        List<PointD> gridV = getGridPoints(v, nodePositions);

        for(PointD p_u: gridU){
            for(PointD p_v: gridV){
                gridPoints.add(new Tuple4<INode, PointD, INode, PointD>(u, p_u, v, p_v));
            }
        }

        return gridPoints;
    }

    /**
     * Returns the four surrounding integer grid points of point u
     * @param node - non-grid node
     * @return points - List of surrounding grid points
     */
    public static List<PointD> getGridPoints(INode node, Mapper<INode, PointD> nodePositions) {
        List<PointD> points = new ArrayList<>();
        PointD u = nodePositions.getValue(node);
        points.add(new PointD(Math.floor(u.getX()), Math.floor(u.getY())));
        points.add(new PointD(Math.ceil(u.getX()), Math.floor(u.getY())));
        points.add(new PointD(Math.floor(u.getX()), Math.ceil(u.getY())));
        points.add(new PointD(Math.ceil(u.getX()), Math.ceil(u.getY())));
        return points;
    }

}