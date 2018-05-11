package sidepanel;

import javax.swing.*;
import java.util.function.Consumer;

class DoubleSlider {
  private final JSlider slider;
  private final JTextField textField;
  private double currentMaxValue;
  private Consumer<Double> valueListener;

  DoubleSlider(JSlider slider, JTextField textField, JSpinner spinner, double initialMaxValue) {
    this.slider = slider;
    this.textField = textField;
    this.currentMaxValue = initialMaxValue;

    slider.addChangeListener(e -> {
      JSlider source = (JSlider) e.getSource();
      double value = source.getValue() / 1000.0;
      setValue(value);
      fire(value);
    });

    textField.addActionListener(e -> {
      try {
        double value = Double.parseDouble(textField.getText());
        if (value > slider.getMinimum() && value <= slider.getMaximum()) {
          setValue(value);
          fire(value);
        }
      } catch (NumberFormatException nfe) {
        System.out.println("Invalid Input");
      }
    });

    spinner.addChangeListener(e -> {
      JSpinner source = (JSpinner) e.getSource();
      double value = (Double) source.getValue();
      int maxValSlider = (int) (value * 1000);
      slider.setMaximum(maxValSlider);
      currentMaxValue = value;
    });
  }

  private void fire(double value) {
    if (valueListener != null) {
      valueListener.accept(value);
    }
  }

  void setValueListener(Consumer<Double> valueListener) {
    this.valueListener = valueListener;
  }

  public void setValue(double value) {
    textField.setText(Double.toString(value));
    slider.setValue((int) (1000 * value));
  }

  public double getCurrentMaxValue() {
    return currentMaxValue;
  }
}
