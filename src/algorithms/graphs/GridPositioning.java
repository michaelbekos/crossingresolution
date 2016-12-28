package algorithms.graphs;

import com.yworks.yfiles.geometry.*;
import com.yworks.yfiles.graph.*;

import java.util.*;

import com.yworks.yfiles.layout.CopiedLayoutGraph;
import com.yworks.yfiles.layout.LayoutGraphAdapter;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.layout.organic.OrganicRemoveOverlapsStage;
import com.yworks.yfiles.layout.organic.RemoveOverlapsStage;
import layout.algo.ForceAlgorithmApplier;
import util.*;
import util.graph2d.Intersection;
import util.graph2d.LineSegment;

/**
 * Created by Jessica Wolz on 01.12.16.
 */

public class GridPositioning {

    private IGraph graph;
    private static IMapper<INode, PointD> nodePositions;
    private static Comparator<Tuple2<PointD, Double>> byAngle = (p1, p2) -> p1.b.compareTo(p2.b);
    private static Comparator<Tuple3<PointD, PointD, Double>> byAngles = (p1, p2) -> p1.c.compareTo(p2.c);


    /**
     * Constructor
     * @param graph - input graph
     */
    public GridPositioning(IGraph graph) {
        this.graph = graph;
        nodePositions = ForceAlgorithmApplier.initPositionMap(this.graph);
    }

    public static void gridGraph(IGraph g){
        GridPositioning grid = new GridPositioning(g);
        boolean gridding = grid.isGridded(g);
        
        while (gridding == false) {
            ForceAlgorithmApplier.applyNodePositionsToGraph(g, grid.getGridNodesRespectively());

            grid.removeOverlaps(0.1);
            gridding = grid.isGridded(g);
        }
        
    }
    /**
     * Computes integer grid points respectively by crossing angle if such exists
     * Otherwise respectively to the minimum angle of the graph
     * @return nodePositions - integer grid node positions
     */
    public IMapper<INode, PointD> getGridNodesRespectively() {
        Set<INode> seenNodes = new HashSet<>();
        List<Tuple3<LineSegment, LineSegment, Intersection>> crossings = MinimumAngle.getCrossings(this.graph, Maybe.just(nodePositions));
        IMapper<INode, PointD> temp = ForceAlgorithmApplier.initPositionMap(this.graph);

        List<Tuple2<PointD, Double>> coord = new ArrayList<>();
        List<Tuple3<PointD, PointD, Double>> coordCrossing = new ArrayList<>();
        boolean contained = false;
        Set<INode> containedNodes = new HashSet<>();

        // computes the grid nodes respectively for single nodes or for crossing nodes
        for (INode u : graph.getNodes()) {
            // insures that coordinates to same node are not added twice
            if (seenNodes.contains(u)) {
                break;
            }
            seenNodes.add(u);
            PointD currU = nodePositions.getValue(u);

            for (INode v : graph.neighbors(INode.class, u)) {
                if (seenNodes.contains(v)) {
                    break;
                }
                seenNodes.add(v);
                PointD currV = nodePositions.getValue(v);
                LineSegment l1 = new LineSegment(currU, currV);
                LineSegment l2 = new LineSegment(currV, currU);
                if (crossings.contains(l1) || crossings.contains(l2)) {
                    containedNodes.add(u);
                    containedNodes.add(v);
                    coordCrossing.clear();
                    coordCrossing.addAll(addCoordinates(getGridPoints(u,v), temp));
                    Collections.sort(coordCrossing, byAngles);
                    if (coordCrossing.size() > 0) {
                        nodePositions.setValue(u, coordCrossing.get(coordCrossing.size() - 1).a);
                        nodePositions.setValue(v, coordCrossing.get(coordCrossing.size() - 1).b);
                    }
                }
            }

           getGridNodes(containedNodes);

        }
        return nodePositions;
    }

    /**
     * Iterates over all PointD's in coords and computes minimum angle
     * Adds this to output coordinates
     * @param  coords - contains surrounding grid points and original nodes
     * @param pos - node positions of entire graph
     * @return coordinates - contains all minimum angles with node positions
     */
    private List<Tuple3<PointD, PointD, Double>> addCoordinates(List<Tuple4<INode, PointD,INode, PointD>> coords, IMapper<INode, PointD> pos) {
        List<Tuple3<PointD, PointD, Double>> coordinates = new ArrayList<>();

        for(Tuple4<INode, PointD,INode, PointD> tup : coords){
            PointD p_u = tup.b,
                    p_v = tup.d;
            INode i_u = tup.a,
                    i_v = tup.c;
            coordinates.add(new Tuple3<>(p_u, p_v, getResultingAngle(pos, i_u, p_u, i_v, p_v)));
        }

        return coordinates;
    }

    /**
     * Iterates over all PointD's in gridPointsSingle and computes minimum angle
     * Adds this to output coordinates
     * @param u - original node
     * @param gridPoints - contains surrounding grid points
     * @param pos - node positions of entire graph
     * @return coordinates - contains all minimum angles with node positions
     */
    private List<Tuple2<PointD, Double>> addCoordinates(INode u, List<PointD> gridPoints, IMapper<INode, PointD> pos) {
        List<Tuple2<PointD, Double>> coordinates = new ArrayList<>();
        for(PointD p : gridPoints) {
            coordinates.add(new Tuple2<>(p, getResultingAngle(pos, u, p)));
        }
        return coordinates;
    }

    /**
     * Computes integer grid points node per node
     * @return nodePositions - holds new positions of all nodes
     */
    public IMapper<INode, PointD> getGridNodes(Set<INode> containedNodes) {

        IMapper<INode, PointD> temp = ForceAlgorithmApplier.initPositionMap(this.graph);
        List<Tuple2<PointD, Double>> coord = new ArrayList<>();

        for (INode u : graph.getNodes()) {
            if(containedNodes.contains(u)){ break; }
            coord.clear();
            coord.addAll(addCoordinates(u, getGridPoints(u), temp));

            Collections.sort(coord, byAngle);
            if (coord.size() > 0) {
                nodePositions.setValue(u, coord.get(coord.size() - 1).a);
            }
        }

        return nodePositions;
    }

    /**
     * Removes node overlaps.
     */
    public void removeOverlapsOrganic(){
        OrganicRemoveOverlapsStage removal = new OrganicRemoveOverlapsStage();
        LayoutGraphAdapter adap = new LayoutGraphAdapter(this.graph);
        CopiedLayoutGraph g2 = adap.createCopiedLayoutGraph();
        removal.applyLayout(g2);
        LayoutUtilities.applyLayout(this.graph, removal);
    }


    /**
     * Removes node overlaps.
     */
    public void removeOverlaps(double dist){
        RemoveOverlapsStage removal = new RemoveOverlapsStage(dist);
        LayoutGraphAdapter adap = new LayoutGraphAdapter(this.graph);
        CopiedLayoutGraph g2 = adap.createCopiedLayoutGraph();
        removal.applyLayout(g2);
        LayoutUtilities.applyLayout(this.graph, removal);
    }

    /**
     * Updates node positions and computes new minimum Angle of several nodes
     * @param map   - input node positions
     * @param u,v   - nodes to be updated
     * @param posU, posV - new position of nodes
     * @return Double - computes minimum angle of new positions
     */
    public Double getResultingAngle(IMapper<INode, PointD> map, INode u, PointD posU, INode v, PointD posV) {
        map.setValue(u, posU);
        map.setValue(v, posV);
        return getResultingAngle(map);
    }


    /**
     * Updates node positions and computes new minimum Angle
     * @param map  - input node positions
     * @param node - node to be updated
     * @param p    - new position of node
     * @return Double - computes minimum angle of new positions
     */
    public Double getResultingAngle(IMapper<INode, PointD> map, INode node, PointD p) {
        map.setValue(node, p);
        return getResultingAngle(map);
    }

    /**
     * Computes minimum angle of positions
     * @param map - Input node positions
     * @return Double - minimum angle of graph
     */
    public Double getResultingAngle(IMapper<INode, PointD> map) {

        Maybe<Double> tempAngle = MinimumAngle.getMinimumAngle(this.graph, Maybe.just(map));
        if (tempAngle.hasValue()) {
            return tempAngle.get();
        }
        return 0.0;
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
    public List<Tuple4<INode, PointD, INode, PointD>> getGridPoints(INode u, INode v) {
        List<Tuple4<INode, PointD, INode, PointD>> gridPoints = new ArrayList<>();
        List<PointD> gridU = getGridPoints(u);
        List<PointD> gridV = getGridPoints(v);

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
    public List<PointD> getGridPoints(INode node) {
        List<PointD> points = new ArrayList<>();
        PointD u = nodePositions.getValue(node);
        points.add(new PointD(Math.floor(u.getX()), Math.floor(u.getY())));
        points.add(new PointD(Math.ceil(u.getX()), Math.floor(u.getY())));
        points.add(new PointD(Math.floor(u.getX()), Math.ceil(u.getY())));
        points.add(new PointD(Math.ceil(u.getX()), Math.ceil(u.getY())));
        return points;
    }

}