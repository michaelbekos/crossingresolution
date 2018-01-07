package algorithms.fpp;


import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.algorithms.*;
import com.yworks.yfiles.algorithms.YVector;
import com.yworks.yfiles.geometry.PointD;

import com.yworks.yfiles.utils.IListEnumerable;
import util.Util;

/**
 * Created by Ama on 14.12.2017.
 */
public class EdgeComparator {


    private IEdge edge;
    private YVector compare;
    private IGraph graph;

    public EdgeComparator(IEdge edge, IGraph graph) {
        this.edge = edge;
        this.graph = graph;
        // compare = new YVector((r.bendCount() > 0 ?
        // Utilities.getBendPoint(r.getBend(0)) : Utilities.getTargetPoint(e)),
        // Utilities.getSourcePoint(e));

        //   compare = new YVector(new YPoint(-1, -1), new YPoint(0, 0));
        // compare = new YVector(new YPoint(-1, -1), new YPoint(0, 0));
    }


    public int compare(IEdge o1, IEdge o2) {
        assert o1.getSourceNode() == o2.getSourceNode() && o1.getSourceNode() == edge.getSourceNode();
        IListEnumerable<IBend> bends1= edge.getBends();
        IListEnumerable<IBend> bends2= edge.getBends();
        YVector edge1 = new YVector(
                (bends1.size() > 0 ? Utilities.getBendPoint(bends1.first())
                        : (edge.getSourceNode() == o1.getSourceNode() ? Utilities.toYPoint( o1.getTargetNode().getLayout().getCenter())
                        : Utilities.toYPoint( o1.getSourceNode().getLayout().getCenter()))), Utilities.toYPoint( edge.getSourceNode().getLayout().getCenter()));
        YVector edge2 = new YVector(
                (bends2.size() > 0 ? Utilities.getBendPoint(bends2.first())
                        : (edge.getSourceNode() == o2.getSourceNode() ? Utilities.toYPoint( o2.getTargetNode().getLayout().getCenter())
                        : Utilities.toYPoint( o2.getSourceNode().getLayout().getCenter()))), Utilities.toYPoint( edge.getSourceNode().getLayout().getCenter()));

        double angle1 = YVector.angle(compare, edge1);
        double angle2 = YVector.angle(compare, edge2);
        return (int) Math.signum(angle1 - angle2);
        // return (int) Math.signum(angle2 - angle1);
    }


}
