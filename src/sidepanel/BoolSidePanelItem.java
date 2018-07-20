package sidepanel;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;

import javax.swing.*;
import java.awt.event.ItemEvent;

class BoolSidePanelItem extends SidePanelItem<Boolean> {
  private JCheckBox checkBox;
  private Boolean visible;

  BoolSidePanelItem(String name, Boolean visible, JPanel sidePanel, GridBagState gridBagState) {
    super(name, sidePanel, gridBagState);
//    super.setVisible(visible);
    this.visible = visible;
    init();
  }

  @Override
  void createComponents(JPanel sidePanel, GridBagState gridBagState) {
    checkBox = Components.addCheckBox(getName(), sidePanel, gridBagState, this::itemListener, visible);
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
