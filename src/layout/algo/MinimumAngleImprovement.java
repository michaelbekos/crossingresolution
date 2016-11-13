package layout.algo;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import util.NodeFunctions;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jessica Wolz on 06.11.16.
 */
public class MinimumAngleImprovement {

    private IGraph graph;
    private NodeFunctions func;


    public MinimumAngleImprovement(IGraph graph){
        this.graph = graph;
        this.func = new NodeFunctions();
    }
    /**
     * Minimal Angle Improvement by moving points along x axis
     * @param x - step size
     */
    public void minimumAngleImprovement(double x){
        if(x == 0) return;
        IGraph tempGraph = this.graph;

        //File name = new File("nodes.txt");
        //try {
        //    func.copyToFile(this.graph, name);
        //} catch (FileNotFoundException e) {
        //    e.printStackTrace();
        //}
        //List<PointD> temp = func.readFromFile(name);
        List<PointD> origNodeLocations = new ArrayList<PointD>(graph.getNodes().size());
        graph.getNodes().forEach(
            u -> origNodeLocations.add(
                new PointD(
                    u.getLayout().getCenter().getX(), 
                    u.getLayout().getCenter().getY())));
        ArrayList<Double> minAngles = new ArrayList<>();

        for(int j = 0; j * x < 1; j++){

            //moving to right by x steps
            for (INode u : tempGraph.getNodes()) {
                double u_x = u.getLayout().getCenter().getX()+(x * j);
                double u_y = u.getLayout().getCenter().getY();
                tempGraph.setNodeCenter(u, new PointD(u_x, u_y));
                tempGraph = GridDrawing.roundingGridTemp(tempGraph);
            }

            minAngles.add(MinimumAngle.getMinimumAngle(tempGraph).get());

            // reseting the original graph
            int i = 0;
            for (INode u : tempGraph.getNodes()) {
                double u_x = origNodeLocations.get(i).getX();
                double u_y = origNodeLocations.get(i).getY();
                tempGraph.setNodeCenter(u, new PointD(u_x, u_y));
                i++;
            }

        }

        //minAngles.add(MinimumAngle.getMinimumAngle(tempGraph).get());


        for(int j = 0; j < minAngles.size(); j++){
            System.out.print("Movement i = ");
            System.out.printf("%.4f", x*j);
            System.out.println(" : " + minAngles.get(j));
        }

    }
}
