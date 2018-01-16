package layout.algo.layoutinterface;

import javax.swing.*;

public class SidePanelItemFactory implements ILayoutInterfaceItemFactory {
  private JPanel sidePanel;

  public SidePanelItemFactory(JPanel sidePanel) {
    this.sidePanel = sidePanel;
  }

  @Override
  public AbstractLayoutInterfaceItem<Double> doubleParameter(String name, double minValue, double maxValue) {
    return new DoubleSidePanelItem(name, sidePanel, minValue, maxValue);
  }

  @Override
  public AbstractLayoutInterfaceItem<Boolean> booleanParameter(String name) {
    return new BoolSidePanelItem(name, sidePanel);
  }
}
