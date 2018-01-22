package algorithms.canonicalOrder;

import com.yworks.yfiles.algorithms.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Ama on 22.01.2018.
 */
public class CanonicalOrderMy {

    public Graph graph;
    private PlanarEmbedding planarEmbedding;
    private Node v1;
    private Node v2;
    private List<Dart> v1v2Face;
    private ArrayList<ArrayList<Node>> canonicalOrder;

    private HashSet<Node> outerFace;

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
    }

    private void calcOrder(){

       Iterator<Node> nodeIta =  outerFace.iterator();
       v1 = nodeIta.next();
       v2 = nodeIta.next();
       Node vn = nodeIta.next();
       out.setBool(v1, true);
       out.setBool(v2, true);
       out.setBool(vn, true);

        for(int k = graph.getNodeArray().length - 1; k>= 2; k-- ){

        }

    }
    public ArrayList<ArrayList<Node>> getCanonicalOrder(){return canonicalOrder;}

    private Node getTargetNode(Dart dart){
        Node targetNode;
        if(dart.isReversed()){
            targetNode = dart.getAssociatedEdge().target();
        }else{
            targetNode = dart.getAssociatedEdge().source();
        }
        return targetNode;
    }

    private Node getSourceNode(Dart dart){
        Node targetNode;
        if(dart.isReversed()){
            targetNode = dart.getAssociatedEdge().source();
        }else{
            targetNode = dart.getAssociatedEdge().target();
        }
        return targetNode;
    }
}
