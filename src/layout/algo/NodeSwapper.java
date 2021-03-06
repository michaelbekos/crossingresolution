package layout.algo;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.utils.PositionMap;
import util.graph2d.Intersection;

import java.util.Random;

/**
 * Created by Jessica Wolz on 07.02.17.
 */
public class NodeSwapper {


    /**
     * Calls the swapping functions depending on amount of nodes & if the crossing nodes should be used
     * @param g - input graph
     * @param amount - how many nodes should be swapped
     * @param crossing - true if crossing nodes should be swapped
     */
    public static void swapNodes(IGraph g, int amount, boolean crossing){
        Mapper<INode, PointD> nodePos = PositionMap.FromIGraph(g);
        if(amount <= 4 && crossing){
            PositionMap.applyToGraph(g, swapCrossingNodes(g, nodePos, amount));
        } else {
            PositionMap.applyToGraph(g, swapRandomNodes(g, nodePos, amount));
        }
    }

    /**
     * Swaps only nodes from the minimum crossing
     * @return Positions with swapped nodes
     */
    private static Mapper<INode, PointD> swapCrossingNodes(IGraph g, Mapper<INode, PointD> nodePos, int amount) {
        Intersection cross = MinimumAngle.getMinimumAngleCrossing(g, nodePos).get();

        if(cross != null){
            if(amount == 2){
                PointD temp = cross.segment1.p1;
                nodePos.setValue(cross.segment1.n1, cross.segment2.p1);
                nodePos.setValue(cross.segment2.n1, temp);
            }
            if(amount == 3){
                PointD temp = cross.segment1.p1;
                PointD temp2 = cross.segment1.p2;
                nodePos.setValue(cross.segment1.n1, cross.segment2.p1);
                nodePos.setValue(cross.segment1.n2, temp);
                nodePos.setValue(cross.segment2.n1, temp2);
            }
            if(amount == 4){
                PointD temp = cross.segment1.p1;
                PointD temp2 = cross.segment1.p2;
                PointD temp3 = cross.segment2.p2;
                nodePos.setValue(cross.segment1.n1, cross.segment2.p1);
                nodePos.setValue(cross.segment1.n2, temp3);
                nodePos.setValue(cross.segment2.n1, temp);
                nodePos.setValue(cross.segment2.n2, temp2);
            }
        }
        return nodePos;
    }

    /**
     * Swaps random nodes in graph
     * @return Positions with swapped nodes
     */
    private static Mapper<INode, PointD> swapRandomNodes(IGraph g, Mapper<INode, PointD> nodePos, int amount){
        Random rand = new Random();
        int nodes = g.getNodes().size();
        if(amount < nodes) {
            INode[] swapNodes = new INode[amount];
            PointD[] swapPoints = new PointD[amount];
            for (int i = 0; i < amount; i++) {
                swapNodes[i] = g.getNodes().getItem(rand.nextInt(nodes));
                swapPoints[i] = swapNodes[i].getLayout().getCenter();
            }

            PointD temp = swapPoints[0];
            for(int i = 0; i < amount-1; i++){
                nodePos.setValue(swapNodes[i], swapPoints[i+1]);
            }
            nodePos.setValue(swapNodes[amount-1], temp);

        PositionMap.applyToGraph(g, nodePos);
        } else {System.out.println("Not enough nodes in the graph to swap.");}

        return nodePos;
    }

}
