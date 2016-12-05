package layout.algo;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.algorithms.GraphConnectivity;
import com.yworks.yfiles.algorithms.YPoint;
import com.yworks.yfiles.algorithms.YVector;
import com.yworks.yfiles.geometry.Matrix2D;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.IMapper;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.layout.YGraphAdapter;
import util.Maybe;
import util.Tuple2;
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
                //temp.scale(threshold * springStiffness * Math.log(YPoint.distance(p_u, p_v) / springNaturalLength));
                // spring force from paper
                temp.scale(threshold*(YPoint.distance(p_u, p_v)-springNaturalLength));
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

    }

    public static void calculateSinusForceEades(IGraph graph, double threshold, IMapper<INode, List<YVector>> map ) {

        for (INode n1 : graph.getNodes()) {
            for (INode n2 : graph.neighbors(INode.class, n1)) {

                List<YVector> vectors = new ArrayList<YVector>();
                YPoint point2 = new YPoint (n2.getLayout().getCenter().getX(), n2.getLayout().getCenter().getY());

                for (INode n3 : graph.neighbors(INode.class, n1)) {
                    if (n2.equals(n3)) {
                        continue;
                    }
                    List<YVector> vectors2 = new ArrayList<YVector>();
                    if (graph.degree(n1) <= 0){
                        continue;
                    }
                    double optAngle = (360 / graph.degree(n1));
                    LineSegment l1, l2;
                    l1 = new LineSegment(n1, n2);
                    l2 = new LineSegment(n1, n3);
                    Maybe<Intersection> mi = l1.intersects(l2, false);
                    if (mi.hasValue()) {
                        Intersection inter = mi.get();

                        YVector t1 = new YVector(new YPoint(l1.p1.getX(), l1.p1.getY()), new YPoint(l1.p2.getX(), l1.p2.getY()));
                        YVector t2 = new YVector(new YPoint(l2.p1.getX(), l2.p1.getY()), new YPoint(l2.p2.getX(), l2.p2.getY()));

                        t1.norm();
                        t2.norm();

                        t1.scale(threshold * Math.sin((Math.toRadians(optAngle) - Math.toRadians(inter.orientedAngle))/2.0));
                        t2.scale(threshold * Math.sin((Math.toRadians(optAngle) - Math.toRadians(inter.orientedAngle))/2.0));
                        //t1.rotate(Math.PI/2);
                        t2.scale(-1);
                        //t2.rotate(Math.PI/2);

                        vectors.add(t1);
                        vectors2.add(t2);


                    }
                    map.getValue(n3).addAll(vectors2);
                }
                map.getValue(n2).addAll(vectors);
            }
        }

    }

}
