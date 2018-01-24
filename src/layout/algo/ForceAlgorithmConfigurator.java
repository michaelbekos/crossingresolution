package layout.algo;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.forces.IForce;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ForceAlgorithmConfigurator implements ILayoutConfigurator {
  public List<IForce> forces = new LinkedList<>();

  private AbstractLayoutInterfaceItem<Mapper<INode, PointD>> debugVectors;

  public ForceAlgorithmConfigurator addForce(IForce force) {
    forces.add(force);
    return this;
  }


  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    for (IForce force : forces) {
      force.init(itemFactory);
    }
    debugVectors = itemFactory.debugVectors("Forces");
  }

  @Override
  public ArrayList<AbstractLayoutInterfaceItem> getAbstractLayoutInterfaceItems(){
    // TODO:
    ArrayList<AbstractLayoutInterfaceItem> parameterList = new ArrayList<>();
    for (IForce f : forces) {
      parameterList.addAll(f.getAbstractLayoutInterfaceItems());
    }
    return parameterList;
  }

  public AbstractLayoutInterfaceItem<Mapper<INode, PointD>> getDebugVectors() {
    return debugVectors;
  }
}
