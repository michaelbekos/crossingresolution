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
    private GeneticAlgorithm<ForceAlgorithmApplier> geneticAlgorithm;
    private Thread geneticAlgorithmThread;
    final double Epsilon = 0.01;
    final Function<PointD, PointD> rotate = (p -> new PointD(p.getY(), -p.getX()));
    final Double[] springThreshholds = new Double[]{0.01, 0.01, 0.01, 0.1};
    final Boolean[] algoModifiers = new Boolean[]{false, false};
    private GraphComponent view;
    private JLabel infoLabel;
    private JProgressBar progressBar;
    private IGraph graph;

    public InitForceAlgorithm(GraphComponent view,IGraph graph, JLabel infoLabel, JProgressBar progressBar){
        this.view = view;
        this.infoLabel = infoLabel;
        this.progressBar = progressBar;
        this.graph = graph;
    }


    public ForceAlgorithmApplier defaultForceAlgorithmApplier(int iterations){

        ForceAlgorithmApplier fd = new ForceAlgorithmApplier(view, iterations, Maybe.just(progressBar), Maybe.just(infoLabel));
        fd.modifiers = springThreshholds.clone();
        fd.switches = algoModifiers.clone();
        fd.algos.add(new NodePairForce(p1 -> (p2 -> {
            double electricalRepulsion = 50000,
                    threshold = fd.modifiers[0];
            PointD t = PointD.subtract(p1, p2);
            double dist = t.getVectorLength();
            if(dist <= Epsilon){
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
            if(dist <= Epsilon){
                return new PointD(0, 0);
            }
            t = PointD.div(t, dist);
            //t = PointD.times(threshold * springStiffness * Math.log(dist / springNaturalLength), t);
            t = PointD.times(t, threshold * (dist - springNaturalLength));
            return t;
        })));

        fd.algos.add(new CrossingForce(e1 -> (e2 -> (angle -> {
            double threshold = fd.modifiers[2];
            if(e1.getVectorLength() <= Epsilon ||
                    e2.getVectorLength() <= Epsilon){
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
            if(e1.getVectorLength() <= Epsilon ||
                    e2.getVectorLength() <= Epsilon){
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
    public static Random rand = new Random();
    public void initializeGeneticAlgorithm(){
        geneticAlgorithm = GeneticAlgorithm.<ForceAlgorithmApplier>newGeneticAlgorithm_FunGen(
                (faa -> {
                    faa.runNoDraw();
                    return faa;
                }),
                ((faa1, faa2) -> {
                    Maybe<Double> ma1 = MinimumAngle.getMinimumAngle(graph, Maybe.just(faa1.nodePositions)),
                            ma2 = MinimumAngle.getMinimumAngle(graph, Maybe.just(faa2.nodePositions));
                    if(ma1.hasValue() && !ma2.hasValue()){
                        return -1;
                    }
                    if(!ma1.hasValue() && ma2.hasValue()){
                        return 1;
                    }
                    if(!ma1.hasValue() && !ma2.hasValue()){
                        return 0;
                    }
                    Double a1 = ma1.get(),
                            a2 = ma2.get();
                    return a1.compareTo(a2);
                }),
                20,
                (fa -> {
                    IMapper<INode, PointD> nodePositions = ForceAlgorithmApplier.copyNodePositionsMap(fa.nodePositions, graph.getNodes().stream());

                    List<Tuple3<LineSegment, LineSegment, Intersection>> crossings = MinimumAngle.getCrossingsSorted(graph, Maybe.just(nodePositions));
                    ForceAlgorithmApplier fa2 = fa.clone();
                    if(crossings.size() == 0) {
                        return fa2;
                    }

                    List<Tuple3<LineSegment, LineSegment, Intersection>> mostInteresting = crossings.subList(0, (int) Math.ceil(crossings.size() / 50.0));

                    //random choice
                    //int nodeIndex = rand.nextInt(graph.getNodes().size());
                    //INode node = graph.getNodes().getItem(nodeIndex);
                    INode node = null;
                    int nodeDegree = Integer.MAX_VALUE;
                    Tuple3<LineSegment, LineSegment, Intersection> nodeCrossing = null;
                    int whichNode = -1;

                    //random crossing
                    int crossingIndex = rand.nextInt(mostInteresting.size());
                    nodeCrossing = mostInteresting.get(crossingIndex);
                    whichNode = rand.nextInt(4);
                    INode[] nodes = new INode[]{
                            nodeCrossing.a.n1.get(),
                            nodeCrossing.a.n2.get(),
                            nodeCrossing.b.n1.get(),
                            nodeCrossing.b.n2.get()
                    };
                    node = nodes[whichNode];

                    if(node == null || nodeCrossing == null || whichNode < 0){
                        // ??? This CAN'T happen. Compiler thinks it can, but it can't.
                        return fa2;
                    }
                    //PointD pos = nodePositions.getValue(node);
                    PointD pos = nodeCrossing.c.intersectionPoint;
                    PointD direction = new PointD(0, 0);
                    switch(whichNode){
                        case 0: direction = PointD.negate(nodeCrossing.b.ve);
                            break;
                        case 1: direction = nodeCrossing.b.ve;
                            break;
                        case 2: direction = PointD.negate(nodeCrossing.a.ve);
                            break;
                        case 3: direction = nodeCrossing.a.ve;
                            break;
                    }
                    if(nodeCrossing.c.orientedAngle > 90){
                        direction = PointD.negate(direction);
                    }
                    if(direction.getVectorLength() <= Epsilon){
                        return fa2;
                    }
                    direction = direction.getNormalized();
                    PointD posOld = pos;
                    pos = PointD.add(pos, PointD.times(fa.modifiers[2], direction));
                    nodePositions.setValue(node, pos);
                    fa2.nodePositions = nodePositions;

                    // russian roulette to change a modifier
                    if(fa2.modifiers.length > 0 && rand.nextDouble() > 0.5){
                        // randomly modify one spring threshhold
                        int modIndex = rand.nextInt(fa.modifiers.length);
                        // smallest double > 0
                        double minVal = Math.nextAfter(0, Double.POSITIVE_INFINITY);
                        // value should remain 0 < val <= 1
                        fa2.modifiers[modIndex] = Math.min(1, Math.max(minVal, fa.modifiers[modIndex] * rand.nextDouble() * 2));
                    }

                    // russian roulette to change a switch
                    if(fa2.switches.length > 0 && rand.nextDouble() > 0.5){
                        // randomly modify one spring threshhold
                        int switchIndex = rand.nextInt(fa.switches.length);
                        // smallest double > 0
                        double minVal = Math.nextAfter(0, Double.POSITIVE_INFINITY);
                        // value should remain 0 < val <= 1
                        fa2.switches[switchIndex] = (fa.switches[switchIndex] == false);
                    }
                    return fa2;
                }));
        geneticAlgorithm.bestChanged = Maybe.just(faa -> {
            faa.draw(graph);
            view.updateUI();
        });
        ForceAlgorithmApplier fa = defaultForceAlgorithmApplier(250);
        geneticAlgorithm.instances.add(fa);
        geneticAlgorithmThread = new Thread(geneticAlgorithm);
    }

}
