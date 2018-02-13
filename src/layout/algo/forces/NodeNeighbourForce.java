package layout.algo.forces;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.ForceAlgorithmConfigurator;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;
import util.G;

public class NodeNeighbourForce implements IForce {
  private IGraph graph;
  private AbstractLayoutInterfaceItem<Double> weight;

  public NodeNeighbourForce(IGraph graph) {
    this.graph = graph;
  }


  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    weight = itemFactory.doubleParameter("Node Neighbor Force", 0.0, 300, 200, true);
    weight.setValue(170.0);
  }

  @Override
  public Mapper<INode, PointD> calculate(Mapper<INode, PointD> forces, Mapper<INode, PointD> nodePositions) {
    if (weight.getValue() == 0) {
      return forces;
    }
    //for(INode n1: graph.getNodes()){
    graph.getNodes().parallelStream().forEach(n1 -> {
      PointD p1 = nodePositions.getValue(n1);
      PointD f1 = new PointD(0, 0);
      for(INode n2: graph.neighbors(INode.class, n1)){

        PointD p2 = nodePositions.getValue(n2);
        PointD f = applySpringForce(p1, p2);
        f1 = PointD.add(f1, f);
      }
      synchronized(forces){
        PointD f0 = forces.getValue(n1);
        forces.setValue(n1, PointD.add(f0, f1));
      }
    });
    return forces;
  }

  private PointD applySpringForce(PointD p1, PointD p2) {
    double springNaturalLength = weight.getValue();
    PointD t = PointD.subtract(p2, p1);
    double dist = t.getVectorLength();
    if(dist <= G.Epsilon){
      return new PointD(0, 0);
    }
    t = PointD.div(t, dist);
    double x = dist - springNaturalLength;
    x = x / springNaturalLength;
    double forceStrength = Math.atan(Math.pow(x, 4)) * x;
    if(Double.isNaN(forceStrength)) {
      System.out.println("!NaN!");
      System.out.println(dist);
      return new PointD(0, 0);
    }
    //System.out.println(forceStrength * configurator.modifiers[1]);
    t = PointD.times(t, forceStrength);
    //return new PointD(0, 0);
    return t;
  }
}