package sidepanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

class BoolSidePanelItem extends SidePanelItem<Boolean> {
  private JCheckBox checkBox;
  private final boolean useAsMasterCheckbox;

  BoolSidePanelItem(String name, JPanel sidePanel, GridBagState gridBagState, boolean useAsMasterCheckbox) {
    super(name, sidePanel, gridBagState);
    this.useAsMasterCheckbox = useAsMasterCheckbox;
    init();
  }

  @Override
  void createComponents(JPanel sidePanel, GridBagState gridBagState) {
    //add checkbox
    GridBagConstraints cCustomPanel = new GridBagConstraints();
    if (useAsMasterCheckbox) {
      cCustomPanel.gridx = 0;
    } else {
      cCustomPanel.gridx = gridBagState.getX();
    }
    cCustomPanel.gridy = gridBagState.increaseY();
    cCustomPanel.gridwidth = 3;
    cCustomPanel.anchor = GridBagConstraints.LINE_START;
    checkBox = new JCheckBox(getName());
    sidePanel.add(checkBox, cCustomPanel);
    checkBox.addItemListener(this::itemListener);
  }

  @Override
  public void setValue(Boolean value) {
    super.setValue(value);
    checkBox.setSelected(value);
  }

  private void itemListener(ItemEvent itemEvent) {
    setValue((itemEvent.getStateChange() == ItemEvent.SELECTED));
  }

  @Override
  public void addListener(Object listener) {
    if (listener instanceof  ItemListener) {
      checkBox.addItemListener((ItemListener) listener);
    }
  }

}
