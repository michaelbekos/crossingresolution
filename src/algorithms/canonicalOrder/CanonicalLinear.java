package algorithms.canonicalOrder;

import algorithms.fpp.EdgeComparator;
import com.yworks.yfiles.algorithms.*;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.layout.YGraphAdapter;

import java.util.*;


/**
 * Created by Ama on 16.12.2017.
 */
public class CanonicalLinear {

    public IGraph graph;
    private YGraphAdapter graphAdapter;
    private Graph yGraph;
  //  private PlanarInformation planarInformation;
    private PlanarEmbedding planarEmbedding;
    private Node v1;
    private Node v2;
    //private Face v1v2Face;
    private List<Dart> v1v2Face;
    private ArrayList<ArrayList<Node>> canonicalOrder;

    // sets
    private HashSet<Node> outerFace; // set of nodes in the current outerface
    private HashSet<Node> possibleNextNodes;
    private HashSet<List<Dart>> possibleNextFaces;

    // maps
    private INodeMap isInsertedNode;
    private Map<List<Dart>, Boolean> isInsertedFace;

    private Map<List<Dart>, Integer> outE; // number of Edges in the current outerFace
    private Map<List<Dart>, Integer> outV; // number of Nodes in the current outerFace
    private Map<List<Dart>, Boolean> isSeparating;
    private INodeMap separated;
    private INodeMap visited;

    public CanonicalLinear(IGraph graph) {
        this.graph = graph;
        this.graphAdapter = new  YGraphAdapter(graph);
        this.yGraph = graphAdapter.getYGraph();
        init();
        calcOrder();

    }

    /**
     * the main part of the algorithm
     */
    private void calcOrder() {
        preparations();
        // removeNode(v2);

        while (!possibleNextFaces.isEmpty() | !possibleNextNodes.isEmpty()) {

            if (!possibleNextNodes.isEmpty()) {
                Node node = possibleNextNodes.iterator().next();
                removeNode(node);

            }
            if (!possibleNextFaces.isEmpty()) {
                List<Dart> f = possibleNextFaces.iterator().next();
                removeFace(f);
            }
        }

        // only v1v2 face remains
        addv1v2FaceToCanonicalOrder();

      //  removeReversedEdges(); //TODO : nötig?
        //System.out.println(canonicalOrder.toString());

    }

    /**
     * determine v1, v2, vn and the v1v2-face
     */
    private void preparations() {
        //EdgeCursor ec = planarInformation.getOuterFace().edges();
        List<Dart> ld = planarEmbedding.getOuterFace();
        v1 = ld.get(0).getAssociatedEdge().source();
        //v1 =  ec.edge().source();
        //ec.next();
        Node vn = ld.get(1).getAssociatedEdge().source();
        //Node vn = ec.edge().source();
        //ec.toLast();
        v2 = ld.get(ld.size() - 1).getAssociatedEdge().source();
        //v2 = ec.edge().source();

        possibleNextNodes.add(vn);

        Edge e = v1.getEdgeTo(v2);
        v1v2Face = planarEmbedding.getDarts(e)[0].getFace();
        //v1v2Face = planarInformation.faceOf(e);

        // System.out.println("v1:" + v1.toString() + " v2:" + v2.toString()
        // + " vn:" + vn.toString() + " v1v2Face:" + v1v2Face.toString());

    }

    /**
     * init planarInformation, sets and maps and update their entries
     */
    private void init() {
       // this.planarInformation = new PlanarInformation(graph);
        this.planarEmbedding = new PlanarEmbedding(yGraph);
      //  addReversedEdges(); //TODO : nötig?
        sortEdges();
        //planarInformation.calcFaces(); //TODO: Ist das notwendig?
        Utilities.setOuterFace(planarEmbedding, graph);

        canonicalOrder = new ArrayList<>();

        initMaps();
        initSets();
    }

    private void initSets() {
        outerFace = new HashSet<>();
        possibleNextNodes = new HashSet<>();
        possibleNextFaces = new HashSet<>();

        for (Dart dart : planarEmbedding.getOuterFace()) {
            //Edge edge = planarInformation.getReverse(ec.edge());
            Edge edge = dart.getOppositeDart().getAssociatedEdge();
            Node node = edge.source();
            outerFace.add(node);
        }
    }

    private void initMaps() {
        separated = yGraph.createNodeMap();
        visited = yGraph.createNodeMap();
       // outE = planarInformation.createFaceMap();
       // outV = planarInformation.createFaceMap();
        outE = new HashMap<List<Dart>, Integer>();
        outV = new HashMap<List<Dart>, Integer>();
        isInsertedNode = yGraph.createNodeMap();
       // isInsertedFace = planarInformation.createFaceMap();
       // isSeparating = planarInformation.createFaceMap();
        isInsertedNode = yGraph.createNodeMap();
        isInsertedFace = new HashMap<List<Dart>, Boolean>();
        for (Node node : yGraph.getNodes()) {
            separated.setInt(node, 0);
            visited.setInt(node, 0);
            isInsertedNode.setBool(node, false);
        }

        for (List<Dart> face : planarEmbedding.getFaces()) {
            outE.put(face, 0);
            outV.put(face, 0);
            isInsertedFace.put(face, false);
            isSeparating.put(face, false);
        }
        // outer face = inserted
        isInsertedFace.put(planarEmbedding.getOuterFace(), true);

        // update face variables in dependency of the outerface
        for (Dart dart : planarEmbedding.getOuterFace()) {
            Edge innerEdge = dart.getOppositeDart().getAssociatedEdge();
            Node node = innerEdge.source();
            increaseOutE(innerEdge);
            increaseOutV(node);
        }
    }

    /**
     * outV +1 for every Face adjacent to the given node.
     *
     * @param node
     */
    private void increaseOutV(Node node) {
        for (Edge edge : node.getOutEdges()) {
            List<Dart> face = planarEmbedding.getDarts(edge)[0].getFace();
            outV.put(face, outV.get(face) + 1);
        }
    }

    /**
     * outE +1 for the Face adjacent to the given edge.
     *
     * @param edge
     */
    private void increaseOutE(Edge edge) {
        List<Dart> face = planarEmbedding.getDarts(edge)[0].getFace();
        outE.put(face, outE.get(face) + 1);
    }

    /**
     * sort edges, necessary to calculate faces in planarInformation
     */
    private void sortEdges() {
        for (Node node : yGraph.getNodes()) {
            IEdge iEdge = graphAdapter.getOriginalEdge(node.getOutEdgeCursor().edge());
           // node.sortOutEdges(new EdgeComparator(iEdge,graph));  //TODO: EdgeComparator

        }
    }

    /**
     * delete Node an his adjacent faces. Update new outer nodes and their faces
     *
     * @param deletedNode
     */
    private void removeNode(Node deletedNode) {
        // chain from left to the right neighbour
        ArrayList<Node> newOuterChain = calcNewOuterChain(deletedNode);

        // remove node and faces
        outerFace.remove(deletedNode);
        possibleNextNodes.remove(deletedNode);
        isInsertedNode.setBool(deletedNode, true);
        for (Edge edge : deletedNode.getOutEdges()) {
            List<Dart> face = planarEmbedding.getDarts(edge)[0].getFace();
            isInsertedFace.put(face, true);
            possibleNextFaces.remove(face);
        }

        updateVisited(deletedNode);

        // update outerFace set!
        for (int i = 1; i < newOuterChain.size() - 1; i++) {
            outerFace.add(newOuterChain.get(i));
        }

        // update outE, outV and separated
        updateFaceVars(newOuterChain);
        updateSeparated(newOuterChain);

        // update possible next face-/node sets
        updateSets(newOuterChain);

        addNodeToCanonicalOrder(deletedNode);

    }

    /**
     * delete Face and add chain to canonical order. Updates outerFace. Updates
     * node-/ and facevars.
     *
     * @param deleteFace
     */
    private void removeFace(List<Dart> deleteFace) {

        // begins with first of deleteChain
        ArrayList<Node> faceNodes = sortedNodesFromFace2(deleteFace);
        possibleNextFaces.remove(deleteFace);
        isInsertedFace.put(deleteFace, true);

        ArrayList<Node> newOuterChain = new ArrayList<>();
        ArrayList<Node> deleteChain = new ArrayList<>();

        int deleteLength = outV.get(deleteFace) - 2;
        for (int i = 0; i < deleteLength; i++) {
            Node n = faceNodes.remove(0);
            deleteChain.add(n);
            outerFace.remove(n);
            possibleNextNodes.remove(n);
            isInsertedNode.setBool(n, true);
            updateVisited(n);
        }

        // System.out.println("deleteChain: " + deleteChain);
        // now deleteChain in counter clockwise order
        Collections.reverse(faceNodes);
        newOuterChain = faceNodes;

        // update outerFace set!
        for (int i = 1; i < newOuterChain.size() - 1; i++) {
            outerFace.add(newOuterChain.get(i));
        }

        // update outE, outV and separated
        updateFaceVars(newOuterChain);
        updateSeparated(newOuterChain);

        // update possible next face-/node sets
        updateSets(newOuterChain);

        addChainToCanonicalOrder(deleteChain);

    }

    private void updateVisited(Node node) {
        // visitet+=1 for all neighboues of node
        for (Edge edge : node.getOutEdges()) {
            Node n = edge.target();
            visited.setInt(n, visited.getInt(n) + 1);
        }
    }

    /**
     * computes list of nodes, beginning with the first of the delete-chain. in
     * clockwise Order!
     *
     * @param f
     * @return ArrayList of nodes
     */
    private ArrayList<Node> sortedNodesFromFace2(List<Dart> f) {
        ArrayList<Node> sortNodes = new ArrayList<>();
        ArrayList<Node> faceNodes = nodesFromFace(f);
        Collections.reverse(faceNodes); // now in clockwise order!
        int index = 0;
        Node lastNode = faceNodes.get(faceNodes.size() - 1);
        for (Node n : faceNodes) {
            if (DegreeOf(lastNode) > 2 && DegreeOf(n) == 2) {
                index = faceNodes.indexOf(n);
                break;
            }
            lastNode = n;
        }
        for (int i = index; i < faceNodes.size(); i++) {
            sortNodes.add(faceNodes.get(i));
        }
        for (int i = 0; i < index; i++) {
            sortNodes.add(faceNodes.get(i));
        }

        return sortNodes; // clockwise order
    }

    private ArrayList<Node> nodesFromFace(List<Dart> f) {

        ArrayList<Node> nodes = new ArrayList<>();

        for (Dart d : f) {
            Edge edge = d.getAssociatedEdge();
            Node node = edge.target();
            nodes.add(node);
        }

        return nodes; // counter-clockwise
    }

    private int DegreeOf(Node node) {
        int counter = 0;
        for (Edge edge : node.getOutEdges()) {
            Node n = edge.target();
            if (!isInsertedNode.getBool(n))
                counter++;
        }

        return counter;
    }

    private void updateSets(ArrayList<Node> newOuterChain) {

        // update possible next nodes
        for (int k = 0; k < newOuterChain.size(); k++) {
            Node n = newOuterChain.get(k);
            for (List<Dart> f : facesOfNode(n)) {
                for (Node n2 : nodesFromFace(f)) {
                    if (separated.getInt(n2) == 0 && visited.getInt(n2) > 0
                            && n2 != v1 && n2 != v2)
                        possibleNextNodes.add(n2);
                    else
                        possibleNextNodes.remove(n2);
                }
            }

        }

        // update possible next faces

        Node prevNode = newOuterChain.get(0);
        for (int j = 1; j < newOuterChain.size(); j++) {
            Edge e = newOuterChain.get(j).getEdgeTo(prevNode);
            List<Dart> f = planarEmbedding.getDarts(e)[0].getFace();
            if (outV.get(f) == outE.get(f) + 1 && outE.get(f) >= 2
                    && f != v1v2Face)
                possibleNextFaces.add(f);
            else {
                possibleNextFaces.remove(f);
            }

            prevNode = newOuterChain.get(j);
        }

    }

    /**
     * update outE, outV
     *
     * @param newOuterChain
     *            now uncovered chain of nodes after deleting a face or node
     */
    private void updateFaceVars(ArrayList<Node> newOuterChain) {

        Node prevNode = newOuterChain.get(0);
        for (int i = 1; i < newOuterChain.size(); i++) {
            Edge e = newOuterChain.get(i).getEdgeTo(prevNode);

            List<Dart> f = planarEmbedding.getDarts(e)[0].getFace();

            outE.put(f, outE.get(f) + 1);// outE++
            prevNode = newOuterChain.get(i);
        }

        for (int i = 1; i < newOuterChain.size() - 1; i++) {
            for (List<Dart> f : facesOfNode(newOuterChain.get(i))) {
                outV.put(f, outV.get(f) + 1);// outV++
            }

        }

    }

    /**
     * update separated for every node of every face adjacent to the new outer
     * Chain
     *
     * @param newOuterChain
     */
    private void updateSeparated(ArrayList<Node> newOuterChain) {

        for (int i = 0; i < newOuterChain.size(); i++) {
            for (List<Dart> f : facesOfNode(newOuterChain.get(i)))
                for (Node n : nodesFromFace(f)) {
                    separated.setInt(n, 0);
                    for (List<Dart> innerFace : facesOfNode(n)) {
                        if (outV.get(innerFace) >= 3)
                            separated.setInt(n, 1);
                        if (outV.get(innerFace) == 2
                                && outE.get(innerFace) == 0)
                            separated.setInt(n, 1);
                    }

                }
        }
    }

    private List<List<Dart>> facesOfNode(Node node) {
        List<List<Dart>> faces = new ArrayList<>();

        for (Edge edge : node.getOutEdges()) {
            List<Dart> f = planarEmbedding.getDarts(edge)[0].getFace();
            if (!isInsertedFace.get(f))
                faces.add(f);
        }

        return faces;

    }

    /**
     * computes the chain which is new in the outer face after deleting the
     * given node.
     *
     * @param parentNode
     * @return
     */
    private ArrayList<Node> calcNewOuterChain(Node parentNode) {
        Node firstNode = leftOuterNode(parentNode);
        List<Dart> firstFace = planarEmbedding.getDarts(parentNode.getEdgeTo(firstNode))[0].getFace();
        ArrayList<Node> outerChain = new ArrayList<>();
        outerChain.add(firstNode);

        ArrayList<List<Dart>> faces = sortedFaces(parentNode, firstFace);
        for (List<Dart> f : faces) {
            // ervery Node of every face
            ArrayList<Node> nodes = sortedNodesFromFace(f, parentNode);
            for (int i = 2; i < nodes.size(); i++) {
                outerChain.add(nodes.get(i));
            }

        }

        // System.out.println(outerChain.toString());
        return outerChain;
    }

    /**
     * adjacent faces of a given node in counter clockwise order, beginning with
     * the given first face
     *
     * @param node
     * @param firstFace
     * @return
     */
    private ArrayList<List<Dart>> sortedFaces(Node node, List<Dart> firstFace) {
        Boolean startAddingToList = false;
        ArrayList<List<Dart>> faces = new ArrayList<>();

        for (Edge edge : node.getOutEdges()) {
            List<Dart> f = planarEmbedding.getDarts(edge)[0].getFace();

            if (!isInsertedFace.get(f) && startAddingToList) {
                faces.add(f);
            }

            if (f == firstFace) // found first face
                startAddingToList = true;
        }

        for (Edge edge : node.getOutEdges()) {
            List<Dart> f = planarEmbedding.getDarts(edge)[0].getFace();

            if (!isInsertedFace.get(f)) {
                faces.add(f);
            }
            if (f == firstFace) // found first face
                break;

        }

        Collections.reverse(faces); // counter-clockwise order
        return faces;
    }

    /**
     * determin the leftmost neigbour of the node in the current outer face.
     *
     * @param node
     * @return
     */
    private Node leftOuterNode(Node node) {
        Node retNode = null;
        Node last = null;
        IEdgeCursor ec = node.getEdgeCursor();

        while (ec.ok()) {// ignore inserted nodes
            retNode = ec.edge().target();
            if (!isInsertedNode.getBool(retNode))
                break;
            ec.next();
        }

        if (outerFace.contains(retNode)) {
            last = retNode;
            while (ec.ok()) {// ignore inserted nodes
                ec.next();
                retNode = ec.edge().target();
                if (!isInsertedNode.getBool(retNode))
                    break;
            }
            if (outerFace.contains(retNode)) {
                return last;
            } else {
                while (ec.ok()) {// search next outer node
                    ec.next();
                    retNode = ec.edge().target();
                    if (!isInsertedNode.getBool(retNode)
                            && outerFace.contains(retNode)) {
                        break;
                    }
                }
            }

        } else {
            while (ec.ok()) {// search next outer node
                ec.next();
                retNode = ec.edge().target();
                if (!isInsertedNode.getBool(retNode)
                        && outerFace.contains(retNode)) {
                    break;
                }
            }
        }

        return retNode;
    }

    /**
     * list of face nodes. counter-clockwise order with parentNode as the first;
     *
     * @param f
     * @param parentNode
     * @return
     */
    private ArrayList<Node> sortedNodesFromFace(List<Dart> f, Node parentNode) {
        Boolean startAddingToList = false;
        ArrayList<Node> nodes = new ArrayList<>();
        for (Dart dart : f) {

            Edge edge = dart.getAssociatedEdge();
            Node node = edge.target();

            if (node == parentNode)
                startAddingToList = true;
            if (startAddingToList)
                nodes.add(node);
        }
        for (Dart dart : f) {

            Edge edge = dart.getAssociatedEdge();
            Node node = edge.target();
            if (node == parentNode)
                break;

            nodes.add(node);

        }
        return nodes;
    }

    /**
     * add reversed edges, necessary to calculate faces in planarInformation
     */
 /*   private void addReversedEdges() {
        for (IEdge ed : graph.getEdges()) {
            this.planarEmbedding.
            this.planarInformation.createReverse(ed);
        }
    }
*/
 /*
    private void removeReversedEdges() {
        for (IEdge ed : graph.getEdges()) {
            if (planarInformation.isInsertedEdge(ed)) {
                graph.remove(ed);           //TODO: evtl aus yGraph noch entfehrnen und Adapter anpassen
                //graph.removeEdge(ed);
            }
        }
    }
*/
    private void addNodeToCanonicalOrder(Node n) {
        ArrayList<Node> nodeAsList = new ArrayList<>();
        nodeAsList.add(n);
        canonicalOrder.add(0, nodeAsList);
    }

    private void addChainToCanonicalOrder(ArrayList<Node> nodes) {
        canonicalOrder.add(0, nodes);

    }

    private void addv1v2FaceToCanonicalOrder() {
        ArrayList<Node> lastFace = sortedNodesFromFace(v1v2Face, v2);
        Collections.reverse(lastFace);
        // now v1,.....,v2
        lastFace.remove(0);
        lastFace.remove(lastFace.size() - 1);

        addChainToCanonicalOrder(lastFace);
        addNodeToCanonicalOrder(v2);
        addNodeToCanonicalOrder(v1);
    }

    public ArrayList<ArrayList<Node>> getCanonicalOrder() {
        return canonicalOrder;
    }

}
