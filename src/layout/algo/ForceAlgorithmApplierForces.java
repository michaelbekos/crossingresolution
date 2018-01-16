package layout.algo;

import algorithms.graphs.CachedMinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.forces.CrossingForce;
import layout.algo.forces.IncidentEdgesForce;
import layout.algo.forces.NodeNeighbourForce;
import layout.algo.forces.NodePairForce;
import util.Tuple2;
import util.Tuple3;
import util.Util;
import util.graph2d.LineSegment;

import java.util.*;
import java.util.stream.Collectors;

//merge with ForceDirectedFactory.java

public class ForceAlgorithmApplierForces {
    public IGraph graph;
    public Mapper<INode, PointD> nodePositions;
    public CachedMinimumAngle cMinimumAngle;


    public  ForceAlgorithmApplierForces (IGraph graph, Mapper<INode, PointD> nodePositions, CachedMinimumAngle cMinimumAngle) {
        this.graph = graph;
        this.nodePositions = nodePositions;
        this.cMinimumAngle = cMinimumAngle;
    }


    // all nodes with all nodes
    public Mapper<INode, PointD> calculatePairwiseForces(List<NodePairForce> algos, Mapper<INode, PointD> map){
        graph.getNodes().parallelStream().forEach(n1 -> {
            PointD p1 = nodePositions.getValue(n1);
            PointD f1 = new PointD(0, 0);
            for(INode n2: graph.getNodes()){
                if(n1.equals(n2)) continue;
                PointD p2 = nodePositions.getValue(n2);
                // applying spring force
                for(NodePairForce fa: algos){
                    PointD force = fa.apply(p1).apply(p2);
                    f1 = PointD.add(f1, force);
                }
            }
            synchronized(map){
                PointD f0 = map.getValue(n1);
                map.setValue(n1, PointD.add(f0, f1));
            }
        });
        return map;
    }

    // all nodes with their neighbours
    public Mapper<INode, PointD> calculateNeighbourForces(List<NodeNeighbourForce> algos, Mapper<INode, PointD> map){
        //for(INode n1: graph.getNodes()){
        graph.getNodes().parallelStream().forEach(n1 -> {
            PointD p1 = nodePositions.getValue(n1);
            PointD f1 = new PointD(0, 0);
            for(INode n2: graph.neighbors(INode.class, n1)){
                PointD p2 = nodePositions.getValue(n2);
                for(NodeNeighbourForce fa: algos){
                    PointD force = fa.apply(p1).apply(p2);
                    f1 = PointD.add(f1, force);
                }
            }
            synchronized(map){
                PointD f0 = map.getValue(n1);
                map.setValue(n1, PointD.add(f0, f1));
            }
        });
        return map;
    }

    // all nodes: their incident edges with each other. Forces on neighbours.
    public Mapper<INode, PointD> calculateIncidentForces(List<IncidentEdgesForce> algos, Mapper<INode, PointD> map) {
        //http://www.euclideanspace.com/maths/algebra/vectors/angleBetween/
        //for (INode n1 : graph.getNodes()) {
        graph.getNodes().parallelStream().forEach(n1 -> {
            PointD p1 = nodePositions.getValue(n1);
            Integer n1degree = graph.degree(n1);
            if(n1degree < 2) return;
            //nonStrictlyEqualPairs(graph.neighbors(INode.class, n1).stream(), graph.neighbors(INode.class, n1).stream()).forEach(n2n3 -> {
            List<Tuple3<INode, INode, Double>> neighboursWithAngle =
                    Util.nonEqalPairs(graph.neighbors(INode.class, n1).stream(), graph.neighbors(INode.class, n1).stream()).map(
                            n2n3 -> {
                                INode n2 = n2n3.a,
                                        n3 = n2n3.b;
                                PointD p2 = nodePositions.getValue(n2),
                                        p3 = nodePositions.getValue(n3);
                                PointD v1 = PointD.subtract(p2, p1);
                                PointD v2 = PointD.subtract(p3, p1);

                                Double angle = Math.toDegrees(Math.atan2(v2.getY(), v2.getX()) - Math.atan2(v1.getY(), v1.getX()));
                                return new Tuple3<>(n2n3, angle);
                            })
                            .map(n2n3d -> {
                                if(n2n3d.c < 0)
                                    return new Tuple3<>(n2n3d, 360 + n2n3d.c);
                                else
                                    return n2n3d;
                            })
                            .collect(Collectors.toList());
            Comparator<Tuple3<INode, INode, Double>> byAngle =
                    (t1, t2) -> Double.compare(t1.c, t2.c);
            Collections.sort(neighboursWithAngle, byAngle);
            Tuple3<INode, INode, Double> n2n3 = neighboursWithAngle.get(0);
            neighboursWithAngle.remove(0);
            Set<INode> seenNodes = new HashSet<>();
            boolean nextFound = true;
            // go from node to node until all nodes have been visited
            // visit the first node twice --> don't add it before the first iteration
            while(nextFound) {
                INode n2 = n2n3.a,
                        n3 = n2n3.b;
                seenNodes.add(n3);
                PointD p2 = nodePositions.getValue(n2),
                        p3 = nodePositions.getValue(n3);
                PointD f2 = new PointD(0, 0),
                        f3 = new PointD(0, 0);
                PointD v1 = PointD.subtract(p2, p1);
                PointD v2 = PointD.subtract(p3, p1);
                Double angle = n2n3.c;
                for (IncidentEdgesForce fa : algos) {
                    Tuple2<PointD, PointD> forces = fa
                            .apply(v1)
                            .apply(v2)
                            .apply(angle)
                            .apply(n1degree);
                    f2 = PointD.add(f2, forces.a);
                    f3 = PointD.add(f3, forces.b);
                }
                synchronized(map){
                    PointD f2_1 = map.getValue(n2),
                            f3_1 = map.getValue(n3);
                    map.setValue(n2, PointD.add(f2_1, f2));
                    map.setValue(n3, PointD.add(f3_1, f3));
                }
                nextFound = false;
                for(Tuple3<INode, INode, Double> next: neighboursWithAngle) {
                    if(next.a.equals(n3) && !seenNodes.contains(next.b)){
                        n2n3 = next;
                        nextFound = true;
                        break;
                    }
                }
            }
        });
        return map;
    }

    // all crossings: forces on all four nodes
    public Mapper<INode, PointD> calculateCrossingForces(List<CrossingForce> algos, Mapper<INode, PointD> map){
        cMinimumAngle.getCrossings(graph, nodePositions).parallelStream().forEach(intersection -> {
            LineSegment l1 = intersection.segment1,
                    l2 = intersection.segment2;
            INode n1 = l1.n1,
                    n2 = l1.n2,
                    n3 = l2.n1,
                    n4 = l2.n2;
            PointD p1 = l1.p1,
                    p2 = l1.p2,
                    p3 = l2.p1,
                    p4 = l2.p2,
                    f1 = new PointD(0, 0),
                    f2 = new PointD(0, 0),
                    f3 = new PointD(0, 0),
                    f4 = new PointD(0, 0),
                    v1 = PointD.add(p1, PointD.negate(p2)),
                    v2 = PointD.add(p3, PointD.negate(p4));
            // apply cosinus force
            for(CrossingForce fa: algos){
                Tuple2<PointD, PointD> forces =
                        fa.apply(v1).apply(v2).apply(intersection.orientedAngle);
                PointD force1 = forces.a, force2 = forces.b;
                f1 = PointD.add(f1, force1);
                f2 = PointD.add(f2, PointD.negate(force1));
                f3 = PointD.add(f3, force2);
                f4 = PointD.add(f4, PointD.negate(force2));
            }
            synchronized(map){
                PointD f1_1 = map.getValue(n1),
                        f2_1 = map.getValue(n2),
                        f3_1 = map.getValue(n3),
                        f4_1 = map.getValue(n4);
                map.setValue(n1, PointD.add(f1, f1_1));
                map.setValue(n2, PointD.add(f2, f2_1));
                map.setValue(n3, PointD.add(f3, f3_1));
                map.setValue(n4, PointD.add(f4, f4_1));
            }
        });
        return map;
    }
}
