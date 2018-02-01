package sidepanel;

import javax.swing.*;
import java.awt.*;

class IntegerSidePanelItem extends SidePanelItem<Integer> {
  private final int minValue;
  private final int maxValue;
  private final int threshold;
  private JTextField textField;
  private JSlider slider;

  IntegerSidePanelItem(String name, int minValue, int maxValue, int threshold, JPanel sidePanel, GridBagState gridBagState) {
    super(name, sidePanel, gridBagState);
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.threshold = threshold;

    init();
  }

  @Override
  void createComponents(JPanel sidePanel, GridBagState gridBagState) {
    //maybe textfield vs slider for ints?
    //add slider
    GridBagConstraints cLabel = new GridBagConstraints();
    GridBagConstraints cSlider = new GridBagConstraints();
    GridBagConstraints cSliderMax = new GridBagConstraints();
    cSlider.fill = GridBagConstraints.HORIZONTAL;

    textField = new JTextField(null);
    textField.setColumns(5);
    GridBagConstraints cout = new GridBagConstraints();

    slider = new JSlider(minValue, maxValue);
    slider.addChangeListener(e -> {
      JSlider source = (JSlider) e.getSource();
      int val = source.getValue();
      setValue(val);
    });

    textField.addActionListener(e -> {
      try {
        if (textField.getText().matches("\\d+") && Integer.parseInt(textField.getText()) > slider.getMinimum() && Integer.parseInt(textField.getText()) <= slider.getMaximum()) { //checks is double
          setValue(Integer.parseInt(textField.getText()));
        }
      } catch (NumberFormatException nfe) {
        System.out.println("Invalid Input");
      }
    });

    double max = maxValue;
    SpinnerModel model = new SpinnerNumberModel(max * threshold, threshold / 10.0, 1000 * max * threshold, threshold);
    JSpinner spinner = new JSpinner(model);
    JComponent editor = new JSpinner.NumberEditor(spinner, "#,##0");
    spinner.setEditor(editor);
    ((JSpinner.DefaultEditor) (spinner.getEditor())).getTextField().setColumns(5);
    spinner.addChangeListener(e -> {
      JSpinner source = (JSpinner) e.getSource();
      slider.setMaximum((int) (double) source.getValue());
    });

    slider.setSize(400, 30);
    cLabel.gridx = 1;
    cLabel.gridy = gridBagState.increaseY();

    cout.gridx = 0;
    cout.gridy = gridBagState.increaseY();
    cout.fill = GridBagConstraints.HORIZONTAL;
    cSlider.gridx = 1;
    cSlider.gridy = gridBagState.getY();
//                    cSlider.gridwidth = 7;
    cSlider.fill = GridBagConstraints.HORIZONTAL;
    cSliderMax.gridy = gridBagState.getY();
    cSliderMax.gridx = 2;
//                    cSlider.gridwidth =  1;
//                    cSliderMax.weightx = 0.2;
    cSliderMax.fill = GridBagConstraints.HORIZONTAL;
    sidePanel.add(new JLabel(getName()), cLabel);
    sidePanel.add(textField, cout);
    sidePanel.add(slider, cSlider);
    sidePanel.add(spinner, cSliderMax);
  }

  @Override
  public void setValue(Integer value) {
    super.setValue(value);
    textField.setText(Integer.toString(value));
    slider.setValue(value);
  }
}
