package util;

import com.yworks.yfiles.geometry.*;
import com.yworks.yfiles.graph.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yworks.yfiles.graph.styles.INodeStyle;
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
    private static Comparator<Tuple3<LineSegment, LineSegment, Intersection>> byIntersectionAngle = (p1, p2) -> p1.c.angle.compareTo(p2.c.angle);


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
     * Fast gridding. Considers single nodes only.
     * @param g
     */
    public static void simpleGridGraph(IGraph g){
        Mapper<INode, PointD> nodePositions = ForceAlgorithmApplier.initPositionMap(g);

       while (GridPositioning.isGridded(g) == false) {
            ForceAlgorithmApplier.applyNodePositionsToGraph(g, GridPositioning.getGridNodesFast(g, nodePositions));
            GridPositioning.removeOverlaps(g, 0.001);
       }

    }

    public static Mapper<INode, PointD> copyPositions(Mapper<INode, PointD> nodePos, IGraph g){
        Mapper<INode, PointD> temp = new Mapper<>();
        for(INode u : g.getNodes()) {
            temp.setValue(u, nodePos.getValue(u));
        }

        return temp;
    }

    public static List<Tuple2<PointD, Double>> checkIntersection(Mapper<INode, PointD> temp, INode n1, PointD p1, IEdge e1, IGraph g, double crossingAngle){
        // set v to initial grid point
        temp.setValue(n1, p1);
        double interAngle;

        List<Tuple2<PointD, Double>> goodGridPoints = new ArrayList<>();
        List<Tuple2<PointD, Double>> badGridPoints = new ArrayList<>();

        List<Tuple3<LineSegment, LineSegment, Intersection>> intersectList = MinimumAngle.intersectsWith(e1, g, temp, true);
        if(!intersectList.isEmpty()) {
            Collections.sort(intersectList, byIntersectionAngle);
            interAngle = intersectList.get(intersectList.size() - 1).c.angle;
            if (interAngle > crossingAngle) { goodGridPoints.add(new Tuple2<>(p1, interAngle));}
            else { badGridPoints.add(new Tuple2<>(p1, interAngle)); }
        }
        if(!goodGridPoints.isEmpty()){
            return goodGridPoints;
        }
        return badGridPoints;
    }

    public static Mapper<INode, PointD> getGridNodesFast(IGraph g, Mapper<INode, PointD> nodePos){
        Maybe<Tuple3<LineSegment, LineSegment, Intersection>> minCrossing = MinimumAngle.getMinimumAngleCrossing(g, Maybe.just(nodePos));
        Mapper<INode, PointD> temp = GridPositioning.getGridNodes(g, nodePos);

        // no crossings exist, just do simple gridding
        if(!minCrossing.hasValue()) { return temp; }

        List<Tuple2<PointD, Double>> goodGridPoints = new ArrayList<>();

        double crossingAngle = minCrossing.get().c.angle;

        Set<INode> seenNodes = new HashSet<>();
        for(INode n1 : g.getNodes()){
            if(seenNodes.contains(n1)) continue;
            seenNodes.add(n1);

            // get four new grid points for n1
            List<PointD> newCoordV =  getGridPoints(n1, temp);

            for(INode u : g.neighbors(INode.class, n1)){

                goodGridPoints.clear();

                // u is already gridded
                if(seenNodes.contains(u)) continue;

                // edge does not exist
                if(g.getEdge(n1,u) == null) continue;
                IEdge e1 = g.getEdge(n1,u);

                // no crossings with this edge
                if(MinimumAngle.intersectsWith(e1, g, temp, true).isEmpty()){
                    //nodePos.setValue(n1, new PointD((double)Math.round(n1.getLayout().getX()), (double)Math.round(n1.getLayout().getY())));
                    continue;
                }

                // check whether gridding creates new crossings
                // set n1 to initial grid point
                List<Tuple2<PointD, Double>> gridPoints = checkIntersection(temp, n1, newCoordV.get(0), e1, g, crossingAngle);
                goodGridPoints.addAll(gridPoints);

                // set n1 to second grid point
                gridPoints = checkIntersection(temp, n1, newCoordV.get(1), e1, g, crossingAngle);
                goodGridPoints.addAll(gridPoints);

                // set n1 to third grid point
                gridPoints = checkIntersection(temp, n1, newCoordV.get(2), e1, g, crossingAngle);
                goodGridPoints.addAll(gridPoints);

                // set n1 to fourth grid point
                gridPoints = checkIntersection(temp, n1, newCoordV.get(3), e1, g, crossingAngle);
                goodGridPoints.addAll(gridPoints);

                Collections.sort(goodGridPoints, byAngle);
                if (!goodGridPoints.isEmpty()) {
                    nodePos.setValue(n1, goodGridPoints.get(goodGridPoints.size()-1).a);
                    if (goodGridPoints.get(goodGridPoints.size()-1).b > crossingAngle) {
                        crossingAngle = goodGridPoints.get(goodGridPoints.size()-1).b;
                    }
                } else {
                    nodePos.setValue(n1, new PointD((double)Math.round(n1.getLayout().getX()), (double)Math.round(n1.getLayout().getY())));
                }

            }
            goodGridPoints.clear();
        }

        return nodePos;
    }

    /**
     * Computes integer grid points respectively by crossing angle if such exists
     * Otherwise respectively to the minimum angle of the graph
     * @return nodePositions - integer grid node positions
     */
    private static Mapper<INode, PointD> getGridNodesRespectively(IGraph graph, Mapper<INode, PointD> nodePos){
        // take minimum crossing
        // try not to break it
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
                l.intersects(j,true).andThen(i -> {
                    coordCrossing.add(new Tuple3<>(l,j, i.angle));
                });
            }
        }

        Collections.sort(coordCrossing, byCrossingAngle);
        if (!coordCrossing.isEmpty()) {
            Tuple3<LineSegment, LineSegment, Double> lastCrossing = coordCrossing.get(coordCrossing.size() - 1);
            res.add(new Tuple2<>(lastCrossing.a.n1.get(), lastCrossing.a.p1));
            res.add(new Tuple2<>(lastCrossing.a.n2.get(), lastCrossing.a.p2));
            res.add(new Tuple2<>(lastCrossing.b.n1.get(), lastCrossing.b.p1));
            res.add(new Tuple2<>(lastCrossing.b.n2.get(), lastCrossing.b.p2));
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
     * Without considerung resulting angle
     * @return nodePositions - holds new positions of all nodes
     */
    public static Mapper<INode, PointD> getGridNodes(IGraph graph, Mapper<INode, PointD> nodePositions) {
        for (INode u : graph.getNodes()) {
                nodePositions.setValue(u, new PointD((double)Math.round(u.getLayout().getX()), (double)Math.round(u.getLayout().getY())));
            }
        return nodePositions;
    }

    /**
     * Computes integer grid points for each node not already contained.
     * With considering resulting angle
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