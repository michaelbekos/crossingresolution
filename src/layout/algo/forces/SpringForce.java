package layout.algo.forces;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

public class SpringForce implements IForce {
  private IGraph graph;
  private double springNaturalLength;
  private double threshold;
  private AbstractLayoutInterfaceItem<Double> springStiffness;

  public SpringForce(IGraph graph, double springNaturalLength, double threshold) {
    this.graph = graph;
    this.springNaturalLength = springNaturalLength;
    this.threshold = threshold;
  }

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    springStiffness = itemFactory.doubleParameter("Spring Stiffness Force", 0.0, 150, true);
    springStiffness.setValue(150.0);
  }

  @Override
  public Mapper<INode, PointD> calculate(Mapper<INode, PointD> forces, Mapper<INode, PointD> nodePositions) {
    if (springStiffness.getValue() == 0) {
      return forces;
    }
    for (INode u : graph.getNodes()) {
      PointD p_u = nodePositions.getValue(u);
      //Calculate Spring forces...
      for (INode v : graph.neighbors(INode.class, u)) {
        PointD p_v = nodePositions.getValue(v);

        PointD force = PointD.subtract(p_v, p_u).getNormalized();
        force = PointD.times(force, threshold * springStiffness.getValue() * Math.log(p_u.distanceTo(p_v) / springNaturalLength));

        forces.setValue(u, PointD.add(forces.getValue(u), force));
      }
    }

    return forces;
  }


  public void toggleCheckbox(boolean value) {
    springStiffness.toggleCheckbox(value);
  }
}
