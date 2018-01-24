package algorithms.canonicalOrder;

import com.yworks.yfiles.algorithms.*;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Ama on 22.01.2018.
 */
public class CanonicalOrderMy {

    public Graph graph;
    private PlanarEmbedding planarEmbedding;
    private Node v1;
    private Node v2;
    private List<Dart> v1v2Face;
    private ArrayList<Node> canonicalOrder;

    private HashSet<Node> outerFace;
    private ArrayList<Node> nextNodes; // List of Node with  mark == false, out == true, != v1,v2

    private INodeMap isInsertedNode;
    private Map<List<Dart>,Boolean> isInsertedFace;
    private ArrayList<Node>  outerChain;
    //  private List<Dart> outerChainDart;

    private INodeMap chords; //INT
    private INodeMap out;   //bool
    private INodeMap mark; //bool

    private boolean random = false;

    public CanonicalOrderMy(Graph graph, PlanarEmbedding planarEmbedding, boolean random){
        this.graph = graph;
        this.random = random;
        this.planarEmbedding = planarEmbedding;
        init();
        calcOrder();
    }

    private void init(){
        canonicalOrder = new ArrayList<>();
        outerFace = new HashSet<>();
        isInsertedFace = new HashMap<>();

        for (Dart dart : planarEmbedding.getOuterFace()) {
            outerFace.add(getSourceNode(dart));
        }

        // Init Maps
        chords = graph.createNodeMap();
        out = graph.createNodeMap();
        mark = graph.createNodeMap();

        for(Node node : graph.getNodeArray()){
            chords.setInt(node, 0);
            out.setBool(node, false);
            mark.setBool(node, false);

        }
        //merken welche Faces schon verwendet wurden
        for(List<Dart> face : planarEmbedding.getFaces()){
            isInsertedFace.put(face, false);
        }
        isInsertedFace.put(planarEmbedding.getOuterFace(), true);
    }
    private void removeNode(Node deletedNode) {
        ArrayList<Node> newOuterChain = calcNewOuterChain(deletedNode);
    }

    private ArrayList<Node> calcNewOuterChain(Node parentNode) {
        // Node firstNode = leftOuterNode(parentNode);
        return null;
    }




    private void calcOrder(){

        Iterator<Node> nodeIta =  outerFace.iterator();
        v1 = nodeIta.next();
        v2 = nodeIta.next();
        Node vn = nodeIta.next();
        out.setBool(v1, true);
        out.setBool(v2, true);
        out.setBool(vn, true);


        nextNodes = new ArrayList<>();
        outerChain = new ArrayList<>();


        nextNodes.add(vn);


        outerChain.add(v1);
        outerChain.add(vn);
        outerChain.add(v2);


        for(int k = graph.getNodeArray().length - 1; k >= 2; k-- ){

            Node vk = getNextNode();
            canonicalOrder.add(vk);
            System.out.println("Iteration: " + k + "     node Index: " + vk.index());


            //find Index for node vk
            int indexVk = indexInOuterChain(vk);
            System.out.println("indexVK = " + indexVk );
            //node1 and node2 are the start and end node from the next chain
            Node node1, node2;
            if(indexVk == outerChain.size()-1){
                node2 = outerChain.get(0);
            }else{
                node2 = outerChain.get(indexVk + 1);
            }
            if(indexVk == 0){
                node1 = outerChain.get(outerChain.size()-1);
            }else{
                node1 = outerChain.get(indexVk -1);
            }

            mark.setBool(vk, true);


            Dart curDart = planarEmbedding.getOutgoingDarts(vk).get(0); // Default wert, besser mit der vav2 Kante

            //find Dart to node2, were its starts to insert the nodes in the chain
            for(Dart outDart : planarEmbedding.getOutgoingDarts(vk)){
                System.out.println("outDart" );
                if(isInsertedFace.get(outDart.getFace())){
                    continue;
                }
                Node targetNode = getTargetNode(outDart);
                System.out.println("vk " + vk );
                System.out.println("T " + outDart.getAssociatedEdge().target() + "  S  " + outDart.getAssociatedEdge().source() + "  Reversed? " + outDart.isReversed());
                System.out.println("node2 " + node2.index() + "   targetNode:  " + targetNode.index() );
                if(node2.equals(targetNode)){
                    curDart = outDart;
                    System.out.println("Found Start node" );
                    break;
                }

            }


            boolean reachedInsertedNode = false; //reached node1
            ArrayList<Node> outerChainTmp = new ArrayList<>();
            // add new nodes to the chain in the right order
            while(true){
                isInsertedFace.put(curDart.getFace(), true);
                List<Dart> cycledDarts = getCycleDartsfromStartDart(curDart);
                //found the last Dart
                if(getTargetNode(cycledDarts.get(1)).equals(node1)){
                    System.out.println("Found last dart");
                    break;
                }
                outerChainTmp.add(getTargetNode(cycledDarts.get(1)));
                curDart = cycledDarts.get(2).getOppositeDart();
            }
            //merge  the outerChains

            ArrayList<Dart> outerChainDartNew = new ArrayList<>(); //the new sub Chain which will inserted in the old outerChain
            ArrayList<Node> outerChainNew = new ArrayList<>();
            for(int i = 0; i < indexVk; i++){
                outerChainNew.add(outerChain.get(i));
                System.out.println("preChain: " +i + "Nodeindex: " + outerChain.get(i));
            }
            Collections.reverse(outerChainTmp);
            for(Node node : outerChainTmp){
                System.out.println("midChain: " + node);
                outerChainNew.add(node);
            }

            for(int i = indexVk+1; i < outerChain.size(); i++){
                outerChainNew.add(outerChain.get(i));
                System.out.println("sufChain: " +i+ "Nodeindex: " + outerChain.get(i));
                //  outerChainDartNew.add(outerChainDart.get(i));
            }
            outerChain = outerChainNew;

            int p =  indexVk - 1; //Index for the first neighbor of vk
            int q =  (indexVk  + outerChainTmp.size() - 1); // Index for the last neighbor of vk
            System.out.println("p : " + p + "  q: " + q);
            //update chords from wi and his neighbors
            for(int i = p + 1; i < q; i++){
                out.setBool(outerChain.get(i), true);
                System.out.println("Add to nextNodes?");
                if(!mark.getBool(outerChain.get(i))){
                    System.out.println("Yes");
                    nextNodes.add(outerChain.get(i));
                }
            }
            if(q == p+1){
                Node vq = outerChain.get(q);
                chords.setInt(vq, chords.getInt(vq) - 1);
                Node vp = outerChain.get(p);
                chords.setInt(vp, chords.getInt(vp) - 1);
            }
            for(int i = p + 1; i < q; i++){
                //update
                Node wi = outerChain.get(i);
                List<Dart> outDarts = planarEmbedding.getOutgoingDarts(wi);
                for(Dart dart : outDarts){
                    Node neighbore = getTargetNode(dart);
                    if(out.getBool(neighbore) && !(neighbore.equals(outerChain.get(i-1))) && !(neighbore.equals(outerChain.get(i+1)))){
                        chords.setInt(wi, chords.getInt(wi) + 1 );
                        boolean  isNotInChain = true;
                        for(int j = p+1; i <= q-1; i++){
                            isNotInChain = isNotInChain && !(neighbore.equals(outerChain.get(j)));
                        }
                        if(isNotInChain){
                            chords.setInt(neighbore, chords.getInt(neighbore) + 1 );
                        }
                    }
                }
            }
        }
        //outerChain = toCounterclockwiseAndShift(outerChain);

    }
    public ArrayList<Node> getCanonicalOrder(){
        for(Node node : canonicalOrder){
            System.out.println("getCano:" + node.index());
        }

        return canonicalOrder;
    }

    private Node getSourceNode(Dart dart){
        Node targetNode;
        if(dart.isReversed()){
            targetNode = dart.getAssociatedEdge().target();
        }else{
            targetNode = dart.getAssociatedEdge().source();
        }
        return targetNode;
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

    /**
     * Returns a node  x with the following attributes:
     * mark(x) == false, out(x) == true, chords(x) ==  0, x != v1,v2
     *
     * @return
     */
    private Node getNextNode(){
        System.out.println("Num of NextNodes: " +nextNodes.size());
        for(Node node : nextNodes){
            if(chords.getInt(node) == 0){
                nextNodes.remove(node);
                return node;
            }
        }
        System.out.println("No next Node, return null");
        return null;
    }

    private List<Dart> getCycleDartsfromStartDart(Dart startDart){
        List<Dart> face = startDart.getFace();
        ArrayList<Dart> cycleFace = new ArrayList<>();
        int indexFromFirstDart = 0;
        boolean foundStartDart = false;
        for(int i = 0; i < face.size(); i++){
            if(startDart.equals(face.get(i))){
                indexFromFirstDart = i;
                foundStartDart = true;
            }
            if(foundStartDart){
                cycleFace.add(face.get(i));
            }
        }
        for(int i = 0; i < indexFromFirstDart; i++){
            cycleFace.add(face.get(i));
        }
        return cycleFace;
    }

    private int indexInOuterChain(Node node){
        for(int i = 0; i < outerChain.size(); i++){
            if(outerChain.get(i).equals(node)){
                return i;
            }
        }
        return 0; // fix it
    }

    private ArrayList<Node> toCounterclockwiseAndShift(ArrayList<Node>  oldList){
        ArrayList<Node> newList = new ArrayList<>();
        //SHIFT
        for(int i = 1; i < oldList.size(); i++ ){
            newList.add(oldList.get(i));
        }
        newList.add(oldList.get(0));
        //Reverse
        Collections.reverse(newList);
        return  newList;
    }
}
