package algorithms.canonicalOrder;


import com.yworks.yfiles.algorithms.*;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.layout.YGraphAdapter;

import java.util.*;

/**
 * Created by Ama on 16.12.2017.
 */
public class CanonicalOrder {


    public IGraph graph;
    private Graph yGraph;
    private YGraphAdapter graphAdapter;
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

    // saves the order of added elements in possibleNextNodes and possibleNextFaces to obtain determinism
    private LinkedList<Node> possibleNextNodes2;
    private LinkedList<List<Dart>> possibleNextFaces2;

    // maps
    private INodeMap isInsertedNode;
    private Map<List<Dart>,Boolean> isInsertedFace;

    private Map<List<Dart>,Integer> outE; // number of Edges in the current outerFace
    private Map<List<Dart>,Integer> outV; // number of Nodes in the current outerFace
    private Map<List<Dart>,Boolean> isSeparating;
    private INodeMap separated;
    private INodeMap visited;


    private boolean random = false;

    /**
     * Instantiating this class calculates a Canonical Order for graph g with corresponding planar information p in
     * linear time. Note that graph g have to be already embedded. Embedding is stored in planar information p.
     *
     * @param g
     *            graph to get the Canonical Order for
     * @param p
     *            corresponding planar information
     */
    public CanonicalOrder(IGraph g, PlanarEmbedding p, boolean random) {
        this.graph = g;
        this.graphAdapter = new YGraphAdapter(g);
        this.yGraph = graphAdapter.getYGraph();
        this.random = random;
        planarEmbedding = p;
        init();
        calcOrder();

    }

    /**
     * Calculates the canonical order.
     */
    private void calcOrder() {
        preparations();
        while (!possibleNextFaces.isEmpty() | !possibleNextNodes.isEmpty()) {

            if (!possibleNextNodes.isEmpty()) {
                Node n;
                if (!random) { // if not random, take the next node out of the list
                    n = possibleNextNodes2.poll();
                    while (!possibleNextNodes.contains(n))
                        n = possibleNextNodes2.poll();
                } else
                    // if random, take the node from the hashset iterator, which behaves randomly
                    n = possibleNextNodes.iterator().next();
                // System.out.println(n);
                removeNode(n);
            }
            if (!possibleNextFaces.isEmpty()) {
                List<Dart> f;
                if (!random) {
                    f = possibleNextFaces2.poll();
                    while (!possibleNextFaces.contains(f))
                        f = possibleNextFaces2.poll();
                } else
                    f = possibleNextFaces.iterator().next();
                // System.out.println(f);
                removeFace(f);
            }
        }

        // only v1v2 face remains
        addv1v2FaceToCanonicalOrder();

        // removeReversedEdges();
        // System.out.println(canonicalOrder.toString());
    }

    /**
     * determine v1, v2, vn and the v1v2-face
     */
    private void preparations() {
        List<Dart> outerFace = planarEmbedding.getOuterFace();
        //EdgeCursor ec = planarInformation.getOuterFace().edges();
        v1 = outerFace.get(0).getAssociatedEdge().source();
        //v1 = ec.edge().source();
        //ec.next();
        Node vn = outerFace.get(1).getAssociatedEdge().source();;
        //ec.toLast();
        v2 = outerFace.get(outerFace.size()-1).getAssociatedEdge().source();;
        // System.out.println("v1: " + v1 + " " + v1.degree() + " v2: " + v2 + " " + v2.degree() + " vn: " + vn + " "
        // + vn.degree());
        if (v2.degree() > v1.degree()) {
            if (v2.degree() > vn.degree()) {
                Node tmp;
                tmp = v1;
                v1 = vn;
                vn = v2;
                v2 = tmp;
            }
        } else if (v1.degree() > vn.degree()) {
            Node tmp;
            tmp = v2;
            v2 = vn;
            vn = v1;
            v1 = tmp;

        }
        // System.out.println("v1: " + v1 + " " + v1.degree() + " v2: " + v2 + " " + v2.degree() + " vn: " + vn + " "
        // + vn.degree());
        possibleNextNodes.add(vn);
        possibleNextNodes2.add(vn);

        Edge e = v1.getEdgeTo(v2);
        v1v2Face = planarEmbedding.getDarts(e)[0].getFace(); // TODO: NULLPOINT FACE
       // v1v2Face = planarInformation.faceOf(e);
    }

    /**
     * init planarInformation, sets and maps and update their entries
     */
    private void init() {
        canonicalOrder = new ArrayList<>();
        initMaps();
        initSets();
    }

    private void initSets() {
        outerFace = new HashSet<>();
        possibleNextNodes = new HashSet<>();
        possibleNextFaces = new HashSet<>();
        possibleNextNodes2 = new LinkedList<>();
        possibleNextFaces2 = new LinkedList<>();

        Iterator<Dart> id = planarEmbedding.getOuterFace().iterator();
        while(id.hasNext()){
            Edge edge = id.next().getAssociatedEdge();
            Node node = edge.source();
            outerFace.add(node);
        }
  /*      for (EdgeCursor ec = planarInformation.getOuterFace().edges(); ec.ok(); ec.next()) {
            Edge edge = planarInformation.getReverse(ec.edge());
            Node node = edge.source();
            outerFace.add(node);
        }
  */
    }

    private void initMaps() {

        separated = yGraph.createNodeMap();
        visited = yGraph.createNodeMap();
        outE = new HashMap<List<Dart>, Integer>();
        outV = new HashMap<List<Dart>, Integer>();
        //  outE = planarInformation.createFaceMap();
        //  outV = planarInformation.createFaceMap();
        isInsertedNode = yGraph.createNodeMap();
        isInsertedFace = new HashMap<List<Dart>, Boolean>();
        isSeparating = new HashMap<List<Dart>, Boolean>();
        //isInsertedFace = planarInformation.createFaceMap();
        //isSeparating = planarInformation.createFaceMap();

        for (Node node : yGraph.getNodes()) {
            separated.setInt(node, 0);
            visited.setInt(node, 0);
            isInsertedNode.setBool(node, false);
        }

        for (List<Dart> f : planarEmbedding.getFaces()) {
            outE.put(f, 0);
            outV.put(f, 0);
            isInsertedFace.put(f, false);
            isSeparating.put(f, false);

        }
        // outer face = inserted
        isInsertedFace.put(planarEmbedding.getOuterFace(), true);
        // update face variables in dependency of the outerface
        for (Dart d : planarEmbedding.getOuterFace()) {
            Edge innerEdge = d.getAssociatedEdge();
            Node node = innerEdge.source();
            increaseOutE(innerEdge);
            increaseOutV(node);
        }
    }

    /**
     * outE +1 for every Face adjacent to the given node.
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
     * outV +1 for the Face adjacent to the given edge.
     *
     * @param edge
     */
    private void increaseOutE(Edge edge) {
        List<Dart> face = planarEmbedding.getDarts(edge)[0].getFace();
        outE.put(face, outE.get(face) + 1);
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
     * delete Face and add chain to canonical order. Updates outerFace. Updates node-/ and facevars.
     *
     * @param deleteFace
     */
    private void removeFace(List<Dart> deleteFace) {

        // begins with first of deleteChain
        ArrayList<Node> faceNodes = sortedNodesFromFace2(deleteFace);
        possibleNextFaces.remove(deleteFace);
        isInsertedFace.put(deleteFace,true);

        ArrayList<Node> newOuterChain = new ArrayList<>();
        ArrayList<Node> deleteChain = new ArrayList<>();

        int deleteLength = outV.get(deleteFace) - 2 ;
        for (int i = 0; i < deleteLength; i++) {
            Node n = faceNodes.remove(0);
            deleteChain.add(n);
            outerFace.remove(n);
            possibleNextNodes.remove(n);
            isInsertedNode.setBool(n, true);
            updateVisited(n);
        }

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
     * computes list of nodes, beginning with the first of the delete-chain. in clockwise Order!
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
        Iterator<Dart> di = f.iterator();
        while (di.hasNext()) {
            Edge edge = di.next().getAssociatedEdge();
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
                    if (separated.getInt(n2) == 0 && visited.getInt(n2) > 0 && n2 != v1 && n2 != v2) {
                        possibleNextNodes.add(n2);
                        possibleNextNodes2.add(n2);
                    } else
                        possibleNextNodes.remove(n2);
                }
            }
        }

        // update possible next faces
        Node prevNode = newOuterChain.get(0);
        for (int j = 1; j < newOuterChain.size(); j++) {
            Edge e = newOuterChain.get(j).getEdgeTo(prevNode);
            List<Dart> f = planarEmbedding.getDarts(e)[0].getFace();;
            if (outV.get(f) == outE.get(f) + 1 && outE.get(f) >= 2 && f != v1v2Face) {
                possibleNextFaces.add(f);
                possibleNextFaces2.add(f);
            } else {
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

            List<Dart> f = planarEmbedding.getDarts(e)[0].getFace();;

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
     * update separated for every node of every face adjacent to the new outer Chain
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
                        if (outV.get(innerFace) == 2 && outE.get(innerFace) == 0)
                            separated.setInt(n, 1);
                    }
                }
        }
    }

    private ArrayList<List<Dart>> facesOfNode(Node node) {
        ArrayList<List<Dart>> faces = new ArrayList<>();

        for (Edge edge : node.getOutEdges()) {
            List<Dart> f = planarEmbedding.getDarts(edge)[0].getFace();
            if (!isInsertedFace.get(f))
                faces.add(f);
        }
        return faces;
    }

    /**
     * computes the chain which is new in the outer face after deleting the given node.
     *
     * @param parentNode
     * @return
     */
    private ArrayList<Node> calcNewOuterChain(Node parentNode) {
        Node firstNode = leftOuterNode(parentNode);
        List<Dart> firstFace =  planarEmbedding.getDarts(parentNode.getEdge(firstNode))[0].getFace();
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
     * adjacent faces of a given node in counter clockwise order, beginning with the given first face
     *
     * @param node
     * @param firstFace
     * @return
     */
    private ArrayList<List<Dart>> sortedFaces(Node node, List<Dart> firstFace) {
        Boolean startAddingToList = false;
        ArrayList<List<Dart>> faces = new ArrayList<>();

        for (Edge edge : node.getOutEdges()) {
            List<Dart> f = planarEmbedding.getDarts(edge)[0].getFace();;;

            if (!isInsertedFace.get(f) && startAddingToList) {
                faces.add(f);
            }

            if (f == firstFace) // found first face
                startAddingToList = true;
        }

        for (Edge edge : node.getOutEdges()) {
            List<Dart> f = planarEmbedding.getDarts(edge)[0].getFace();;;

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
                    if (!isInsertedNode.getBool(retNode) && outerFace.contains(retNode)) {
                        break;
                    }
                }
            }

        } else {
            while (ec.ok()) {// search next outer node
                ec.next();
                retNode = ec.edge().target();
                if (!isInsertedNode.getBool(retNode) && outerFace.contains(retNode)) {
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
        for (Dart d : f) {

            Edge edge = d.getAssociatedEdge();
            Node node = edge.target();

            if (node == parentNode)
                startAddingToList = true;
            if (startAddingToList)
                nodes.add(node);
        }
        for (Dart d : f) {

            Edge edge = d.getAssociatedEdge();
            Node node = edge.target();
            if (node == parentNode)
                break;

            nodes.add(node);

        }
        return nodes;
    }

/*    private void removeReversedEdges() {
        for (Edge ed : yGraph.getEdges()) {
            if (planarInformation.isInsertedEdge(ed))
                yGraph.removeEdge(ed);
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


