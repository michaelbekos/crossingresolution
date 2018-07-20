package sidepanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;

final class Components {
  private Components() {}

  static JCheckBox addCheckBox(String name, JPanel sidePanel, GridBagState gridBagState, ItemListener itemListener) {
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = gridBagState.getX();
    gridBagConstraints.gridy = gridBagState.increaseY();
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;

    JCheckBox checkBox = new JCheckBox(name);
    checkBox.addItemListener(itemListener);

    sidePanel.add(checkBox, gridBagConstraints);
    return checkBox;
  }
  static JCheckBox addCheckBox(String name, JPanel sidePanel, GridBagState gridBagState, ItemListener itemListener, Boolean visible) {
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = gridBagState.getX();
    gridBagConstraints.gridy = gridBagState.increaseY();
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;

    JCheckBox checkBox = new JCheckBox(name);
    checkBox.addItemListener(itemListener);
    if (visible) {
      System.out.println(visible);
      sidePanel.add(checkBox, gridBagConstraints);
    }
    return checkBox;
  }

  static DoubleSlider addDoubleSlider(String name, double minValue, double initialMaxValue, JPanel sidePanel, GridBagState gridBagState) {
    GridBagConstraints cLabel = new GridBagConstraints();
    GridBagConstraints cSlider = new GridBagConstraints();
    GridBagConstraints cSliderMax = new GridBagConstraints();
    cSlider.fill = GridBagConstraints.HORIZONTAL;

    JTextField textField = new JTextField(null);
    textField.setColumns(5);
    GridBagConstraints cout = new GridBagConstraints();

    JSlider slider = new JSlider((int) minValue, (int) (initialMaxValue * 1000));

    SpinnerModel model = new SpinnerNumberModel(initialMaxValue, minValue, Double.MAX_VALUE, initialMaxValue / 10);
    JSpinner spinner = new JSpinner(model);
    JComponent editor = new JSpinner.NumberEditor(spinner, "#,##0.###");
    spinner.setEditor(editor);
    ((JSpinner.DefaultEditor) (spinner.getEditor())).getTextField().setColumns(5);
    spinner.setValue(initialMaxValue);


    slider.setSize(400, 30);
    cLabel.gridx = gridBagState.getX() + 1;
    cLabel.gridy = gridBagState.increaseY();

    cout.gridx = gridBagState.getX();
    cout.gridy = gridBagState.increaseY();
    cout.fill = GridBagConstraints.HORIZONTAL;
    cSlider.gridx = gridBagState.getX() + 1;
    cSlider.gridy = gridBagState.getY();
    cSlider.fill = GridBagConstraints.HORIZONTAL;
    cSliderMax.gridy = gridBagState.getY();
    cSliderMax.gridx = gridBagState.getX() + 2;
    cSliderMax.fill = GridBagConstraints.HORIZONTAL;

    sidePanel.add(new JLabel(name), cLabel);
    sidePanel.add(textField, cout);
    sidePanel.add(slider, cSlider);
    sidePanel.add(spinner, cSliderMax);

    return new DoubleSlider(slider, textField, spinner, initialMaxValue);
  }

  static IntSlider addIntSlider(String name, int minValue, int initialMaxValue, JPanel sidePanel, GridBagState gridBagState) {
    GridBagConstraints cLabel = new GridBagConstraints();
    GridBagConstraints cSlider = new GridBagConstraints();
    GridBagConstraints cSliderMax = new GridBagConstraints();
    cSlider.fill = GridBagConstraints.HORIZONTAL;

    JTextField textField = new JTextField(null);
    textField.setColumns(5);
    GridBagConstraints cOut = new GridBagConstraints();

    JSlider slider = new JSlider(minValue, initialMaxValue);

    SpinnerModel model = new SpinnerNumberModel(initialMaxValue, minValue, Integer.MAX_VALUE, initialMaxValue / 10);
    JSpinner spinner = new JSpinner(model);
    JComponent editor = new JSpinner.NumberEditor(spinner, "#,##0");
    spinner.setEditor(editor);
    ((JSpinner.DefaultEditor) (spinner.getEditor())).getTextField().setColumns(5);

    slider.setSize(400, 30);
    cLabel.gridx = gridBagState.getX() + 1;
    cLabel.gridy = gridBagState.increaseY();

    cOut.gridx = gridBagState.getX();
    cOut.gridy = gridBagState.increaseY();
    cOut.fill = GridBagConstraints.HORIZONTAL;
    cSlider.gridx = gridBagState.getX() + 1;
    cSlider.gridy = gridBagState.getY();
    cSlider.fill = GridBagConstraints.HORIZONTAL;
    cSliderMax.gridy = gridBagState.getY();
    cSliderMax.gridx = gridBagState.getX() + 2;
    cSliderMax.fill = GridBagConstraints.HORIZONTAL;
    sidePanel.add(new JLabel(name), cLabel);
    sidePanel.add(textField, cOut);
    sidePanel.add(slider, cSlider);
    sidePanel.add(spinner, cSliderMax);

    return new IntSlider(slider, textField, spinner, initialMaxValue);
  }
}
