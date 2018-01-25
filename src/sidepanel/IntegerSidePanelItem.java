package sidepanel;

import javax.swing.*;

public class IntegerSidePanelItem extends SidePanelItem<Integer>{
    private final int minValue;
    private final int maxValue;
    private final int threshold;

    IntegerSidePanelItem(String name, JTabbedPane sidePanel, int minValue, int maxValue, int threshold) {
        super(name, sidePanel);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.threshold = threshold;
    }

    public int getMinValue() {
        return  minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public int getThreshold() {
        return threshold;
    }
}
