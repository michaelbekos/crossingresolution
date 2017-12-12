package layout.algo;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.IMapper;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import util.Maybe;
import util.Tuple3;
import util.graph2d.Intersection;
import util.graph2d.LineSegment;

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
        Mapper<INode, PointD> nodePos = ForceAlgorithmApplier.initPositionMap(g);
        if(amount <= 4 && crossing){
            ForceAlgorithmApplier.applyNodePositionsToGraph(g, swapCrossingNodes(g, nodePos, amount));
        } else {
            ForceAlgorithmApplier.applyNodePositionsToGraph(g, swapRandomNodes(g, nodePos, amount));
        }
    }

    /**
     * Swaps only nodes from the minimum crossing
     * @return Positions with swapped nodes
     */
    private static Mapper<INode, PointD> swapCrossingNodes(IGraph g, Mapper<INode, PointD> nodePos, int amount) {
        Tuple3<LineSegment, LineSegment, Intersection> cross = MinimumAngle.getMinimumAngleCrossing(g, nodePos).get();

        if(cross != null){
            if(amount == 2){
                PointD temp = cross.a.p1;
                nodePos.setValue(cross.a.n1, cross.b.p1);
                nodePos.setValue(cross.b.n1, temp);
            }
            if(amount == 3){
                PointD temp = cross.a.p1;
                PointD temp2 = cross.a.p2;
                nodePos.setValue(cross.a.n1, cross.b.p1);
                nodePos.setValue(cross.a.n2, temp);
                nodePos.setValue(cross.b.n1, temp2);
            }
            if(amount == 4){
                PointD temp = cross.a.p1;
                PointD temp2 = cross.a.p2;
                PointD temp3 = cross.b.p2;
                nodePos.setValue(cross.a.n1, cross.b.p1);
                nodePos.setValue(cross.a.n2, temp3);
                nodePos.setValue(cross.b.n1, temp);
                nodePos.setValue(cross.b.n2, temp2);
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

        ForceAlgorithmApplier.applyNodePositionsToGraph(g, nodePos);
        } else {System.out.println("Not enough nodes in the graph to swap.");}

        return nodePos;
    }

}
