package layout.algo;

import com.yworks.yfiles.algorithms.YPoint;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.view.GraphComponent;
import util.NodeFunctions;

import java.util.*;

/**
 * Created by Jessica Wolz on 04.11.16.
 */
public class GridDrawing {

    private GraphComponent view;
    private IGraph graph;
    private NodeFunctions func;

    public GridDrawing(GraphComponent view)
    {
        this.view = view;
        this.graph = view.getGraph();
        this.func = new NodeFunctions();

        double x = 2;
    }


    /**
     * Calculate grid positions for each node by rounding down.
     */
    public void roundingGrid()
    {
        for (INode u : graph.getNodes())
        {
            double u_x = u.getLayout().getCenter().x;
            double u_y = u.getLayout().getCenter().y;

            YPoint p_u = new YPoint(u_x, u_y);

            //Calculate Integer Grid Points
            if(u_x % 2 != 0){
                u_x = Math.floor(u_x);
            }
            if(u_y % 2 != 0){
                u_y = Math.floor(u_y);
            }
            this.view.getGraph().setNodeCenter(u, new PointD(u_x,u_y));

        }
        this.view.updateUI();
    }

    /**
     * Calculate grid positions by relative position to the most left point
     * Can be changed to any point in the graph, actually.
     * @param x - parameter how much the points should be altered
     */
    public void blowUpGridPoints(double x)
    {

        List<INode> graphNodes = func.compareAndSort(this.graph);
        INode firstN = graphNodes.get(0);


        PointD leftP, currP, tempP;
        leftP = firstN.getLayout().getCenter();
        Iterator<INode> iter = graphNodes.iterator();

        // change position of each node
        while(iter.hasNext())
        {
            INode currN = iter.next();
            currP = currN.getLayout().getCenter();
            tempP = PointD.add(currP, PointD.times(x,PointD.subtract(currP, leftP)));
            this.view.getGraph().setNodeCenter(currN, new PointD(tempP.getX(), tempP.getY()));

        }
        this.view.updateUI();
    }

    /**
     * Used to move all vertices left/right or up/down
     * @param x - amount of movements
     */
    public void moveOneRight(double x){
        for (INode u : graph.getNodes())
        {
            double u_x = u.getLayout().getCenter().x;
            double u_y = u.getLayout().getCenter().y;

            this.view.getGraph().setNodeCenter(u, new PointD(u_x+x,u_y));

        }
        this.view.updateUI();
    }

}

