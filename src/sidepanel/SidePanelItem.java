package sidepanel;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;

import javax.swing.*;

abstract class SidePanelItem<T> extends AbstractLayoutInterfaceItem<T> {
  private final JPanel sidePanel;
  private final GridBagState gridBagState;
  private int gridY;

  SidePanelItem(String name, JPanel sidePanel, GridBagState gridBagState) {
    super(name);
    this.sidePanel = sidePanel;
    this.gridBagState = gridBagState;
  }

  /**
   * All child classes must call this method from the constructor after they're fully initialized!
   */
  final void init() {
    createComponents(sidePanel, gridBagState);
    gridY = gridBagState.getY();
  }

  abstract void createComponents(JPanel sidePanel, GridBagState gridBagState);

  int getGridY() {
    return gridY;
  }

}
