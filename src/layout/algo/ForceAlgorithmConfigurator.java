package layout.algo;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.util.ArrayList;
import java.util.List;

public class ForceAlgorithmConfigurator implements ILayoutConfigurator {
  private AbstractLayoutInterfaceItem<Double> nodePairWeight;
  private AbstractLayoutInterfaceItem<Double> nodeNeighborWeight;
  private AbstractLayoutInterfaceItem<Double> crossingForce;
  private AbstractLayoutInterfaceItem<Double> incidentEdgesForce;
  private AbstractLayoutInterfaceItem<Boolean> perpendicular;

  // this is mainly required for the genetic algorithm - you should usually access the fields directly
  private List<AbstractLayoutInterfaceItem> weights;

  private AbstractLayoutInterfaceItem<Mapper<INode, PointD>> debugVectors;

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    nodePairWeight = itemFactory.doubleParameter("Node Pair Force", 0.01, 1, 0.1);
    nodeNeighborWeight = itemFactory.doubleParameter("Node Neighbor Force", 1, 300, 200);
    incidentEdgesForce = itemFactory.doubleParameter("Incident Edges Force", 0.1, 1,1);
    crossingForce = itemFactory.doubleParameter("Crossings Force", 0.01, 1,0.1);
    perpendicular = itemFactory.booleanParameter("Perpendicular");

    nodePairWeight.setValue(0.01);
    nodeNeighborWeight.setValue(170.0);
    incidentEdgesForce.setValue(0.1);
    crossingForce.setValue(0.01);
    perpendicular.setValue(false);

    weights = new ArrayList<>(5);
    weights.add(nodePairWeight);
    weights.add(nodeNeighborWeight);
    weights.add(incidentEdgesForce);
    weights.add(crossingForce);
    weights.add(perpendicular);

    debugVectors = itemFactory.debugVectors("Forces");
  }

  @Override
  public ArrayList<AbstractLayoutInterfaceItem> getAbstractLayoutInterfaceItems(){
    ArrayList<AbstractLayoutInterfaceItem> parameterList = new ArrayList<>();
    parameterList.add(nodePairWeight);
    parameterList.add(nodeNeighborWeight);
    parameterList.add(crossingForce);
    parameterList.add(incidentEdgesForce);
    parameterList.add(perpendicular);
    return parameterList;
  }

  public ForceAlgorithmConfigurator copy(ILayoutInterfaceItemFactory itemFactory) {
    ForceAlgorithmConfigurator clone = new ForceAlgorithmConfigurator();
    clone.init(itemFactory);

    clone.nodePairWeight.setValue(this.nodePairWeight.getValue());
    clone.nodeNeighborWeight.setValue(this.nodeNeighborWeight.getValue());
    clone.incidentEdgesForce.setValue(this.incidentEdgesForce.getValue());
    clone.crossingForce.setValue(this.crossingForce.getValue());
    clone.perpendicular.setValue(this.perpendicular.getValue());

    return clone;
  }

  public AbstractLayoutInterfaceItem<Double> getNodePairWeight() {
    return nodePairWeight;
  }

  public AbstractLayoutInterfaceItem<Double> getNodeNeighborWeight() {
    return nodeNeighborWeight;
  }

  public AbstractLayoutInterfaceItem<Double> getCrossingForce() {
    return crossingForce;
  }

  public AbstractLayoutInterfaceItem<Double> getIncidentEdgesForce() {
    return incidentEdgesForce;
  }

  public AbstractLayoutInterfaceItem<Boolean> getPerpendicular() {
    return perpendicular;
  }

  public List<AbstractLayoutInterfaceItem> getWeights() {
    return weights;
  }

  public AbstractLayoutInterfaceItem<Mapper<INode, PointD>> getDebugVectors() {
    return debugVectors;
  }
}
