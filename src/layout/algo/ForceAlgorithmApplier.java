package layout.algo;

import com.sun.istack.internal.Nullable;
import com.yworks.yfiles.algorithms.YVector;
import com.yworks.yfiles.geometry.*;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.graph.styles.IEdgeStyle;
import com.yworks.yfiles.graph.styles.PolylineEdgeStyle;
import com.yworks.yfiles.view.*;
import com.yworks.yfiles.utils.IEnumerable;


import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.awt.Color;


import javax.swing.*;

import util.*;
import util.graph2d.*;
import algorithms.graphs.*;
import view.visual.*;

public class ForceAlgorithmApplier implements Runnable {
  // keep track of a bestSolution across all FAAs
  public static Tuple4<Mapper<INode, PointD>, Optional<Double>, Double[], Boolean[]> bestSolution = null;
  public static List<ICanvasObject> canvasObjects = new ArrayList<>();
  // call this whenever the underlying graph changes structurally!
  public static void init(){
    bestSolution = null;
  }

  public static Mapper<INode, PointD> newNodePointMap(){
    return new Mapper<>(new WeakHashMap<>());
  }
  // copy all entries of im to a new map
  public static Mapper<INode, PointD> copyNodePositionsMap(Mapper<INode, PointD> im){
    Mapper<INode, PointD> res = newNodePointMap();
    im.getEntries().forEach(e -> {
      INode n = e.getKey();
      PointD p = e.getValue();
      res.setValue(n, p);
    });
    return res;
  }
  // initPositionMap copies all node positions of a given graph to a positionMap.
  public static Mapper<INode, PointD> initPositionMap(IGraph g){
    Mapper<INode, PointD> nodePos = newNodePointMap();
    g.getNodes().stream().forEach(n1 -> {
      PointD p1 = n1.getLayout().getCenter();
      nodePos.setValue(n1, n1.getLayout().getCenter());
    });
    return nodePos;
  }  
  // initForceMap creates a forceMap with default force (0,0).
  public static Mapper<INode, PointD> initForceMap(IGraph g){
    Mapper<INode, PointD> map = newNodePointMap();
    map.setDefaultValue(new PointD(0, 0));
    return map;
  }
  // apply positions in a positionMap to a graph.
  public static IGraph applyNodePositionsToGraph(IGraph g, Mapper<INode, PointD> nodePositions){
    for(Map.Entry<INode, PointD> e: nodePositions.getEntries()){
      INode n1 = e.getKey();
      PointD p1 = e.getValue();
      g.setNodeCenter(n1, p1);
    }
    return g;
  }
  // add all forces to the corresponding nodes
  public static Mapper<INode, PointD> applyForces(Mapper<INode, PointD> nodePositions, Mapper<INode, PointD> forces){
    for(Map.Entry<INode, PointD> e: nodePositions.getEntries()){
      INode n1 = e.getKey();
      PointD f1 = forces.getValue(n1),
             p1 = e.getValue(),
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

  /* * * * * * * * * * * * * *
   * STATIC PART ENDS HERE  *
   * * * * * * * * * * * * * */

  // list of algos to apply to graph
  public List<ForceAlgorithm> algos = new LinkedList<>();
  // underlying graph info
  public GraphComponent view;
  public IGraph graph;
  public int maxNoOfIterations;
  public int currNoOfIterations;
  public int maxMinAngleIterations;
  @Nullable
  private JProgressBar progressBar;
  @Nullable
  private JLabel infoLabel;
  public double maxMinAngle;
  public double minEdgeLength;
  // current nodePositions, so we can calculate in the background
  public Mapper<INode, PointD> nodePositions;
  public boolean running = false;
  // modifiers for algos
  public Double[] modifiers = new Double[0];
  public Boolean[] switches = new Boolean[0];

  public CachedMinimumAngle cMinimumAngle = new CachedMinimumAngle();

  public ForceAlgorithmApplier(GraphComponent view, int maxNoOfIterations) {
    this(view, maxNoOfIterations, null, null);
  }

  public ForceAlgorithmApplier(GraphComponent view, int maxNoOfIterations, JProgressBar progressBar, JLabel infoLabel){
    this.view = view;
    this.graph = view.getGraph();
    nodePositions = ForceAlgorithmApplier.initPositionMap(graph);
    this.maxNoOfIterations = maxNoOfIterations;
    this.minEdgeLength = ShortestEdgeLength.getShortestEdge(graph).map(x -> x.b).orElse(0.0);
    this.maxMinAngle = cMinimumAngle.getMinimumAngleCrossing(graph, nodePositions).map(t -> t.angle).orElse(0.0);
    this.maxMinAngleIterations = 0;
    this.progressBar = progressBar;
    this.infoLabel = infoLabel;
    improveSolution();
  }

  @Override
  public ForceAlgorithmApplier clone(){
    ForceAlgorithmApplier ret = new ForceAlgorithmApplier(this.view, this.maxNoOfIterations, this.progressBar, this.infoLabel);
    ret.graph = this.graph;
    ret.nodePositions = ForceAlgorithmApplier.copyNodePositionsMap(this.nodePositions);
    ret.modifiers = this.modifiers.clone();
    ret.switches = this.switches.clone();
    ret.algos = this.algos;

    return ret;
  }

  // set a list of node positions. takes care of cache invalidation, tracking best solutions.
  public void setNodePositions(List<Tuple2<INode, PointD>> ups){
    synchronized(nodePositions){
      for(Tuple2<INode, PointD> up: ups){
        INode u = up.a;
        PointD p = up.b;
        if(nodePositions.getValue(u) == null){
          // new node!
          System.out.println("! new node !");
          nodePositions = initPositionMap(graph);
          break;
        }
        nodePositions.setValue(u, p);
      }
    }
    cMinimumAngle.invalidate();
    improveSolution();
  }
  // simpler setNodePositions
  public void setNodePosition(INode u, PointD p){
    setNodePositions(Arrays.asList(new Tuple2<>(u, p)));
  }
  // simpler setNodePositions
  public void resetNodePosition(INode u){
    setNodePosition(u, u.getLayout().getCenter());
  }
  // simpler setNodePositions
  public void resetNodePositions(Collection<INode> us){
    List<Tuple2<INode, PointD>> ups = us.stream()
      .map(u -> new Tuple2<>(u, u.getLayout().getCenter()))
      .collect(Collectors.toList());
    setNodePositions(ups);
  }

  // show all forces that would be applied to nodes currently
  public void showForces(){
    this.clearDrawables();
    Mapper<INode, PointD> map = calculateAllForces();
    this.displayVectors(map);
  }

  // apply internal position map to graph
  public void showNodePositions(){
    ForceAlgorithmApplier.applyNodePositionsToGraph(graph, nodePositions);
    this.view.updateUI();
  }

  // apply internal position map to graph
  public void showNodePositions(int j){
    ForceAlgorithmApplier.applyNodePositionsToGraph(graph, nodePositions);
    // update only every 20 iterations
    if(j % 10 == 0) {
      this.view.updateUI();
    }
  }
  /**
   * run(maxNoOfIterations) has three modes:
   * - run(0): showForces()
   * - run(x < 0): run indefinitely
   * - run(x): run x rounds
   */ 
  public void run() {
    int nodes = this.graph.getNodes().size();
    running = true;
    if (this.maxNoOfIterations == 0) {
      showForces();
    }

    if (this.maxNoOfIterations < 0) {
      int j = 0;
      while (running){
        long startTime = System.nanoTime();
        this.clearDrawables();
        synchronized(nodePositions){
          nodePositions = applyAlgos();
        }
        cMinimumAngle.invalidate();
        improveSolution();
        // update of ui not every time when more than 200 nodes
        if(nodes > 200){
         // if( j % 20 == 0){ showNodePositions(); }
          showNodePositions(j);
        } else {
          showNodePositions();
        }

        this.currNoOfIterations = j;
        long endTime = System.nanoTime();
        System.out.println("Time taken: " + (endTime - startTime)/1000000 + " ms");
        try {
          Thread.sleep(1);
        } catch (InterruptedException exc) {
          System.out.println("Sleep interrupted!");
          //Do nothing...
        }
        if (infoLabel != null) {
          infoLabel.setText(displayMinimumAngle(graph));
        }
        j++;
      }
    } else {
      for (int i = 0; i < this.maxNoOfIterations; i++) {
        this.clearDrawables();
        nodePositions = applyAlgos();
        ForceAlgorithmApplier.applyNodePositionsToGraph(graph, nodePositions);
        this.currNoOfIterations = i;
        // update only every 10 iterations
        if(nodes > 200){
          if(i % 10 == 0){ this.view.updateUI();}
        } else {
          this.view.updateUI();
        }
        try {
          Thread.sleep(1);
        } catch (InterruptedException exc) {
          System.out.println("Sleep interrupted!");
          //Do nothing...
        }
        int progress = Math.round(100 * i / this.maxNoOfIterations);
        if (progressBar != null) {
          progressBar.setValue(progress);
        }
        if (infoLabel != null) {
          infoLabel.setText(displayMinimumAngle(graph));
        }
        running = false;
      }
    }

    if (progressBar != null) {
      progressBar.setValue(0);
    }
    JOptionPane.showMessageDialog(null, displayMaxMinAngle(), "Maximal Minimum Angle", JOptionPane.INFORMATION_MESSAGE);
    
    displayMinimumAngle(graph);
    this.view.updateUI();
    running = false;
  }

  /**
   * improveSolution: check current node positions and last best positions (metric: minimum crossing angle). If better, update.
   */
  public void improveSolution(){
    Mapper<INode, PointD> sol = nodePositions;
    Optional<Double> solutionAngle = cMinimumAngle.getMinimumAngle(graph, sol);
    /* best solution is a tuple of:
     * - nodePositions :: Mapper (INode, PointD)
     * - crossingAngle (if any) :: Optional Double
     * - force parameters :: [Double]
     * - force switches :: [Bool]
     */
    // do it lazy, since we might not need to
    Supplier<Tuple4<Mapper<INode, PointD>, Optional<Double>, Double[], Boolean[]>> thisSol
      = (() -> new Tuple4<>(copyNodePositionsMap(sol), solutionAngle, modifiers.clone(), switches.clone()));
    // if we hadn't had a previous best yet, update with our
    if (bestSolution == null) {
      bestSolution = thisSol.get();
      return;
    }
    // otherwise: previous best exists. get it...
    Tuple4<Mapper<INode, PointD>, Optional<Double>, Double[], Boolean[]> prevBest = bestSolution;
    // ... if previous one had no crossings, we can't be better, so we stop.
    if(!prevBest.b.isPresent()) return;
    // ... otherwise, if this one has no crossings, it must be better, so we update.
    if(!solutionAngle.isPresent()){
      bestSolution = thisSol.get();
      return;
    }
    // ... if the previous angle was better, we return.
    if(prevBest.b.get() >= solutionAngle.get()) return;
    // ... otherwise, ours is better, so we update.
    bestSolution = thisSol.get();
    
  }

  public void runNoDraw() {
    running = true;
    for (int i = 0; i < this.maxNoOfIterations && running; i++) {
      nodePositions = applyAlgos();
      improveSolution();
    }
    running = false;
  }

  public void draw(IGraph g){
    ForceAlgorithmApplier.applyNodePositionsToGraph(g, nodePositions);
    displayMinimumAngle(g);
    this.view.updateUI();
    if (infoLabel != null) {
      infoLabel.setText(displayMinimumAngle(g));
    }
    this.view.updateUI();
  }
  /**
   * Displays vectors for debugging purposes
   */
  public void displayVectors(Mapper<INode, PointD> map) {
    for(INode u: graph.getNodes()){
      PointD vector = map.getValue(u);
      System.out.println(vector);
      this.canvasObjects.add(this.view.getBackgroundGroup().addChild(
        new VectorVisual(this.view, vector, u, Color.GREEN),
        ICanvasObjectDescriptor.VISUAL));
    }
    this.view.updateUI();
  }

  public void clearDrawables() {
    for (ICanvasObject o: canvasObjects) {
      o.remove();
    }
    canvasObjects.clear();
    this.view.updateUI();
  }

  /* * * * * * * * * * * * *
   * BORING STUFF ENDS HERE *
   * * * * * * * * * * * * */

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

  

  public Mapper<INode, PointD> calculateAllForces(){
    List<NodePairForce> nodePairA = 
      ForceAlgorithmApplier.filterListSubclass(algos, NodePairForce.class);
    List<NodeNeighbourForce> nodeNeighbourA = 
      ForceAlgorithmApplier.filterListSubclass(algos, NodeNeighbourForce.class);
    List<IncidentEdgesForce> incidentEdgesA = 
      ForceAlgorithmApplier.filterListSubclass(algos, IncidentEdgesForce.class);
    List<CrossingForce> edgeCrossingsA = 
      ForceAlgorithmApplier.filterListSubclass(algos, CrossingForce.class);

    Mapper<INode, PointD> map = ForceAlgorithmApplier.initForceMap(graph);

    map = calculatePairwiseForces(nodePairA, map);
    map = calculateNeighbourForces(nodeNeighbourA, map);
    map = calculateIncidentForces(incidentEdgesA, map);
    map = calculateCrossingForces(edgeCrossingsA, map);

    return map;
  }

  // applyAlgos: calculateForces -> applyForces -> reset cache
  public Mapper<INode, PointD> applyAlgos(){
    Mapper<INode, PointD> res = applyForces(nodePositions, calculateAllForces());
    cMinimumAngle.invalidate();
    return res;
  }

  /**
   * Updates minimum crossing angle on the fly
   * @param graph
   * @return text - text to be displayed in gui
   */
  public String displayMinimumAngle(IGraph graph) {
    Optional<Intersection> crossing = cMinimumAngle.getMinimumAngleCrossing(graph, nodePositions);

    MinimumAngle.resetHighlighting(graph);
    Optional<String> s = crossing.map(currCross -> {
      if(currCross.angle > this.maxMinAngle){
        this.maxMinAngle = currCross.angle;
        this.maxMinAngleIterations = this.currNoOfIterations;
      }
      
      MinimumAngle.highlightCrossing(currCross);
      return DisplayMessagesGui.createMinimumAngleMsg(currCross);
    });
    return s.orElse("No crossings!");
    
  }

/**
   * Gets Message for Popup, which holds the maximal
   * minimum angle
   * @return text - the generated text for the pop up message
   */
  public String displayMaxMinAngle(){
    return DisplayMessagesGui.createMaxMinAngleMsg(this.maxMinAngle, this.maxMinAngleIterations);
  }


}

