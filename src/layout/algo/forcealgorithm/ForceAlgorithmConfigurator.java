package layout.algo.forcealgorithm;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.forcealgorithm.forces.IForce;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.util.*;

public class ForceAlgorithmConfigurator implements ILayoutConfigurator {
  public List<IForce> forces = new LinkedList<>();
  private ArrayList<AbstractLayoutInterfaceItem> itemList;


  private AbstractLayoutInterfaceItem<Mapper<INode, PointD>> debugVectors;

  public ForceAlgorithmConfigurator addForce(IForce force) {
    forces.add(force);
    return this;
  }


  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    itemList = new ArrayList<>();

    Collection<AbstractLayoutInterfaceItem<Boolean>> toggleableParameters = new HashSet<>();
    for (IForce force : forces) {
      force.init(itemFactory, toggleableParameters);
      for(AbstractLayoutInterfaceItem abs : force.getItems()){
          itemList.add(abs);
      }
    }

    AbstractLayoutInterfaceItem<Boolean> enableDisableAll = itemFactory.masterToggle("Select All", toggleableParameters);
    enableDisableAll.setValue(true);
    itemList.add(enableDisableAll);

    debugVectors = itemFactory.debugVectors("Forces");
  }
  @Override
  public ArrayList<AbstractLayoutInterfaceItem> getItems(){
    return itemList;
  }


  AbstractLayoutInterfaceItem<Mapper<INode, PointD>> getDebugVectors() {
    return debugVectors;
  }

  @Override
  public Optional<Map<String, AbstractLayoutInterfaceItem<Boolean>>> getBooleanParameters() {
    return Optional.empty();
  }
}
