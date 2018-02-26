package layout.algo.forcealgorithm.forces;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.util.Collection;

public class SpringForce implements IForce {
  private IGraph graph;
  private double springNaturalLength;
  private double threshold;
  private AbstractLayoutInterfaceItem<Double> springStiffness;
  private AbstractLayoutInterfaceItem<Boolean> activated;

  public SpringForce(IGraph graph, double springNaturalLength, double threshold) {
    this.graph = graph;
    this.springNaturalLength = springNaturalLength;
    this.threshold = threshold;
  }

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory, Collection<AbstractLayoutInterfaceItem<Boolean>> toggleableParameters) {
    springStiffness = itemFactory.doubleParameter("Spring Stiffness Force", 0.0, 150);
    springStiffness.setValue(150.0);

    activated = itemFactory.toggleableParameter(springStiffness);
    activated.setValue(true);
    toggleableParameters.add(activated);
  }

  @Override
  public Mapper<INode, PointD> calculate(Mapper<INode, PointD> forces, Mapper<INode, PointD> nodePositions) {
    if (!activated.getValue()) {
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


}