package sidepanel;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Collection;

class MasterToggleSidePanelItem extends SidePanelItem<Boolean> {
  private final Collection<AbstractLayoutInterfaceItem<Boolean>> parameters;
  private JCheckBox checkBox;

  MasterToggleSidePanelItem(String name, Collection<AbstractLayoutInterfaceItem<Boolean>> parameters, JPanel sidePanel, GridBagState gridBagState) {
    super(name, sidePanel, gridBagState);
    this.parameters = parameters;
    init();
  }

  @Override
  void createComponents(JPanel sidePanel, GridBagState gridBagState) {
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    checkBox = new JCheckBox();
    sidePanel.add(checkBox, gridBagConstraints);
    checkBox.addItemListener(itemEvent -> {
      boolean selected = itemEvent.getStateChange() == ItemEvent.SELECTED;
      setValue(selected);
      for (AbstractLayoutInterfaceItem<Boolean> parameter : parameters) {
        parameter.setValue(selected);
      }
    });
  }

  @Override
  public void setValue(Boolean value) {
    super.setValue(value);
    checkBox.setSelected(value);
  }
}
