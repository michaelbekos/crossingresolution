package layout.algo;

import com.sun.istack.internal.Nullable;
import com.yworks.yfiles.geometry.*;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.view.*;


import java.util.*;
import java.util.stream.*;
import java.awt.Color;


import javax.swing.*;

import layout.algo.forces.*;
import layout.algo.utils.PositionMap;
import util.*;
import util.graph2d.*;
import algorithms.graphs.*;
import view.visual.*;

public class ForceAlgorithmApplier implements ILayout {

  // list of algos to apply to graph
  public List<IForce> algos = new LinkedList<>();
  // underlying graph info
  public GraphComponent view;
  public IGraph graph;
  private int maxNoOfIterations;
  private int currNoOfIterations;
  private int maxMinAngleIterations;
  @Nullable
  private JProgressBar progressBar;
  @Nullable
  private JLabel infoLabel;
  private double maxMinAngle;
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
    this.maxNoOfIterations = maxNoOfIterations;
    this.maxMinAngleIterations = 0;
    this.progressBar = progressBar;
    this.infoLabel = infoLabel;
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

  @Override
  public void init() {
    nodePositions = PositionMap.FromIGraph(graph);
    maxMinAngle = cMinimumAngle.getMinimumAngleCrossing(graph, nodePositions).map(t -> t.angle).orElse(0.0);
    TrashCan.improveSolution(cMinimumAngle, graph, modifiers, nodePositions, switches);
  }

  @Override
  public boolean executeStep(int iteration) {
    nodePositions = applyAlgos();
    return false;
  }

  @Override
  public Mapper<INode, PointD> getNodePositions() {
    return nodePositions;
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
    TrashCan.improveSolution(cMinimumAngle, graph, modifiers, nodePositions, switches);
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

  public void runNoDraw() {
    running = true;
    for (int i = 0; i < this.maxNoOfIterations && running; i++) {
      nodePositions = applyAlgos();
      TrashCan.improveSolution(cMinimumAngle, graph, modifiers, nodePositions, switches);
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
      TrashCan.canvasObjects.add(this.view.getBackgroundGroup().addChild(
        new VectorVisual(this.view, vector, u, Color.GREEN),
        ICanvasObjectDescriptor.VISUAL));
    }
    this.view.updateUI();
  }

  public void clearDrawables() {
    for (ICanvasObject o: TrashCan.canvasObjects) {
      o.remove();
    }
    TrashCan.canvasObjects.clear();
    this.view.updateUI();
  }

  /* * * * * * * * * * * * *
   * BORING STUFF ENDS HERE *
   * * * * * * * * * * * * */


  public Mapper<INode, PointD> calculateAllForces(){
    Mapper<INode, PointD> map = ForceAlgorithmApplier.initForceMap(graph);

    for (IForce force : algos) {
      map = force.calculate(map, nodePositions);
    }

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
}

