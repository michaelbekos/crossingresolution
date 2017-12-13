/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

/**
 *
 * @author fouli
 */
public class CyclicEdgeComparator implements java.util.Comparator<y.base.Edge>
{
    private y.view.Graph2D graph;

    public CyclicEdgeComparator(y.view.Graph2D graph)
    {
        this.graph = graph;
    }

    /**
     * Compares two edges that must share a common end point.
     */
    public int compare(y.base.Edge e1, y.base.Edge e2)
    {
        y.base.Node c;
        y.base.Node u;
        y.base.Node v;

        if (e1.source() == e2.source())
        {
            c = e1.source();
            u = e1.target();
            v = e2.target();
        }
        else if (e1.source() == e2.target())
        {
            c = e1.source();
            u = e1.target();
            v = e2.source();
        }
        else if (e1.target() == e2.source())
        {
            c = e1.target();
            u = e1.source();
            v = e2.target();
        }
        else if (e1.target() == e2.target())
        {
            c = e1.target();
            u = e1.source();
            v = e2.source();
        }
        else
        {
            return -1;
        }

        y.geom.YVector cVector = new y.geom.YVector(graph.getCenterX(c)+1, graph.getCenterY(c), graph.getCenterX(c), graph.getCenterY(c));
        y.geom.YVector uVector = new y.geom.YVector(graph.getCenterX(u), graph.getCenterY(u), graph.getCenterX(c), graph.getCenterY(c));
        y.geom.YVector vVector = new y.geom.YVector(graph.getCenterX(v), graph.getCenterY(v), graph.getCenterX(c), graph.getCenterY(c));

        double tu = y.geom.YVector.angle(uVector, cVector);
        double tv = y.geom.YVector.angle(vVector, cVector);

        if (tu == tv)
        {
            return 0;
        }
        else if (tu > tv)
        {
            if (e1.source() == u || e1.target() == u)
            {
                return 1;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            if (e1.source() == u || e1.target() == u)
            {
                return -1;
            }
            else
            {
                return 1;
            }
        }
    }
}
