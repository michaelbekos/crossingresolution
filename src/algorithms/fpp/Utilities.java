package algorithms.fpp;

/**
 * Created by Ama on 06.01.2018.
 */

import com.yworks.yfiles.algorithms.Rectangle2D;
import com.yworks.yfiles.algorithms.YPoint;
import com.yworks.yfiles.algorithms.YVector;
import com.yworks.yfiles.geometry.IPoint;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.*;

import java.util.ArrayList;
import java.util.HashMap;




/**
 * Created by Ama on 14.12.2017.
 */
public class Utilities {


    public final static double EPS = 0.0000001;
    /**
     * Calculates the crossing between the two lines n1 + r(pL-n1) and n2 + q(pR-n2). If lines are parallel or collinear
     * then n1 + (n2-n1)/2 is returned.
     *
     * @param n1
     *            position vector line 1
     * @param pL
     *            point pL such that n1-pL is direction vector of line 1
     * @param n2
     *            position vector line 2
     * @param pR
     *            point pR such that n2-pR is direction vector of line 2
     * @param graph
     *            corresponding Graph g
     * @return crossing point
     */
    static YPoint calculateCrossing(INode n1, YPoint pL, INode n2, YPoint pR, IGraph graph) {
        double ax = n1.getLayout().getCenter().getX();
        double ay = n1.getLayout().getCenter().getY();
        double px = pL.getX() - ax;
        double py = pL.getY() - ay;
        double bx = n2.getLayout().getCenter().getX();
        double by = n2.getLayout().getCenter().getY();
        double qx = pR.getX() - bx;
        double qy = pR.getY() - by;
        // case dividing by zero
        double div = (py * qx / qy - px);
        if (Math.abs(div) < 0.0001)
            return new YPoint(ax + (bx - ax) / 2, ay + (by - ay) / 2);
        // formula: equalization of the two line equations in 2-D vector space:
        // n1 + r(pL-n1) = n2 + q(pR-n2)
        double r = (ax - bx) / div + (by - ay) / (py - px * qy / qx);
        return new YPoint(ax + r * px, ay + r * py);
    }

    /**
     * Calculates the crossing between the two lines n1 + r(pL-n1) and n2 + q(pR-n2). If lines are parallel or collinear
     * then n1 + (n2-n1)/2 is returned.
     *
     * @param p1
     *            position vector line 1
     * @param p2
     *            point pL such that n1-pL is direction vector of line 1
     * @param p3
     *            position vector line 2
     * @param p4
     *            point pR such that n2-pR is direction vector of line 2
     * @return crossing point
     */
    static YPoint calculateCrossing(YPoint p1, YPoint p2, YPoint p3, YPoint p4) {
        double ax =p1.getX();
        double ay = p1.getY();
        double px = p2.getX() - ax;
        double py = p2.getY() - ay;
        double bx = p3.getX();
        double by = p3.getY();
        double qx = p4.getX() - bx;
        double qy = p4.getY() - by;
        // case dividing by zero
        double div = (py * qx / qy - px);
        if (Math.abs(div) < 0.0001)
            return null;
        // formula: equalization of the two line equations in 2-D vector space:
        // n1 + r(pL-n1) = n2 + q(pR-n2)
        double r = (ax - bx) / div + (by - ay) / (py - px * qy / qx);
        return new YPoint(ax + r * px, ay + r * py);
    }


    static YPoint calculateCrossing3(YPoint p1, YPoint p2, YPoint p3, YPoint p4) {
        double q1x =p1.getX();
        double q1y = p1.getY();
        double q2x = p2.getX();
        double q2y = p2.getY();
        double q3x = p3.getX();
        double q3y = p3.getY();
        double q4x = p4.getX();
        double q4y = p4.getY();

        // case dividing by zero
        double div = (q1x-q2x) * (q3y-q4y) - (q1y-q2y) * (q3x-q4x);
        System.out.println(div);
        if (Math.abs(div) < 0.0001)
            return null;
        double x = (q1x*q2y-q2x*q1y) * (q3x-q4x) - (q1x-q2x) *(q3x*q4y-q3y*q4x);
        System.out.println(x);
        x/= div;
        double y = (q1x*q2y-q2x*q1y) * (q3y-q4y) - (q1y-q2y) *(q3x*q4y-q3y*q4x);
        System.out.println(y);
        y /= div;
        // formula: equalization of the two line equations in 2-D vector space
        return new YPoint(x,y);
    }


    /**
     * Get the coordinates of a node as a YPoint
     *
     * @param graph
     *            the corresponding graph
     * @param node
     *            the node the get the coordinates from
     * @return ypoint with node coordinates
     */
    static YPoint getCoords(IGraph graph, INode node) {
        return new YPoint(node.getLayout().getCenter().getX(), node.getLayout().getCenter().getY());
    }

    /**
     * Calculates a distance with: max(node.height,node.width) * factor
     *
     * @param node
     *            node n to get width/height from
     * @param graph
     *            corresponding graph g
     * @param factor
     *            factor to multiply with
     * @return the calculated distance
     */
    static double getMinDistance(IGraph graph, INode node, double factor) {
        // System.out.println("seize : " + r.getHeight() + " " + r.getWidth());
        return Math.max(node.getLayout().getHeight(), node.getLayout().getWidth()) * factor;
    }

    /**
     * Swaps the order of elements in a array list, last = first, second = last-1 etc.
     *
     * @param al
     *            ArrayList to swap
     * @return swapped list
     */
    static ArrayList<INode> swapArrayList(ArrayList<INode> al) {
        ArrayList<INode> tmp = new ArrayList<>();
        for (INode n : al) {
            tmp.add(0, n);
        }
        return tmp;
    }

    /**
     * Calculates the distance between to points a and b.
     *
     * @param xa
     *            point's a x-coordinates
     * @param ya
     *            point's a y-coordinates
     * @param xb
     *            point's b x-coordinates
     * @param yb
     *            point's b y-coordinates
     * @return the distance
     */
    static double distance(double xa, double ya, double xb, double yb) {
        return Math.sqrt((xa - xb) * (xa - xb) + (ya - yb) * (ya - yb));
    }

    /**
     * Calculates the distance between two nodes.
     *
     * @param node1
     *            node n1
     * @param node2
     *            node n2
     * @return the distance between n1 and n2
     */
    static double calculateDistance(INode node1, INode node2, IGraph graph) {
        return distance(node1.getLayout().getCenter().getX(), node1.getLayout().getCenter().getY(), node2.getLayout().getCenter().getX(), node2.getLayout().getCenter().getY());
    }

    /**
     * Calculates the distance between two nodes.
     *
     * @param node1
     *            node1
     * @param b
     *            poitn b
     * @return the distance between n1 and n2
     */
    static double calculateDistance(INode node1, YPoint b, IGraph graph) {
        return distance(node1.getLayout().getCenter().getX(), node1.getLayout().getCenter().getY(), b.getX(), b.getY());
    }

    /**
     * Calculates the angles between two egdes (center,a) and (center,b).
     *
     * @param xc
     *            center-point x coord
     * @param yc
     *            center-point y coord
     * @param xa
     *            first point x coord
     * @param ya
     *            first point y coord
     * @param xb
     *            second point x coord
     * @param yb
     *            second point y coord
     * @return angle in radians (clockwise angle in screen-coordinates (left->right, top->bottom), so angle in [0,2PI]
     */
    static double calculateAngle(double xc, double yc, double xa, double ya, double xb, double yb) {
        // use YVector for easy calculation
        YVector v1 = new YVector(new YPoint(xc, yc), new YPoint(xa, ya));
        YVector v2 = new YVector(new YPoint(xc, yc), new YPoint(xb, yb));
        return YVector.angle(v1, v2);
    }

    /**
     * Calculates the angles between two egdes (center,a) and (center,b).
     *
     * @param center
     *            the center node
     * @param a
     *            node a
     * @param b
     *            node b
     * @return angle in radians (clockwise angle in screen-coordinates (left->right, top->bottom), so angle in [0,2PI]
     */
    static double calculateAngle(INode center, INode a, INode b, IGraph graph) {

        // get coordinates
        double xa = a.getLayout().getCenter().getX();
        double ya = a.getLayout().getCenter().getY();
        double xb = b.getLayout().getCenter().getX();
        double yb = b.getLayout().getCenter().getY();
        double xc = center.getLayout().getCenter().getX();
        double yc = center.getLayout().getCenter().getY();

        return calculateAngle(xc, yc, xa, ya, xb, yb);
    }

    /**
     * Calculates the angles between two egdes (center,a) and (center,b).
     *
     * @param center
     *            the center node
     * @param a
     *            node n1
     * @param b
     *            node n2
     * @return angle in radians (clockwise angle in screen-coordinates (left->right, top->bottom), so angle in [0,2PI]
     */
    static double calculateAngle(INode center, YPoint a, INode b, IGraph g) {

        // get coordinates
        double xa = a.getX();
        double ya = a.getY();
        double xb = b.getLayout().getCenter().getX();
        double yb = b.getLayout().getCenter().getY();
        double xc = center.getLayout().getCenter().getX();
        double yc = center.getLayout().getCenter().getY();

        return calculateAngle(xc, yc, xa, ya, xb, yb);
    }

    /**
     * Calculates the angles between two egdes (center,a) and (center,b).
     *
     * @param center
     *            the center node
     * @param a
     *            node n1
     * @param b
     *            node n2
     * @return angle in radians (clockwise angle in screen-coordinates (left->right, top->bottom), so angle in [0,2PI]
     */
    static double calculateAngle(INode center, YPoint a, YPoint b, IGraph graph) {

        // get coordinates
        double xa = a.getX();
        double ya = a.getY();
        double xb = b.getX();
        double yb = b.getY();
        double xc = center.getLayout().getCenter().getX();
        double yc = center.getLayout().getCenter().getY();
        return calculateAngle(xc, yc, xa, ya, xb, yb);
    }

    /**
     * Calculates the angles between two egdes (center,a) and (center,b).
     *
     * @param center
     *            the center node
     * @param a
     *            node n1
     * @param b
     *            node n2
     * @return angle in radians (clockwise angle in screen-coordinates (left->right, top->bottom), so angle in [0,2PI]
     */
    public static double calculateAngle(YPoint center, YPoint a, YPoint b) {
        // get coordinates
        double xa = a.getX();
        double ya = a.getY();
        double xb = b.getX();
        double yb = b.getY();
        double xc = center.getX();
        double yc = center.getY();

        return calculateAngle(xc, yc, xa, ya, xb, yb);
    }

    /**
     * Calculates the angles between two egdes (center,a) and (center,b).
     *
     * @param center
     *            the center node
     * @param a
     *            node n1
     * @param b
     *            node n2
     * @return angle in radians (clockwise angle in screen-coordinates (left->right, top->bottom), so angle in [0,2PI]
     */
    static double calculateAngle(INode center, INode a, YPoint b, IGraph graph) {

        // get coordinates
        double xa = a.getLayout().getCenter().getX();
        double ya = a.getLayout().getCenter().getY();
        double xb = b.getX();
        double yb = b.getY();
        double xc = center.getLayout().getCenter().getX();
        double yc = center.getLayout().getCenter().getY();

        return calculateAngle(xc, yc, xa, ya, xb, yb);
    }

    /**
     * Calculates the angles between two egdes (center,a) and (center,b).
     *
     * @param center
     *            the center node
     * @param a
     *            node n1
     * @param graph
     *            graph g
     * @return angle in radians (clockwise angle in screen-coordinates (left->right, top->bottom), so angle in [0,2PI]
     */
    static double calculateAngleToZero(INode center, INode a, IGraph graph) {

        // get coordinates
        double xa = a.getLayout().getCenter().getX();
        double ya = a.getLayout().getCenter().getY();
        double xc = center.getLayout().getCenter().getX();
        double yc = center.getLayout().getCenter().getY();

        return calculateAngle(xc, yc, xa, ya, xc + 1, yc);
    }

    /**
     * Calculates the angles between two edges a and b to the center node n in dependence of bends. Takes the first bend
     * on edge a and b for angle calculation or if no bends exists, the edge's target nodes.
     *
     * @param n
     *            the center node
     * @param a
     *            edge a
     * @param b
     *            edge b
     * @return angle in radians (clockwise angle in screen-coordinates (left->right, top->bottom), so angle in [0,2PI]
     */
    static double calculateAngleLookingForBends(INode n, IEdge a, IEdge b, PlanarInformation p, IGraph g) {

        // Edge a
        YPoint ap, bp;
        IEdge e1 = a;
        // get the non inserted edge to check for bends
        if (p.isInsertedEdge(a)) {
            e1 = p.getReverse(a);
        }
        // if no bends, use the target node of edge a
        if (e1.getBends().size() == 0) {
            ap = new YPoint(a.getTargetNode().getLayout().getCenter().getX(), a.getTargetNode().getLayout().getCenter().getY());
            // if just one bend or two bends with e1 has direction (center->a), then take first bend
        }
        else if (e1.getBends().size() == 1 || e1.equals(a)) {
            ap = new YPoint(e1.getBends().first().getLocation().getX(), e1.getBends().first().getLocation().getY());
            // else take second bend
        }
        else {
            ap = new YPoint(e1.getBends().getItem(1).getLocation().getX(), e1.getBends().getItem(1).getLocation().getY());
        }

        // same for edge b

        // Edge a
        IEdge e2 = b;
        // get the non inserted edge to check for bends
        if (p.isInsertedEdge(b)) {
            e2 = p.getReverse(b);
        }
        // if no bends, use the target node of edge b
        if (e2.getBends().size() == 0) {
            bp = new YPoint(b.getTargetNode().getLayout().getCenter().getX(), b.getTargetNode().getLayout().getY());
            // if just one bend or two bends with e1 has direction (center->b), then take first bend
        }
        else if (e2.getBends().size() == 1 || e2.equals(b)) {
            bp = new YPoint(e2.getBends().first().getLocation().getX(), e2.getBends().first().getLocation().getY());
            // else take second bend
        }
        else {
            bp = new YPoint(e2.getBends().getItem(1).getLocation().getX(), e2.getBends().getItem(1).getLocation().getY();
        }

        // use YVector for easy calculation
        YVector v1 = new YVector(new YPoint(n.getLayout().getCenter().getX(), n.getLayout().getCenter().getY()), ap);
        YVector v2 = new YVector(new YPoint(n.getLayout().getCenter().getX(), n.getLayout().getCenter().getY()), bp);
        return YVector.angle(v1, v2);
    }

    /**
     * Calculates a new coordinates for a node "a" if you rotate "a" around the node "center" counter-clockwise with
     * angle alpha. Does not rotate the node! Just calculating coordinates. Does also scale the distance if wanted.
     *
     * Remark: In pixel coordinates this will rotate in clockwise order because y-axis is top->bottom
     *
     * @param center
     *            the center node to rotate around
     * @param xa
     *            x coordinate of node a
     * @param ya
     *            y coordinate of node a
     * @param alpha
     *            the angle to rotate
     * @param scale
     *            scale factor for distance to center node, scale = final distance
     * @param graph
     *            the graph the nodes belong to
     * @return the rotated coordinates as a YPoint
     */
    static YPoint calculateNextNodeAnglePosition(INode center, double xa, double ya, double alpha, double scale,
                                                 IGraph graph) {
        // How it works: calc vector (center->a), multiply with rotation matrix, calculate final point coordiantes
        // System.out.println("Node calc:");
        double xc = center.getLayout().getCenter().getX();
        double yc = center.getLayout().getCenter().getY();
        // System.out.println(xc + " " + yc + " " + xa + " " + ya + " " + alpha
        // + " " + scale);
        xa = xa - xc;
        ya = ya - yc;
        // System.out.println(xa + " " + ya);
        double px = xa * Math.cos(alpha) - ya * Math.sin(alpha);
        double py = xa * Math.sin(alpha) + ya * Math.cos(alpha);
        // System.out.println(px + " " + py);
        double l = Math.sqrt(px * px + py * py);
        px = px / l * scale + xc;
        py = py / l * scale + yc;
        // System.out.println(px + " " + py);
        // System.out.println("res: " + px + " " + py);
        return new YPoint(px, py);
    }

    /**
     * Calculates a new coordinates for a node "a" if you rotate "a" around the node "center" counter-clockwise with
     * angle alpha. Does not rotate the node! Just calculating coordinates. Does also scale the distance if wanted.
     *
     * Remark: In pixel coordinates this will rotate in clockwise order because y-axis is top->bottom
     *
     * @param center
     *            the center node to rotate around
     * @param a
     *            node a to get rotate coordinates for
     * @param alpha
     *            the angle to rotate
     * @param scale
     *            scale factor for distance to center node, scale = final distance
     * @param graph
     *            the graph the nodes belong to
     * @return the rotated coordinates as a YPoint
     */
    static YPoint calculateNextNodeAnglePosition(INode center, INode a, double alpha, double scale, IGraph graph) {
        double xa = a.getLayout().getCenter().getX();
        double ya = a.getLayout().getCenter().getY();
        return calculateNextNodeAnglePosition(center, xa, ya, alpha, scale, graph);
    }

    /**
     * Calculates a new coordinates for a node "a" if you rotate "a" around the node "center" counter-clockwise with
     * angle alpha. Does not rotate the node! Just calculating coordinates. Does also scale the distance if wanted.
     *
     * The node a is the edge's target node if - edge e is not inserted and has no bends - edge e is inserted and the
     * reverse edge has no bends - otherwise node a is the first bend node
     *
     * Remark: In pixel coordinates this will rotate in clockwise order because y-axis is top->bottom
     *
     * @param center
     *            the center node to rotate around
     * @param e
     *            the edge to look at
     * @param alpha
     *            the angle to rotate
     * @param scale
     *            scale factor for distance to center node, scale = final distance
     * @param p
     *            the graph's corresponding planar information
     *
     * @param graph
     *            the graph the nodes belong to
     * @return the rotated coordinates as a YPoint
     */
    static YPoint calcNextAnglePosLookingForBends(INode center, IEdge e, double alpha, double scale, PlanarInformation p,
                                                  IGraph graph) {
        // if inserted edge, getReverse to work with
        if (p.isInsertedEdge(e)) {
            e = p.getReverse(e);
        }

        // bendCount = 0, than we can take the originally target node
        if (e.getBends().size() == 0){
            if (e.getSourceNode().equals(center)) {
                return calculateNextNodeAnglePosition(center, e.getTargetNode(), alpha, scale, graph);
            } else {
                return calculateNextNodeAnglePosition(center, e.getSourceNode(), alpha, scale, graph);
            }
        }
        // else take the first bend
        if (e.getBends().size() == 1 || e.getSourceNode().equals(center)) {
            double xa = e.getBends().first().getLocation().getX();
            double ya = e.getBends().first().getLocation().getY();
            return calculateNextNodeAnglePosition(center, xa, ya, alpha, scale, graph);
        } else {
            double xa = e.getBends().getItem(1).getLocation().getX();
            double ya = e.getBends().getItem(1).getLocation().getY()
            return calculateNextNodeAnglePosition(center, xa, ya, alpha, scale, graph);
        }
    }

    /**
     * Insert a bend in an edge e at position pp in a graph g with corresponding planar information p. The bend is
     * inserted next to the edge source node if edge is not inserted else next to the target node. Does only look for
     * one existing bend. Other bend will drop out.
     *
     * @param e
     *            Edge to insert bend in
     * @param pp
     *            point to place bend
     * @param p
     *            the graph g's corresponding planar information
     * @param graph
     *            the graph the edge belong to
     */
    static void insertBend(IEdge e, YPoint pp, INode n, PlanarInformation p, IGraph graph) {
        double x = pp.getX();
        double y = pp.getY();
        insertBend(e, x, y, n, p, graph);
    }

    /**
     * Insert a bend in an edge e at position pp in a graph g with corresponding planar information p. The bend is
     * inserted next to the edge source node if edge is not inserted else next to the target node. Does only look for
     * one existing bend. Other bend will drop out.
     *
     * @param edge
     *            Edge to insert bend in
     * @param x
     *            the bend's x-coordinate
     * @param y
     *            the bend's y-coordinate
     * @param p
     *            the graph g's corresponding planar information
     * @param graph
     *            the graph the edge belong to
     */
    static void insertBend(IEdge edge, double x, double y, INode n, PlanarInformation p, IGraph graph) {
        // check if the current edge is inserted, take the reverse if
        // yes, because we want to add the bend to the original edge in
        // graph
        if (p.isInsertedEdge(edge)) {
            edge = p.getReverse(edge);
        }
        // check if edge has already an bend and handle the bends
        // correctly (otherwise bend order will be wrong)
        if (edge.getBends().size() != 0 && n.equals(edge.getSourceNode())) {
            IBend b = edge.getBends().first();
            for(IBend bend : edge.getBends()){
                graph.remove(bend);
            }
            graph.addBend(edge,new PointD(x,y));
            graph.addBend(edge,new PointD(b.getLocation().getX(), b.getLocation().getY()));

        } else{
            graph.addBend(edge,new PointD(x,y));
        }
    }

    /**
     * Adds for every edge in graph g a reversed edge. Function will add double directed edges if there already reversed
     * edges!
     *
     * @param graph
     *            graph graph
     * @param p
     *            corresponding planar information
     */
    static void addReversedEdges(IGraph graph, PlanarInformation p) {
        for (IEdge ed : graph.getEdges()) {
            p.createReverse(ed);
        }
    }

    /**
     * Sort edges, necessary to calculate faces in planarInformation.
     *
     * @param graph
     *            the graph to sort edges for
     */
    static void sortEdges(IGraph graph) {
        for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
        {
            nc.node().sortOutEdges(new EdgeComparator(nc.node().firstOutEdge(), graph));
        }
    }

    /**
     * Remove all edges, which are marked as inserted in planar information p, out of the graph g.
     *
     * @param graph
     *            The graph g to remove inserted edges
     * @param p
     *            Planar information in which edges are marked as inserted
     */
    static void removeReversedEdges(IGraph graph, PlanarInformation p) {
        for (IEdge ed : graph.getEdges()) {
            if (p.isInsertedEdge(ed)){
                graph.remove(ed);
            }
        }
    }

    /**
     * Creates a deep copy of Graph2D. Following attributes are copied: - nodes - edges - node positions and labels
     * Optional: You can set notAllEdges to true, if you're graph contains inserted edges, that should not be copied.
     * Then a list of inserted edges and a planar information is required. If you just want a copy, set the parameters
     * to null and notAllEdges to false.
     *
     * @param graph
     *            The Graph2D to deep copy
     * @param insertedEdges
     *            a list of inserted edges that should not be copied or null (if you have inserted your own edges that
     *            are not marked as inserted in planar information)
     * @param p
     *            a planar information to avoid copying inserted edges
     * @param notAllEdges
     *            true if not all edges should be copied otherwise false
     * @return a deep copy of g
     */
    static IGraph deepCopy(IGraph graph, ArrayList<IEdge> insertedEdges, PlanarInformation p, boolean notAllEdges) {
        HashMap<INode, INode> oldToNew = new HashMap<>();
        IGraph newG = graph.getC ; //TODO: wird die Methode überhaupt benutzt
        // deep copy nodes, labels and positions
        for (INode node : graph.getNodes()) {
            INode newNode = newG.createNode();
            oldToNew.put(node, newNode);
            newG.getRealizer(n).setCenter(g.getRealizer(nc.node()).getCenterX(), g.getRealizer(nc.node()).getCenterY());
            newG.getRealizer(n).setLabelText(g.getRealizer(nc.node()).getLabelText());
        }
        // deep copy edges
        for (IEdge edge : graph.getEdges()) {
            if (!notAllEdges || (!insertedEdges.contains(ec.edge()) && !p.isInsertedEdge(ec.edge())))
                newG.createEdge(oldToNew.get(ec.edge().source()), oldToNew.get(ec.edge().target()));
        }
        return newG;
    }

    /**
     * Returns an angle in degree.
     *
     * @param radian
     *            the angle in radians
     * @return the angle in degree
     */
    static double getDegree(double radian) {
        return radian / (2 * Math.PI) * 360;
    }

    /**
     * Same as linesIntersect.
     * @param s1
     * @param e1
     * @param s2
     * @param e2
     * @return
     */
    public static boolean intersectsLine(YPoint s1, YPoint e1, YPoint s2, YPoint e2) {
        return linesIntersect(s1.getX(), s1.getY(), e1.getX(), e1.getY(), s2.getX(), s2.getY(), e2.getX(), e2.getY());
    }

    /**
     * Copied from java.awt.geom.Line2D
     * Returns an indicator of where the specified point {@code (px,py)} lies with respect to the line segment from
     * {@code (x1,y1)} to {@code (x2,y2)}. The return value can be either 1, -1, or 0 and indicates in which direction
     * the specified line must pivot around its first end point, {@code (x1,y1)}, in order to point at the specified
     * point {@code (px,py)}.
     * <p>
     * A return value of 1 indicates that the line segment must turn in the direction that takes the positive X axis
     * towards the negative Y axis. In the default coordinate system used by Java 2D, this direction is
     * counterclockwise.
     * <p>
     * A return value of -1 indicates that the line segment must turn in the direction that takes the positive X axis
     * towards the positive Y axis. In the default coordinate system, this direction is clockwise.
     * <p>
     * A return value of 0 indicates that the point lies exactly on the line segment. Note that an indicator value of 0
     * is rare and not useful for determining collinearity because of floating point rounding issues.
     * <p>
     * If the point is colinear with the line segment, but not between the end points, then the value will be -1 if the
     * point lies "beyond {@code (x1,y1)}" or 1 if the point lies "beyond {@code (x2,y2)}".
     *
     * @param x1
     *            the X coordinate of the start point of the specified line segment
     * @param y1
     *            the Y coordinate of the start point of the specified line segment
     * @param x2
     *            the X coordinate of the end point of the specified line segment
     * @param y2
     *            the Y coordinate of the end point of the specified line segment
     * @param px
     *            the X coordinate of the specified point to be compared with the specified line segment
     * @param py
     *            the Y coordinate of the specified point to be compared with the specified line segment
     * @return an integer that indicates the position of the third specified coordinates with respect to the line
     *         segment formed by the first two specified coordinates.
     * @since 1.2
     */
    public static int relativeCCW(double x1, double y1, double x2, double y2, double px, double py) {
        x2 -= x1;
        y2 -= y1;
        px -= x1;
        py -= y1;
        double ccw = px * y2 - py * x2;
        if (ccw == 0.0) {
            // The point is colinear, classify based on which side of
            // the segment the point falls on. We can calculate a
            // relative value using the projection of px,py onto the
            // segment - a negative value indicates the point projects
            // outside of the segment in the direction of the particular
            // endpoint used as the origin for the projection.
            ccw = px * x2 + py * y2;
            if (ccw > 0.0) {
                // Reverse the projection to be relative to the original x2,y2
                // x2 and y2 are simply negated.
                // px and py need to have (x2 - x1) or (y2 - y1) subtracted
                // from them (based on the original values)
                // Since we really want to get a positive answer when the
                // point is "beyond (x2,y2)", then we want to calculate
                // the inverse anyway - thus we leave x2 & y2 negated.
                px -= x2;
                py -= y2;
                ccw = px * x2 + py * y2;
                if (ccw < 0.0) {
                    ccw = 0.0;
                }
            }
        }
        return (ccw < 0.0) ? -1 : ((ccw > 0.0) ? 1 : 0);
    }

    /**
     * Copied from java.awt.geom.Line2D but changed in that way that intersections at the start or and end points are
     * handled as no intersections.
     *
     * Tests if the line segment from {@code (x1,y1)} to {@code (x2,y2)} intersects the
     * line segment from {@code (x3,y3)} to {@code (x4,y4)}.
     *
     * @param x1
     *            the X coordinate of the start point of the first specified line segment
     * @param y1
     *            the Y coordinate of the start point of the first specified line segment
     * @param x2
     *            the X coordinate of the end point of the first specified line segment
     * @param y2
     *            the Y coordinate of the end point of the first specified line segment
     * @param x3
     *            the X coordinate of the start point of the second specified line segment
     * @param y3
     *            the Y coordinate of the start point of the second specified line segment
     * @param x4
     *            the X coordinate of the end point of the second specified line segment
     * @param y4
     *            the Y coordinate of the end point of the second specified line segment
     * @return <code>true</code> if the first specified line segment and the second specified line segment intersect
     *         each other; <code>false</code> otherwise.
     * @since 1.2
     */
    public static boolean linesIntersect(double x1, double y1, double x2, double y2, double x3, double y3, double x4,
                                         double y4) {
        return ((relativeCCW(x1, y1, x2, y2, x3, y3) * relativeCCW(x1, y1, x2, y2, x4, y4) < 0) && (relativeCCW(x3, y3,
                x4, y4, x1, y1) * relativeCCW(x3, y3, x4, y4, x2, y2) < 0));
    }

    /**
     * Calculates the distance from point p to line (o,e). Uses java.awt.geom.Line2D.ptLineDist
     *
     * @param p
     *            Point p
     * @param o
     *            start point of line
     * @param e
     *            end point of line
     * @return the distance from p to line (o,e)
     */
    public static double distanceToLine(YPoint p, YPoint o, YPoint e) {
        return java.awt.geom.Line2D.ptLineDist(o.getX(), o.getY(), e.getX(), e.getY(), p.getX(), p.getY());
    }

    /**
     * Checks if an infinite line represented by points liS and liE has an intersection with an line segment represented
     * by lsS and lsE. Special variant: Only intersections point in liS + (lisS->lieE) direction are found.
     *
     * @param liS
     *            infinite line's start node
     * @param liE
     *            infinite line's end node
     * @param lsS
     *            line segments first node
     * @param lsE
     *            line segments last node
     * @return the intersection point as YPoint or null if there was no intersection
     */
    public static YPoint calcInfinitLineAndLineSegmentInterSect(YPoint liS, YPoint liE, YPoint lsS, YPoint lsE) {
        // calculate intersection point of the two infinite lines
        YPoint p = y.geom.Geom.calcIntersection(liS, liE, lsS, lsE);
        // System.out.println("P: " + p + " start: " + lsS + " end: " + lsE);
        // p == null then there was no intersection / parallel
        if (p == null)
            return null;
        // check if the intersection point is one of the line segments //TODO? seems wrong....
        if (lsS.equals(p) || lsE.equals(p)){
            System.out.println("EQUALS: p: "+ p + " s: "+ lsE + "e: " + lsE);
            return null;
        }
        // check if intersection point is between the line segment
        boolean isInXRange = (p.getX() >= lsS.getX() && p.getX() <= lsE.getX())
                || (p.getX() <= lsS.getX() && p.getX() >= lsE.getX());
        // floating point fix:
        if(lsS.getX() == lsE.getX())
            if(Math.abs(lsS.getX()-p.getX())<EPS)
                isInXRange = true;
        boolean isInYRange = (p.getY() >= lsS.getY() && p.getY() <= lsE.getY())
                || (p.getY() <= lsS.getY() && p.getY() >= lsE.getY());
        if(lsS.getY() == lsE.getY())
            if(Math.abs(lsS.getY()-p.getY())<EPS)
                isInYRange = true;
        // check if the intersection is on the right site, we only consider intersections in lisS, liE direction
        boolean distanceCheck = distance(liS.getX(), liS.getY(), p.getX(), p.getY()) > distance(liE.getX(), liE.getY(),
                p.getX(), p.getY());
        // second check
        // YVector v = new YVector(liE, liS);
        // YVector w = new YVector(p, liS);
        // YVector check = new YVector(p, liS);
        // v.scale(-1);
        // w.add(v);
        // boolean distanceCheck2 = w.getX() * check.getX() >= 0 && w.getY() * check.getY() >= 0;
        boolean xOk = (liS.getX() - p.getX()) * (liE.getX() - p.getX()) <= 0;
        boolean yOk = (liS.getY() - p.getY()) * (liE.getY() - p.getY()) <= 0;
        boolean distanceCheck2 = !(xOk && yOk);
        System.out.println("P: " + p + " start: " + liS + " end: " + liE + " Sstart: " + lsS + " Send: " + lsE);
        System.out.println("Check1: " + distanceCheck + " check2: " + distanceCheck2 + " xRange: " + isInXRange
                + " yRange: " + isInYRange);
        // if all test are ok, we have an intersection, else not
        if (isInXRange && isInYRange && distanceCheck && distanceCheck2)
            return p;
        else
            return null;
    }

    /**
     * Checks if an node n's new position newP bounding box intersects edge e in graph g. n's new bounding box is
     * calculated out of the current/old posion oldP and the current/old bounding box rec.
     *
     * @param rec
     *            Node n's current bounding box
     * @param edge
     *            the edge to check intersection for
     * @param graph
     *            the corresponding graph g
     * @param newP
     *            n's new postion to check
     * @param oldP
     *            n's current/old position
     * @return true if bounding box intersects, else false
     */
    public static boolean checkBoxLineCrossing(Rectangle2D rec, IEdge edge, IGraph graph, YPoint newP, YPoint oldP) {
        YPoint start = getCoords(graph, edge.getSourceNode());
        YPoint end = getCoords(graph, edge.getTargetNode());
        // calculate new bounding box
        double xdif = newP.getX() - oldP.getX();
        double ydif = newP.getY() - oldP.getY();
        double x = rec.getX() + xdif;
        double y = rec.getY() + ydif;
        Rectangle2D rec2 = new Rectangle2D(x, y, rec.getHeight(), rec.getWidth());
        // check
        return rec2.intersectsLine(start.getX(), start.getY(), end.getX(), end.getY());
    }

    public static void main(String[] args) {
        //P: X: -331.3517461894706 Y: -180.99182754667112 end: X: -331.3517461894705 Y: -180.99182754667106 Sstart: X: -465.9627399914545 Y: -331.3341965790749 Send: X: -420.0 Y: -280.0
        YPoint p1 = new YPoint(-164.85802160743677,-102.29469630176287);
        YPoint p2 = new YPoint(-331.3517461894705,-180.99182754667106);
        YPoint p3 = new YPoint(-465.9627399914545,-331.3341965790749);
        YPoint p4 = new YPoint(-420.0,-0.0);
        double test = (-164.85802160743677 + 331.3517461894705) * (-331.3341965790749+280.0)-( -102.29469630176287 +  180.99182754667106)* (-465.9627399914545  +420.0);//   (q1x-q2x) * (q3y-q4y) - (q1y-q2y) * (q3x-q4x);
        double a = (-164.85802160743677 + 331.3517461894705) * (-331.3341965790749+280.0);
        double b = ( -102.29469630176287 +  180.99182754667106)* (-465.9627399914545  +420.0);
        System.out.println(a-b);
        System.out.println( a + " " + b);
        System.out.println(test);
        System.out.println(y.geom.Geom.calcIntersection(p1,p2,p3,p4));
        System.out.println(calculateCrossing(p1, p2, p3, p4));
        System.out.println(calculateCrossing3(p1, p2, p3, p4));
    }
    public static YPoint getPointForNode(INode node, IGraph graph) {
        //  NodeRealizer realizer = graph.getRealizer(node);
        return new YPoint(node.getLayout().getCenter().getX(), node.getLayout().getCenter().getY());
    }

    public static YPoint getBendPoint(IBend b) {
        return new YPoint(b.getLocation().getX(), b.getLocation().getY());
    }
    public static YPoint toYPoint(IPoint point) {
        return new YPoint(point.getX(), point.getY());
    }
    public static YPoint toYPoint(PointD point) {
        return new YPoint(point.getX(), point.getY());
    }


}
