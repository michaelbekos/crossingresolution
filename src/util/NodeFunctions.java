package util;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.utils.IEnumerator;
import com.yworks.yfiles.utils.IListEnumerable;

import java.io.*;
import java.util.*;

/**
 * Created by Jessica Wolz on 04.11.16.
 */
public class NodeFunctions {

    /**
     * Sorts a list of nodes of the graph ascending according to the x coordinate.
     * * @param graph - the input graph
     */
    public List<INode> compareAndSort(IGraph graph) {

        IListEnumerable<INode> graphNodes = graph.getNodes();
        List<INode> target = new ArrayList<>();
        graphNodes.forEach(target::add);
        Comparator<INode> byX = (INode n1, INode n2) -> Double.compare(n1.getLayout().getCenter().x, n2.getLayout().getCenter().x);
        Collections.sort(target, byX);
        return target;
    }

    /**
     * Copies all nodes from the current graph into a file
     * @param graph - input graph
     * @param name - input file
     * @throws FileNotFoundException
     */
    public void copyToFile(IGraph graph, File name) throws FileNotFoundException {

        try {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(name), "utf-8"))) {
                for (INode u : graph.getNodes()) {
                    writer.write(u.getLayout().getCenter().getX() + " " + u.getLayout().getCenter().getY());
                    writer.write(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads Node Coordinates from txt File
     * @param name - current file with nodes
     * @return points - list of pointDs
     */
    public List<PointD> readFromFile(File name) {
        List<PointD> points = new ArrayList<>();

        try {
            Scanner scan = new Scanner(name);

            while(scan.hasNextDouble())
            {
                double u_x = Double.parseDouble(scan.next());
                double u_y = Double.parseDouble(scan.next());
                points.add(new PointD(u_x, u_y));

            }

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        return points;
    }




    /**
     * Used to move all vertices left/right or up/down
     * @param x - amount of movements
     * @param graph - the input graph
     * @return movedNodes - all nodes moved by x
     */
    public IGraph moveNodes(double x, IGraph graph){

        IGraph movedNodes = graph;

        for (INode u : movedNodes.getNodes())
        {
            double u_x = u.getLayout().getCenter().x;
            double u_y = u.getLayout().getCenter().y;

            movedNodes.setNodeCenter(u, new PointD(u_x+x,u_y-x));

        }

        return movedNodes;
    }


    /**
     * Prints all Nodes of a Graph
     * @param graph - input graph
     */
    public void printNodes(IGraph graph){

        for (INode u : graph.getNodes())
        {
            double u_x = u.getLayout().getCenter().x;
            double u_y = u.getLayout().getCenter().y;

            System.out.println("(" + u_x + "," + u_y + ")");
        }

    }


}
