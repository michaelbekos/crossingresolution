package layout.algo.forces;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.util.ArrayList;

public class SpringForce implements IForce {
  private IGraph graph;
  private double springNaturalLength;
  private double threshold;
  private double springStiffness;

  public SpringForce(IGraph graph, double springNaturalLength, double threshold, double springStiffness) {
    this.graph = graph;
    this.springNaturalLength = springNaturalLength;
    this.threshold = threshold;
    this.springStiffness = springStiffness;
  }

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {

  }

  @Override
  public Mapper<INode, PointD> calculate(Mapper<INode, PointD> forces, Mapper<INode, PointD> nodePositions) {

    for (INode u : graph.getNodes())
    {
      PointD p_u = nodePositions.getValue(u);
      //Calculate Spring forces...
      for (INode v : graph.neighbors(INode.class, u))
      {
        PointD p_v = nodePositions.getValue(v);

        PointD force = PointD.subtract(p_v, p_u).getNormalized();
        force = PointD.times(force, threshold * springStiffness * Math.log(p_u.distanceTo(p_v) / springNaturalLength));

        forces.setValue(u, PointD.add(forces.getValue(u), force));
      }
    }

    return forces;
  }

  @Override
  public ArrayList<AbstractLayoutInterfaceItem> getAbstractLayoutInterfaceItems() {
    ArrayList<AbstractLayoutInterfaceItem> parameterList = new ArrayList<>();
    return parameterList;
  }
}
