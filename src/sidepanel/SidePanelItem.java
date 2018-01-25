package sidepanel;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;

import javax.swing.*;

public class SidePanelItem<T> extends AbstractLayoutInterfaceItem<T> {
  final JTabbedPane sidePanel;

  SidePanelItem(String name, JTabbedPane sidePanel) {
    super(name);
    this.sidePanel = sidePanel;
  }
}
