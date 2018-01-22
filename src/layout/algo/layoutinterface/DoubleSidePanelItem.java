package layout.algo.layoutinterface;

import javax.swing.*;

public class DoubleSidePanelItem extends SidePanelItem<Double> {
  private final double minValue;
  private final double maxValue;
  private final double threshold;

  DoubleSidePanelItem(String name, JTabbedPane sidePanel, double minValue, double maxValue, double threshold) {
    super(name, sidePanel);
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.threshold = threshold;
  }

  public double getMinValue() {
    return  minValue;
  }

  public double getMaxValue() {
    return maxValue;
  }

  public double getThreshold() {
    return threshold;
  }
}
