package layout.algo;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.utils.IListEnumerable;
import com.yworks.yfiles.view.GraphComponent;
import util.NodeFunctions;
import util.RandomGraphGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by Jessica Wolz on 04.11.16.
 */
public class GridDrawing {

    //private GraphComponent view;
    private IGraph graph;
    private NodeFunctions func;
    final private IGraph graphFinal;


    public GridDrawing(IGraph graph)
    {
        this.graph = graph;
        this.func = new NodeFunctions();
        graphFinal = graph;
        //double x = 2;
    }


    /**
     * Calculate grid positions for each node by rounding down or up depending on floating point.
     */
    public void roundingGrid()
    {
        for (INode u : graph.getNodes()) {
            double u_x = u.getLayout().getCenter().x;
            double u_y = u.getLayout().getCenter().y;

            //Calculate Integer Grid Points
            if (u_x % 2 != 0) {
                if (u_x - Math.floor(u_x) < 0.5) {
                    u_x = Math.floor(u_x);
                } else {
                    u_x = Math.ceil(u_x);
                }
            }
            if (u_y % 2 != 0) {
                if (u_y - Math.floor(u_y) < 0.5) {
                    u_y = Math.floor(u_y);
                } else {
                    u_y = Math.ceil(u_y);
                }
            }
            this.graph.setNodeCenter(u, new PointD(u_x, u_y));

        }
    }

    /**
     * Calculate grid positions for each node by rounding down or up depending on floating point
     * temporarily without updating UI
     * @return gridGraph - Graph with grid points
     */
    public static IGraph roundingGridTemp(IGraph graph)
    {
        IGraph gridGraph = graph;
        for (INode u : gridGraph.getNodes()) {
            double u_x = u.getLayout().getCenter().x;
            double u_y = u.getLayout().getCenter().y;

            //Calculate Integer Grid Points
            if (u_x % 2 != 0) {
                if (u_x - Math.floor(u_x) < 0.5) {
                    u_x = Math.floor(u_x);
                } /*else {
                    u_x = Math.ceil(u_x);
                }*/
            }
            if (u_y % 2 != 0) {
                if (u_y - Math.floor(u_y) < 0.5) {
                    u_y = Math.floor(u_y);
                } /*else {
                    u_y = Math.ceil(u_y);
                }*/
            }
            gridGraph.setNodeCenter(u, new PointD(u_x, u_y));

        }

        return gridGraph;
    }

    /**
     * Calculate grid positions for each node by rounding down
     */
    public void lowerRoundingGrid()
    {
        IGraph gridGraph = this.graph;
        for (INode u : gridGraph.getNodes()) {
            double u_x = u.getLayout().getCenter().x;
            double u_y = u.getLayout().getCenter().y;

            //Calculate Integer Grid Points
            if (u_x % 2 != 0) {
                u_x = Math.floor(u_x);
            }
            if (u_y % 2 != 0) {
                u_y = Math.floor(u_y);
            }
            gridGraph.setNodeCenter(u, new PointD(u_x, u_y));

        }
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
            this.graph.setNodeCenter(currN, new PointD(tempP.getX(), tempP.getY()));

        }
        //this.view.updateUI();
    }

}

