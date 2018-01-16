package layout.algo.layoutinterface;

import javax.swing.*;

public class DoubleSidePanelItem extends SidePanelItem<Double> {
  private final double minValue;
  private final double maxValue;

  DoubleSidePanelItem(String name, JPanel sidePanel, double minValue, double maxValue) {
    super(name, sidePanel);
    this.minValue = minValue;
    this.maxValue = maxValue;
  }
}
