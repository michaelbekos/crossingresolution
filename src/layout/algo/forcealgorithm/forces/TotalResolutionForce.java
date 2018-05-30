package layout.algo.forcealgorithm.forces;

import com.yworks.yfiles.algorithms.LineSegment;
import com.yworks.yfiles.algorithms.YPoint;
import com.yworks.yfiles.algorithms.YVector;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.util.ArrayList;
import java.util.Collection;

public class TotalResolutionForce implements IForce {
    private final IGraph graph;
    private AbstractLayoutInterfaceItem<Double> threshold;
    private AbstractLayoutInterfaceItem<Boolean> activated;
    private AbstractLayoutInterfaceItem<Boolean> activated1;
    private AbstractLayoutInterfaceItem<Boolean> activated2;
    private AbstractLayoutInterfaceItem<Boolean> activated3;
    private AbstractLayoutInterfaceItem<Boolean> activated4;

    private AbstractLayoutInterfaceItem<Double> force1;
    private AbstractLayoutInterfaceItem<Double> force2;
    private AbstractLayoutInterfaceItem<Double> force3;
    private AbstractLayoutInterfaceItem<Double> force4;

    ArrayList<AbstractLayoutInterfaceItem> itemList;

    public TotalResolutionForce(IGraph graph) {
        this.graph = graph;
    }

    @Override
    public void init(ILayoutInterfaceItemFactory itemFactory, Collection<AbstractLayoutInterfaceItem<Boolean>> toggleableParameters) {
        itemList = new ArrayList<>();
        threshold = itemFactory.doubleParameter("Total Resolution Force", 0.0, 1.0);
        threshold.setValue(1.0);
        itemList.add(threshold);

        activated = itemFactory.toggleableParameter(threshold);
        activated.setValue(true);
        toggleableParameters.add(activated);
        itemList.add(activated);

        force1 = itemFactory.doubleParameter("Crossing1", 0.0, 0.2);
        force1.setValue(0.13);
        itemList.add(force1);

        activated1 = itemFactory.toggleableParameter(force1);
        activated1.setValue(true);
        toggleableParameters.add(activated1);
        itemList.add(activated1);

        force2 = itemFactory.doubleParameter("Crossing2", 0.0, 0.1);
        force2.setValue(0.05);
        itemList.add(force2);


        activated2 = itemFactory.toggleableParameter(force2);
        activated2.setValue(true);
        toggleableParameters.add(activated2);
        itemList.add(activated2);

        force3 = itemFactory.doubleParameter("Angular1", 0.0, 0.1);
        force3.setValue(0.03);
        itemList.add(force3);


        activated3 = itemFactory.toggleableParameter(force3);
        activated3.setValue(true);
        toggleableParameters.add(activated3);
        itemList.add(activated3);


        force4 = itemFactory.doubleParameter("Angular2", 0.0, 0.1);
        force4.setValue(0.008);
        itemList.add(force4);

        activated4 = itemFactory.toggleableParameter(force4);
        activated4.setValue(true);
        toggleableParameters.add(activated4);
        itemList.add(activated4);
    }

    @Override
    public ArrayList<AbstractLayoutInterfaceItem> getItems(){
        return itemList;
    }

    /**
     * Calculate vectors with total resolution algorithm i.e. calculateTotalResolutionForces
     */
    @Override
    public Mapper<INode, PointD> calculate(Mapper<INode, PointD> forces, Mapper<INode, PointD> nodePositions) {
        if (!activated.getValue()) {
            return forces;
        }

        final double MAX_FORCES = 10;
        double c1,c2,c3, c4;
        c1 = activated1.getValue() ? force1.getValue() : 0;
        c2 = activated2.getValue() ? force2.getValue() : 0;
        c3 = activated3.getValue() ? force3.getValue() : 0;
        c4 = activated4.getValue() ? force4.getValue() : 0;

        //Calculate Total Resolution forces
        for (INode v : graph.getNodes()) {

            //Sort in cyclic order the adjacent edges
            ArrayList<IEdge> edgeList = new ArrayList<>();
            for (IEdge e : graph.edgesAt(v)){   //TODO: check ports
                edgeList.add(e);
            }

            if (edgeList.size()==1) continue;
            //Cyclic Edge Comparator - Compares two edges that must share a common end point.
            edgeList.sort(((IEdge e1, IEdge e2) -> {
                INode c;
                INode w;    //u
                INode x;    //v

                if (e1.getSourceNode() == e2.getSourceNode()) {
                    c = e1.getSourceNode();
                    w = e1.getTargetNode();
                    x = e2.getTargetNode();
                }
                else if (e1.getSourceNode() == e2.getTargetNode()) {
                    c = e1.getSourceNode();
                    w = e1.getTargetNode();
                    x = e2.getSourceNode();
                }
                else if (e1.getTargetNode() == e2.getSourceNode()) {
                    c = e1.getTargetNode();
                    w = e1.getSourceNode();
                    x = e2.getTargetNode();
                }
                else if (e1.getTargetNode() == e2.getTargetNode()) {
                    c = e1.getTargetNode();
                    w = e1.getSourceNode();
                    x = e2.getSourceNode();
                }
                else {
                    return -1;
                }

                YVector cVector = new YVector(c.getLayout().getCenter().getX()+1, c.getLayout().getCenter().getY(), c.getLayout().getCenter().getX(), c.getLayout().getCenter().getY());
                YVector uVector = new YVector(w.getLayout().getCenter().getX(), w.getLayout().getCenter().getY(), c.getLayout().getCenter().getX(), c.getLayout().getCenter().getY());
                YVector vVector = new YVector(x.getLayout().getCenter().getX(), x.getLayout().getCenter().getY(), c.getLayout().getCenter().getX(), c.getLayout().getCenter().getY());

                double tu = YVector.angle(uVector, cVector);
                double tv = YVector.angle(vVector, cVector);

                if (tu == tv) {
                    return 0;
                }
                else if (tu > tv) {
                    if (e1.getSourceNode() == w || e1.getTargetNode() == w) {
                        return 1;
                    }
                    else {
                        return -1;
                    }
                }
                else {
                    if (e1.getSourceNode() == w || e1.getTargetNode() == w) {
                        return -1;
                    }
                    else {
                        return 1;
                    }
                }
            }));

            for (int i = 0; i < edgeList.size(); i++) {

                IEdge e1 = edgeList.get(i);
                IEdge e2 = edgeList.get((i+1)%edgeList.size());


                INode u1 = ( v == e1.getSourceNode() ) ? e1.getTargetNode() : e1.getSourceNode();
                INode u2 = ( v == e2.getSourceNode() ) ? e2.getTargetNode() : e2.getSourceNode();


                YPoint p_v = new YPoint(v.getLayout().getCenter().getX(), v.getLayout().getCenter().getY());
                YPoint p_u1 = new YPoint(u1.getLayout().getCenter().getX(), u1.getLayout().getCenter().getY());
                YPoint p_u2 = new YPoint(u2.getLayout().getCenter().getX(), u2.getLayout().getCenter().getY());

                YVector v_u1 = new YVector(p_u1, p_v);
                YVector v_u2 = new YVector(p_u2, p_v);

                YVector u1_u2 = new YVector(p_u2, p_u1);
                YVector u2_u1 = new YVector(p_u1, p_u2);

                u1_u2.norm();
                u2_u1.norm();

                double angle = YVector.angle(v_u2, v_u1);

                double[] f = calculateForceFactorOnEdgeEdgeRepulsion(c3, c4, angle, v.getPorts().size(), p_v, p_u1, p_u2);


                // Apply first force on perpendicular vector
                u1_u2.scale(f[0]);
                PointD u1_u2_f = new PointD(u1_u2.getX(), u1_u2.getY());
                u1_u2_f = PointD.times(u1_u2_f, threshold.getValue());
                u1_u2_f = (u1_u2_f.getVectorLength() < MAX_FORCES) ? u1_u2_f : PointD.times(u1_u2_f, MAX_FORCES/u1_u2_f.getVectorLength());
                forces.setValue(u1, PointD.add(forces.getValue(u1), u1_u2_f));

                u2_u1.scale(f[0]);
                PointD u2_u1_f = new PointD(u2_u1.getX(), u2_u1.getY());
                u2_u1_f = PointD.times(u2_u1_f, threshold.getValue());
                u2_u1_f = (u2_u1_f.getVectorLength() < MAX_FORCES) ? u2_u1_f : PointD.times(u2_u1_f, MAX_FORCES/u2_u1_f.getVectorLength());
                forces.setValue(u2, PointD.add(forces.getValue(u2), u2_u1_f));

                // Apply second force on bisection vector
                YVector bisection = bisectionVector(v_u1, v_u2);

                YVector perpendicular_u1 = YVector.orthoNormal(bisection);
                YVector perpendicular_u2 = YVector.orthoNormal(bisection);

                PointD force = new PointD(perpendicular_u1.getX(), perpendicular_u1.getY()).getNormalized();
                force = PointD.times(force, threshold.getValue() * f[1]);
                force = (force.getVectorLength() < MAX_FORCES) ? force : PointD.times(force, MAX_FORCES/force.getVectorLength());
                forces.setValue(u1, PointD.add(forces.getValue(u1), force));

                PointD force2 = new PointD(perpendicular_u2.getX(), perpendicular_u2.getY()).getNormalized();
                force2 = PointD.times(force2, -1 * threshold.getValue() * f[1]);
                force2 = (force2.getVectorLength() < MAX_FORCES) ? force2 : PointD.times(force2, MAX_FORCES/force2.getVectorLength());
                forces.setValue(u2, PointD.add(forces.getValue(u2), force2));
            }

        }

        IEdge[] edgeArray = new IEdge[graph.getEdges().size()];
        int k=0;
        for (IEdge e : graph.getEdges()) {
            edgeArray[k] = e;
            k++;
        }
        for (int i=0; i<edgeArray.length; i++) {
            for (int j=i+1; j<edgeArray.length; j++) {
                // e1 = (u1,u2) u1<u2 and e2 = (v1,v2) v1<v2

                INode u1 = (edgeArray[i].getSourceNode()).getLayout().getCenter().getX() <= edgeArray[i].getTargetNode().getLayout().getCenter().getX() ? edgeArray[i].getSourceNode() : edgeArray[i].getTargetNode();
                INode u2 = (edgeArray[i].getSourceNode()).getLayout().getCenter().getX() >  edgeArray[i].getTargetNode().getLayout().getCenter().getX() ? edgeArray[i].getSourceNode() : edgeArray[i].getTargetNode();
                INode v1 = (edgeArray[j].getSourceNode()).getLayout().getCenter().getX() <= edgeArray[j].getTargetNode().getLayout().getCenter().getX() ? edgeArray[j].getSourceNode() : edgeArray[j].getTargetNode();
                INode v2 = (edgeArray[j].getSourceNode()).getLayout().getCenter().getX() >  edgeArray[j].getTargetNode().getLayout().getCenter().getX() ? edgeArray[j].getSourceNode() : edgeArray[j].getTargetNode();

                YPoint p_u1 = new YPoint(u1.getLayout().getCenter().getX(), u1.getLayout().getCenter().getY());
                YPoint p_u2 = new YPoint(u2.getLayout().getCenter().getX(), u2.getLayout().getCenter().getY());
                YPoint p_v1 = new YPoint(v1.getLayout().getCenter().getX(), v1.getLayout().getCenter().getY());
                YPoint p_v2 = new YPoint(v2.getLayout().getCenter().getX(), v2.getLayout().getCenter().getY());

                LineSegment l_e1 = new LineSegment(p_u1, p_u2);
                LineSegment l_e2 = new LineSegment(p_v1, p_v2);

                YPoint p = LineSegment.getIntersection(l_e2, l_e1);

                if (p != null) {
                    //e1 intersects e2 at p
                    YVector vector_p_u1 = new YVector(p_u1, p);
                    YVector vector_p_u2 = new YVector(p_u2, p);

                    YVector vector_p_v1 = new YVector(p_v1, p);
                    YVector vector_p_v2 = new YVector(p_v2, p);

                    YVector vector_u1_p = new YVector(p, p_u1);
                    YVector vector_v1_p = new YVector(p, p_v1);

                    YVector orthonormal, bisection;
                    double angle, uDirection, vDirection, u1Direction, v1Direction, u2Direction, v2Direction;

                    if (YVector.angle(vector_p_u2, vector_p_v2) < Math.PI/2 ) {
                        angle = YVector.angle(vector_p_u2, vector_p_v2);
                        bisection = bisectionVector(vector_p_u2, vector_p_v2);

                        u1Direction =  1;
                        v1Direction = -1;
                        u2Direction = -1;
                        v2Direction =  1;
                    }
                    else if (YVector.angle(vector_p_v2, vector_p_u2) < Math.PI/2 ) {
                        angle = YVector.angle(vector_p_v2, vector_p_u2);
                        bisection = bisectionVector(vector_p_v2, vector_p_u2);

                        u1Direction = -1;
                        v1Direction =  1;
                        u2Direction =  1;
                        v2Direction = -1;
                    }
                    else if (YVector.angle(vector_p_v1, vector_p_u2) < Math.PI/2 ) {
                        angle = YVector.angle(vector_p_v1, vector_p_u2);
                        bisection = bisectionVector(vector_p_v1, vector_p_u2);

                        u1Direction = -1;
                        v1Direction = -1;
                        u2Direction =  1;
                        v2Direction =  1;
                    }
                    else if (YVector.angle(vector_p_u1, vector_p_v2) < Math.PI/2 ) {
                        angle = YVector.angle(vector_p_u1, vector_p_v2);
                        bisection = bisectionVector(vector_p_u1, vector_p_v2);

                        u1Direction = -1;
                        v1Direction = -1;
                        u2Direction =  1;
                        v2Direction =  1;
                    }
                    else {
                        continue;
                    }

                    double[] f_1 = calculateForceFactorOnEdgeCrossing(c1, c2, angle, p, p_u2, p_v2);
                    double[] f_2 = calculateForceFactorOnEdgeCrossing(c1, c2, angle, p, p_u1, p_v1);

                    double[] f_u1_v1 = calculateForceFactorOnEdgeCrossing(c1, c2, angle, p, p_u1, p_v1);
                    double[] f_u1_v2 = calculateForceFactorOnEdgeCrossing(c1, c2, angle, p, p_u1, p_v2);
                    double[] f_u2_v1 = calculateForceFactorOnEdgeCrossing(c1, c2, angle, p, p_u2, p_v1);
                    double[] f_u2_v2 = calculateForceFactorOnEdgeCrossing(c1, c2, angle, p, p_u2, p_v2);


                    //Calculate vector on u2
                    PointD p_u1_D = new PointD(p_u1.getX(), p_u1.getY());
                    PointD p_u2_D = new PointD(p_u2.getX(), p_u2.getY());
                    PointD p_v1_D = new PointD(p_v1.getX(), p_v1.getY());
                    PointD p_v2_D = new PointD(p_v2.getX(), p_v2.getY());

                    PointD v1_u2_force = PointD.subtract(p_u2_D, p_v1_D).getNormalized();
                    PointD u2_v2_force = PointD.subtract(p_v2_D, p_u2_D).getNormalized();
                    PointD v2_u1_force = PointD.subtract(p_u1_D, p_v2_D).getNormalized();
                    PointD u1_v1_force = PointD.subtract(p_v1_D, p_u1_D).getNormalized();

                    PointD u2_v1_force = PointD.subtract(p_v1_D, p_u2_D).getNormalized();
                    PointD v2_u2_force = PointD.subtract(p_u2_D, p_v2_D).getNormalized();
                    PointD u1_v2_force = PointD.subtract(p_v2_D, p_u1_D).getNormalized();
                    PointD v1_u1_force = PointD.subtract(p_u1_D, p_v1_D).getNormalized();

                    v1_u2_force = PointD.times(v1_u2_force, threshold.getValue() * f_u2_v1[0]);
                    u2_v2_force = PointD.times(u2_v2_force, threshold.getValue() * f_u2_v2[0]);
                    v2_u1_force = PointD.times(v2_u1_force, threshold.getValue() * f_u2_v1[0]);
                    u1_v1_force = PointD.times(u1_v1_force, threshold.getValue() * f_u1_v1[0]);

                    u2_v1_force = PointD.times(u2_v1_force, threshold.getValue() * f_u2_v1[0]);
                    v2_u2_force = PointD.times(v2_u2_force, threshold.getValue() * f_u2_v2[0]);
                    u1_v2_force = PointD.times(u1_v2_force, threshold.getValue() * f_u1_v2[0]);
                    v1_u1_force = PointD.times(v1_u1_force, threshold.getValue() * f_u1_v1[0]);

                    v1_u2_force = (v1_u2_force.getVectorLength() < MAX_FORCES) ? v1_u2_force : PointD.times(v1_u2_force, MAX_FORCES/v1_u2_force.getVectorLength());
                    u2_v2_force = (u2_v2_force.getVectorLength() < MAX_FORCES) ? u2_v2_force : PointD.times(u2_v2_force, MAX_FORCES/u2_v2_force.getVectorLength());
                    v2_u1_force = (v2_u1_force.getVectorLength() < MAX_FORCES) ? v2_u1_force : PointD.times(v2_u1_force, MAX_FORCES/v2_u1_force.getVectorLength());
                    u1_v1_force = (u1_v1_force.getVectorLength() < MAX_FORCES) ? u1_v1_force : PointD.times(u1_v1_force, MAX_FORCES/u1_v1_force.getVectorLength());

                    u2_v1_force = (u2_v1_force.getVectorLength() < MAX_FORCES) ? u2_v1_force : PointD.times(u2_v1_force, MAX_FORCES/u2_v1_force.getVectorLength());
                    v2_u2_force = (v2_u2_force.getVectorLength() < MAX_FORCES) ? v2_u2_force : PointD.times(v2_u2_force, MAX_FORCES/v2_u2_force.getVectorLength());
                    u1_v2_force = (u1_v2_force.getVectorLength() < MAX_FORCES) ? u1_v2_force : PointD.times(u1_v2_force, MAX_FORCES/u1_v2_force.getVectorLength());
                    v1_u1_force = (v1_u1_force.getVectorLength() < MAX_FORCES) ? v1_u1_force : PointD.times(v1_u1_force, MAX_FORCES/v1_u1_force.getVectorLength());


                    forces.setValue(u1, PointD.add(forces.getValue(u1), u1_v1_force));
                    forces.setValue(u1, PointD.add(forces.getValue(u1), u1_v2_force)); //fixed probable mistake, i.e. u1_v1 -> u1_v2

                    forces.setValue(u2, PointD.add(forces.getValue(u2), u2_v1_force));
                    forces.setValue(u2, PointD.add(forces.getValue(u2), u2_v2_force));

                    forces.setValue(v1, PointD.add(forces.getValue(v1), v1_u1_force));
                    forces.setValue(v1, PointD.add(forces.getValue(v1), v1_u2_force));

                    forces.setValue(v2, PointD.add(forces.getValue(v2), v2_u1_force));
                    forces.setValue(v2, PointD.add(forces.getValue(v2), v2_u2_force));



                    //YVector perpendicular_1 = YVector.orthoNormal(bisection);       //new YVector(-var0.getY() / var0.len, var0.getX() / var0.len);
                    PointD perpendicular_1_f = new PointD(-bisection.getY() / bisection.length(), bisection.getX() / bisection.length());
                    PointD perpendicular_2_f = new PointD(-bisection.getY() / bisection.length(), bisection.getX() / bisection.length());
                    PointD perpendicular_3_f = new PointD(-bisection.getY() / bisection.length(), bisection.getX() / bisection.length());
                    PointD perpendicular_4_f = new PointD(-bisection.getY() / bisection.length(), bisection.getX() / bisection.length());

                    perpendicular_1_f = PointD.times(perpendicular_1_f, v2Direction * threshold.getValue() * f_1[1]);
                    perpendicular_1_f = (perpendicular_1_f.getVectorLength() < MAX_FORCES) ? perpendicular_1_f : PointD.times(perpendicular_1_f, MAX_FORCES/perpendicular_1_f.getVectorLength() );
                    perpendicular_2_f = PointD.times(perpendicular_2_f, u2Direction * threshold.getValue() * f_1[1]);
                    perpendicular_2_f = (perpendicular_2_f.getVectorLength() < MAX_FORCES) ? perpendicular_2_f : PointD.times(perpendicular_2_f, MAX_FORCES/perpendicular_2_f.getVectorLength() );
                    perpendicular_3_f = PointD.times(perpendicular_3_f, u1Direction * threshold.getValue() * f_2[1]);
                    perpendicular_3_f = (perpendicular_3_f.getVectorLength() < MAX_FORCES) ? perpendicular_3_f : PointD.times(perpendicular_3_f, MAX_FORCES/perpendicular_3_f.getVectorLength() );
                    perpendicular_4_f = PointD.times(perpendicular_4_f, v1Direction * threshold.getValue() * f_2[1]);
                    perpendicular_4_f = (perpendicular_4_f.getVectorLength() < MAX_FORCES) ? perpendicular_4_f : PointD.times(perpendicular_4_f, MAX_FORCES/perpendicular_4_f.getVectorLength() );

                    forces.setValue(v2, PointD.add(forces.getValue(v2), perpendicular_1_f));
                    forces.setValue(u2, PointD.add(forces.getValue(u2), perpendicular_2_f));
                    forces.setValue(u1, PointD.add(forces.getValue(u1), perpendicular_3_f));
                    forces.setValue(v1, PointD.add(forces.getValue(v1), perpendicular_4_f));
                }
            }
        }
        return forces;
    }

    private static YVector bisectionVector(YVector a, YVector b) {
        YVector unit_a = YVector.getNormal(a);
        YVector unit_b = YVector.getNormal(b);
        YVector bisection = YVector.add(unit_a, unit_b);
        bisection.norm();

        return bisection;
    }

    private static double[] calculateForceFactorOnEdgeEdgeRepulsion(double c3, double c4, double angle, int degree, YPoint p_v, YPoint p_u1, YPoint p_u2) {
        double l = Math.sqrt(Math.pow(YPoint.distance(p_v, p_u1),2) + Math.pow(YPoint.distance(p_v, p_u2),2)
                - 2 * YPoint.distance(p_v, p_u1) * YPoint.distance(p_v, p_u2) * Math.cos(2*Math.PI/degree));

        double f_e = c3 * Math.log(YPoint.distance(p_u1, p_u2)/l);

        double f_t = c4 * Math.abs(2*Math.PI/degree-angle)/angle;
        if (angle > 2*Math.PI/degree ) {
            f_t = f_t * (-1);
        }

        return new double[] {f_e, f_t};
    }

    private static double[] calculateForceFactorOnEdgeCrossing(double c1, double c2, double angle, YPoint p, YPoint p_u, YPoint p_v) {
        double l = Math.sqrt(Math.pow(YPoint.distance(p, p_u),2)+ Math.pow(YPoint.distance(p, p_v),2));

        double f_e = c1 * Math.log(YPoint.distance(p_u, p_v)/l);

        double f_t = c2 * (Math.PI/2-angle)/angle;

        return new double[] {f_e, f_t};
    }


}
