package layout.algo;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.algorithms.GraphConnectivity;
import com.yworks.yfiles.algorithms.YPoint;
import com.yworks.yfiles.algorithms.YVector;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.IMapper;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.layout.YGraphAdapter;
import util.Tuple3;
import util.graph2d.Intersection;
import util.graph2d.LineSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by bekos on 10/28/16.
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
    public static void calculateSpringForcesEades(IGraph graph, double springStiffness, double springNaturalLength, double threshold, IMapper<INode, List<YVector>> map)
    {
        List<YVector> vectors;

        for (INode u : graph.getNodes())
        {
            vectors = new ArrayList<YVector>();

            double u_x = u.getLayout().getCenter().x;
            double u_y = u.getLayout().getCenter().y;

            YPoint p_u = new YPoint(u_x, u_y);

            //Calculate Spring forces...
            for (INode v : graph.neighbors(INode.class, u))
            {
                double v_x = v.getLayout().getCenter().x;
                double v_y = v.getLayout().getCenter().y;

                YPoint p_v = new YPoint(v_x, v_y);

                YVector temp = new YVector(p_v, p_u);
                temp.norm();

                temp.scale(threshold * springStiffness * Math.log(YPoint.distance(p_u, p_v) / springNaturalLength));

                vectors.add(temp);
            }
            map.getValue(u).addAll(vectors);
        }
    }

    /**
     * Calculate electric forces with classical spring embedder algorithm.
     * @param graph - the input graph.
     * @param electricalRepulsion - the electrical repulsion.
     * @param threshold - a threshold value.
     * @param map - the NodeMap, where the calculated forces will be stored (might be non-empty).
     */
    public static void calculateElectricForcesEades(IGraph graph, double electricalRepulsion, double threshold, IMapper<INode, List<YVector>> map)
    {
        YGraphAdapter adapter = new YGraphAdapter(graph);
        boolean[] reached = new boolean[graph.getNodes().size()];

        List<YVector> vectors;

        for (INode u : graph.getNodes())
        {
            vectors = new java.util.ArrayList<YVector>();

            double u_x = u.getLayout().getCenter().x;
            double u_y = u.getLayout().getCenter().y;

            YPoint p_u = new YPoint(u_x, u_y);

            GraphConnectivity.reachable(adapter.getYGraph(), adapter.getCopiedNode(u), false, reached);

            //Calculate Electrical forces
            for (INode v : graph.getNodes())
            {
                if (u == v || !reached[adapter.getCopiedNode(v).index()]) continue;

                double v_x = v.getLayout().getCenter().x;
                double v_y = v.getLayout().getCenter().y;

                YPoint p_v = new YPoint(v_x, v_y);

                YVector temp = new YVector(p_u, p_v);
                temp.norm();

                temp.scale(threshold * electricalRepulsion / Math.pow(YPoint.distance(p_u, p_v),2));
                vectors.add(temp);
            }
            map.getValue(u).addAll(vectors);
        }
    }

    /**
     * Calculate cosine forces
     * @param graph - the input graph.
     * @param threshold - a threshold value.
     * @param map - the NodeMap, where the calculated forces will be stored (might be non-empty).
     */
    public static void calculateCosineForcesEades(IGraph graph, double threshold, IMapper<INode, List<YVector>> map)
    {
        for (Tuple3<LineSegment, LineSegment, Intersection> c : MinimumAngle.getCrossings(graph))
        {
            // crossing of (a,b) and (c,d)
            double a_x = c.a.p1.getX();
            double a_y = c.a.p1.getY();

            double b_x = c.a.p2.getX();
            double b_y = c.a.p2.getY();

            double c_y = c.b.p1.getY();
            double c_x = c.b.p1.getX();

            double d_x = c.b.p2.getX();
            double d_y = c.b.p2.getY();

            YPoint p_a = new YPoint(a_x, a_y);
            YPoint p_b = new YPoint(b_x, b_y);
            YPoint p_c = new YPoint(a_x, a_y);
            YPoint p_d = new YPoint(b_x, b_y);

            YVector firstTemp = new YVector(p_a, p_b);
            YVector secTemp = new YVector(p_c,p_d);

            firstTemp.norm();
            secTemp.norm();

            firstTemp.scale(threshold * Math.cos(c.c.angle));
            map.getValue(c.a.n1.get()).add(firstTemp); // a
            firstTemp.scale(-1);
            map.getValue(c.a.n2.get()).add(firstTemp); // b

            secTemp.scale(threshold * Math.cos(c.c.angle));
            map.getValue(c.b.n1.get()).add(secTemp); // c
            secTemp.scale(-1);
            map.getValue(c.b.n2.get()).add(secTemp); // d

        }


    }

}
