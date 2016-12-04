package layout.algo;

import com.yworks.yfiles.algorithms.YVector;
import com.yworks.yfiles.geometry.*;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.graph.styles.IEdgeStyle;
import com.yworks.yfiles.graph.styles.PolylineEdgeStyle;
import com.yworks.yfiles.view.*;
import com.yworks.yfiles.utils.IEnumerable;


import java.util.*;
import java.util.stream.*;
import java.awt.Color;


import javax.swing.*;

import util.*;
import util.graph2d.*;
import algorithms.graphs.*;
import view.visual.*;

public class ForceAlgorithmApplier implements Runnable {
  public List<ForceAlgorithm> algos = new LinkedList<>();
  public GraphComponent view;
  public IGraph graph;
  public int maxNoOfIterations;
  public int currNoOfIterations;
  public int maxMinAngleIterations;
  public static List<ICanvasObject> canvasObjects = new ArrayList<>();
  public Maybe<JProgressBar> progressBar;
  public Maybe<JLabel> infoLabel;
  public double maxMinAngle;
  public double minEdgeLength;
  public IMapper<INode, PointD> nodePositions;
  public boolean running = false;

  public static IMapper<INode, PointD> copyNodePositionsMap(IMapper<INode, PointD> im, Stream<INode> nodes){
    IMapper<INode, PointD> res = new Mapper<>(new WeakHashMap<INode, PointD>());
    nodes.forEach(n -> {
      PointD p1 = im.getValue(n);
      res.setValue(n, im.getValue(n));
    });
    return res;
  }

  public ForceAlgorithmApplier(GraphComponent view, int maxNoOfIterations, Maybe<JProgressBar> progressBar, Maybe<JLabel> infoLabel){

    this.view = view;
    this.graph = view.getGraph();
    nodePositions = ForceAlgorithmApplier.initPositionMap(graph);
    this.maxNoOfIterations = maxNoOfIterations;
    this.minEdgeLength = ShortestEdgeLength.getShortestEdge(graph).fmap(x -> x.b).getDefault(0.0);
    this.maxMinAngle = MinimumAngle.getMinimumAngleCrossing(graph, Maybe.just(nodePositions)).fmap(t -> t.c.angle).getDefault(0.0);
    this.maxMinAngleIterations = 0;
    this.progressBar = progressBar;
    this.infoLabel = infoLabel;
    
  }

  public static IMapper<INode, PointD> initPositionMap(IGraph g){
    IMapper<INode, PointD> nodePos = new Mapper<>(new WeakHashMap<>());
    g.getNodes().stream().forEach(n1 -> {
      PointD p1 = n1.getLayout().getCenter();
      nodePos.setValue(n1, n1.getLayout().getCenter());
    });
    return nodePos;
  }

  public void resetNodePosition(INode u){
    System.out.println("resetting node position: " + u);
    nodePositions.setValue(u, u.getLayout().getCenter());
  }

  public void run() {
    running = true;
    if (this.maxNoOfIterations == 0) {
      this.clearDrawables();
      IMapper<INode, PointD> map = calculateAllForces();
      this.displayVectors(map);
    }

    if (this.maxNoOfIterations < 0) {
      int j = 0;
      while (running){
        this.clearDrawables();
        nodePositions = applyAlgos();
        ForceAlgorithmApplier.applyNodePositionsToGraph(graph, nodePositions);
        this.currNoOfIterations = j;
        this.view.updateUI();
        try {
          Thread.sleep(1);
        } catch (InterruptedException exc) {
          System.out.println("Sleep interrupted!");
          //Do nothing...
        }
        infoLabel.andThen(p -> p.setText(displayMinimumAngle(graph) /*+ displayEdgeLength(graph)*/));
        j++;
      }
    } else {
      for (int i = 0; i < this.maxNoOfIterations; i++) {
        this.clearDrawables();
        nodePositions = applyAlgos();
        ForceAlgorithmApplier.applyNodePositionsToGraph(graph, nodePositions);
        this.currNoOfIterations = i;
        this.view.updateUI();
        try {
          Thread.sleep(1);
        } catch (InterruptedException exc) {
          System.out.println("Sleep interrupted!");
          //Do nothing...
        }
        int progress = Math.round(100 * i / this.maxNoOfIterations);
        progressBar.andThen(p -> p.setValue(progress));
        infoLabel.andThen(p -> p.setText(displayMinimumAngle(graph) /*+ displayEdgeLength(graph)*/));
        running = false;
      }
    }

    progressBar.andThen(p -> {
      p.setValue(0);
    });
    JOptionPane.showMessageDialog(null, displayMaxMinAngle(), "Maximal Minimum Angle", JOptionPane.INFORMATION_MESSAGE);
    this.view.fitContent();
    displayMinimumAngle(graph);
    this.view.updateUI();
    running = false;
  }

  public void runNoDraw() {
    running = true;
    for (int i = 0; i < this.maxNoOfIterations && running; i++) {
      nodePositions = applyAlgos();
    }
    running = false;
  }

  public void draw(IGraph g){
    ForceAlgorithmApplier.applyNodePositionsToGraph(g, nodePositions);
    this.view.updateUI();
    //displayMinimumAngle(g); 
    infoLabel.andThen(p -> p.setText(displayMinimumAngle(graph) /*+ displayEdgeLength(graph)*/));
  }
  /**
   * Displays vectors for debugging purposes
   */
  public void displayVectors(IMapper<INode, PointD> map) {
    for(INode u: graph.getNodes()){
      YVector vector = new YVector(map.getValue(u).getX(), map.getValue(u).getY());
      this.canvasObjects.add(this.view.getBackgroundGroup()
            .addChild(new VectorVisual(this.view, vector, u, Color.RED),
                ICanvasObjectDescriptor.VISUAL));
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

  public IMapper<INode, PointD> calculatePairwiseForces(List<NodePairForce> algos, IMapper<INode, PointD> map){
    for(INode n1: graph.getNodes()){
      PointD p1 = nodePositions.getValue(n1);
      PointD f1 = map.getValue(n1);
      for(INode n2: graph.getNodes()){
        if(n1.equals(n2)) continue;
        PointD p2 = nodePositions.getValue(n2);
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
  public IMapper<INode, PointD> calculateNeighbourForces(List<NodeNeighbourForce> algos, IMapper<INode, PointD> map){
    for(INode n1: graph.getNodes()){
      PointD p1 = nodePositions.getValue(n1);
      PointD f1 = map.getValue(n1);
      for(INode n2: graph.neighbors(INode.class, n1)){
        PointD p2 = nodePositions.getValue(n2);
        for(NodeNeighbourForce fa: algos){
          PointD force = fa.apply(p1).apply(p2);
          f1 = PointD.add(f1, force);
        }
      }
      map.setValue(n1, f1);
    }
    return map;
  }

  public IMapper<INode, PointD> calculateIncidentForces(List<IncidentEdgesForce> algos, IMapper<INode, PointD> map) {
    for (INode n1 : graph.getNodes()) {
      PointD p1 = nodePositions.getValue(n1);
      Integer n1degree = graph.degree(n1);
      nonEqualPairs(graph.neighbors(INode.class, n1).stream(), graph.neighbors(INode.class, n1).stream()).forEach(n2n3 -> {
        INode n2 = n2n3.a,
              n3 = n2n3.b;
        PointD p2 = nodePositions.getValue(n2),
               p3 = nodePositions.getValue(n3);
        PointD f2 = map.getValue(n2);
        PointD f3 = map.getValue(n3);
        LineSegment l1, l2;
        l1 = new LineSegment(p1, p2);
        l2 = new LineSegment(p1, p3);
        double cosAngle = PointD.scalarProduct(l1.ve, l2.ve) / (l1.ve.getVectorLength() * l2.ve.getVectorLength());
        cosAngle = Math.max(-1, Math.min(1, cosAngle));
        Double angle = Math.toDegrees(Math.acos(cosAngle));
        for (IncidentEdgesForce fa : algos) {
          Tuple2<PointD, PointD> forces = fa
                  .apply(l1.ve)
                  .apply(l2.ve)
                  .apply(angle)
                  .apply(n1degree);
          f2 = PointD.add(f2, forces.a);
          f3 = PointD.add(f3, forces.b);
        }
        map.setValue(n2, f2);
        map.setValue(n3, f3);
      });
    }
    return map;
  }
  public static <T1, T2> Stream<Tuple2<T1, T2>> nonEqualPairs(Stream<T1> s1, Stream<T2> s2){
    Set<T1> seenNodes = new HashSet<>();
    List<T2> s2l = s2.collect(Collectors.toList());
    return s1.flatMap(n1 -> {
      seenNodes.add(n1);
      return s2l.stream()
        .filter(n2 -> !seenNodes.contains(n2))
        .map(n2 -> new Tuple2<>(n1, n2));
    });
  }
  public IMapper<INode, PointD> calculateCrossingForces(List<CrossingForce> algos, IMapper<INode, PointD> map){
    for(Tuple3<LineSegment, LineSegment, Intersection> ci: MinimumAngle.getCrossings(graph, Maybe.just(nodePositions))){
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

  public static IGraph applyNodePositionsToGraph(IGraph g, IMapper<INode, PointD> nodePositions){
    for(INode n1: g.getNodes()){
      PointD p1 = nodePositions.getValue(n1);
      g.setNodeCenter(n1, p1);
    }
    return g;
  }
  public static IMapper<INode, PointD> applyForces(IGraph g, IMapper<INode, PointD> nodePositions, IMapper<INode, PointD> map){
    IMapper<INode, PointD> oldPos = copyNodePositionsMap(nodePositions, g.getNodes().stream());
    for(INode n1: g.getNodes()){
      PointD f1 = map.getValue(n1),
             p1 = nodePositions.getValue(n1),
             p2;
      p2 = PointD.add(f1, p1);
      nodePositions.setValue(n1, p2);
    }
    return nodePositions;
  }

  //everythings fine, this is checked, the compiler just doesn't get it
  @SuppressWarnings("unchecked")
  public static <T1, T extends T1> List<T> filterListSubclass(List<T1> l, Class<T> type){
    return l.parallelStream()
      .filter(a -> type.isInstance(a))
      .map(a -> (T) a)
      .collect(Collectors.toList());
  }

  public IMapper<INode, PointD> calculateAllForces(){
    List<NodePairForce> nodePairA = 
      ForceAlgorithmApplier.filterListSubclass(algos, NodePairForce.class);
    List<NodeNeighbourForce> nodeNeighbourA = 
      ForceAlgorithmApplier.filterListSubclass(algos, NodeNeighbourForce.class);
    List<IncidentEdgesForce> incidentEdgesA = 
      ForceAlgorithmApplier.filterListSubclass(algos, IncidentEdgesForce.class);
    List<CrossingForce> edgeCrossingsA = 
      ForceAlgorithmApplier.filterListSubclass(algos, CrossingForce.class);

    IMapper<INode, PointD> map = ForceAlgorithmApplier.initForceMap(graph);

    map = calculatePairwiseForces(nodePairA, map);
    map = calculateNeighbourForces(nodeNeighbourA, map);
    map = calculateIncidentForces(incidentEdgesA, map);
    map = calculateCrossingForces(edgeCrossingsA, map);

    return map;
  }

  public IMapper<INode, PointD> applyAlgos(){
    return applyForces(graph, nodePositions, calculateAllForces());
  }

  /**
   * Updates minimum crossing angle on the fly
   * @param graph
   * @return text - text to be displayed in gui
   */
  public String displayMinimumAngle(IGraph graph) {
    Maybe<Tuple3<LineSegment, LineSegment, Intersection>> crossing = MinimumAngle.getMinimumAngleCrossing(graph, Maybe.just(nodePositions));


    Maybe<String> s = crossing.fmap(currCross -> {
      if(currCross.c.angle > this.maxMinAngle){
        this.maxMinAngle = currCross.c.angle;
        this.maxMinAngleIterations = this.currNoOfIterations;
      }
      MinimumAngle.resetHighlighting(this.graph);
      MinimumAngle.highlightCrossing(currCross);
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


  
}

