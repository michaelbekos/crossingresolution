package util;

import y.base.*;
import y.geom.Triangulator;
import y.geom.YPoint;
import y.layout.planar.CombinatorialEmbedder;
import y.layout.planar.Embedder;
import y.layout.planar.FaceCursor;
import y.layout.planar.PlanarInformation;
import y.util.YRandom;

import java.awt.*;
import java.util.*;

/**
 * Created by michael on 5/15/2015.
 */
public class RandomTriangulationGenerator {
    protected int n;

    /**
     * Creates an object of type RandomTriangulationGenerator,
     * which corresponds to a family of fully-triangulated graphs.
     *
     * @param n The number indicating the member of the family
     */
    public RandomTriangulationGenerator(int n)
    {
        this.n = n;
    }

    /**
     * generates a new triangulation on n vertices.
     * @return the n-member of the family of the triangulations.
     */
    public Graph generate()
    {
        YList points = new YList();
        YRandom r = new YRandom(System.currentTimeMillis());
        int s = n*1000;
        for (int i = 0; i < n-3; i++)
        {
            double x = r.nextInt(s);
            double y = r.nextInt(s);
            points.add((x+y <= s) ? new YPoint(x, y) : new YPoint(-x, y));
        }
        points.add(new YPoint(0,s+1));
        points.add(new YPoint(s+1,-1));
        points.add(new YPoint(-s-1,-1));

        Graph g = new Graph();
        Triangulator.triangulatePoints(points, g, g.createNodeMap(), g.createEdgeMap());

        LinkedList<Edge> remove = new LinkedList<Edge>();

        EdgeMap map = g.createEdgeMap();

        for (EdgeCursor ec = g.edges(); ec.ok(); ec.next())
        {
            if (map.get(ec.edge())==null)
            {
                map.set(ec.edge(), Color.green);
                map.set(ec.edge().target().getEdgeTo(ec.edge().source()), Color.red);
            }
        }
        for (EdgeCursor ec = g.edges(); ec.ok(); ec.next())
        {
            if (map.get(ec.edge()) == Color.red)
            {
                remove.add(ec.edge());
            }
        }
        for (Edge e : remove)
        {
            g.removeEdge(e);
        }

        //Stellate all faces that are not triangular
        PlanarInformation information = new PlanarInformation(g);
        Embedder embedder = new CombinatorialEmbedder();
        embedder.setPlanarInformation(information);
        embedder.embed();

        java.util.List<Edge> extraEdges = new ArrayList<Edge>();
        for (EdgeCursor ec = g.edges(); ec.ok(); ec.next())
        {
            if (information.isInsertedEdge(ec.edge()))
            {
                extraEdges.add(ec.edge());
            }
        }

        for (FaceCursor fc = information.faces(); fc.ok(); fc.next())
        {
            if (fc.face().edges().size() >= 4)
            {
                Node v = g.createNode();
                for (EdgeCursor ec = fc.face().edges(); ec.ok(); ec.next())
                {
                    g.createEdge(v, ec.edge().source());
                }
            }
        }

        return g;
    }
}
