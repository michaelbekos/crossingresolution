package layout.algo.forcealgorithm;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.forcealgorithm.forces.IForce;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.util.Collection;
import java.util.HashSet;
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
    Collection<AbstractLayoutInterfaceItem<Boolean>> toggleableParameters = new HashSet<>();
    for (IForce force : forces) {
      force.init(itemFactory, toggleableParameters);
    }

    AbstractLayoutInterfaceItem<Boolean> enableDisableAll = itemFactory.masterToggle("Select All", toggleableParameters);
    enableDisableAll.setValue(true);

    debugVectors = itemFactory.debugVectors("Forces");
  }

  AbstractLayoutInterfaceItem<Mapper<INode, PointD>> getDebugVectors() {
    return debugVectors;
  }
}
