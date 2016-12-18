import java.util.*;
import java.util.function.*;
import javax.swing.JLabel;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.view.*;

import util.*;
import util.graph2d.*;
import layout.algo.*;
import algorithms.graphs.*;

public abstract class InitGeneticAlgorithm {
    public static Random rand = new Random();
    
    public static GeneticAlgorithm<ForceAlgorithmApplier> defaultGeneticAlgorithm(ForceAlgorithmApplier firstFAA, IGraph graph, GraphComponent view, Maybe<JLabel> infoLabel){
          GeneticAlgorithm<ForceAlgorithmApplier> geneticAlgorithm = GeneticAlgorithm.newGeneticAlgorithm_FunGen(
                  (faa -> {
                      faa.runNoDraw();
                      return faa;
                  }),
                  ((faa1, faa2) -> {
                      Maybe<Double> ma1 = faa1.cMinimumAngle.getMinimumAngle(graph, Maybe.just(faa1.nodePositions)),
                          ma2 = faa2.cMinimumAngle.getMinimumAngle(graph, Maybe.just(faa2.nodePositions));
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
          geneticAlgorithm.bestChanged = Maybe.just(faa -> {
              faa.draw(graph);
          });
          
          geneticAlgorithm.instances.add(firstFAA);
          return geneticAlgorithm;
      }
}