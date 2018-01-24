package algorithms.canonicalOrder;


import com.yworks.yfiles.algorithms.*;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.layout.YGraphAdapter;

import java.util.*;

/**
 * Created by Ama on 16.12.2017.
 */
/**
 * Calculates the canonical order of a triconnected planar graph.
 *
 * @author Findan Eisenhut | Clean up, code reformatting and little bug fix: Philemon
 *         Schucker 21.04.2015
 *
 */
/**
 * Calculates the canonical order of a triconnected planar graph.
 *
 * 02.08.15: Some Changes:
 *
 * - Bug fix: nodes were not added to the result list
 *
 * - Canonical Order algorithm is now deterministic => replaced random hashset iterator by a list which saves the order
 * of added elements in possibleNextFaces and possibleNextNodes
 *
 * - Code clean up and Code reformatting
 *
 * @author Findan Eisenhut | Changes by Philemon Schucker
 *
 */
public class CanonicalOrder {

    public Graph graph;
    private PlanarEmbedding planarEmbedding;
    private Node v1;
    private Node v2;
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

    private Map<List<Dart>,Integer> outD; // number of Darts in the current outerFace
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
    public CanonicalOrder(Graph g, PlanarEmbedding p, boolean random) {
        this.graph = g;
        this.random = random;
        this.planarEmbedding = p;
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
                } else {
                    f = possibleNextFaces.iterator().next();
                }
                    // System.out.println(f);
                removeFace(f);
            }
        }

        // only v1v2 face remains
        addv1v2FaceToCanonicalOrder();

        // removeReversedEdges();
         System.out.println("119Canorder: "+ canonicalOrder.toString());
    }

    /**&
     * determine v1, v2, vn and the v1v2-face
     */
    private void preparations() {
        List<Dart> outerFace = planarEmbedding.getOuterFace();
        Dart curDart = outerFace.get(0);
        Node[] nodes = graph.getNodeArray();
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("NodeNum: " +nodes.length + " Nodes: "+ nodes[0].toString() + "---"+ nodes[1].index() + "---"+ nodes[2].index() + "---"+ nodes[3].index()+ "---" );
        System.out.println("Dart: Source: " + curDart.getAssociatedEdge().source() + "  Target:  " + curDart.getAssociatedEdge().target() + "  Reversed?" + curDart.isReversed());
        System.out.println("DartNextOP: Source: " + outerFace.get(1).getAssociatedEdge().source() + "  Target:  " + outerFace.get(1).getAssociatedEdge().target() + "Reversed?" + outerFace.get(1).isReversed());
        System.out.println("DartNextOP: Source: " + outerFace.get(outerFace.size()-1).getAssociatedEdge().source() + "  Target:  " + outerFace.get(outerFace.size()-1).getAssociatedEdge().target() + "Reversed?" + outerFace.get(outerFace.size()-1).isReversed());
        System.out.println("--------------------------------OuterFace-----------------------------------------------");
        for(Dart dart : outerFace){
            System.out.println("---Dart: Source: " + dart.getAssociatedEdge().source() + "  Target:  " + dart.getAssociatedEdge().target() + "  Reversed?" + dart.isReversed());

        }
        System.out.println("---------------------------------ALL Faces----------------------------------------------");
        int facNum = 0;
        for(List<Dart> face : planarEmbedding.getFaces()){
            for (Dart dart : face) {
                //System.out.println("+"+facNum+"+Dart: Source: " + dart.getAssociatedEdge().source() + "  Target:  " + dart.getAssociatedEdge().target() + "  Reversed?" + dart.isReversed());
                System.out.println("+"+facNum+"+Dart: Source: " + getSourceNode(dart) + "  Target:  " + getTargetNode(dart) + "  Reversed?" + dart.isReversed());
            }
            System.out.println("-------------------------------------------------------------------------------");
            facNum++;
        }

        v1 = getSourceNode(curDart);
        curDart = outerFace.get(1);
        Node vn = getSourceNode(curDart);
        curDart = outerFace.get(outerFace.size()-1); // the last Dart
        v2 = getSourceNode(curDart);

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
         System.out.println("v1: " + v1 + " " + v1.degree() + " v2: " + v2 + " " + v2.degree() + " vn: " + vn + " "
         + vn.degree());
        possibleNextNodes.add(vn);
        possibleNextNodes2.add(vn);



        Edge edge = v1.getEdge(v2);
        Dart dart1 = planarEmbedding.getDarts(edge)[0];
        Dart dart2 = planarEmbedding.getDarts(edge)[1];

        Node targetNode, sourceNode;
        if(dart1.isReversed()){
            sourceNode = getTargetNode(dart1);
            targetNode = getSourceNode(dart1);
        }else{

            sourceNode = getSourceNode(dart1);
            targetNode = getTargetNode(dart1);
        }
        if(v1.equals(sourceNode) && v2.equals(targetNode)){
            v1v2Face = dart1.getFace();
        }else{
            v1v2Face = dart2.getFace();
        }
        if(v1v2Face == null){
            System.out.println("!!!!!!!!!!!!!!! v1v2Face is empty !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
 /*   Edge edge = v1.getEdge(v2);
for(Dart dart : planarEmbedding.getDarts(edge)){
            if(dart.isReversed() && ){
                v1v2Face = dart.getFace();
                break;
            }
        } */



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
        Node node;

        for (Dart dart : planarEmbedding.getOuterFace()) {
            node = getTargetNode(dart);
            outerFace.add(node);
        }
    }

    private void initMaps() {
        separated = graph.createNodeMap();
        visited = graph.createNodeMap();
        outD = new HashMap<List<Dart>, Integer>();
        outV = new HashMap<List<Dart>, Integer>();
        isInsertedNode = graph.createNodeMap();
        isInsertedFace = new HashMap<List<Dart>, Boolean>();
        isSeparating = new HashMap<List<Dart>, Boolean>();


        for (INodeCursor nc = graph.getNodeCursor(); nc.ok(); nc.next()) {
            separated.setInt(nc.node(), 0);
            visited.setInt(nc.node(), 0);
            isInsertedNode.setBool(nc.node(), false);
        }

        for (List<Dart> face : planarEmbedding.getFaces()) {
            outD.put(face, 0);
            outV.put(face, 0);
            isInsertedFace.put(face, false);
            isSeparating.put(face, false);
        }
        // outer face = inserted
        isInsertedFace.put(planarEmbedding.getOuterFace(), true);

        // update face variables in dependency of the outerface
        for(Dart dart : planarEmbedding.getOuterFace()){
            Dart innerDart = dart.getOppositeDart();
            Node node = getSourceNode(innerDart);
            increaseOutD(innerDart);
            increaseOutV(node);
        }

    }

    /**
     * outE +1 for every Face adjacent to the given node.
     *
     * @param node
     */
    private void increaseOutV(Node node) {
        for (Dart dart : planarEmbedding.getOutgoingDarts(node)) {
            List<Dart> face = dart.getFace();
            outV.put(face, outV.get(face) + 1);
        }
    }

    /**
     * outV +1 for the Face adjacent to the given edge.
     *
     * @param dart
     */
    private void increaseOutD(Dart dart) {
        List<Dart> face = dart.getFace();
        outD.put(face, outD.get(face) + 1);
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
        for (Dart dart : planarEmbedding.getOutgoingDarts(deletedNode)) {
            List <Dart> face = dart.getFace();
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
     //   System.out.println("337 Canorder: "+ canonicalOrder.toString());

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
       // System.out.println("383Canorder: "+ canonicalOrder.toString());

    }

    private void updateVisited(Node node) {
        // visitet+=1 for all neighboues of node
        for(Dart dart : planarEmbedding.getOutgoingDarts(node)){
            Node n = getTargetNode(dart);
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

        for (Dart dart : f) {
            Edge edge = dart.getAssociatedEdge();
            Node node = edge.target();
            nodes.add(node);
        }
        return nodes; // counter-clockwise
    }

    private int DegreeOf(Node node) { //TODO: nur die  Kanten oder die Darts
        int counter = 0;
        for (Dart outDart : planarEmbedding.getOutgoingDarts(node)) {
            Node n = getTargetNode(outDart);
        }

 /*       int counter2 = 0;
        for (Dart outDart : planarEmbedding.getOutgoingDarts(node)) {
            Node n1,n2;

                n1 = outDart.getAssociatedEdge().target();

                n2 = outDart.getAssociatedEdge().source();

            if (!isInsertedNode.getBool(n1) && !node.equals(n1))
                counter2++;
            if (!isInsertedNode.getBool(n2) && !node.equals(n2))
                counter2++;

        }
 */      // System.out.println("Counter1: " + counter +"     Counter2: " + counter2);
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
            Dart d = getDart(newOuterChain.get(j), prevNode);
            List<Dart> f = d.getFace();
            if (outV.get(f) == outD.get(f) + 1 && outD.get(f) >= 2 && f != v1v2Face) {
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
            Dart d = getDart(newOuterChain.get(i), prevNode);
            List<Dart> f = d.getFace();
            outD.put(f,outD.get(f) + 1);
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
                        if (outV.get(innerFace) == 2 && outD.get(innerFace) == 0)
                            separated.setInt(n, 1);
                    }
                }
        }
    }

    private List<List<Dart>> facesOfNode(Node node) {
        List<List<Dart>> faces = new ArrayList<>();

        for(Dart outgoingDart : planarEmbedding.getOutgoingDarts(node)){
            List<Dart> f = outgoingDart.getFace();
            if(!isInsertedFace.get(f)){
                faces.add(f);
            }
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
        List<Dart> firstFace = getDart(parentNode, firstNode).getFace();
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
        // System.out.println("Outerchain: " + outerChain.toString());
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

        for (Dart outDart : planarEmbedding.getOutgoingDarts(node)) {
            List<Dart> f = outDart.getFace();

            if (!isInsertedFace.get(f) && startAddingToList) {
                faces.add(f);
            }

            if (f == firstFace) // found first face
                startAddingToList = true;
        }

        for (Dart outDart : planarEmbedding.getOutgoingDarts(node)) {     //TODO: why is there a scound for?
            List<Dart> f = outDart.getFace();

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
    private Node leftOuterNode(Node node) { //TODO: im Orginalcode waren es alle ausgehenden und eingehenden Kanten, bei den eingehenden wäre target= node, ist das gewollt?

        Node retNode = null;
        Node last = null;
        List<Dart> outDarts = planarEmbedding.getOutgoingDarts(node);

        int dartIterator = 0;
        int numOfDarts = outDarts.size();
        while (dartIterator < numOfDarts) {// ignore inserted nodes
            retNode = getTargetNode(outDarts.get(dartIterator));
            if(!isInsertedNode.getBool(retNode)){
                break;
            }
            dartIterator++;
        }

        if (outerFace.contains(retNode)) {
            last = retNode;
            while (dartIterator < numOfDarts) {// ignore inserted nodes
                dartIterator++ ;                                    //TODO: wiso als erstes incre: ?
                retNode = getTargetNode(outDarts.get(dartIterator));
                if (!isInsertedNode.getBool(retNode))
                    break;
            }
            if (outerFace.contains(retNode)) {
                return last;
            } else {
                while (dartIterator < numOfDarts) {// search next outer node
                    dartIterator++;
                    retNode = getTargetNode(outDarts.get(dartIterator));
                    if (!isInsertedNode.getBool(retNode) && outerFace.contains(retNode)) {
                        break;
                    }
                }
            }

        } else {
            while (dartIterator < numOfDarts) {// search next outer node
                dartIterator++;
                retNode = getTargetNode(outDarts.get(dartIterator));
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
      //  System.out.println("709 FACE: " + f.size());
        if(f == null){
            System.out.println("Cant sort Nodes from Face, because the List is empty.");
            //return nodes;
        }
        for (Dart dart : f ){
            Node node = getTargetNode(dart);

            if (node == parentNode)
                startAddingToList = true;
            if (startAddingToList)
                nodes.add(node);
        }
        for (Dart dart : f) {
            Node node = getTargetNode(dart);
            if (node == parentNode)
                break;

            nodes.add(node);

        }
        return nodes;
    }
/* useless in 3.1
    private void removeReversedEdges() {
        for (Edge ed : graph.getEdgeArray()) {
            if (planarInformation.isInsertedEdge(ed))
                graph.removeEdge(ed);
        }
    }
*/
    private void addNodeToCanonicalOrder(Node n) {
        //   System.out.println("CanorderNode:" + n.toString() );

        ArrayList<Node> nodeAsList = new ArrayList<>();
        nodeAsList.add(n);
        canonicalOrder.add(0, nodeAsList);
    }

    private void addChainToCanonicalOrder(ArrayList<Node> nodes) {
      //  System.out.println("CanorderNodeCHain:" + nodes.toString() );
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

   /* private  Dart getDart(Node source, Node target){
        System.out.println("Num of Darts: " + planarEmbedding.getOutgoingDarts(source).size());

        for(Dart dart : planarEmbedding.getOutgoingDarts(source)){
            Node n;
            if(dart.isReversed()){
                n = dart.getAssociatedEdge().target();
                System.out.println("RRR");
            }else{
                System.out.println("III");

                n = dart.getAssociatedEdge().source();
            }
            if(target.equals(n)){
                System.out.println("Return the dart");
                return dart;
            }
        }
        System.out.println("There not existes a dart for this pair.");
        return null;
    }*/
    private  Dart getDart(Node source, Node target){


        for(Dart dart : planarEmbedding.getOutgoingDarts(source)){
            Node n1,n2;

                n1 = dart.getAssociatedEdge().target();

                n2 = dart.getAssociatedEdge().source();
           // System.out.println("GESUCHT: " + source.index() + ",  "  + target.index() + "  GEFUNDEN: " + n2.index() + ",   " + n1.index());

            if(target.equals(n1)){
             //   System.out.println("Return the dart");
                return dart;
            }
            if(target.equals(n2)){
              //  System.out.println("Return the dart Reverse");
                return dart.getOppositeDart();
            }
          //  System.out.println("falxcher dart");

        }
       // System.out.println("There not existes a dart for this pair");
        return null;
    }

    private Node getSourceNode(Dart dart){
        Node sourceNode;
        if(dart.isReversed()){
            sourceNode = dart.getAssociatedEdge().target();
        }else{
            sourceNode = dart.getAssociatedEdge().source();
        }
        return sourceNode;
    }

    private Node getTargetNode(Dart dart){
        Node targetNode;
        if(dart.isReversed()){
            targetNode = dart.getAssociatedEdge().source();
        }else{
            targetNode = dart.getAssociatedEdge().target();
        }
        return targetNode;
    }
}
