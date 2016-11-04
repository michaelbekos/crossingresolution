package util;

import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.utils.IListEnumerable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Jessica Wolz on 04.11.16.
 */
public class NodeFunctions {


    /**
     * Sorts a list of nodes of the graph ascending according to the x coordinate.
     * @param graph - The input graph.
     */
    public List<INode> compareAndSort(IGraph graph){

        IListEnumerable<INode> graphNodes = graph.getNodes();
        List<INode> target = new ArrayList<>();
        graphNodes.forEach(target::add);
        Comparator<INode> byX = (INode n1, INode n2) -> Double.compare(n1.getLayout().getCenter().x, n2.getLayout().getCenter().x);
        Collections.sort(target, byX);
        return target;
    }

}
