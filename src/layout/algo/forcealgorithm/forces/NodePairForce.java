package layout.algo.forcealgorithm.forces;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;
import util.G;

import java.util.Collection;

public class NodePairForce implements IForce {
  private IGraph graph;
  private AbstractLayoutInterfaceItem<Double> weight;
  private AbstractLayoutInterfaceItem<Boolean> activated;

  public NodePairForce(IGraph graph) {
    this.graph = graph;
  }

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory, Collection<AbstractLayoutInterfaceItem<Boolean>> toggleableParameters) {
    weight = itemFactory.doubleParameter("Node Pair Force", 0.0, 1);
    weight.setValue(0.01);

    activated = itemFactory.toggleableParameter(weight);
    activated.setValue(true);
    toggleableParameters.add(activated);
  }

  @Override
  public Mapper<INode, PointD> calculate(Mapper<INode, PointD> forces, Mapper<INode, PointD> nodePositions) {
    if (!activated.getValue()) {
      return forces;
    }
    graph.getNodes().parallelStream().forEach(n1 -> {
      PointD p1 = nodePositions.getValue(n1);
      PointD f1 = new PointD(0, 0);
      for(INode n2: graph.getNodes()){
        if(n1.equals(n2)) continue;
        PointD p2 = nodePositions.getValue(n2);
        // applying spring force
        PointD f = applyElectricForce(p1, p2);
        f1 = PointD.add(f1, f);
      }
      synchronized(forces){
        PointD f0 = forces.getValue(n1);
        forces.setValue(n1, PointD.add(f0, f1));
      }
    });
    return forces;
  }

  private PointD applyElectricForce(PointD p1, PointD p2) {
    double electricalRepulsion = 50000;
    double threshold = weight.getValue();
    PointD t = PointD.subtract(p1, p2);
    double dist = t.getVectorLength();
    if(dist <= G.Epsilon){
      return new PointD(0, 0);
    }
    t = PointD.div(t, dist);
    t = PointD.times(threshold * electricalRepulsion / Math.pow(dist, 2), t);
    return t;
  }


}