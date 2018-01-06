package algorithms.canonicalOrder;
ackage algorithms.canonicalOrder;

        import algorithms.fpp.EdgeComparator;
        import y.base.*;
        import y.layout.planar.Face;
        import y.layout.planar.FaceCursor;
        import y.layout.planar.FaceMap;
        import y.layout.planar.PlanarInformation;
        import y.view.Graph2D;

        import java.util.ArrayList;
        import java.util.Collections;
        import java.util.HashSet;

/**
 * Calculates a canoncial order of a given graph.
 *
 * @author Original: Findan Eisenhut, Updated + Cleaned up: Philemon Schucker
 *
 */
public class CanonicalLinear {

    public Graph2D graph;
    private PlanarInformation planarInformation;
    private Node v1;
    private Node v2;
    private Face v1v2Face;
    private ArrayList<ArrayList<Node>> canonicalOrder;

    // sets
    private HashSet<Node> outerFace; // set of nodes in the current outerface
    private HashSet<Node> possibleNextNodes;
    private HashSet<Face> possibleNextFaces;

    // maps
    private NodeMap isInsertedNode;
    private FaceMap isInsertedFace;

    private FaceMap outE; // number of Edges in the current outerFace
    private FaceMap outV; // number of Nodes in the current outerFace
    private FaceMap isSeparating;
    private NodeMap separated;
    private NodeMap visited;

    public CanonicalLinear(Graph2D g) {
        this.graph = g;
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
                Node n = possibleNextNodes.iterator().next();
                removeNode(n);

            }
            if (!possibleNextFaces.isEmpty()) {
                Face f = possibleNextFaces.iterator().next();
                removeFace(f);
            }
        }

        // only v1v2 face remains
        addv1v2FaceToCanonicalOrder();

        removeReversedEdges();
        //System.out.println(canonicalOrder.toString());

    }

    /**
     * determine v1, v2, vn and the v1v2-face
     */
    private void preparations() {
        EdgeCursor ec = planarInformation.getOuterFace().edges();

        v1 = ec.edge().source();
        ec.next();
        Node vn = ec.edge().source();
        ec.toLast();
        v2 = ec.edge().source();

        possibleNextNodes.add(vn);

        Edge e = v1.getEdgeTo(v2);
        v1v2Face = planarInformation.faceOf(e);

        // System.out.println("v1:" + v1.toString() + " v2:" + v2.toString()
        // + " vn:" + vn.toString() + " v1v2Face:" + v1v2Face.toString());

    }

    /**
     * init planarInformation, sets and maps and update their entries
     */
    private void init() {
        this.planarInformation = new PlanarInformation(graph);
        addReversedEdges();
        sortEdges();
        planarInformation.calcFaces();
        Utilities.setOuterFace(planarInformation, graph);

        canonicalOrder = new ArrayList<>();

        initMaps();
        initSets();
    }

    private void initSets() {
        outerFace = new HashSet<>();
        possibleNextNodes = new HashSet<>();
        possibleNextFaces = new HashSet<>();

        for (EdgeCursor ec = planarInformation.getOuterFace().edges(); ec.ok(); ec
                .next()) {
            Edge edge = planarInformation.getReverse(ec.edge());
            Node node = edge.source();
            outerFace.add(node);
        }
    }

    private void initMaps() {
        separated = graph.createNodeMap();
        visited = graph.createNodeMap();
        outE = planarInformation.createFaceMap();
        outV = planarInformation.createFaceMap();
        isInsertedNode = graph.createNodeMap();
        isInsertedFace = planarInformation.createFaceMap();
        isSeparating = planarInformation.createFaceMap();

        for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
            separated.setInt(nc.node(), 0);
            visited.setInt(nc.node(), 0);
            isInsertedNode.setBool(nc.node(), false);
        }

        for (FaceCursor fc = planarInformation.faces(); fc.ok(); fc.next()) {
            outE.setInt(fc.face(), 0);
            outV.setInt(fc.face(), 0);
            isInsertedFace.setBool(fc.face(), false);
            isSeparating.setBool(fc.face(), false);
        }
        // outer face = inserted
        isInsertedFace.setBool(planarInformation.getOuterFace(), true);

        // update face variables in dependency of the outerface
        for (EdgeCursor ec = planarInformation.getOuterFace().edges(); ec.ok(); ec
                .next()) {
            Edge innerEdge = planarInformation.getReverse(ec.edge());
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
        for (EdgeCursor ec = node.outEdges(); ec.ok(); ec.next()) {
            Face face = planarInformation.faceOf(ec.edge());
            outV.setInt(face, outV.getInt(face) + 1);
        }
    }

    /**
     * outE +1 for the Face adjacent to the given edge.
     *
     * @param edge
     */
    private void increaseOutE(Edge edge) {
        Face face = planarInformation.faceOf(edge);
        outE.setInt(face, outE.getInt(face) + 1);
    }

    /**
     * sort edges, necessary to calculate faces in planarInformation
     */
    private void sortEdges() {
        for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
            nc.node().sortOutEdges(
                    new EdgeComparator(nc.node().firstOutEdge(), graph));
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
        for (EdgeCursor ec = deletedNode.outEdges(); ec.ok(); ec.next()) {
            Face face = planarInformation.faceOf(ec.edge());
            isInsertedFace.setBool(face, true);
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
    private void removeFace(Face deleteFace) {

        // begins with first of deleteChain
        ArrayList<Node> faceNodes = sortedNodesFromFace2(deleteFace);
        possibleNextFaces.remove(deleteFace);
        isInsertedFace.setBool(deleteFace, true);

        ArrayList<Node> newOuterChain = new ArrayList<>();
        ArrayList<Node> deleteChain = new ArrayList<>();

        int deleteLength = outV.getInt(deleteFace) - 2;
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
        for (EdgeCursor ec = node.outEdges(); ec.ok(); ec.next()) {
            Node n = ec.edge().target();
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
    private ArrayList<Node> sortedNodesFromFace2(Face f) {
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

    private ArrayList<Node> nodesFromFace(Face f) {

        ArrayList<Node> nodes = new ArrayList<>();

        for (EdgeCursor ec = f.edges(); ec.ok(); ec.next()) {
            Edge edge = ec.edge();
            Node node = edge.target();
            nodes.add(node);
        }

        return nodes; // counter-clockwise
    }

    private int DegreeOf(Node node) {
        int counter = 0;
        for (EdgeCursor ec = node.outEdges(); ec.ok(); ec.next()) {
            Node n = ec.edge().target();
            if (!isInsertedNode.getBool(n))
                counter++;
        }

        return counter;
    }

    private void updateSets(ArrayList<Node> newOuterChain) {

        // update possible next nodes
        for (int k = 0; k < newOuterChain.size(); k++) {
            Node n = newOuterChain.get(k);
            for (Face f : facesOfNode(n)) {
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
            Face f = planarInformation.faceOf(e);
            if (outV.getInt(f) == outE.getInt(f) + 1 && outE.getInt(f) >= 2
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

            Face f = planarInformation.faceOf(e);

            outE.setInt(f, outE.getInt(f) + 1);// outE++
            prevNode = newOuterChain.get(i);
        }

        for (int i = 1; i < newOuterChain.size() - 1; i++) {
            for (Face f : facesOfNode(newOuterChain.get(i))) {
                outV.setInt(f, outV.getInt(f) + 1);// outV++
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
            for (Face f : facesOfNode(newOuterChain.get(i)))
                for (Node n : nodesFromFace(f)) {
                    separated.setInt(n, 0);
                    for (Face innerFace : facesOfNode(n)) {
                        if (outV.getInt(innerFace) >= 3)
                            separated.setInt(n, 1);
                        if (outV.getInt(innerFace) == 2
                                && outE.getInt(innerFace) == 0)
                            separated.setInt(n, 1);
                    }

                }
        }
    }

    private ArrayList<Face> facesOfNode(Node node) {
        ArrayList<Face> faces = new ArrayList<>();

        for (EdgeCursor ec = node.outEdges(); ec.ok(); ec.next()) {
            Face f = planarInformation.faceOf(ec.edge());
            if (!isInsertedFace.getBool(f))
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
        Face firstFace = planarInformation.faceOf(parentNode
                .getEdgeTo(firstNode));
        ArrayList<Node> outerChain = new ArrayList<>();
        outerChain.add(firstNode);

        ArrayList<Face> faces = sortedFaces(parentNode, firstFace);
        for (Face f : faces) {
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
    private ArrayList<Face> sortedFaces(Node node, Face firstFace) {
        Boolean startAddingToList = false;
        ArrayList<Face> faces = new ArrayList<>();

        for (EdgeCursor ec = node.outEdges(); ec.ok(); ec.next()) {
            Face f = planarInformation.faceOf(ec.edge());

            if (!isInsertedFace.getBool(f) && startAddingToList) {
                faces.add(f);
            }

            if (f == firstFace) // found first face
                startAddingToList = true;
        }

        for (EdgeCursor ec = node.outEdges(); ec.ok(); ec.next()) {
            Face f = planarInformation.faceOf(ec.edge());

            if (!isInsertedFace.getBool(f)) {
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
        EdgeCursor ec = node.edges();

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
    private ArrayList<Node> sortedNodesFromFace(Face f, Node parentNode) {
        Boolean startAddingToList = false;
        ArrayList<Node> nodes = new ArrayList<>();
        for (EdgeCursor ec = f.edges(); ec.ok(); ec.next()) {

            Edge edge = ec.edge();
            Node node = edge.target();

            if (node == parentNode)
                startAddingToList = true;
            if (startAddingToList)
                nodes.add(node);
        }
        for (EdgeCursor ec = f.edges(); ec.ok(); ec.next()) {

            Edge edge = ec.edge();
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
    private void addReversedEdges() {
        for (Edge ed : graph.getEdgeArray()) {
            this.planarInformation.createReverse(ed);
        }
    }

    private void removeReversedEdges() {
        for (Edge ed : graph.getEdgeArray()) {
            if (planarInformation.isInsertedEdge(ed))
                graph.removeEdge(ed);
        }
    }

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


