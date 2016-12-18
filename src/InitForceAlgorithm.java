import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.IMapper;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.view.GraphComponent;
import layout.algo.*;
import util.Maybe;
import util.Tuple2;
import util.Tuple3;
import util.graph2d.Intersection;
import util.graph2d.LineSegment;

import javax.swing.*;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Jessica Wolz on 10.12.16.
 */
public class InitForceAlgorithm {
    public final static Function<PointD, PointD> rotate = (p -> new PointD(p.getY(), -p.getX()));

    public static ForceAlgorithmApplier defaultForceAlgorithmApplier(int iterations, GraphComponent view, Maybe<JProgressBar> progressBar, Maybe<JLabel> infoLabel){
        ForceAlgorithmApplier fd = new ForceAlgorithmApplier(view, iterations, progressBar, infoLabel);
        
        fd.algos.add(new NodePairForce(p1 -> (p2 -> {
            double electricalRepulsion = 50000,
                    threshold = fd.modifiers[0];
            PointD t = PointD.subtract(p1, p2);
            double dist = t.getVectorLength();
            if(dist <= G.Epsilon){
                return new PointD(0, 0);
            }
            t = PointD.div(t, dist);
            t = PointD.times(threshold * electricalRepulsion / Math.pow(dist, 2), t);
            return t;
        })));
        fd.algos.add(new NodeNeighbourForce(p1 -> (p2 -> {
            double springStiffness = 150,
                    springNaturalLength = 100,
                    threshold = fd.modifiers[1];
            PointD t = PointD.subtract(p2, p1);
            double dist = t.getVectorLength();
            if(dist <= G.Epsilon){
                return new PointD(0, 0);
            }
            t = PointD.div(t, dist);
            //t = PointD.times(threshold * springStiffness * Math.log(dist / springNaturalLength), t);
            t = PointD.times(t, threshold * (dist - springNaturalLength));
            return t;
        })));

        fd.algos.add(new CrossingForce(e1 -> (e2 -> (angle -> {
            double threshold = fd.modifiers[2];
            if(e1.getVectorLength() <= G.Epsilon ||
                    e2.getVectorLength() <= G.Epsilon){
                return new Tuple2<>(new PointD(0, 0), new PointD(0, 0));
            }
            PointD t1 = e1.getNormalized();
            PointD t2 = e2.getNormalized();
            PointD t1Neg = PointD.negate(t1);
            PointD t2Neg = PointD.negate(t2);
            PointD t1_ = new PointD(0,0),
                    t2_ = new PointD(0,0);

            t1_ = PointD.times(t2Neg, threshold * Math.cos(Math.toRadians(angle)));
            t2_ = PointD.times(t1Neg, threshold * Math.cos(Math.toRadians(angle)));
            t1 = PointD.times(t1, threshold * Math.cos(Math.toRadians(angle)));
            t2 = PointD.times(t2, threshold * Math.cos(Math.toRadians(angle)));

            t1 = rotate.apply(PointD.negate(t1));
            t2 = rotate.apply(t2);
            // if(perpendicular?)
            if(fd.switches[0]) {
                return new Tuple2<>(t1, t2);
            }
            // else direction of other edge
            else{
                return new Tuple2<>(t1_, t2_);
            }
            /*if(angle > 60 && angle < 120){
                return new Tuple2<>(new PointD(0, 0), new PointD(0, 0));
            }
            t1 = PointD.times(t1, threshold * Math.cos(2.0 / 3.0 * Math.toRadians(angle)));
            t2 = PointD.times(t2, threshold * Math.cos(2.0 / 3.0 * Math.toRadians(angle)));
        */

        }))));

        fd.algos.add(new IncidentEdgesForce(e1 -> (e2 -> (angle -> (deg -> {
            if(deg <= 0) return new Tuple2<>(new PointD(0, 0), new PointD(0, 0));
            double threshold = fd.modifiers[3],
                    optAngle = (360 / deg);
            if(e1.getVectorLength() <= G.Epsilon ||
                    e2.getVectorLength() <= G.Epsilon){
                return new Tuple2<>(new PointD(0, 0), new PointD(0, 0));
            }
            PointD t1 = e1.getNormalized();
            PointD t2 = e2.getNormalized();
            Double neg = Math.signum(angle);

            t1 = PointD.times(t1, neg * threshold * Math.sin((Math.toRadians(optAngle - Math.abs(angle)))/2.0));
            t2 = PointD.times(t2, neg * threshold * Math.sin((Math.toRadians(optAngle - Math.abs(angle)))/2.0));
            t1 = rotate.apply(t1);
            t2 = PointD.negate(t2);
            t2 = rotate.apply(t2);
            return new Tuple2<>(t1, t2);
        })))));
        return fd;
    }
}
