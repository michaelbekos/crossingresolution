package sidepanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

class BoolSidePanelItem extends SidePanelItem<Boolean> {
  private JCheckBox checkBox;

  BoolSidePanelItem(String name, JPanel sidePanel, GridBagState gridBagState) {
    super(name, sidePanel, gridBagState);
    init();
  }

  @Override
  void createComponents(JPanel sidePanel, GridBagState gridBagState) {
    //add checkbox
    GridBagConstraints cCustomPanel = new GridBagConstraints();
    cCustomPanel.gridy = gridBagState.increaseY();
    cCustomPanel.gridx = 0;
    cCustomPanel.gridwidth = 3;
    cCustomPanel.anchor = GridBagConstraints.LINE_START;
    checkBox = new JCheckBox(getName());
    sidePanel.add(checkBox, cCustomPanel);
    checkBox.addItemListener(itemEvent -> setValue((itemEvent.getStateChange() == ItemEvent.SELECTED)));
  }

  @Override
  public void setValue(Boolean value) {
    super.setValue(value);
    checkBox.setSelected(value);
  }
}
