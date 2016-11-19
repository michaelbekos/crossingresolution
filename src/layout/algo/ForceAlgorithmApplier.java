package layout.algo;

import com.yworks.yfiles.geometry.*;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.view.*;

import java.util.*;
import java.util.stream.*;
import java.awt.Color;

import util.*;
import util.graph2d.*;
import algorithms.graphs.*;
import view.visual.*;

public class ForceAlgorithmApplier implements Runnable {
  public List<ForceAlgorithm> algos = new LinkedList<>();
  protected GraphComponent view;
  protected IGraph graph;
  protected int maxNoOfIterations;   
  protected static List<ICanvasObject> canvasObjects = new ArrayList<>();

    
  public ForceAlgorithmApplier(GraphComponent view, int maxNoOfIterations){
    this.view = view;
    this.graph = view.getGraph();
    this.maxNoOfIterations = maxNoOfIterations;
  }

  public void run() {
    for (int i=0; i < this.maxNoOfIterations; i++) {
      this.clearDrawables();
      ForceAlgorithmApplier.applyAlgos(algos, graph);
      this.view.updateUI();
      try {
        Thread.sleep(1);
      }
      catch (InterruptedException exc) {
        //Do nothing...
      }
    }
  }
  protected void draw() {
  }
  
  private void clearDrawables() {
    for (ICanvasObject o: canvasObjects) {
        o.remove();
    }
    canvasObjects.clear();
    this.view.updateUI();
  }

  // modifies g in-place!
  public static void applyAlgos(List<ForceAlgorithm> algos, IGraph g){
    List<NodePairForce> nodePairA;
    List<NodeNeighbourForce> nodeNeighbourA;
    List<CrossingForce> edgeCrossingsA;
    List<IncidentEdgesForce> incidentEdgesA;
    IMapper<INode, PointD> map = new Mapper<>(new WeakHashMap<>()); 

    nodePairA = algos.parallelStream()
      .filter(a -> a instanceof NodePairForce)
      .map(a -> (NodePairForce) a)
      .collect(Collectors.toList());

    nodeNeighbourA = algos.parallelStream()
      .filter(a -> a instanceof NodeNeighbourForce)
      .map(a -> (NodeNeighbourForce) a)
      .collect(Collectors.toList());

    edgeCrossingsA = algos.parallelStream()
      .filter(a -> a instanceof CrossingForce)
      .map(a -> (CrossingForce) a)
      .collect(Collectors.toList());

    incidentEdgesA = algos.parallelStream()
      .filter(a -> a instanceof IncidentEdgesForce)
      .map(a -> (IncidentEdgesForce) a)
      .collect(Collectors.toList());

    for(INode n1: g.getNodes()){
      map.setValue(n1, new PointD(0, 0));
    }

    for(INode n1: g.getNodes()){
      PointD p1 = n1.getLayout().getCenter();
      PointD f1 = map.getValue(n1);
      Integer n1degree = g.degree(n1);
      for(INode n2: g.getNodes()){
        if(n1.equals(n2)) continue;
        PointD p2 = n2.getLayout().getCenter();
        for(NodePairForce fa: nodePairA){
          PointD force = fa.apply(p1).apply(p2);
          f1 = PointD.add(f1, force);
        }
      }
      for(INode n2: g.neighbors(INode.class, n1)){
        PointD p2 = n2.getLayout().getCenter();
        PointD f2 = map.getValue(n2);
        for(INode n3: g.neighbors(INode.class, n1)){
          if(n2.equals(n3)) continue;
          PointD f3 = map.getValue(n3);
          LineSegment l1, l2;
          l1 = new LineSegment(n1, n2);
          l2 = new LineSegment(n1, n3);
          Maybe<Intersection> mi = l1.intersects(l2, false);
          if(mi.hasValue()){
            Intersection i = mi.get();
            for(IncidentEdgesForce fa: incidentEdgesA){
              Tuple2<PointD, PointD> forces = fa
                .apply(l1.ve)
                .apply(l2.ve)
                .apply(i.orientedAngle)
                .apply(n1degree);
              PointD f2_1, f3_1;
              f2_1 = forces.a;
              f3_1 = forces.b;
              f2 = PointD.add(f2, f2_1);
              f3 = PointD.add(f3, f3_1);
            }
          }
          map.setValue(n3, f3);
        }
        for(NodeNeighbourForce fa: nodeNeighbourA){
          PointD force = fa.apply(p1).apply(p2);
          f1 = PointD.add(f1, force);
        }
        map.setValue(n2, f2);
      }
      map.setValue(n1, f1);
    }

    for(Tuple3<LineSegment, LineSegment, Intersection> ci: MinimumAngle.getCrossings(g)){
      LineSegment l1 = ci.a,
                  l2 = ci.b;
      Intersection i = ci.c;
      INode n1 = l1.n1.get(),
            n2 = l1.n2.get(),
            n3 = l2.n1.get(),
            n4 = l2.n2.get();
      PointD p1 = l1.p1, 
             p2 = l1.p2,
             p3 = l2.p1,
             p4 = l2.p2,
             f1 = map.getValue(n1),
             f2 = map.getValue(n2),
             f3 = map.getValue(n3),
             f4 = map.getValue(n4),             
             v1 = PointD.add(p1, PointD.negate(p2)),
             v2 = PointD.add(p3, PointD.negate(p4));

      for(CrossingForce fa: edgeCrossingsA){
        Tuple2<PointD, PointD> forces = 
          fa.apply(v1).apply(v2).apply(i.orientedAngle);
        PointD force1 = forces.a, force2 = forces.b;
        f1 = PointD.add(f1, force1);
        f2 = PointD.add(f2, PointD.negate(force1));
        f3 = PointD.add(f3, force2);
        f4 = PointD.add(f4, PointD.negate(force2));
      }
      map.setValue(n1, f1);
      map.setValue(n2, f2);
      map.setValue(n3, f3);
      map.setValue(n4, f4);
    }
    for(INode n1: g.getNodes()){
      PointD f1 = map.getValue(n1),
             p1 = n1.getLayout().getCenter();
      p1 = PointD.add(f1, p1);
      g.setNodeCenter(n1, p1);
    }
  }

}

