package algorithms.graphs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.yworks.yfiles.algorithms.*;


public class Connectivity {

    //no need for instantiation...
    private Connectivity() {

    }

    /**
     * Makes a given planar graph biconnected keeping it's planarity.
     *
     * Remark: Is not optimized for minimum number of new edges because this
     * function is intended as prestep for graph triangulation.
     *
     * @param graph
     *            a planar graph
     * @return the inserted edges as a List
     */
    public static ArrayList<Edge> makeGraphBiconnectedPlanar(Graph graph) {
        int n = graph.getNodeArray().length;
        PlanarEmbedding plan = new PlanarEmbedding(graph);
        if(n < graph.getNodeArray().length)
            throw new IllegalArgumentException("Input Graph is not planar!");
        return makeGraphBiconnectedPlanar(graph, plan, true);
    }

    /**
     * Makes a given planar graph biconnected keeping it's planarity.
     *
     * Remark I: Is not optimized for minimum number of new edges because this
     * function is intended as prestep for graph triangulation.
     *
     * @param graph
     *            a planar graph
     * @param plan
     *            a planar embedding of the given graph
     * @param removePlanInsertedEdges
     *            removes the double edges inserted by the planar embedding
     *            "plan " if true.
     * @return the inserted edges as a List
     */
    public static ArrayList<Edge> makeGraphBiconnectedPlanar(Graph graph, PlanarEmbedding plan, boolean removePlanInsertedEdges) {

        ArrayList<Edge> insertedEdges = new ArrayList<Edge>();

        // stores if node is cutvertice
        INodeMap nmap = graph.createNodeMap();

        // stores for each edge the biconnected component number
        IEdgeMap emap = graph.createEdgeMap();

        // get biconnected components

        if (com.yworks.yfiles.algorithms.GraphConnectivity.biconnectedComponents(graph, emap, nmap) <= 1) {
            // clean up
            graph.disposeEdgeMap(emap);
            graph.disposeNodeMap(nmap);
            /*if (removePlanInsertedEdges)
                removeInsertedEdges(graph, plan);
            */ // finish
            return insertedEdges;
        }

        // iterate over all nodes to find cutvertices in node map
        INodeCursor nc = graph.getNodeCursor();

        // for every cutvertice
        for (Node curNode = nc.node(); nc.ok(); nc.next(), curNode = nc.node()) {
            if (nmap.getBool(curNode)) {

                // get the first outgoing edge
                Dart preDart = plan.getOutgoingDarts(curNode).get(0);
                Edge preEdgeD = preDart.getAssociatedEdge();

                int preEdgeBCNumber = emap.getInt(preEdgeD);

                Dart dartToStopAt = preDart;

                // for each other outgoing edges

                for (Dart curDart = plan.getCyclicNext(preDart); curDart != dartToStopAt; curDart = plan.getCyclicNext(curDart)) {
                    int curEdgeBCNumber = emap.getInt(curDart);

                    // ignore selfloops and edge pairs that belong to the same
                    // biconnected component
                    if (preEdgeBCNumber == -1 || preEdgeBCNumber == curEdgeBCNumber) {
                        preEdgeBCNumber = curEdgeBCNumber;
                        preDart = curDart;
                        continue;
                    }

                    // get the two next nodes of the two different biconnected
                    // components outer face
                    Node source = preDart.getAssociatedEdge().opposite(curNode);
                    Node target = curDart.getAssociatedEdge().opposite(curNode);

                    // in case of edge already exists go to next
                    if (graph.containsEdge(source, target) || graph.containsEdge(target, source)) {
                        preEdgeBCNumber = curEdgeBCNumber;
                        preDart = curDart;
                        continue;
                    }
                    // create a new edge and insert it at the correct place (and
                    // it's reverse)
                    //TODO: Ist das richtig umgesetzt mit dem Reverse? MUss noch impl. werden
                    /*
                    Edge newEdge = graph.createEdge(source,
                            plan.getReverse(preEdge), target, curEdge,
                            Graph.BEFORE, Graph.AFTER);
                    Edge newEdgeRev = graph.createEdge(target,
                            plan.getReverse(curEdge), source, preEdge,
                            Graph.AFTER, Graph.BEFORE);
                    */

                    // update the planar information about the new reverse edges
                   // plan.setReverse(newEdge, newEdgeRev);
                    // mark one as inserted
                   // plan.markAsInsertedEdge(newEdgeRev);

                    // merge connecting edge and newEdge to the start
                    // biconnected component to avoid double edges
                    // so set component number in maps
                    emap.setInt(curDart.getAssociatedEdge(), preEdgeBCNumber);
                    //emap.setInt(plan.getReverse(curEdge), preEdgeBCNumber);
                    //emap.setInt(newEdge, preEdgeBCNumber);
                    //emap.setInt(plan.getReverse(newEdge), preEdgeBCNumber);

                    // remember new inserted edge
                    // insertedEdges.add(newEdge);

                    // prepare for next for loop
                    preEdgeBCNumber = curEdgeBCNumber;
                    preDart = curDart;

                }

            }
        }
        // free resources
        graph.disposeEdgeMap(emap);
        graph.disposeNodeMap(nmap);
       /* if (removePlanInsertedEdges)
            removeInsertedEdges(graph, plan);
        */return insertedEdges;
    }

    /**
     * Removes all edges that were inserted through the planar embedding.
     *
     * @param graph
     *            The graph to remove the edges from
     * @param plan
     *            The graph's corresponding embedding
     */
   /* private static void removeInsertedEdges(Graph graph, PlanarEmbedding plan) {
        for (EdgeCursor cursor = graph.edges(); cursor.ok(); cursor.next()) {
            Edge edge = cursor.edge();
            if (plan.isInsertedEdge(edge)) {
                graph.removeEdge(edge);
            }
        }
    }
*/
    /**
     * Triangulates a given planar graph keeping it's planarity.
     *
     * @param graph
     *            a planar graph
     * @return inserted edges in a list
     */
    public static ArrayList<Edge> triangulatePlanarGraph(Graph graph) throws IllegalArgumentException {

        // get start embedding to work with
        int n = graph.getNodeArray().length;
        PlanarEmbedding plan = new PlanarEmbedding(graph);
        /*
        CombinatorialEmbedder emb = new CombinatorialEmbedder();
        emb.setPlanarInformation(plan);
        emb.embed();
        */
        if(n < graph.getNodeArray().length) {
          //  removeInsertedEdges(graph, plan);
            throw new IllegalArgumentException("Input Graph is not planar!");
        }
        ArrayList<Edge> insertedEdges = makeGraphBiconnectedPlanar(graph, plan, true);

        // calculate new embedding

        //emb.dispose();
        plan = new PlanarEmbedding(graph);
        //emb.setPlanarInformation(plan);
        //emb.embed();

        // for every face check if it is triangulated, if not triangulate it
        for (List<Dart> face : plan.getFaces()) {
            triangulateFace(face, graph, insertedEdges);
        }

        // clean up
       // removeInsertedEdges(graph, plan);
        return insertedEdges;
    }

    /**
     * Triangulates a given face.
     *
     * @param curFace
     *            the face to triangulate
     */
    private static void triangulateFace(List<Dart> curFace, Graph graph,
                                        ArrayList<Edge> insertedEdges) {

        // get the face's nodes
      //  Iterator<Dart> dartIta = curFace.iterator();

        // faces with size <=3 are ok, nothing to do
        if (curFace.size() <= 3)
            return;

        // create nodemap for preventing for double neighbor edges
        INodeMap nmap = graph.createNodeMap();

        // find the face's node with minimum degree
        Dart curDart = curFace.get(0);
        MyNode list;
        if(curDart.isReversed()){
            list = new MyNode(curDart.getAssociatedEdge().source());
        } else {
            list = new MyNode(curDart.getAssociatedEdge().target());
        }
        MyNode ptr = list;
        MyNode v1Node = ptr;

        // jump to second edge , for loop starts with 1
        // for all edges get source, add to helper list, and find node with
        // minimum degree
        for (int i = 1; i < curFace.size(); i++) {
            curDart = curFace.get(i);
            if(curDart.isReversed()){
                ptr.next = new MyNode(curDart.getAssociatedEdge().source());
            } else {
                ptr.next = new MyNode(curDart.getAssociatedEdge().target());
            }
            MyNode tmp = ptr;
            ptr = ptr.next;
            ptr.prev = tmp;
            if(curDart.isReversed()){
                if (v1Node.node.degree() > curDart.getAssociatedEdge().source().degree()) {
                    v1Node = ptr;
                }
            } else {
                if (v1Node.node.degree() > curDart.getAssociatedEdge().target().degree()) {
                    v1Node = ptr;
                }
            }

        }
        // connect head and tail
        ptr.next = list;
        list.prev = ptr;

        // check v1 for more than 2 face neighbors
        // first mark v1's neighbors
        for (INodeCursor nc = v1Node.node.getNeighborCursor(); nc.ok(); nc.next()) {
            nmap.setBool(nc.node(), true);
        }

        // second, check face nodes
        boolean hasMoreThan2FaceNeighbors = false;

        // v1's potential neighbor which is not v2 or vk
        MyNode vjNode = null;
        for (MyNode cur = v1Node.next.next; cur != v1Node.prev; cur = cur.next) {
            if (nmap.getBool(cur.node)) {
                hasMoreThan2FaceNeighbors = true;
                vjNode = cur;
                break;
            }
        }

        if (hasMoreThan2FaceNeighbors) {
            // case 1: minNode has more than two neighbor face vertices

            // create new edges (vj+1,v3),(vj+1,v4),...,(vj+1,vj-1)
            for (MyNode cur = v1Node.next.next; cur != vjNode; cur = cur.next) {
                Edge newEdge = graph.createEdge(vjNode.next.node, cur.node);
                insertedEdges.add(newEdge);
            }
            // create new edges (v2,vj+1),(v2,vj+2),...,(v2,vk)
            for (MyNode cur = vjNode.next; cur != v1Node; cur = cur.next) {
                Edge newEdge = graph.createEdge(v1Node.next.node, cur.node);
                insertedEdges.add(newEdge);
            }
        } else {
            // case 2: minNode has only two neighbor face vertices
            // create new edges (v1,v2),(v1,v3),....,(v1,vk)
            for (MyNode cur = v1Node.next.next; cur != v1Node.prev; cur = cur.next) {
                Edge newEdge = graph.createEdge(v1Node.node, cur.node);
                insertedEdges.add(newEdge);

            }
        }

        // free resources
        graph.disposeNodeMap(nmap);

    }

    /**
     * Helper class. Represents a simple double linked list element
     *
     * @author Philemon Schucker
     *
     */
    protected static class MyNode {

        protected MyNode prev;
        protected MyNode next;
        protected Node node;

        protected MyNode(Node node) {
            this.node = node;
        }

    }
}

