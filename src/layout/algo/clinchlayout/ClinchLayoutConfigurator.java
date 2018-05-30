package layout.algo.clinchlayout;

import com.yworks.yfiles.view.IGraphSelection;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.util.ArrayList;

public class ClinchLayoutConfigurator implements ILayoutConfigurator {
  AbstractLayoutInterfaceItem<IGraphSelection> selection;
  AbstractLayoutInterfaceItem<Double> initialStepSize;
  AbstractLayoutInterfaceItem<Double> stepSizeMultiplier;
  ArrayList<AbstractLayoutInterfaceItem> itemList;

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    itemList = new ArrayList<>();

    selection = itemFactory.selection("Selection");
    itemList.add(selection);

    initialStepSize = itemFactory.doubleParameter("Initial Step Size", 0.5, 5.0);
    initialStepSize.setValue(2.5);
    itemList.add(initialStepSize);

    stepSizeMultiplier = itemFactory.doubleParameter("Step Size Multiplier", 1.0, 5.0);
    stepSizeMultiplier.setValue(1.5);
    itemList.add(stepSizeMultiplier);

  }
  @Override
  public ArrayList<AbstractLayoutInterfaceItem> getItems(){
    return itemList;
  }

}
