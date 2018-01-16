package layout.algo;

import com.sun.istack.internal.Nullable;
import com.yworks.yfiles.geometry.*;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.view.*;


import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.awt.Color;


import javax.swing.*;

import layout.algo.forces.*;
import layout.algo.utils.PositionMap;
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

  // initForceMap creates a forceMap with default force (0,0).
  private static Mapper<INode, PointD> initForceMap(IGraph g){
    Mapper<INode, PointD> map = PositionMap.newPositionMap();
    map.setDefaultValue(new PointD(0, 0));
    return map;
  }

  // add all forces to the corresponding nodes
  private static Mapper<INode, PointD> applyForces(Mapper<INode, PointD> nodePositions, Mapper<INode, PointD> forces) {
    for (Map.Entry<INode, PointD> e : nodePositions.getEntries()) {
      INode node = e.getKey();

      PointD position = e.getValue();
      PointD force = forces.getValue(node);

      nodePositions.setValue(node, PointD.add(position, force));
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
    nodePositions = PositionMap.FromIGraph(graph);
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
    ret.nodePositions = PositionMap.copy(this.nodePositions);
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
          nodePositions = PositionMap.FromIGraph(graph);
          break;
        }
        nodePositions.setValue(u, p);
      }
    }
    cMinimumAngle.invalidate();
    improveSolution();
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
    PositionMap.applyToGraph(graph, nodePositions);
    this.view.updateUI();
  }

  // apply internal position map to graph
  public void showNodePositions(int j){
    PositionMap.applyToGraph(graph, nodePositions);
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
        PositionMap.applyToGraph(graph, nodePositions);
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
      = (() -> new Tuple4<>(PositionMap.copy(sol), solutionAngle, modifiers.clone(), switches.clone()));
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
    PositionMap.applyToGraph(g, nodePositions);
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

    ForceAlgorithmApplierForces forces = new ForceAlgorithmApplierForces(graph, nodePositions, cMinimumAngle);
    map = forces.calculatePairwiseForces(nodePairA, map);
    map = forces.calculateNeighbourForces(nodeNeighbourA, map);
    map = forces.calculateIncidentForces(incidentEdgesA, map);
    map = forces.calculateCrossingForces(edgeCrossingsA, map);

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

