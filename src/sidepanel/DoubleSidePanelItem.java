package sidepanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class DoubleSidePanelItem extends SidePanelItem<Double> {
  private double minValue;
  private double initialMaxValue;

  private JTextField textField;
  private JSlider slider;

  DoubleSidePanelItem(String name, double minValue, double initialMaxValue, JPanel sidePanel, GridBagState gridBagState) {
    super(name, sidePanel, gridBagState);
    this.minValue = minValue;
    this.initialMaxValue = initialMaxValue;
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

    slider = new JSlider((int) minValue, (int) (initialMaxValue * 1000));
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

    SpinnerModel model = new SpinnerNumberModel(initialMaxValue, minValue, Double.MAX_VALUE, initialMaxValue / 10);
    JSpinner spinner = new JSpinner(model);
    JComponent editor = new JSpinner.NumberEditor(spinner, "#,##0.###");
    spinner.setEditor(editor);
    ((JSpinner.DefaultEditor) (spinner.getEditor())).getTextField().setColumns(5);
    spinner.setValue(this.initialMaxValue);
    spinner.addChangeListener(e -> {
        JSpinner source = (JSpinner) e.getSource();
        Double val = (Double) source.getValue();
        int maxValSlider = (int) (val * 1000);
        slider.setMaximum(maxValSlider);
        this.initialMaxValue = val;
    });


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

    sidePanel.add(new JLabel(getName()), cLabel);
    sidePanel.add(textField, cout);
    sidePanel.add(slider, cSlider);
    sidePanel.add(spinner, cSliderMax);
  }

  @Override
  public void setValue(Double value) {
    value = Math.min(Math.max(minValue, value), initialMaxValue);
    super.setValue(value);
    textField.setText(Double.toString(value));
    slider.setValue((int) (1000 * value));
  }

  @Override
  public void addListener(Object listener) {
    if (listener instanceof ChangeListener) {
      slider.addChangeListener((ChangeListener) listener);
    }
  }
}
