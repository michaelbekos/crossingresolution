package layout.algo;

import com.yworks.yfiles.algorithms.YVector;
import com.yworks.yfiles.algorithms.YPoint;
import com.yworks.yfiles.geometry.*;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.view.*;

import java.util.*;
import java.util.stream.*;
import java.awt.Color;

import javax.swing.JProgressBar;
import javax.swing.JOptionPane;

import util.*;
import util.graph2d.*;
import algorithms.graphs.*;
import view.visual.*;

public class ForceAlgorithmApplier implements Runnable {
  public List<ForceAlgorithm> algos = new LinkedList<>();
  protected GraphComponent view;
  protected IGraph graph;
  protected int maxNoOfIterations;
  protected int currNoOfIterations;
  protected int maxMinAngleIterations;
  protected static List<ICanvasObject> canvasObjects = new ArrayList<>();
  public Maybe<JProgressBar> progressBar;
  protected double maxMinAngle;
  protected double minEdgeLength;

  
  public ForceAlgorithmApplier(GraphComponent view, int maxNoOfIterations, Maybe<JProgressBar> progressBar){
    this.view = view;
    this.graph = view.getGraph();
    this.maxNoOfIterations = maxNoOfIterations;
    this.minEdgeLength = ShortestEdgeLength.getShortestEdge(graph).get().b;
    this.maxMinAngle = MinimumAngle.getMinimumAngleCrossing(graph).get().c.angle;
    this.maxMinAngleIterations = 0;
    this.progressBar = progressBar;
  }

  public void run() {
    if (this.maxNoOfIterations == 0) {
      this.clearDrawables();
      IMapper<INode, PointD> map = ForceAlgorithmApplier.calculateAllForces(algos, graph);
      this.displayVectors(map);
    }

    for (int i = 0; i < this.maxNoOfIterations; i++) {
      this.clearDrawables();
      ForceAlgorithmApplier.applyAlgos(algos, graph);
      this.view.updateUI();
      try {
        Thread.sleep(1);
      } catch (InterruptedException exc) {
        System.out.println("Sleep interrupted!");
        //Do nothing...
      }
      int progress = Math.round(100 * i / this.maxNoOfIterations);
      progressBar.andThen(p -> p.setValue(progress));
    }

    progressBar.andThen(p -> {
      p.setValue(0);
    });
    JOptionPane.showMessageDialog(null, displayMaxMinAngle(), "Maximal Minimum Angle", JOptionPane.INFORMATION_MESSAGE);
    this.view.fitContent();
    this.view.updateUI();
  }

  /**
   * Displays vectors for debugging purposes
   */
  protected void displayVectors(IMapper<INode, PointD> map) {
    for( INode u: graph.getNodes()){
      YVector vector = new YVector(0,0);

      List<YVector> vectors = new ArrayList<YVector>();
      vectors.add(new YVector(map.getValue(u).getX(), map.getValue(u).getY()));

      Iterator<YVector> it = vectors.iterator();
      while (it.hasNext()){
        YVector temp = it.next();
        this.canvasObjects.add(this.view.getBackgroundGroup()
              .addChild(new VectorVisual(this.view, temp, u, Color.RED),
                  ICanvasObjectDescriptor.VISUAL));
      }
    }
    this.view.updateUI();
  }

  private void clearDrawables() {
    for (ICanvasObject o: canvasObjects) {
      o.remove();
    }
    canvasObjects.clear();
    this.view.updateUI();
  }
  public static IMapper<INode, PointD> calculatePairwiseForces(List<NodePairForce> algos, IGraph g, IMapper<INode, PointD> map){
    for(INode n1: g.getNodes()){
      PointD p1 = n1.getLayout().getCenter();
      PointD f1 = map.getValue(n1);
      for(INode n2: g.getNodes()){
        if(n1.equals(n2)) continue;
        PointD p2 = n2.getLayout().getCenter();
        // applying spring force
        for(NodePairForce fa: algos){
          PointD force = fa.apply(p1).apply(p2);
          f1 = PointD.add(f1, force);
        }
      }
      map.setValue(n1, f1);
    }
    return map;
  }
  public static IMapper<INode, PointD> calculateNeighbourForces(List<NodeNeighbourForce> algos, IGraph g, IMapper<INode, PointD> map){
    for(INode n1: g.getNodes()){
      PointD p1 = n1.getLayout().getCenter();
      PointD f1 = map.getValue(n1);
      for(INode n2: g.neighbors(INode.class, n1)){
        PointD p2 = n2.getLayout().getCenter();
        for(NodeNeighbourForce fa: algos){
          PointD force = fa.apply(p1).apply(p2);
          f1 = PointD.add(f1, force);
        }
      }
      map.setValue(n1, f1);
    }
    return map;
  }

  public static IMapper<INode, PointD> calculateIncidentForces(List<IncidentEdgesForce> algos, IGraph g, IMapper<INode, PointD> map){
    for(INode n1: g.getNodes()){
      Integer n1degree = g.degree(n1);
      for(INode n2: g.neighbors(INode.class, n1)){
        PointD f2 = map.getValue(n2);
        for(INode n3: g.neighbors(INode.class, n1)){
          if(n2.equals(n3)) continue;
          PointD f3 = map.getValue(n3);
          LineSegment l1, l2;
          l1 = new LineSegment(n1, n2);
          l2 = new LineSegment(n1, n3);
          Double angle = Math.toDegrees(Math.acos(PointD.scalarProduct(l1.ve, l2.ve) / (l1.ve.getVectorLength() * l2.ve.getVectorLength())));
          for(IncidentEdgesForce fa: algos){
            Tuple2<PointD, PointD> forces = fa
              .apply(l1.ve)
              .apply(l2.ve)
              .apply(angle)
              .apply(n1degree);
            f2 = PointD.add(f2, forces.a);
            f3 = PointD.add(f3, forces.b);
          }
          map.setValue(n3, f3);
        }
        map.setValue(n2, f2);
      }
    }
    return map;
  }

  public static IMapper<INode, PointD> calculateCrossingForces(List<CrossingForce> algos, IGraph g, IMapper<INode, PointD> map){
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
    // apply cosinus force
      for(CrossingForce fa: algos){
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
    return map;
  }

  public static IMapper<INode, PointD> initForceMap(IGraph g){
    IMapper<INode, PointD> map = new Mapper<>(new WeakHashMap<>());
    for(INode n1: g.getNodes()){
      map.setValue(n1, new PointD(0, 0));
    }
    return map;
  }

  public static IGraph applyForces(IGraph g, IMapper<INode, PointD> map){
    for(INode n1: g.getNodes()){
      PointD f1 = map.getValue(n1),
          p1 = n1.getLayout().getCenter();
      p1 = PointD.add(f1, p1);
      g.setNodeCenter(n1, p1);
    }
    return g;
  }

  public static <T1, T extends T1> List<T> filterListSubclass(List<T1> l, Class<T> type){
    return l.parallelStream()
      .filter(a -> type.isInstance(a))
      .map(a -> (T) a)
      .collect(Collectors.toList());
  }

  public static IMapper<INode, PointD> calculateAllForces(List<ForceAlgorithm> algos, IGraph g){
    List<NodePairForce> nodePairA = 
      filterListSubclass(algos, NodePairForce.class);
    List<NodeNeighbourForce> nodeNeighbourA = 
      filterListSubclass(algos, NodeNeighbourForce.class);
    List<IncidentEdgesForce> incidentEdgesA = 
      filterListSubclass(algos, IncidentEdgesForce.class);
    List<CrossingForce> edgeCrossingsA = 
      filterListSubclass(algos, CrossingForce.class);

    IMapper<INode, PointD> map = initForceMap(g);

    map = calculatePairwiseForces(nodePairA, g, map);
    map = calculateNeighbourForces(nodeNeighbourA, g, map);
    map = calculateIncidentForces(incidentEdgesA, g, map);
    map = calculateCrossingForces(edgeCrossingsA, g, map);

    return map;
  }

  public static IGraph applyAlgos(List<ForceAlgorithm> algos, IGraph g){
    return applyForces(g, calculateAllForces(algos, g));
  }

  /**
   * Updates minimum crossing angle on the fly
   * @param graph
   * @return text - text to be displayed in gui
   */
  public String displayMinimumAngle(IGraph graph) {
    Maybe<Tuple3<LineSegment, LineSegment, Intersection>> crossing = MinimumAngle.getMinimumAngleCrossing(graph);

    /*for(Tuple3<LineSegment, LineSegment, Intersection> cross: MinimumAngle.getCrossings(graph)) {
      if (cross.c.angle < this.minAngle){
        this.minAngle = cross.c.angle;
        currCross = cross;
        //updateCriticalEdges(cross.c);
        }
      }*/

    Maybe<String> s = crossing.fmap(currCross -> {
      if(currCross.c.angle > this.maxMinAngle){
        this.maxMinAngle = currCross.c.angle;
        this.maxMinAngleIterations = this.currNoOfIterations;
      }
      displayCriticalEdges(currCross);
      return DisplayMessagesGui.createMinimumAngleMsg(currCross);
    });
    return s.getDefault("No crossings!");
    
  }

  /**
   * Gets Message for Popup, which holds the maximal
   * minimum angle
   * @return text - the generated text for the pop up message
   */
  public String displayMaxMinAngle(){
    return DisplayMessagesGui.createMaxMinAngleMsg(this.maxMinAngle, this.maxMinAngleIterations);
  }

  public String displayEdgeLength(IGraph graph){
    Maybe<Tuple2<LineSegment, Double>> edges = ShortestEdgeLength.getShortestEdge(graph);
    LineSegment line = new LineSegment(new PointD(0,0), new PointD(0,0));
    double currLength = 0.0;
    if(edges.hasValue()){
      line = edges.get().a;
      currLength = edges.get().b;
    }
    if(currLength < this.minEdgeLength){
      this.minEdgeLength = currLength;

    }
    return DisplayMessagesGui.createEdgeLengthMsg(this.minEdgeLength, line);
  }

  /**
   * Displays vectors for debugging purposes
   */
  protected void displayCriticalEdges(Tuple3<LineSegment, LineSegment, Intersection> crossing) {

    crossing.a.n1.andThen(n1 -> 
      crossing.a.n2.andThen(n2 -> 
      crossing.b.n1.andThen(n3 ->
      crossing.b.n2.andThen(n4 -> {
      YPoint u1 = new YPoint(n1.getLayout().getCenter().x, n1.getLayout().getCenter().y),
             u2 = new YPoint(n2.getLayout().getCenter().x, n2.getLayout().getCenter().y),
             u3 = new YPoint(n3.getLayout().getCenter().x, n3.getLayout().getCenter().y),
             u4 = new YPoint(n4.getLayout().getCenter().x, n4.getLayout().getCenter().y);
  
      YVector v1 = new YVector(u1, u2);
      YVector v2 = new YVector(u3, u4);
  
       /* this.canvasObjects.add(this.view.getBackgroundGroup()
          .addChild(new VectorVisual(this.view, v1, n1, Color.RED),
                ICanvasObjectDescriptor.VISUAL));
  
      this.canvasObjects.add(this.view.getBackgroundGroup()
          .addChild(new VectorVisual(this.view, v2, n3, Color.RED),
              ICanvasObjectDescriptor.VISUAL));*/
       //TODO: Add coloring of edges
    }))));
    

    
    
  }

}

