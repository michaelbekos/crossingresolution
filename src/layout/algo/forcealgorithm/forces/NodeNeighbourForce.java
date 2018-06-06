package layout.algo.forcealgorithm.forces;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;
import util.G;

import java.util.ArrayList;
import java.util.Collection;

public class NodeNeighbourForce implements IForce {
  private IGraph graph;
  private AbstractLayoutInterfaceItem<Double> weight;
  private AbstractLayoutInterfaceItem<Boolean> activated;
  ArrayList<AbstractLayoutInterfaceItem> itemList;

  public NodeNeighbourForce(IGraph graph) {
    this.graph = graph;
  }


  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory, Collection<AbstractLayoutInterfaceItem<Boolean>> toggleableParameters) {
    itemList = new ArrayList<>();

    weight = itemFactory.doubleParameter("Node Neighbor Force", 0.0, 300);
    weight.setValue(170.0);
    itemList.add(weight);

    activated = itemFactory.toggleableParameter(weight);
    activated.setValue(true);
    itemList.add(activated);
    toggleableParameters.add(activated);
  }
  @Override
  public ArrayList<AbstractLayoutInterfaceItem> getItems(){
    return itemList;
  }


  /**
   * Calculate spring forces with Fruchterman & Reingold algorithm. i.e.calculateAttractiveForcesFR
   */
  @Override
  public Mapper<INode, PointD> calculate(Mapper<INode, PointD> forces, Mapper<INode, PointD> nodePositions) {
    if (!activated.getValue()) {
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