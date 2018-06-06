package layout.algo.forcealgorithm.forces;

import com.yworks.yfiles.algorithms.GraphConnectivity;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.layout.YGraphAdapter;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.util.ArrayList;
import java.util.Collection;

public class ElectricForce implements IForce {
  private final IGraph graph;
  private final double threshold;
  private AbstractLayoutInterfaceItem<Double> electricalRepulsion;
  private AbstractLayoutInterfaceItem<Boolean> activated;
  ArrayList<AbstractLayoutInterfaceItem> itemList;

  public ElectricForce(IGraph graph, double threshold) {
    this.graph = graph;
    this.threshold = threshold;
  }

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory, Collection<AbstractLayoutInterfaceItem<Boolean>> toggleableParameters) {
    itemList = new ArrayList<>();

    electricalRepulsion = itemFactory.doubleParameter("Electric Repulsion Force", 0.0, 100000);
    electricalRepulsion.setValue(50000.0);
    itemList.add(electricalRepulsion);

    activated = itemFactory.toggleableParameter(electricalRepulsion);
    activated.setValue(true);
    itemList.add(activated);
    toggleableParameters.add(activated);
  }

  /**
   * Calculate electric forces with classical spring embedder algorithm. i.e. calculateElectricForcesEades
   */
  @Override
  public ArrayList<AbstractLayoutInterfaceItem> getItems(){
    return itemList;
  }

  @Override
  public Mapper<INode, PointD> calculate(Mapper<INode, PointD> forces, Mapper<INode, PointD> nodePositions) {
    if (!activated.getValue()) {
      return forces;
    }

    YGraphAdapter adapter = new YGraphAdapter(graph);
    boolean[] reached = new boolean[graph.getNodes().size()];

    for (INode u : graph.getNodes()) {
      PointD p_u = nodePositions.getValue(u);

      GraphConnectivity.reachable(adapter.getYGraph(), adapter.getCopiedNode(u), false, reached);

      //Calculate Electrical forces
      for (INode v : graph.getNodes()) {
        if (u == v || !reached[adapter.getCopiedNode(v).index()]) continue;

        PointD p_v = nodePositions.getValue(v);
        PointD force = PointD.subtract(p_u, p_v).getNormalized();
        force = PointD.times(threshold * electricalRepulsion.getValue() / Math.pow(p_u.distanceTo(p_v), 2), force);

        forces.setValue(u, PointD.add(forces.getValue(u), force));
      }
    }

    return forces;
  }

}
