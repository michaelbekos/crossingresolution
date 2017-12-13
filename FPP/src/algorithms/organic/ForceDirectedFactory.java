package algorithms.organic;

/**
 * This class acts a factory for creating different types of forces for a force-directed framework.
 *
 * @author Michael A. Bekos
 */
public class ForceDirectedFactory {

    /**
     * Calculate spring forces with classical spring embedder algorithm.
     * @param graph - the input graph.
     * @param springStiffness - the stiffness of the spring.
     * @param springNaturalLength - the natural length of the spring.
     * @param threshold - a threshold value.
     * @param map - the NodeMap, where the calculated forces will be stored (might be non-empty).
     */
    public static void calculateSpringForcesEades(y.view.Graph2D graph, double springStiffness, double springNaturalLength, double threshold, y.base.NodeMap map)
    {
        java.util.ArrayList<y.geom.YVector> vectors;

        for (y.base.NodeCursor u = graph.nodes(); u.ok(); u.next())
        {
            vectors = new java.util.ArrayList<y.geom.YVector>();

            double u_x = graph.getRealizer(u.node()).getCenterX();
            double u_y = graph.getRealizer(u.node()).getCenterY();

            y.geom.YPoint p_u = new y.geom.YPoint(u_x, u_y);

            boolean[] reached = new boolean[graph.nodeCount()];
            y.algo.GraphConnectivity.reachable(graph, u.node(), false, reached);


            //Calculate Spring forces...
            for (y.base.NodeCursor v = u.node().neighbors(); v.ok(); v.next())
            {
                double v_x = graph.getRealizer(v.node()).getCenterX();
                double v_y = graph.getRealizer(v.node()).getCenterY();

                y.geom.YPoint p_v = new y.geom.YPoint(v_x, v_y);

                y.geom.YVector temp = new y.geom.YVector(p_v, p_u);
                temp.norm();

                temp.scale(threshold * springStiffness * Math.log(y.geom.YPoint.distance(p_u, p_v) / springNaturalLength));

                vectors.add(temp);
            }
            ((java.util.ArrayList<y.geom.YVector>) map.get(u.node())).addAll(vectors);
        }
    }

    /**
     * Calculate electric forces with classical spring embedder algorithm.
     * @param graph - the input graph.
     * @param electricalRepulsion - the electrical repulsion.
     * @param threshold - a threshold value.
     * @param map - the NodeMap, where the calculated forces will be stored (might be non-empty).
     */
    public static void calculateElectricForcesEades(y.view.Graph2D graph, double electricalRepulsion, double threshold, y.base.NodeMap map)
    {
        java.util.ArrayList<y.geom.YVector> vectors;

        for (y.base.NodeCursor u = graph.nodes(); u.ok(); u.next())
        {
            vectors = new java.util.ArrayList<y.geom.YVector>();

            double u_x = graph.getRealizer(u.node()).getCenterX();
            double u_y = graph.getRealizer(u.node()).getCenterY();

            y.geom.YPoint p_u = new y.geom.YPoint(u_x, u_y);

            boolean[] reached = new boolean[graph.nodeCount()];
            y.algo.GraphConnectivity.reachable(graph, u.node(), false, reached);

            //Calculate Electrical forces
            for (y.base.NodeCursor v = graph.nodes(); v.ok(); v.next())
            {
                if (v.node() == u.node() || !reached[v.node().index()]) continue;

                double v_x = graph.getRealizer(v.node()).getCenterX();
                double v_y = graph.getRealizer(v.node()).getCenterY();

                y.geom.YPoint p_v = new y.geom.YPoint(v_x, v_y);

                y.geom.YVector temp = new y.geom.YVector(p_u, p_v);
                temp.norm();

                temp.scale(threshold * electricalRepulsion / Math.pow(y.geom.YPoint.distance(p_u, p_v),2));
                vectors.add(temp);
            }
            ((java.util.ArrayList<y.geom.YVector>) map.get(u.node())).addAll(vectors);
        }
    }

    /**
     * Calculate electric forces with classical spring embedder algorithm.
     * @param graph - the input graph.
     * @param electricalRepulsion - the electrical repulsion.
     * @param threshold - a threshold value.
     * @param map - the NodeMap, where the calculated forces will be stored (might be non-empty).
     */
    public static void calculatePlyForces(y.view.Graph2D graph, double electricalRepulsion, double threshold, y.base.NodeMap map)
    {
        java.util.ArrayList<y.geom.YVector> vectors;

        for (y.base.NodeCursor u = graph.nodes(); u.ok(); u.next())
        {
            vectors = new java.util.ArrayList<y.geom.YVector>();

            double u_x = graph.getRealizer(u.node()).getCenterX();
            double u_y = graph.getRealizer(u.node()).getCenterY();

            y.geom.YPoint p_u = new y.geom.YPoint(u_x, u_y);

            double r_u = getLongestIncidentEdge(graph, u.node())/2;

            boolean[] reached = new boolean[graph.nodeCount()];
            y.algo.GraphConnectivity.reachable(graph, u.node(), false, reached);

            //Calculate Electrical forces
            for (y.base.NodeCursor v = graph.nodes(); v.ok(); v.next())
            {
                if (v.node() == u.node() || !reached[v.node().index()]) continue;

                double v_x = graph.getRealizer(v.node()).getCenterX();
                double v_y = graph.getRealizer(v.node()).getCenterY();

                y.geom.YPoint p_v = new y.geom.YPoint(v_x, v_y);

                double r_v = getLongestIncidentEdge(graph, v.node())/2;

                if (y.geom.YPoint.distance(p_u, p_v) > (r_u + r_v))
                {
                    //No overlap
                    /**
                    y.geom.YVector temp = new y.geom.YVector(p_u, p_v);
                    temp.norm();

                    temp.scale(threshold * electricalRepulsion / Math.log(Math.pow(y.geom.YPoint.distance(p_u, p_v), 2)));
                    vectors.add(temp);
                     **/
                }
                else if ((y.geom.YPoint.distance(p_u, p_v) <= Math.abs(r_u - r_v)))
                {
                    //Inside
                    /**
                    y.geom.YVector temp = new y.geom.YVector(p_u, p_v);
                    temp.norm();

                    temp.scale(threshold * electricalRepulsion / Math.log(Math.pow(y.geom.YPoint.distance(p_u, p_v), 2)));
                    vectors.add(temp);
                    **/
                }
                else
                {
                    //Intersection
                    y.geom.YVector temp = new y.geom.YVector(p_u, p_v);
                    temp.norm();

                    temp.scale(threshold * electricalRepulsion * (Math.pow((r_u + r_v - y.geom.YPoint.distance(p_u, p_v)),1/4)));
                    vectors.add(temp);
                }
            }
            ((java.util.ArrayList<y.geom.YVector>) map.get(u.node())).addAll(vectors);
        }
    }

    /**
     * Returns the length of the longest incident edge of a node.
     * @param g - the graph that the node belongs to
     * @param u - the input node
     * @return the length of the longest incident edge of a node.
     */
    private static double getLongestIncidentEdge(y.view.Graph2D g, y.base.Node u)
    {
        int x1 = (int) g.getRealizer(u).getCenterX();
        int y1 = (int) g.getRealizer(u).getCenterY();

        double longest = 0;
        for (y.base.EdgeCursor ec = u.edges(); ec.ok(); ec.next())
        {
            int x2 = (int) g.getRealizer(ec.edge().source() != u ? ec.edge().source() : ec.edge().target()).getCenterX();
            int y2 = (int) g.getRealizer(ec.edge().source() != u ? ec.edge().source() : ec.edge().target()).getCenterY();
            if (longest < Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)))
            {
                longest = Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
            }
        }
        return longest;
    }
    
    /**
     * Calculate spring forces with Fruchterman & Reingold algorithm.
     * @param graph - the input graph.
     * @param desiredEdgeLength - the desired edge length
     * @param threshold - a threshold value.
     * @param map - the NodeMap, where the calculated forces will be stored (might be non-empty).
     */
    public static void calculateAttractiveForcesFR(y.view.Graph2D graph, double desiredEdgeLength, double threshold,  y.base.NodeMap map)
    {
        for (y.base.NodeCursor u = graph.nodes(); u.ok(); u.next())
        {
            java.util.ArrayList<y.geom.YVector> vectors = new java.util.ArrayList<y.geom.YVector>();

            double u_x = graph.getRealizer(u.node()).getCenterX();
            double u_y = graph.getRealizer(u.node()).getCenterY();

            y.geom.YPoint p_u = new y.geom.YPoint(u_x, u_y);

            boolean[] reached = new boolean[graph.nodeCount()];
            y.algo.GraphConnectivity.reachable(graph, u.node(), false, reached);

            //Attractive forces...
            for (y.base.NodeCursor v = u.node().neighbors(); v.ok(); v.next())
            {
                double v_x = graph.getRealizer(v.node()).getCenterX();
                double v_y = graph.getRealizer(v.node()).getCenterY();

                y.geom.YPoint p_v = new y.geom.YPoint(v_x, v_y);

                y.geom.YVector temp = new y.geom.YVector(p_v, p_u);
                temp.norm();

                temp.scale(threshold * Math.pow(y.geom.YPoint.distance(p_u, p_v),2) / desiredEdgeLength);

                vectors.add(temp);
            }
            ((java.util.ArrayList<y.geom.YVector>) map.get(u.node())).addAll(vectors);
        }
    }

}
