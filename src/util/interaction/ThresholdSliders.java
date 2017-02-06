package util.interaction;

import javax.swing.*;
import javax.swing.event.*;
import util.*;
import java.awt.*;

public class ThresholdSliders {
  public static Tuple3<JPanel, JSpinner[], Integer> create(final Double[] t, String[] names){
    JPanel thresholdSliders = new JPanel();
    thresholdSliders.setLayout(new GridBagLayout());
    GridBagConstraints cSlider = new GridBagConstraints();
    GridBagConstraints cLabel = new GridBagConstraints();
    cSlider.fill = GridBagConstraints.HORIZONTAL;
    cSlider.gridwidth = GridBagConstraints.REMAINDER;
    //JSlider[] sliders = new JSlider[t.length];
    JSpinner[] sliders = new JSpinner[t.length];
    //JSlider slider;
    JSpinner spinner;
    for(int i = 0; i < t.length; i++){
      final int i1 = i;
      double initVal = t[i];
      SpinnerModel model = new SpinnerNumberModel(initVal, 0.001, 1, 0.001);
      spinner = new JSpinner(model);
      JComponent editor = new JSpinner.NumberEditor(spinner, "#,##0.###");
      spinner.setEditor(editor);
      spinner.addChangeListener(new ChangeListener() {
        final Double[] t1 = t;
        final int index = i1;
        public void stateChanged(ChangeEvent e){
          JSpinner source = (JSpinner) e.getSource();
          Double val = (Double) source.getValue();
          t1[index] = val;
        }
      });
      /*slider = new JSlider(0, (int) (20 * 1000 * t[i]));
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
      slider.setSize(450, 30);*/
      spinner.setSize(450, 30);
      cLabel.gridy = 2*i;
      cSlider.gridy = 2*i + 1;
      thresholdSliders.add(new JLabel(names[i]), cLabel);
      //thresholdSliders.add(slider, cSlider);
      thresholdSliders.add(spinner, cSlider);
      //sliders[i] = slider;
      sliders[i] = spinner;
    }
    thresholdSliders.setSize(500, 300);
    thresholdSliders.setMinimumSize(new Dimension(500, 300));
    return new Tuple3<>(thresholdSliders, sliders, 2 * t.length);
  }
  
}