package util.interaction;

import javax.swing.*;
import javax.swing.event.*;
import util.*;
import java.awt.*;

public class ThresholdSliders {
  public static Tuple4<JPanel, JSlider[], JSpinner[], Integer> create(final Double[] t, String[] names){
    JPanel thresholdSliders = new JPanel();
    thresholdSliders.setLayout(new GridBagLayout());
    GridBagConstraints cLabel = new GridBagConstraints();
    GridBagConstraints cSlider = new GridBagConstraints();
    GridBagConstraints cSliderMax = new GridBagConstraints();
    cSlider.fill = GridBagConstraints.HORIZONTAL;
    //cSliderMax.gridwidth = GridBagConstraints.REMAINDER;
    JSlider[] sliders = new JSlider[t.length];
    JSpinner[] spinners = new JSpinner[t.length];
    for(int i = 0; i < t.length; i++){
      final int i1 = i;
      double initVal = t[i];
      
      JSlider slider = new JSlider(0, (int) (20 * 1000 * t[i]));
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

      SpinnerModel model = new SpinnerNumberModel(20 * initVal, initVal / 10.0, 1000 * 20 * initVal, initVal / 10.0);
      JSpinner spinner = new JSpinner(model);
      JComponent editor = new JSpinner.NumberEditor(spinner, "#,##0.###");
      spinner.setEditor(editor);
      spinner.addChangeListener(new ChangeListener() {
        final JSlider s = slider;
        public void stateChanged(ChangeEvent e){
          JSpinner source = (JSpinner) e.getSource();
          Double val = (Double) source.getValue();
          int maxValSlider = (int) (val * 1000);
          s.setMaximum(maxValSlider);
        }
      });

      slider.setSize(400, 30);
      cLabel.gridy = 2*i;
      cSlider.gridy = 2*i + 1;
      cSlider.gridx = 0;
      cSliderMax.gridy = 2*i + 1;
      cSliderMax.gridx = 1;
      thresholdSliders.add(new JLabel(names[i]), cLabel);
      thresholdSliders.add(slider, cSlider);
      thresholdSliders.add(spinner, cSliderMax);
      sliders[i] = slider;
      spinners[i] = spinner;
    }
    thresholdSliders.setSize(500, 300);
    thresholdSliders.setMinimumSize(new Dimension(500, 300));
    return new Tuple4<>(thresholdSliders, sliders, spinners, 2 * t.length);
  }
  
}