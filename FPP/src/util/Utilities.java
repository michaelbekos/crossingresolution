package util;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.layout.planar.CombinatorialEmbedder;
import y.layout.planar.Face;
import y.layout.planar.FaceCursor;
import y.layout.planar.PlanarInformation;
import y.view.Graph2D;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author michael
 */
public class Utilities {

    public static void displayGraphRandomly(y.view.Graph2D graph)
    {
        java.util.Random r = new java.util.Random(System.currentTimeMillis());

        for (y.base.NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
            graph.getRealizer(nc.node()).setLocation(r.nextInt(graph.getCurrentView().getComponent().getWidth()), r.nextInt(graph.getCurrentView().getComponent().getHeight()));
        }
    }

    /**
     * Returns the common node of two edges; null if such does not exist
     * @param e1 the first edge
     * @param e2 the second edge
     * @return the common node, or null if no such exists
     */
    public static Node commonNode(Edge e1, Edge e2) {
        if (e1.source() == e2.source() || e1.source() == e2.target())
        {
            return e1.source();
        }
        else if (e1.target() == e2.source() || e1.target() == e2.target())
        {
            return e1.target();
        }
        else
        {
            return null;
        }
    }

    public static void swap(Object a, Object b)
    {
        Object temp = a;
        a = b;
        b = temp;
    }

    public static int maxDegree(y.base.Graph graph)
    {
        int maxDegree = -1;
        for (y.base.NodeCursor v = graph.nodes(); v.ok(); v.next())
        {
            maxDegree = Math.max(maxDegree, v.node().degree());
        }
        return maxDegree;
    }

    public static double calculateAngularResolution(y.view.Graph2DView view)
    {
        double angle = Double.MAX_VALUE;

        for (y.base.NodeCursor v = view.getGraph2D().nodes(); v.ok(); v.next())
        {
            //Sort in cyclic order the adjacent edges
            y.base.EdgeList edgeList = new y.base.EdgeList();
            for (y.base.EdgeCursor e = v.node().edges(); e.ok(); e.next())
            {
                edgeList.add(e.edge());
            }

            if (edgeList.size()==1) continue;

            edgeList.sort(new util.CyclicEdgeComparator(view.getGraph2D()));

            for (int i = 0; i < edgeList.size(); i++)
            {
               //e1=(v,u1) and e2=(v,u2) are adjacent edges at v.

               y.base.Edge e1 = (y.base.Edge) edgeList.get(i);
               y.base.Edge e2 = (y.base.Edge) edgeList.get((i+1)%edgeList.size());


               y.base.Node u1 = ( v.node() == e1.source() ) ? e1.target() : e1.source();
               y.base.Node u2 = ( v.node() == e2.source() ) ? e2.target() : e2.source();


               y.geom.YPoint p_v = new y.geom.YPoint(view.getGraph2D().getCenterX(v.node()), view.getGraph2D().getCenterY(v.node()));
               y.geom.YPoint p_u1 = new y.geom.YPoint(view.getGraph2D().getCenterX(u1), view.getGraph2D().getCenterY(u1));
               y.geom.YPoint p_u2 = new y.geom.YPoint(view.getGraph2D().getCenterX(u2), view.getGraph2D().getCenterY(u2));

               y.geom.YVector v_u1 = new y.geom.YVector(p_u1, p_v);
               y.geom.YVector v_u2 = new y.geom.YVector(p_u2, p_v);

               angle = Math.min(angle, y.geom.YVector.angle(v_u2, v_u1));
            }
        }
        return 180*angle/Math.PI;
    }

    public static double calculateCrossingResolution(y.view.Graph2DView view)
    {
        double angle = Double.MAX_VALUE;

        y.base.Edge[] edgeArray = new y.base.Edge[view.getGraph2D().edgeCount()];
        int k=0;
        for (y.base.EdgeCursor e = view.getGraph2D().edges(); e.ok(); e.next())
        {
            edgeArray[k] = e.edge();
            k++;
        }
        for (int i=0; i<edgeArray.length; i++)
        {
            for (int j=i+1; j<edgeArray.length; j++)
            {
                // e1 = (u1,u2) u1<u2 and e2 = (v1,v2) v1<v2

                y.base.Node u1 = (view.getGraph2D().getRealizer(edgeArray[i].source()).getCenterX() <= view.getGraph2D().getRealizer(edgeArray[i].target()).getCenterX()) ? edgeArray[i].source() : edgeArray[i].target();
                y.base.Node u2 = (view.getGraph2D().getRealizer(edgeArray[i].source()).getCenterX() >  view.getGraph2D().getRealizer(edgeArray[i].target()).getCenterX()) ? edgeArray[i].source() : edgeArray[i].target();
                y.base.Node v1 = (view.getGraph2D().getRealizer(edgeArray[j].source()).getCenterX() <= view.getGraph2D().getRealizer(edgeArray[j].target()).getCenterX()) ? edgeArray[j].source() : edgeArray[j].target();
                y.base.Node v2 = (view.getGraph2D().getRealizer(edgeArray[j].source()).getCenterX() >  view.getGraph2D().getRealizer(edgeArray[j].target()).getCenterX()) ? edgeArray[j].source() : edgeArray[j].target();

                y.geom.YPoint p_u1 = new y.geom.YPoint(view.getGraph2D().getRealizer(u1).getCenterX(), view.getGraph2D().getRealizer(u1).getCenterY());
                y.geom.YPoint p_u2 = new y.geom.YPoint(view.getGraph2D().getRealizer(u2).getCenterX(), view.getGraph2D().getRealizer(u2).getCenterY());
                y.geom.YPoint p_v1 = new y.geom.YPoint(view.getGraph2D().getRealizer(v1).getCenterX(), view.getGraph2D().getRealizer(v1).getCenterY());
                y.geom.YPoint p_v2 = new y.geom.YPoint(view.getGraph2D().getRealizer(v2).getCenterX(), view.getGraph2D().getRealizer(v2).getCenterY());

                y.geom.LineSegment l_e1 = new y.geom.LineSegment(p_u1, p_u2);
                y.geom.LineSegment l_e2 = new y.geom.LineSegment(p_v1, p_v2);

                y.geom.YPoint p = y.geom.LineSegment.getIntersection(l_e2, l_e1);

                if (p != null)
                {
                    //e1 intersects e2 at p
                    y.geom.YVector vector_p_u1 = new y.geom.YVector(p_u1, p);
                    y.geom.YVector vector_p_u2 = new y.geom.YVector(p_u2, p);

                    y.geom.YVector vector_p_v1 = new y.geom.YVector(p_v1, p);
                    y.geom.YVector vector_p_v2 = new y.geom.YVector(p_v2, p);

                    if (y.geom.YVector.angle(vector_p_u2, vector_p_v2) < Math.PI/2 )
                    {
                        angle = Math.min(angle, y.geom.YVector.angle(vector_p_u2, vector_p_v2));
                    }
                    else if (y.geom.YVector.angle(vector_p_v2, vector_p_u2) < Math.PI/2 )
                    {
                        angle = Math.min(angle, y.geom.YVector.angle(vector_p_v2, vector_p_u2));
                    }
                    else if (y.geom.YVector.angle(vector_p_v1, vector_p_u2) < Math.PI/2 )
                    {
                        angle = Math.min(angle, y.geom.YVector.angle(vector_p_v1, vector_p_u2));
                    }
                    else if (y.geom.YVector.angle(vector_p_u1, vector_p_v2) < Math.PI/2 )
                    {
                        angle = Math.min(angle, y.geom.YVector.angle(vector_p_u1, vector_p_v2));
                    }
                    else
                    {
                        //javax.swing.JOptionPane.showMessageDialog(null, "unexpected case");
                    }
                }
            }
        }
        return 180*angle/Math.PI;
    }

    public static void exportToAdjacencyMatrixFormat(Graph2D g, String fileName)
    {
    	StringBuffer output = new StringBuffer();
        output.append("p be " + g.N() + " " + g.E() + "\n");
    	y.base.NodeMap map = g.createNodeMap();
    	int k = 1;
    	for (NodeCursor nc = g.nodes(); nc.ok(); nc.next())
    	{
    		map.setInt(nc.node(), k);
    		k++;
    	}
    	boolean[][] edges = new boolean[g.N()+1][g.N()+1];
    	for (EdgeCursor ec = g.edges(); ec.ok(); ec.next())
    	{
    		edges[map.getInt(ec.edge().source())][map.getInt(ec.edge().target())] = true;
    		edges[map.getInt(ec.edge().target())][map.getInt(ec.edge().source())] = true;
    	}

        for (int i=1; i<edges.length; i++)
        {
            for (int j=edges.length-1; j>i; j--)
            {
                if (edges[i][j]) {
                    output.append(i + " " + j + "\n");
                }
            }
        }

        /**
         * Standard adjacency matrix format
         *
        for (int i=1; i<edges.length; i++)
        {
            for (int j=edges.length-1; j>i; j--)
            {
                output.append("<"+i+","+j+"> "+(edges[i][j] ? "1" : "0")+", ");
            }
        }
        **/
    	
    	
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(fileName, "UTF-8");
            writer.println(output.toString());
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private static Face getNextFace(PlanarInformation plan, HashMap<Face, Boolean> faceHashMap)
    {
        for (FaceCursor fc = plan.faces(); fc.ok(); fc.next())
        {
            for (EdgeCursor ec = fc.face().edges(); ec.ok(); ec.next())
            {
                if (faceHashMap.get(fc.face()) == null && faceHashMap.get(plan.faceOf(plan.getReverse(ec.edge()))) != null)
                {
                    return fc.face();
                }
            }
        }
        return null;
    }

}
