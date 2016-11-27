package util.interaction;

import javax.swing.*;
import javax.swing.event.*;
import util.*;
import java.awt.*;

public class ThresholdSliders {
  public static JFrame create(Frame owner, final Double[] t){
    JFrame thresholdSliders = new JFrame();
    thresholdSliders.setLayout(new GridLayout(4, 1));
    JSlider[] sliders = new JSlider[4];
    for(int i = 0; i < sliders.length; i++){
      final int i1 = i;
      System.out.println(1 + ", " + (int) (20 * 1000 * t[i]));
      sliders[i] = new JSlider(1, (int) (20 * 1000 * t[i]));
      sliders[i].setValue((int) (1000 * t[i]));
      sliders[i].addChangeListener(new ChangeListener() {
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
      sliders[i].setSize(450, 30);
      thresholdSliders.add(sliders[i], i);
    }
    thresholdSliders.setSize(500, 300);
    return thresholdSliders;
  }
  
}