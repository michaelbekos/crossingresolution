package layout.algo;

import com.yworks.yfiles.view.IGraphSelection;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

public class ClinchLayoutConfigurator implements ILayoutConfigurator {
  AbstractLayoutInterfaceItem<IGraphSelection> selection;
  AbstractLayoutInterfaceItem<Double> initialStepSize;
  AbstractLayoutInterfaceItem<Double> stepSizeMultiplier;

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    selection = itemFactory.selection("Selection");
    initialStepSize = itemFactory.doubleParameter("Initial Step Size", 0.5, 5.0, 1.0);
    stepSizeMultiplier = itemFactory.doubleParameter("Step Size Multiplier", 1.0, 5.0, 1.0);

    initialStepSize.setValue(2.5);
    stepSizeMultiplier.setValue(1.5);
  }
}