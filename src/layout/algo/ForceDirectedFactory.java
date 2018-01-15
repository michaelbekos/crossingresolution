package layout.algo;

import com.yworks.yfiles.algorithms.GraphConnectivity;
import com.yworks.yfiles.algorithms.YPoint;
import com.yworks.yfiles.algorithms.YVector;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.IMapper;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.layout.YGraphAdapter;

import java.util.ArrayList;
import java.util.List;

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

    private static double calculateSlopeAngle(IEdge e) {
        INode u1 = e.getSourceNode();
        double u1_x = u1.getLayout().getCenter().getX();
        double u1_y = u1.getLayout().getCenter().getY();

        INode u2 = e.getTargetNode();
        double u2_x = u2.getLayout().getCenter().getX();
        double u2_y = u2.getLayout().getCenter().getY();

        double dx = u2_x - u1_x;
        double dy = u2_y - u1_y;

        return Math.atan2(dy,dx);
    }

    /**
     * Calculate normal vectors to move each edge to the closest slope
     * @param graph - the input graph.
     * @param numberOfSlopes - number of slopes
     * @param initialAngleDeg - angle in degrees of the first slope (right is zero, clockwise is positive), all other slopes are equidistant
     * @param threshold - a threshold value.
     * @param map - the NodeMap, where the calculated forces will be stored (might be non-empty).
     */
    public static void calculateSlopedSpringForces(IGraph graph, int numberOfSlopes, double initialAngleDeg, double threshold, IMapper<INode, List<YVector>> map) {
        List<Double> slopeAngles = new ArrayList<>();
        double stepSize = (2 * Math.PI)/numberOfSlopes;
        double pos = 2 * Math.PI*(initialAngleDeg/360.0);
        for (int i = 0; i < numberOfSlopes; i++) {
            double x = 10 * Math.cos(pos);
            double y = 10 * Math.sin(pos);

            double slopeAngle = Math.atan2(y, x);
            if (slopeAngle < 0) {
                slopeAngle += Math.PI;
            }
            slopeAngles.add(slopeAngle);
            pos += stepSize;
            if (pos > 2 * Math.PI) {
                pos -= 2 * Math.PI;
            }
        }


        for (IEdge e : graph.getEdges()) {
            double edgeSlopeAngle = calculateSlopeAngle(e);
            double fittedSlopeAngle = slopeAngles.get(0);
            if (edgeSlopeAngle < 0) {
                edgeSlopeAngle += Math.PI;
            }
            if (fittedSlopeAngle < 0){
                fittedSlopeAngle += Math.PI;
            }
            for (double s : slopeAngles) {
                if (Math.abs(s - edgeSlopeAngle) < Math.abs(fittedSlopeAngle - edgeSlopeAngle)) {
                    fittedSlopeAngle = s;
                }
            }


            INode u1 = e.getSourceNode();
            double u1_x = u1.getLayout().getCenter().x;
            double u1_y = u1.getLayout().getCenter().y;

            INode u2 = e.getTargetNode();
            double u2_x = u2.getLayout().getCenter().x;
            double u2_y = u2.getLayout().getCenter().y;

            double dx = u2_x - u1_x;
            double dy = u2_y - u1_y;


            if (edgeSlopeAngle < 0) {
                edgeSlopeAngle += Math.PI;
            }
            if (fittedSlopeAngle < 0){
                fittedSlopeAngle += Math.PI;
            }

            int sign;
            if ((edgeSlopeAngle - fittedSlopeAngle) < 0) {
                sign = -1;
            } else {
                sign = 1;
            }

            YVector source_vec, target_vec;
            source_vec = new YVector(new YPoint(u1_x, u1_y), new YPoint(sign*dy + u1_x, -sign*dx + u1_y));
            target_vec = new YVector(new YPoint(u2_x, u2_y), new YPoint(-sign*dy + u2_x, sign*dx + u2_y));


            source_vec.norm();
            source_vec.scale(threshold * Math.abs(fittedSlopeAngle - edgeSlopeAngle));
            map.getValue(e.getSourceNode()).add(source_vec);

            target_vec.norm();
            target_vec.scale(threshold * Math.abs(fittedSlopeAngle - edgeSlopeAngle));
            map.getValue(e.getTargetNode()).add(target_vec);
        }
    }

}