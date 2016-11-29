package util.interaction;

import javax.swing.*;
import javax.swing.event.*;
import util.*;
import java.awt.*;

public class ThresholdSliders {
  public static JPanel create(final Double[] t, String[] names){
    JPanel thresholdSliders = new JPanel();
    thresholdSliders.setLayout(new GridLayout(t.length, 1));
    JSlider slider;
    for(int i = 0; i < t.length; i++){
      final int i1 = i;
      JPanel subpanel = new JPanel();
      subpanel.setLayout(new GridLayout(2, 1));
      System.out.println(1 + ", " + (int) (20 * 1000 * t[i]));
      slider = new JSlider(1, (int) (20 * 1000 * t[i]));
      slider.setValue((int) (1000 * t[i]));
      slider.addChangeListener(new ChangeListener() {
        final Double[] t1 = t;
        final int index = i1;
        @Override
        public void stateChanged(ChangeEvent e){
          JSlider source = (JSlider) e.getSource();
          int val = source.getValue();
          t1[index] = val / 1000.0;
          System.out.println(t1[index]);
        }
      });
      slider.setSize(450, 30);
      subpanel.add(new JLabel(names[i]));
      subpanel.add(slider);
      thresholdSliders.add(subpanel, i);
    }
    thresholdSliders.setSize(500, 300);
    thresholdSliders.setMinimumSize(new Dimension(500, 300));
    return thresholdSliders;
  }
  
}