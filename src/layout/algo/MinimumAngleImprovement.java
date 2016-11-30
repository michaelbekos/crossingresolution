package layout.algo;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import util.NodeFunctions;
import util.Maybe;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
        List<PointD> origNodeLocations = 
            graph.getNodes().stream()
                .map(u -> new PointD(
                        u.getLayout().getCenter().getX(), 
                        u.getLayout().getCenter().getY()))
                .collect(Collectors.toList());
        List<Double> minAngles = new ArrayList<>();

        for(int j = 0; j * x < 1; j++){
            PointD offset = new PointD(x * j, 0);
            //moving to right by x steps
            for (INode u : tempGraph.getNodes()) {
                tempGraph.setNodeCenter(u,
                    PointD.add(u.getLayout().getCenter(), offset));
            }
            
            tempGraph = GridDrawing.roundingGridTemp(tempGraph);

            minAngles.add(MinimumAngle.getMinimumAngle(tempGraph, Maybe.nothing()).get());

            // reseting the original graph
            int i = 0;
            for (INode u : tempGraph.getNodes()) {
                double u_x = origNodeLocations.get(i).getX();
                double u_y = origNodeLocations.get(i).getY();
                tempGraph.setNodeCenter(u, new PointD(u_x, u_y));
                i++;
            }
        }


        for(int j = 0; j < minAngles.size(); j++){
            System.out.print("Movement i = ");
            System.out.printf("%.4f", x*j);
            System.out.println(" : " + minAngles.get(j));
        }

    }
}
