package layout.algo.event;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.IMapper;
import com.yworks.yfiles.graph.INode;
import util.Maybe;
import util.Tuple3;
import util.graph2d.Intersection;
import util.graph2d.LineSegment;

import java.util.Random;

/**
 * Created by Jessica Wolz on 07.02.17.
 */
public class NodeSwapper {


    public static void swapCrossingNodes(IGraph g, IMapper<INode, PointD> nodePos, int amount) {
        Tuple3<LineSegment, LineSegment, Intersection> cross = MinimumAngle.getMinimumAngleCrossing(g, Maybe.just(nodePos)).get();

        if(cross != null){
            if(amount == 2){
                PointD temp = cross.a.p1;
                nodePos.setValue(cross.a.n1.get(), cross.b.p2);
                nodePos.setValue(cross.b.n2.get(), temp);
            }
            if(amount == 3){
                PointD temp = cross.a.p1;
                PointD temp2 = cross.a.p2;
                nodePos.setValue(cross.a.n1.get(), cross.b.p1);
                nodePos.setValue(cross.a.n2.get(), temp);
                nodePos.setValue(cross.b.n1.get(), temp2);
            }
            if(amount == 4){
                PointD temp = cross.a.p1;
                PointD temp2 = cross.a.p2;
                PointD temp3 = cross.b.p2;
                nodePos.setValue(cross.a.n1.get(), cross.b.p1);
                nodePos.setValue(cross.a.n2.get(), temp3);
                nodePos.setValue(cross.b.n1.get(), temp);
                nodePos.setValue(cross.b.n2.get(), temp2);
            }
        }
    }

    public static void swapRandomNodes(IGraph g, IMapper<INode, PointD> nodePos, int amount){
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
            nodePos.setValue(swapNodes[amount], temp);


        } else {System.out.println("Not enough nodes in the graph to swap.");}
    }

}
