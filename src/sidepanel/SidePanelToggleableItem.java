package sidepanel;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

class SidePanelToggleableItem extends SidePanelItem<Boolean> {
  private final SidePanelItem parameter;
  private JCheckBox checkBox;

  SidePanelToggleableItem(AbstractLayoutInterfaceItem<?> parameter, JPanel sidePanel, GridBagState gridBagState) {
    super("", sidePanel, gridBagState);
    if (!(parameter instanceof SidePanelItem)) {
      throw new AssertionError("assumed argument 'parameter' to be an instance of SidePanelItem, found: "
          + parameter.getClass());
    }
    this.parameter = (SidePanelItem) parameter;
    init();
  }

  @Override
  void createComponents(JPanel sidePanel, GridBagState gridBagState) {
    checkBox = new JCheckBox();
    checkBox.setSelected(true);

    GridBagConstraints cCheckBox = new GridBagConstraints();
    cCheckBox.fill = GridBagConstraints.HORIZONTAL;
    cCheckBox.gridx = 0;
    cCheckBox.gridy = parameter.getGridY();
    sidePanel.add(checkBox, cCheckBox);
    checkBox.addItemListener(this::itemListener);

    JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
    separator.setPreferredSize(new Dimension(5,1));
    GridBagConstraints cgc = new GridBagConstraints();
    cgc.gridx = 1;
    cgc.gridheight = gridBagState.getY() + 1;
    cgc.fill = GridBagConstraints.VERTICAL;
    sidePanel.add(separator, cgc);
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
