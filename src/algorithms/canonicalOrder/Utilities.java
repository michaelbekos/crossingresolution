package algorithms.canonicalOrder;

import com.yworks.yfiles.algorithms.Dart;
import com.yworks.yfiles.algorithms.PlanarEmbedding;
import com.yworks.yfiles.algorithms.YPoint;
import com.yworks.yfiles.geometry.IPoint;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IBend;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.layout.YGraphAdapter;

import java.util.List;

/**
 * Created by Ama on 16.12.2017.
 */
public class Utilities {

    public static void setOuterFace(PlanarEmbedding plan, IGraph graph) {
        // take leftMost node and check his faces
        YGraphAdapter graphAdapter = new YGraphAdapter(graph);
        INode leftMost = null;
        INode rightMost = null;
        INode topMost = null;
        INode bottomMost = null;
        double currentXMin = Double.MAX_VALUE;
        double currentXMax = -Double.MAX_VALUE;
        double currentYMin = Double.MAX_VALUE;
        double currentYMax = -Double.MAX_VALUE;
        for (INode node : graph.getNodes()) {
            //YPoint c = getPointForNode(nc.node(), graph);
            PointD center = node.getLayout().getCenter();
            if (center.x < currentXMin) {
                currentXMin = center.x;
                leftMost = node;
            }
            if (center.x > currentXMax) {
                currentXMax = center.x;
                rightMost = node;
            }
            if (center.y < currentYMin) {
                currentYMin = center.y;
                topMost = node;
            }
            if (center.y > currentYMax) {
                currentYMax = center.y;
                bottomMost = node;
            }
        }
        // now compute extreme bends
        IBend leftMostB = null;
        IBend rightMostB = null;
        IBend topMostB = null;
        IBend bottomMostB = null;
        for (IEdge edge : graph.getEdges()) {
            //EdgeRealizer r = graph.getRealizer(ec.edge());
            for (int i = 0; i < edge.getBends().size(); i++) {
                IBend b =  edge.getBends().getItem(i);
                PointD p = b.getLocation().toPointD();
                if (p.getX() < currentXMin) {
                    currentXMin = p.x;
                    leftMostB = b;
                }
                if (p.x> currentXMax) {
                    currentXMax = p.x;
                    rightMostB = b;
                }
                if (p.y < currentYMin) {
                    currentYMin = p.y;
                    topMostB = b;
                }
                if (p.y > currentYMax) {
                    currentYMax = p.y;
                    bottomMostB = b;
                }
            }
        }
        // if one of the extreme bends is != null then one of the vertices of
        // the corresponding edge has to be chosen instead of the extreme vertex
        if (leftMostB != null) {
            leftMost = leftMostB.getOwner().getSourceNode();
        }
        if (rightMostB != null) {
            rightMost = rightMostB.getOwner().getSourceNode();
        }
        if (topMostB != null) {
            topMost = topMostB.getOwner().getSourceNode();
        }
        if (bottomMostB != null) {
            bottomMost = bottomMostB.getOwner().getSourceNode();
        }
        // System.out.println("leftMost: " + leftMost + " rightMost: " +
        // rightMost + " bottomMost: " + bottomMost + " topMost: " + topMost);
        /*for (FaceCursor fc = plan.faces(); fc.ok(); fc.next()) {
            if (fc.face().contains(bottomMost) && fc.face().contains(topMost)
                    && fc.face().contains(leftMost)
                    && fc.face().contains(rightMost)) {

                plan.setOuterFace(fc.face());
            }
        }
        */
        for(List<Dart> ld : plan.getFaces()){
            if(ld.contains(graphAdapter.getCopiedNode(bottomMost)) &&
                    ld.contains(graphAdapter.getCopiedNode(topMost)) &&
                    ld.contains(graphAdapter.getCopiedNode(leftMost)) &&
                    ld.contains(graphAdapter.getCopiedNode(rightMost))){
                plan.getOuterFace().clear();
                System.out.println("Ist es wirklich leer? : " + plan.getOuterFace().size());
                for(int i = 0; i < ld.size(); i++ ){
                    plan.getOuterFace().add(i, ld.get(i));
                }
            }
        }
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

