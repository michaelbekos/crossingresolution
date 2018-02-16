package sidepanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

class IntegerSidePanelItem extends SidePanelItem<Integer> {
  private int minValue;
  private int initialMaxValue;
  private JTextField textField;
  private JSlider slider;

  IntegerSidePanelItem(String name, int minValue, int initialMaxValue, JPanel sidePanel, GridBagState gridBagState) {
    super(name, sidePanel, gridBagState);
    this.minValue = minValue;
    this.initialMaxValue = initialMaxValue;

    init();
  }

  @Override
  void createComponents(JPanel sidePanel, GridBagState gridBagState) {
    //add slider
    GridBagConstraints cLabel = new GridBagConstraints();
    GridBagConstraints cSlider = new GridBagConstraints();
    GridBagConstraints cSliderMax = new GridBagConstraints();
    cSlider.fill = GridBagConstraints.HORIZONTAL;

    textField = new JTextField(null);
    textField.setColumns(5);
    GridBagConstraints cOut = new GridBagConstraints();

    slider = new JSlider(minValue, initialMaxValue);
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

    SpinnerModel model = new SpinnerNumberModel(initialMaxValue, minValue, Integer.MAX_VALUE, initialMaxValue / 10);
    JSpinner spinner = new JSpinner(model);
    JComponent editor = new JSpinner.NumberEditor(spinner, "#,##0");
    spinner.setEditor(editor);
    ((JSpinner.DefaultEditor) (spinner.getEditor())).getTextField().setColumns(5);
    spinner.addChangeListener(e -> {
      JSpinner source = (JSpinner) e.getSource();
      int val = (int)source.getValue();
      slider.setMaximum(val);
      this.initialMaxValue = val;
    });

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
    sidePanel.add(new JLabel(getName()), cLabel);
    sidePanel.add(textField, cOut);
    sidePanel.add(slider, cSlider);
    sidePanel.add(spinner, cSliderMax);
  }

  @Override
  public void setValue(Integer value) {
    super.setValue(value);
    textField.setText(Integer.toString(value));
    slider.setValue(value);
  }

  @Override
  public void addListener(Object listener) {
    if (listener instanceof ChangeListener) {
      slider.addChangeListener((ChangeListener) listener);
    }
  }
}
