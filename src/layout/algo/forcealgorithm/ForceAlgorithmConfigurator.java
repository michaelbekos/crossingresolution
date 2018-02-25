package layout.algo.forcealgorithm;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.forcealgorithm.forces.IForce;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
    AbstractLayoutInterfaceItem<Boolean> enableDisableAll = itemFactory.booleanParameter("Select All", true);
    enableDisableAll.setValue(true);
    enableDisableAll.addListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent itemEvent) {
        for (IForce force : forces) {
          force.toggleCheckbox(itemEvent.getStateChange() == ItemEvent.SELECTED);
        }
      }
    });

    for (IForce force : forces) {
      force.init(itemFactory);
    }
    debugVectors = itemFactory.debugVectors("Forces");
  }

  public AbstractLayoutInterfaceItem<Mapper<INode, PointD>> getDebugVectors() {
    return debugVectors;
  }
}
