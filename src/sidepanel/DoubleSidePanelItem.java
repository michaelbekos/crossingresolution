package sidepanel;

import javax.swing.*;
import java.awt.*;

class DoubleSidePanelItem extends SidePanelItem<Double> {
  private final double minValue;
  private final double maxValue;
  private final double threshold;

  private JTextField textField;
  private JSlider slider;

  DoubleSidePanelItem(String name, double minValue, double maxValue, double threshold, JPanel sidePanel, GridBagState gridBagState) {
    super(name, sidePanel, gridBagState);
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.threshold = threshold;

    init();
  }


  @Override
  void createComponents(JPanel sidePanel, GridBagState gridBagState) {
    GridBagConstraints cLabel = new GridBagConstraints();
    GridBagConstraints cSlider = new GridBagConstraints();
    GridBagConstraints cSliderMax = new GridBagConstraints();
    cSlider.fill = GridBagConstraints.HORIZONTAL;

    textField = new JTextField(null);
    textField.setColumns(5);
    GridBagConstraints cout = new GridBagConstraints();

    double max = maxValue;
    slider = new JSlider((int) minValue, (int) (max * 1000 * threshold));
    slider.addChangeListener(e -> {
      JSlider source = (JSlider) e.getSource();
      double val = source.getValue() / 1000.0;
      setValue(val);
    });

    textField.addActionListener(e -> {
      try {
        if (Double.parseDouble(textField.getText()) > slider.getMinimum() && Double.parseDouble(textField.getText()) <= slider.getMaximum()) {
          setValue(Double.parseDouble(textField.getText()));
        }
      } catch (NumberFormatException nfe) {
        System.out.println("Invalid Input");
      }
    });


    SpinnerModel model = new SpinnerNumberModel(max * threshold, threshold / 10.0, 1000 * max * threshold, threshold / 10.0);
    JSpinner spinner = new JSpinner(model);
    JComponent editor = new JSpinner.NumberEditor(spinner, "#,##0.###");
    spinner.setEditor(editor);
    ((JSpinner.DefaultEditor) (spinner.getEditor())).getTextField().setColumns(5);
    spinner.addChangeListener(e -> {
      JSpinner source = (JSpinner) e.getSource();
      Double val = (Double) source.getValue();
      int maxValSlider = (int) (val * 1000);
      slider.setMaximum(maxValSlider);
    });


    slider.setSize(400, 30);
    cLabel.gridx = 1;
    cLabel.gridy = gridBagState.increaseY();

    cout.gridx = 0;
    cout.gridy = gridBagState.increaseY();
    cout.fill = GridBagConstraints.HORIZONTAL;
    cSlider.gridx = 1;
    cSlider.gridy = gridBagState.getY();
    cSlider.fill = GridBagConstraints.HORIZONTAL;
    cSliderMax.gridy = gridBagState.getY();
    cSliderMax.gridx = 2;
    cSliderMax.fill = GridBagConstraints.HORIZONTAL;
    sidePanel.add(new JLabel(getName()), cLabel);
    sidePanel.add(textField, cout);
    sidePanel.add(slider, cSlider);
    sidePanel.add(spinner, cSliderMax);
  }

  @Override
  public void setValue(Double value) {
    value = Math.min(Math.max(minValue, value), maxValue);
    super.setValue(value);
    textField.setText(Double.toString(value));
    slider.setValue((int) (1000 * value));
  }
}
