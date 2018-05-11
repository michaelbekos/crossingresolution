package sidepanel;

import javax.swing.*;
import java.util.function.Consumer;

class IntSlider {
  private final JSlider slider;
  private final JTextField textField;
  private int currentMaxValue;
  private Consumer<Integer> valueListener;

  IntSlider(JSlider slider, JTextField textField, JSpinner spinner, int initialMaxValue) {
    this.slider = slider;
    this.textField = textField;
    this.currentMaxValue = initialMaxValue;

    slider.addChangeListener(e -> {
      JSlider source = (JSlider) e.getSource();
      int value = source.getValue();
      setValue(value);
      fire(value);
    });

    textField.addActionListener(e -> {
      try {
        if (textField.getText().matches("\\d+")) {
          int value = Integer.parseInt(textField.getText());
          if (value > slider.getMinimum() && value <= slider.getMaximum()) {
            setValue(value);
          }
        }
      } catch (NumberFormatException nfe) {
        System.out.println("Invalid Input");
      }
    });

    spinner.addChangeListener(e -> {
      JSpinner source = (JSpinner) e.getSource();
      int value = (int) source.getValue();
      slider.setMaximum(value);
      currentMaxValue = value;
    });
  }

  private void fire(int value) {
    if (valueListener != null) {
      valueListener.accept(value);
    }
  }

  void setValueListener(Consumer<Integer> valueListener) {
    this.valueListener = valueListener;
  }

  public void setValue(int value) {
    textField.setText(Integer.toString(value));
    slider.setValue(value);
  }

  public int getCurrentMaxValue() {
    return currentMaxValue;
  }
}
