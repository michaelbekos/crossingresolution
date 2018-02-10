package layout.algo;

import algorithms.graphs.CachedMinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.forces.IForce;
import layout.algo.utils.PositionMap;

import java.util.Map;
import java.util.Optional;

public class ForceAlgorithm implements ILayout {
  public ForceAlgorithmConfigurator configurator;
  private IGraph graph;
  private Mapper<INode, PointD> nodePositions;
  private CachedMinimumAngle cMinimumAngle;

  public ForceAlgorithm(ForceAlgorithmConfigurator configurator, IGraph graph, CachedMinimumAngle cMinimumAngle){
    this.configurator = configurator;
    this.graph = graph;
    this.cMinimumAngle = cMinimumAngle;
  }

  @Override
  public ForceAlgorithm clone(){
    ForceAlgorithm ret = new ForceAlgorithm(configurator, graph, cMinimumAngle);
    ret.nodePositions = PositionMap.copy(this.nodePositions);

    return ret;
  }

  @Override
  public void init() {
    nodePositions = PositionMap.FromIGraph(graph);
  }

  @Override
  public boolean executeStep(int iteration, int maxIterations) {
    nodePositions = applyAlgos();
    return false;
  }

  @Override
  public Mapper<INode, PointD> getNodePositions() {
    return nodePositions;
  }

  public void setNodePositions(Mapper<INode, PointD> nodePositions) {
    this.nodePositions = nodePositions;
    cMinimumAngle.invalidate();
  }

  @Override
  public void showDebug() {
    Mapper<INode, PointD> forces = calculateAllForces();
    configurator.getDebugVectors().setValue(forces);
  }

  @Override
  public void clearDebug() {
    configurator.getDebugVectors().setValue(null);
  }

  private Mapper<INode, PointD> calculateAllForces(){
    Mapper<INode, PointD> map = ForceAlgorithm.initForceMap();

    for (IForce force : configurator.forces) {
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

  public Optional<Double> getMinimumAngle() {
    return cMinimumAngle.getMinimumAngle(graph, nodePositions);
  }
}

