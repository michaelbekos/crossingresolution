package sidepanel;

import javax.swing.*;
import java.awt.event.ItemEvent;

class BoolSidePanelItem extends SidePanelItem<Boolean> {
  private JCheckBox checkBox;

  BoolSidePanelItem(String name, JPanel sidePanel, GridBagState gridBagState) {
    super(name, sidePanel, gridBagState);
    init();
  }

  @Override
  void createComponents(JPanel sidePanel, GridBagState gridBagState) {
    checkBox = Components.addCheckBox(getName(), sidePanel, gridBagState, this::itemListener);
  }

  @Override
  public void setValue(Boolean value) {
    super.setValue(value);
    checkBox.setSelected(value);
  }

  private void itemListener(ItemEvent itemEvent) {
    setValue((itemEvent.getStateChange() == ItemEvent.SELECTED));
  }
}
