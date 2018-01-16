package layout.algo.layoutinterface;

import javax.swing.*;

public class SidePanelItem<T> extends AbstractLayoutInterfaceItem<T> {
  final JPanel sidePanel;

  SidePanelItem(String name, JPanel sidePanel) {
    super(name);
    this.sidePanel = sidePanel;
  }
}
