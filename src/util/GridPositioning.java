package util;

import com.yworks.yfiles.geometry.*;
import com.yworks.yfiles.graph.*;

import java.util.*;

import com.yworks.yfiles.layout.CopiedLayoutGraph;
import com.yworks.yfiles.layout.LayoutGraphAdapter;

import com.yworks.yfiles.layout.organic.RemoveOverlapsStage;
import layout.algo.ForceAlgorithmApplier;

import algorithms.graphs.MinimumAngle;
import util.graph2d.Intersection;
import util.graph2d.LineSegment;

/**
 * Created by Jessica Wolz on 01.12.16.
 */

public class GridPositioning {

    
    private static Comparator<Tuple2<PointD, Double>> byAngle = (p1, p2) -> p1.b.compareTo(p2.b);
    private static Comparator<Tuple3<PointD, PointD, Double>> byAngles = (p1, p2) -> p1.c.compareTo(p2.c);
    private static Comparator<Tuple3<LineSegment, LineSegment, Double>> byCrossingAngle = (p1, p2) -> p1.c.compareTo(p2.c);
    private static Comparator<Intersection> byIntersectionAngle = Comparator.comparing(intersection -> intersection.angle);


    /**
     * Gridding respective to smalles angle crossing.
     * @param g - Input Graph
     */
    public static void gridGraph(IGraph g){
        Mapper<INode, PointD> nodePositions = ForceAlgorithmApplier.initPositionMap(g);

        while (GridPositioning.isGridGraph(g) == false) {
            ForceAlgorithmApplier.applyNodePositionsToGraph(g, postProcess(g, GridPositioning.respectiveCrossingGrid(g, nodePositions)));
            GridPositioning.removeOverlaps(g, 0.0001);

        }
        
    }

    public static void gridQuickAndDirty(IGraph g){
        Mapper<INode, PointD> nodePositions = ForceAlgorithmApplier.initPositionMap(g);

        while(GridPositioning.isGridGraph(g) == false){
            ForceAlgorithmApplier.applyNodePositionsToGraph(g, postProcess(g, GridPositioning.quickAndDirtyGridding(g, nodePositions)));
            GridPositioning.removeOverlaps(g, 0.0001);
        }
    }

    /**
     * Fast gridding. Considers single nodes only.
     * @param g - Input Graph
     */
    public static void gridGraphFast(IGraph g){
        Mapper<INode, PointD> nodePositions = ForceAlgorithmApplier.initPositionMap(g);

       while (GridPositioning.isGridGraph(g) == false) {
            ForceAlgorithmApplier.applyNodePositionsToGraph(g, postProcess(g, GridPositioning.respectiveNodeGrid(g, nodePositions)));
            GridPositioning.removeOverlaps(g, 0.001);
       }


    }

    /**
     * PostProcess creates positive grid points by moving the graph to the right or up
     * @return positive grid points
     */
    private static Mapper<INode, PointD> postProcess(IGraph g, Mapper<INode, PointD> nodePos){
        double posX = 0;
        double posY = 0;
        for(INode u : g.getNodes()){
            if(posX > u.getLayout().getCenter().getX()){
                posX = u.getLayout().getCenter().getX();
            }
            if(posY > u.getLayout().getCenter().getY()){
                posY = u.getLayout().getCenter().getY();
            }
        }

        for(INode u : g.getNodes()) {
            nodePos.setValue(u, new PointD(u.getLayout().getCenter().getX() - posX, u.getLayout().getCenter().getY() - posY));
        }

        return nodePos;
    }
    /**
     * Checks if edge with new coordinates p1 crosses any other edges with angle > crossing angle
     * @return List of grid points with crossing angle
     */
    public static List<Tuple2<PointD, Double>> checkIntersection(Mapper<INode, PointD> temp, INode n1, PointD p1, IEdge e1, IGraph g, double crossingAngle){
        // set v to initial grid point
        temp.setValue(n1, p1);
        double interAngle;

        List<Tuple2<PointD, Double>> goodGridPoints = new ArrayList<>();
        List<Tuple2<PointD, Double>> badGridPoints = new ArrayList<>();

        List<Intersection> intersectList = MinimumAngle.intersectsWith(e1, g, temp, true);
        if(!intersectList.isEmpty()) {
            Collections.sort(intersectList, byIntersectionAngle);
            interAngle = intersectList.get(intersectList.size() - 1).angle;
            // if created angle better than crossing angle, these are good grid points
            if (interAngle > crossingAngle) { goodGridPoints.add(new Tuple2<>(p1, interAngle));}
            // else bad grid points
            else { badGridPoints.add(new Tuple2<>(p1, interAngle)); }
        }
        if(!goodGridPoints.isEmpty()){
            // return the good grid points if possible
            return goodGridPoints;
        }
        return badGridPoints;
    }

    public static Mapper<INode, PointD> quickAndDirtyGridding(IGraph g, Mapper<INode, PointD> nodePos){
        Random rand = new Random();
        Mapper<INode, PointD> temp = GridPositioning.getGridNodes(g, nodePos);

        Set<INode> seenNodes = new HashSet<>();
        for(INode n1 : g.getNodes()){
            if(seenNodes.contains(n1)) continue;
            seenNodes.add(n1);

            // create the grid position randomly
            double rangeMin = 0;
            double rangeMax = 1;
            double nextRand = rangeMin + (rangeMax - rangeMin) * rand.nextDouble();
            //TODO QUICK AND DIRTY
            PointD u = nodePos.getValue(n1);
            if(nextRand >= 0 && nextRand < 0.25){
                temp.setValue(n1, new PointD(Math.floor(u.getX()), Math.floor(u.getY())));
            } else if (nextRand >= 0.25 && nextRand < 0.5){
                temp.setValue(n1, new PointD(Math.ceil(u.getX()), Math.floor(u.getY())));
            } else if (nextRand >= 0.5 && nextRand < 0.75){
                temp.setValue(n1, new PointD(Math.floor(u.getX()), Math.ceil(u.getY())));
            } else if (nextRand >= 0.75 && nextRand <= 1){
                temp.setValue(n1, new PointD(Math.ceil(u.getX()), Math.ceil(u.getY())));
            } else {
                System.out.println("Error in creating random variable for fast and dirty gridding.");
            }
        }

        seenNodes.clear();
        temp.clear();
        return temp;
    }

    /**
     * Creates gridding: Considers for each vertex n1 each incident edge the four grid points around n1 and chooses best
     * possible one
     * @param g - Input Graph
     * @param nodePos - Node Positions of input graph
     * @return gridded node positions
     */
    public static Mapper<INode, PointD> respectiveNodeGrid(IGraph g, Mapper<INode, PointD> nodePos){
        Optional<Intersection> minCrossing = MinimumAngle.getMinimumAngleCrossing(g, nodePos);

        List<Intersection> crossings = MinimumAngle.getCrossings(g, nodePos);
        int crossingCount = crossings.size();

        Mapper<INode, PointD> temp = GridPositioning.getGridNodes(g, nodePos);

        // no crossings exist, just do simple gridding
        if(!minCrossing.isPresent()) { return temp; }

        List<Tuple2<PointD, Double>> goodGridPoints = new ArrayList<>();

        double crossingAngle = minCrossing.get().angle;

        Set<INode> seenNodes = new HashSet<>();
        for(INode n1 : g.getNodes()){
            if(seenNodes.contains(n1)) continue;
            seenNodes.add(n1);

            // get four new grid points for n1
            List<PointD> newCoordV =  getSurroundingGridPos(n1, temp);

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
                    nodePos.setValue(n1, new PointD((double)Math.round(n1.getLayout().getCenter().getX()), (double)Math.round(n1.getLayout().getCenter().getY())));
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
    private static Mapper<INode, PointD> respectiveCrossingGrid(IGraph graph, Mapper<INode, PointD> nodePos){
        // take minimum crossing
        // try not to break it
        Optional<Intersection> minCrossing = MinimumAngle.getMinimumAngleCrossing(graph, nodePos);
        List<Intersection> crossings = MinimumAngle.getCrossings(graph, nodePos);
        int crossingSize = crossings.size();
        List<Tuple2<INode, PointD>> griddedCrossingNodes;
        Set<INode> crossingNodes = new HashSet<>();
        if(minCrossing.isPresent()) {
            // adding the nodes that are contained in minimum crossing
            crossingNodes.add(minCrossing.get().segment1.n1);
            crossingNodes.add(minCrossing.get().segment1.n2);
            crossingNodes.add(minCrossing.get().segment2.n1);
            crossingNodes.add(minCrossing.get().segment2.n2);

            // compute new positions of each node from the minimum crossing
            List<LineSegment> lines = getGridSegments(minCrossing.get().segment1, nodePos);
            List<LineSegment> otherLines = getGridSegments(minCrossing.get().segment2, nodePos);
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
            // if there is no crossings in the graph, just round to integer coordinates
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
        List<Tuple3<LineSegment, LineSegment, Double>> coordCrossing = new ArrayList<>();
        List<Tuple2<INode, PointD>> res = new ArrayList<>();
        // compute the intersection between all line segments in l1 and l2
        for(LineSegment l: l1){
            for(LineSegment j: l2){
                l.intersects(j,true).ifPresent(i -> {
                    coordCrossing.add(new Tuple3<>(l,j, i.angle));
                });
            }
        }

        Collections.sort(coordCrossing, byCrossingAngle);
        if (!coordCrossing.isEmpty()) {
            // add the crossing nodes and their coordinates
            Tuple3<LineSegment, LineSegment, Double> lastCrossing = coordCrossing.get(coordCrossing.size() - 1);
            res.add(new Tuple2<>(lastCrossing.a.n1, lastCrossing.a.p1));
            res.add(new Tuple2<>(lastCrossing.a.n2, lastCrossing.a.p2));
            res.add(new Tuple2<>(lastCrossing.b.n1, lastCrossing.b.p1));
            res.add(new Tuple2<>(lastCrossing.b.n2, lastCrossing.b.p2));
        }
        return res;
    }


    /**
     * Creates 8 new LineSegments with only gridded Nodes
     * @param l - non gridded line segment
     * @param nodePos - Positions of Graph nodes
     * @return list of created grid line segments
     */
    private static List<LineSegment> getGridSegments(LineSegment l,  Mapper<INode, PointD> nodePos){
        INode n1 = l.n1,
                n2 = l.n2;
        // create grid points for both end points of the line segment l
        List<PointD> n1ps = getSurroundingGridPos(n1, nodePos);
        List<PointD> n2ps = getSurroundingGridPos(n2, nodePos);

        // create the new linesegments with the new coordinates
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
                nodePositions.setValue(u, new PointD((double)Math.round(u.getLayout().getCenter().getX()), (double)Math.round(u.getLayout().getCenter().getY())));
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
        // if no node is already gridded compute the gridding for each node
        if(containedNodes.isEmpty()) {
            for (INode u : graph.getNodes()) {
                coord.addAll(addCoordinates(graph, u, getSurroundingGridPos(u, nodePositions), temp));

                Collections.sort(coord, byAngle);
                if (coord.size() > 0) {
                    // choose the best possible grid for node u respective to the crossing angle
                    nodePositions.setValue(u, coord.get(coord.size() - 1).a);
                }
            }
        // else compute the grid for only those nodes not already gridded
        } else {
            for (INode u : graph.getNodes()) {
                if (containedNodes.contains(u)) {
                    continue;
                }
                coord.clear();
                coord.addAll(addCoordinates(graph, u, getSurroundingGridPos(u, nodePositions), temp));

                Collections.sort(coord, byAngle);
                if (coord.size() > 0) {
                    // choose the best possible grid for node u respective to the crossing angle
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

        Optional<Double> tempAngle = MinimumAngle.getMinimumAngle(graph, map);
        return tempAngle.orElse(0.0);
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
    public static boolean isGridGraph(IGraph graph) {
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
     * Returns the four surrounding integer grid points of point u
     * @param node - non-grid node
     * @return points - List of surrounding grid points
     */
    public static List<PointD> getSurroundingGridPos(INode node, Mapper<INode, PointD> nodePositions) {
        List<PointD> points = new ArrayList<>();
        PointD u = nodePositions.getValue(node);
        points.add(new PointD(Math.floor(u.getX()), Math.floor(u.getY())));
        points.add(new PointD(Math.ceil(u.getX()), Math.floor(u.getY())));
        points.add(new PointD(Math.floor(u.getX()), Math.ceil(u.getY())));
        points.add(new PointD(Math.ceil(u.getX()), Math.ceil(u.getY())));
        return points;
    }

}