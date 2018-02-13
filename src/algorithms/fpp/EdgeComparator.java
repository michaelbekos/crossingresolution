package algorithms.fpp;


import com.yworks.yfiles.algorithms.Edge;
import com.yworks.yfiles.algorithms.Graph;
import com.yworks.yfiles.algorithms.YPoint;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.algorithms.YVector;

import com.yworks.yfiles.utils.IListEnumerable;

import java.util.Comparator;


/**
 * Created by Ama on 14.12.2017.
 */

public class EdgeComparator implements Comparator<Edge> {

    private Edge e;
    private YVector compare;
    private Graph g;

    public EdgeComparator(Edge e, Graph g) {
       /* this.e = e;
        this.g = g;
        EdgeRealizer r = g.getRealizer(e);
        compare = new YVector((r.bendCount() > 0 ?
                Utilities.getBendPoint(r.getBend(0)) : Utilities.getTargetPoint(e)),
                Utilities.getSourcePoint(e));
        compare = new YVector(new YPoint(-1, -1), new YPoint(0, 0));
        */
    }

    @Override
    public int compare(Edge o1, Edge o2) {
        /*
        assert o1.source() == o2.source() && o1.source() == e.source();
        EdgeRealizer r1 = g.getRealizer(o1);
        EdgeRealizer r2 = g.getRealizer(o2);
        YVector edge1 = new YVector(
                (r1.bendCount() > 0 ? getBendPoint(r1.getBend(0))
                        : (e.source() == o1.source() ? getTargetPoint(o1)
                        : getSourcePoint(o1))), getSourcePoint(e));
        YVector edge2 = new YVector(
                (r2.bendCount() > 0 ? getBendPoint(r2.getBend(0))
                        : (e.source() == o2.source() ? getTargetPoint(o2)
                        : getSourcePoint(o2))), getSourcePoint(e));
        double angle1 = YVector.angle(compare, edge1);
        double angle2 = YVector.angle(compare, edge2);
        return (int) Math.signum(angle1 - angle2);
        // return (int) Math.signum(angle2 - angle1);
        */
        return  -1; //only for compl
    }

   /* public static YPoint getBendPoint(Bend b) {
        return new YPoint(b.getX(), b.getY());
    }
*/
    /**
     *
     * @param e
     * @return a point lying on the center of the source node of the given edge
     */
/*
    public static YPoint getSourcePoint(Edge e) {
        Graph2D g = (Graph2D) e.getGraph();
        NodeRealizer realizer = g.getRealizer(e.source());
        return new YPoint(realizer.getCenterX(), realizer.getCenterY());
    }

    /**
     *
     * @param e
     * @return a point lying on the center of the target node of the given edge
     */
/*
    public static YPoint getTargetPoint(Edge e) {
        Graph2D g = (Graph2D) e.getGraph();
        NodeRealizer realizer = g.getRealizer(e.target());
        return new YPoint(realizer.getCenterX(), realizer.getCenterY());
    }
    */
}
