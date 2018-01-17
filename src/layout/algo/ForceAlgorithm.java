package layout.algo;

import algorithms.graphs.CachedMinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.ICanvasObject;
import com.yworks.yfiles.view.ICanvasObjectDescriptor;
import layout.algo.forces.IForce;
import layout.algo.utils.PositionMap;
import view.visual.VectorVisual;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ForceAlgorithm implements ILayout {
  public ForceAlgorithmConfigurator configurator;
  public List<IForce> forces = new LinkedList<>();
  private GraphComponent view;
  private IGraph graph;
  private Mapper<INode, PointD> nodePositions;
  private CachedMinimumAngle cMinimumAngle;

  public ForceAlgorithm(ForceAlgorithmConfigurator configurator, GraphComponent view, CachedMinimumAngle cMinimumAngle){
    this.configurator = configurator;
    this.view = view;
    this.graph = view.getGraph();
    this.cMinimumAngle = cMinimumAngle;
  }

  @Override
  public ForceAlgorithm clone(){
    // TODO: clone configurator
    ForceAlgorithm ret = new ForceAlgorithm(configurator, this.view, cMinimumAngle);
    ret.graph = this.graph;
    ret.nodePositions = PositionMap.copy(this.nodePositions);
    ret.forces = this.forces;

    return ret;
  }

  @Override
  public void init() {
    nodePositions = PositionMap.FromIGraph(graph);
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

  // show all forces that would be applied to nodes currently
  public void showForces(){
    this.clearDrawables();
    Mapper<INode, PointD> map = calculateAllForces();
    this.displayVectors(map);
  }

  /**
   * Displays vectors for debugging purposes
   */
  private void displayVectors(Mapper<INode, PointD> map) {
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

  private Mapper<INode, PointD> calculateAllForces(){
    Mapper<INode, PointD> map = ForceAlgorithm.initForceMap();

    for (IForce force : forces) {
      map = force.calculate(map, nodePositions);
    }

    return map;
  }

  // applyAlgos: calculateForces -> applyForces -> reset cache
  private Mapper<INode, PointD> applyAlgos(){
    Mapper<INode, PointD> res = applyForces(nodePositions, calculateAllForces());
    cMinimumAngle.invalidate();
    return res;
  }

  // initForceMap creates a forceMap with default force (0,0).
  private static Mapper<INode, PointD> initForceMap(){
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

