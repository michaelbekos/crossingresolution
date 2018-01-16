import java.util.*;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.view.*;

import layout.algo.utils.PositionMap;
import util.*;
import util.graph2d.*;
import layout.algo.*;
import algorithms.graphs.*;

public abstract class InitGeneticAlgorithm {
    public static Random rand = new Random();

    public static GeneticAlgorithm<ForceAlgorithmApplier> defaultGeneticAlgorithm(List<ForceAlgorithmApplier> firstFAA, IGraph graph, GraphComponent view) {
        return defaultGeneticAlgorithm(firstFAA, graph);
    }

    public static GeneticAlgorithm<ForceAlgorithmApplier> defaultGeneticAlgorithm(ForceAlgorithmApplier firstFAA, IGraph graph){
      List<ForceAlgorithmApplier> firstFAAs = new LinkedList<ForceAlgorithmApplier>();
      firstFAAs.add(firstFAA);
      return defaultGeneticAlgorithm(firstFAAs, graph);
    }
    public static GeneticAlgorithm<ForceAlgorithmApplier> defaultGeneticAlgorithm(List<ForceAlgorithmApplier> firstFAAs, IGraph graph){
          GeneticAlgorithm<ForceAlgorithmApplier> geneticAlgorithm = new GeneticAlgorithm<>(
                  (faa -> {
                      BasicIGraphLayoutExecutor executor = new BasicIGraphLayoutExecutor(faa, graph, 100, 100);
                      executor.run();
                      return faa;
                  }),
                  ((faa1, faa2) -> {
                      Optional<Double> ma1 = faa1.cMinimumAngle.getMinimumAngle(graph, faa1.nodePositions);
                      Optional<Double> ma2 = faa2.cMinimumAngle.getMinimumAngle(graph, faa2.nodePositions);
                      if(ma1.isPresent() && !ma2.isPresent()){
                          return -1;
                      }
                      if(!ma1.isPresent() && ma2.isPresent()){
                          return 1;
                      }
                      if(!ma1.isPresent() && !ma2.isPresent()){
                          return 0;
                      }
                      Double a1 = ma1.get(),
                              a2 = ma2.get();
                      return a1.compareTo(a2);
                  }),
                  5,
                  Either.left(fa -> {
                      Mapper<INode, PointD> nodePositions = PositionMap.copy(fa.nodePositions);
  
                      List<Intersection> crossings = MinimumAngle.getCrossingsSorted(graph, nodePositions);
                      ForceAlgorithmApplier fa2 = fa.clone();
                      if(crossings.size() == 0) {
                          return fa2;
                      }
  
                      List<Intersection> mostInteresting = crossings.subList(0, (int) Math.ceil(crossings.size() / 50.0));
  
                      //random choice
                      //int nodeIndex = rand.nextInt(graph.getNodes().size());
                      //INode node = graph.getNodes().getItem(nodeIndex);
                      INode node = null;
                      int nodeDegree = Integer.MAX_VALUE;
                      int whichNode = -1;
  
                      //random crossing
                      int crossingIndex = rand.nextInt(mostInteresting.size());
                      Intersection nodeCrossing = mostInteresting.get(crossingIndex);
                      whichNode = rand.nextInt(4);
                      INode[] nodes = new INode[]{
                              nodeCrossing.segment1.n1,
                              nodeCrossing.segment1.n2,
                              nodeCrossing.segment2.n1,
                              nodeCrossing.segment2.n2
                      };
                      node = nodes[whichNode];
  
                      if(node == null || nodeCrossing == null || whichNode < 0){
                          // ??? This CAN'T happen. Compiler thinks it can, but it can't.
                          return fa2;
                      }
                      //PointD pos = nodePositions.getValue(node);
                      PointD pos = nodeCrossing.intersectionPoint;
                      PointD direction = new PointD(0, 0);
                      switch(whichNode){
                          case 0: direction = PointD.negate(nodeCrossing.segment2.ve);
                              break;
                          case 1: direction = nodeCrossing.segment2.ve;
                              break;
                          case 2: direction = PointD.negate(nodeCrossing.segment1.ve);
                              break;
                          case 3: direction = nodeCrossing.segment1.ve;
                              break;
                      }
                      if(nodeCrossing.orientedAngle > 90){
                          direction = PointD.negate(direction);
                      }
                      if(direction.getVectorLength() <= G.Epsilon){
                          return fa2;
                      }
                      direction = direction.getNormalized();
                      PointD posOld = pos;
                      pos = PointD.add(pos, PointD.times(fa.modifiers[2], direction));
                      nodePositions.setValue(node, pos);
                      fa2.nodePositions = nodePositions;
                      fa2.cMinimumAngle.invalidate();
  
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
          // TODO: update min angle display, etc.
          geneticAlgorithm.bestChanged = faa -> PositionMap.applyToGraph(graph, faa.getNodePositions());
          
          geneticAlgorithm.instances.addAll(firstFAAs);
          return geneticAlgorithm;
      }
}