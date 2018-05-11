package sidepanel;

import javax.swing.*;

class IntegerSidePanelItem extends SidePanelItem<Integer> {
  private int minValue;
  private int initialMaxValue;
  private IntSlider intSlider;

  IntegerSidePanelItem(String name, int minValue, int initialMaxValue, JPanel sidePanel, GridBagState gridBagState) {
    super(name, sidePanel, gridBagState);
    this.minValue = minValue;
    this.initialMaxValue = initialMaxValue;

    init();
  }

  @Override
  void createComponents(JPanel sidePanel, GridBagState gridBagState) {
    intSlider = Components.addIntSlider(getName(), minValue, initialMaxValue, sidePanel, gridBagState);
    intSlider.setValueListener(super::setValue);
  }

  @Override
  public void setValue(Integer value) {
    super.setValue(value);
    intSlider.setValue(value);
  }
}
