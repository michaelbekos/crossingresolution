package layout.algo;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

public class ForceAlgorithmConfigurator implements ILayoutConfigurator {
  private AbstractLayoutInterfaceItem<Double> nodePairWeight;
  private AbstractLayoutInterfaceItem<Double> nodeNeighborWeight;
  private AbstractLayoutInterfaceItem<Double> crossingForce;
  private AbstractLayoutInterfaceItem<Double> incidentEdgesForce;
  private AbstractLayoutInterfaceItem<Boolean> perpendicular;

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    // TODO: reasonable bounds
    nodePairWeight = itemFactory.doubleParameter("Node Pair Force", 0.01, 2);
    nodeNeighborWeight = itemFactory.doubleParameter("Node Neighbor Force", 0.01, 2);
    incidentEdgesForce = itemFactory.doubleParameter("Incident Edges Force", 0.01, 2);
    crossingForce = itemFactory.doubleParameter("Crossings Force", 0.01, 2);
    perpendicular = itemFactory.booleanParameter("Perpendicular");

    nodePairWeight.setValue(0.01);
    nodeNeighborWeight.setValue(0.01);
    incidentEdgesForce.setValue(0.01);
    crossingForce.setValue(0.1);
    perpendicular.setValue(false);
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
}
