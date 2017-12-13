package algorithms.fpp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import algorithms.algo.Connectivity;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.geom.YPoint;
import y.layout.planar.CombinatorialEmbedder;
import y.layout.planar.Face;
import y.layout.planar.PlanarInformation;
import y.view.Graph2D;
import algorithms.canonicalOrder.CanonicalOrder;

/**
 * This class allows you to calculate a straight-line drawing for an arbitrary connected planar graph on an integer grid
 * with size (2n-4) * (n-2) in linear time.
 *
 * @author Philemon Schucker, Mai 2015
 *
 */
public class FraysseixPachPollack {

    protected ArrayList<Edge> insertedEdges;
    private HashMap<Node, TreeNode> nToTNMap = new HashMap<Node, TreeNode>();
    private HashSet<Node> neighbours = new HashSet<Node>();
    private Graph2D g;
    private TreeNode tree;
    private int multiplierX = 70;
    private int multiplierY = -70; // for FPP
    private FPPSettings settings;

    private Face outerFace;

    /**
     * Creating an instance of this class directly applies FPP-Algorithm on given Graph g.
     *
     * @param g
     *            the graph for which the embedding is calculated
     * @throws Exception
     */
    public FraysseixPachPollack(Graph2D g, FPPSettings settings){
        if (y.algo.GraphChecker.isConnected(g) && y.algo.GraphChecker.isMultipleEdgeFree(g)
                && y.algo.GraphChecker.isSelfLoopFree(g) && y.algo.GraphChecker.isPlanar(g)) {
            this.g = g;
            this.settings = settings;
            multiplierX = settings.scaleFactor;
            multiplierY = -settings.scaleFactor;
            start();
        }
    }

    /**
     * Calculates a straight line embedding on an integer grid.
     *
     * @throws Exception
     */
    public void start(){

        // special cases
        if (g.N() <= 0)
            return;
        if (g.N() == 1) {
            tree = new TreeNode(g.nodes().node(), 0);
            applyFPPresult();
            return;
        }
        if (g.N() == 2) {
            NodeCursor nc = g.nodes();
            tree = new TreeNode(nc.node(), 0);
            nc.next();
            tree.right = new TreeNode(nc.node(), 1);
            tree.right.dx = 1;
            applyFPPresult();
            return;
        }
        if (g.N() == 3) {
            NodeCursor nc = g.nodes();
            tree = new TreeNode(nc.node(), 0);
            nc.next();
            tree.right = new TreeNode(nc.node(), 1);
            tree.right.dx = 1;
            tree.right.y = 1;
            nc.next();
            tree.right.right = new TreeNode(nc.node(), 2);
            tree.right.right.dx = 2;
            applyFPPresult();
            return;
        }

        // preparations

        // triangulate the graph if necessary
        insertedEdges = Connectivity.triangulatePlanarGraph(g);

        // calculate canonical order
        PlanarInformation plan = new PlanarInformation(g);
        CombinatorialEmbedder emb = new CombinatorialEmbedder();
        emb.setPlanarInformation(plan);
        emb.embed();
        CanonicalOrder corder = new CanonicalOrder(g, plan, settings.random);
        ArrayList<ArrayList<Node>> orderdpl = corder.getCanonicalOrder();

        // transform into one list
        ArrayList<Node> canonicalOrder = new ArrayList<Node>();
        orderdpl.forEach(l -> canonicalOrder.add(l.get(0)));

        checkForValidOrdering(canonicalOrder);

        // start Fraysseix Pach Pollak

        // initialize
        TreeNode v2 = new TreeNode(canonicalOrder.get(1), 1, 0, 1, null, null);
        TreeNode v3 = new TreeNode(canonicalOrder.get(2), 2, 1, 1, null, v2);
        TreeNode v1 = new TreeNode(canonicalOrder.get(0), 0, 0, 0, null, v3);
        v2.parent = v3; // ext
        v3.parent = v1; // ext
        nToTNMap.put(canonicalOrder.get(1), v2);
        nToTNMap.put(canonicalOrder.get(2), v3);
        nToTNMap.put(canonicalOrder.get(0), v1);
        tree = v1;

        // 1. Phase: Build a binary tree with relative distances for more Details:
        // "A Linear-time Algorithm for Drawing a Planar Graph on a Grid, M. Chrobak, T.H.Payne"
        for (int k = 3; k < canonicalOrder.size(); k++) {

            // clear hashset for every new run
            neighbours.clear();

            // create new TreeNode and put in hashmap
            TreeNode vk = new TreeNode(canonicalOrder.get(k), k);
            nToTNMap.put(canonicalOrder.get(k), vk);

            // check for all neighbors if they are already placed and put these
            // in a hashset
            for (NodeCursor nc = vk.node.neighbors(); nc.ok(); nc.next()) {
                Node n = nc.node();
                // every placed vertex has already a treeNode
                if (nToTNMap.get(n) != null)
                    neighbours.add(n); // all neighbors in Gk-1

            }

            TreeNode wp1, wq1;
            TreeNode wp = null;
            TreeNode wq = null;

            // for all placed neighbors find the first(wp) and last(wq) on the
            // contour of Graph Gk-1
            for (Node n : neighbours) {
                TreeNode tn = nToTNMap.get(n);
                // if the right child is null or not vk's neighbor than we found
                // the outer right neighbor of vk on the Gk-1's contour
                if (tn.right == null || !neighbours.contains(tn.right.node))
                    wq = tn;
                // if the parent node is null or not vk's neighbor than we found
                // the outer left neighbor of vk on the Gk-1's contour
                if (tn.parent == null || !neighbours.contains(tn.parent.node))
                    wp = tn;
            }
            // get wp+1 and wq-1
            wq1 = wq.parent;
            wp1 = wp.right;

            // stretch center part with +1
            wp1.dx += 1;

            // stretch right part with additional +1
            wq.dx += 1;

            // calc relative distance between wp and wq
            int dxwpwq = calculateRelativeDistance(wp, wq);

            // calc vk's relative x distance and y value
            vk.dx = (-wp.y + dxwpwq + wq.y) / 2;
            vk.y = (wp.y + dxwpwq + wq.y) / 2;

            // ajust wq and wp+1 if p+1 != q
            wq.dx = dxwpwq - vk.dx;
            if (wp1.index != wq.index)
                wp1.dx -= vk.dx;

            // install vk
            // vk replaces wp+1 ... wq-1 on the outer face, so wp's right
            // sibling is now vk
            wp.right = vk;
            vk.parent = wp;// ext

            // and vk's right sibling is wq
            vk.right = wq;
            wq.parent = vk;// ext

            if (wp1.index != wq.index) {
                // if p+1 != wq then wp+1 is vk's left most child
                vk.left = wp1;
                // and wq-1 has no right child anymore, because it is now the
                // right most leaf of wp+1
                wq1.right = null;
            }

        }

        // 2. Phase: calculate final x coordinates
        accumulateOffsets(v1, 0);

        outerFace = plan.getOuterFace();
        applyFPPresult();

        Utilities.removeReversedEdges(g, plan);
        insertedEdges.forEach(e -> g.removeEdge(e));


    }

    /**
     * checks if every node appears once in the canoncical order throws an illegal argument exception if not
     *
     * @param canonicalOrder
     *            canonical order to check
     */
    private void checkForValidOrdering(ArrayList<Node> canonicalOrder) {
        boolean[] b = new boolean[g.N()];
        for (Node n : canonicalOrder)
            b[n.index()] = true;
        boolean test = true;
        for (int i = 0; i < b.length; i++) {
            test = test && b[i];
        }
        if (!test)
            throw new IllegalArgumentException("Canonical Order is not valid");
    }

    /**
     * print a treenodes coordinates to the console
     *
     * @param n
     */
    private void printCoords(TreeNode n) {
        if (n == null)
            return;
        System.out.println("node index: " + n.node.index() + " | canonical index " + (n.index + 1) + " coords: x="
                + n.dx + " y=" + n.y);
        printCoords(n.left);
        printCoords(n.right);

    }

    /**
     * Calculates the relative distance between two nodes wp, wq by summing up the relative distances on the path.
     *
     * @param wp
     *            start node
     * @param wq
     *            end node
     * @return the relative distance: dx(wp+1) + dx(wp+2) + ... +dx(wq)
     */
    private int calculateRelativeDistance(TreeNode wp, TreeNode wq) {
        if (wp == wq)
            return 0;
        TreeNode currentNode = wp.right;
        int dx = 0;
        while (currentNode != wq) {
            dx += currentNode.dx;
            currentNode = currentNode.right;
        }
        return dx += wq.dx;
    }

    /**
     * Calculates the final x coordinates in the tree. Initial call:
     *
     * accumulateOffsets(rootnode, 0);
     *
     * @param node
     *            = root node
     * @param offset
     *            = 0
     */
    private void accumulateOffsets(TreeNode node, int offset) {
        if (node != null) {
            node.dx += offset;
            accumulateOffsets(node.left, node.dx);
            accumulateOffsets(node.right, node.dx);
        }
    }

    /**
     * Get the result from Fraysseix Pach Pollak. The result is stored in a hashmap mapping each node of the graph to a
     * point in NxN
     *
     * @return the result mapping
     */
    public HashMap<Node, YPoint> getFFPResult() {
        HashMap<Node, YPoint> res = new HashMap<Node, YPoint>();
        insertResult(tree, res);
        return res;
    }

    /**
     * Inserts the result stored in the binary tree in a hashmap recursively.
     *
     * @param node
     *            current node to get the result from
     * @param res
     *            the hashmap to insert the result
     */
    private void insertResult(TreeNode node, HashMap<Node, YPoint> res) {
        if (node == null)
            return;
        res.put(node.node, new YPoint(node.dx, node.y));
        insertResult(node.left, res);
        insertResult(node.right, res);

    }

    private void applyFPPresult() {
        HashMap<Node, YPoint> res = getFFPResult();
        Node[] nl = g.getNodeArray();
        for (Node n : nl) {
            YPoint p = res.get(n);
            if (p != null) {
                g.getRealizer(n).setCenter(p.x * multiplierX, p.y * multiplierY);
            }
        }
        if (g.N() < 4)
            return;
        // shift to positiv y values
        EdgeCursor ec = outerFace.edges();
        double minimum = 0;
        for (; ec.ok(); ec.next()) {
            Edge e = ec.edge();
            if (g.getRealizer(e.source()).getCenterY() < minimum)
                minimum = g.getRealizer(e.source()).getCenterY();
        }
        minimum *= -1;
        for (NodeCursor nc = g.nodes(); nc.ok(); nc.next())
            g.getRealizer(nc.node()).setCenterY(g.getRealizer(nc.node()).getCenterY() + minimum);
    }

    /**
     * Helper class for Fraysseix Pach Pollak(FPP). Represents a node of a binary tree. Contains data for FPP.
     *
     * @author Philemon Schucker
     *
     */
    protected static class TreeNode {
        protected TreeNode left;
        protected TreeNode right;
        protected int y;
        protected int dx;
        protected Node node;
        protected int index;
        protected TreeNode parent;

        protected TreeNode(Node node, int index) {
            this.node = node;
            this.index = index;
        }

        protected TreeNode(Node node, int index, int y, int dx, TreeNode left, TreeNode right) {
            this.node = node;
            this.y = y;
            this.dx = dx;
            this.index = index;
            this.left = left;
            this.right = right;
        }
    }

    /**
     * Settings for Fraysseix Pach Pollak. Preinitialized with standard settings. Change via direct access.
     *
     * @author Philemon Schucker
     *
     */
    public static class FPPSettings {

        public boolean optimize = true;
        public boolean random = false;
        public int limitTop = 1;
        public int technique = 1;
        public int runs = 25;
        public int scaleFactor = 70;
        public double limitTopHeightFactor = 3.;
    }

}