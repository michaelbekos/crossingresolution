package layout.algo.forces;

import com.yworks.yfiles.algorithms.GraphConnectivity;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.layout.YGraphAdapter;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

public class ElectricForce implements IForce {
  private final YGraphAdapter adapter;
  private final IGraph graph;
  private final double threshold;
  private final double electricalRepulsion;

  public ElectricForce(IGraph graph, double threshold, double electricalRepulsion) {
    this.graph = graph;
    this.adapter = new YGraphAdapter(graph);
    this.threshold = threshold;
    this.electricalRepulsion = electricalRepulsion;
  }

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {

  }

  @Override
  public Mapper<INode, PointD> calculate(Mapper<INode, PointD> forces, Mapper<INode, PointD> nodePositions) {
    boolean[] reached = new boolean[graph.getNodes().size()];

    for (INode u : graph.getNodes()) {
      PointD p_u = nodePositions.getValue(u);

      GraphConnectivity.reachable(adapter.getYGraph(), adapter.getCopiedNode(u), false, reached);

      //Calculate Electrical forces
      for (INode v : graph.getNodes()) {
        if (u == v || !reached[adapter.getCopiedNode(v).index()]) continue;

        PointD p_v = nodePositions.getValue(v);
        PointD force = PointD.subtract(p_u, p_v).getNormalized();
        force = PointD.times(threshold * electricalRepulsion / Math.pow(p_u.distanceTo(p_v), 2), force);

        forces.setValue(u, PointD.add(forces.getValue(u), force));
      }
    }

    return forces;
  }
}
