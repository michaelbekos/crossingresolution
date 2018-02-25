package layout.algo.forcealgorithm.forces;

import com.yworks.yfiles.algorithms.GraphConnectivity;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.layout.YGraphAdapter;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

public class ElectricForce implements IForce {
  private final IGraph graph;
  private final double threshold;
  private AbstractLayoutInterfaceItem<Double> electricalRepulsion;

  public ElectricForce(IGraph graph, double threshold) {
    this.graph = graph;
    this.threshold = threshold;
  }

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    electricalRepulsion = itemFactory.doubleParameter("Electric Repulsion Force", 0.0, 100000, true);
    electricalRepulsion.setValue(50000.0);
  }

  @Override
  public Mapper<INode, PointD> calculate(Mapper<INode, PointD> forces, Mapper<INode, PointD> nodePositions) {
    if (electricalRepulsion.getValue() == 0) {
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

  public void toggleCheckbox(boolean value) {
    electricalRepulsion.toggleCheckbox(value);
  }
}
