package sidepanel;

import javax.swing.*;

public class DoubleSidePanelItem extends SidePanelItem<Double> {
  private double minValue;
  private double initialMaxValue;
  private DoubleSlider doubleSlider;

  DoubleSidePanelItem(String name, double minValue, double initialMaxValue, JPanel sidePanel, GridBagState gridBagState) {
    super(name, sidePanel, gridBagState);
    this.minValue = minValue;
    this.initialMaxValue = initialMaxValue;
    init();
  }

  @Override
  void createComponents(JPanel sidePanel, GridBagState gridBagState) {
    doubleSlider = Components.addDoubleSlider(getName(), minValue, initialMaxValue, sidePanel, gridBagState);
    doubleSlider.setValueListener(super::setValue);
  }

  @Override
  public void setValue(Double value) {
    value = Math.min(Math.max(minValue, value), doubleSlider.getCurrentMaxValue());
    super.setValue(value);
    doubleSlider.setValue(value);
  }
}
