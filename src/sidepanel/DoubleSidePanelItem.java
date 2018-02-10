package sidepanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;

class DoubleSidePanelItem extends SidePanelItem<Double> {
  private double minValue;
  private double maxValue;
  private final double threshold;
  private final boolean enableCheckbox;

  private JTextField textField;
  private JSlider slider;
  private double currentValue;

  DoubleSidePanelItem(String name, double minValue, double maxValue, double threshold, JPanel sidePanel, GridBagState gridBagState, boolean enableCheckbox) {
    super(name, sidePanel, gridBagState);
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.threshold = threshold;
    this.enableCheckbox = enableCheckbox;

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
      this.maxValue = val;
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

    if (enableCheckbox) {
      JCheckBox checkBox = new JCheckBox();
      GridBagConstraints cCheckBox = new GridBagConstraints();
      cCheckBox.fill = GridBagConstraints.HORIZONTAL;
      cCheckBox.gridx = 0;
      cCheckBox.gridy = gridBagState.getY();
      sidePanel.add(checkBox, cCheckBox);
      checkBox.addItemListener(evt -> {
        if (evt.getStateChange() == ItemEvent.DESELECTED) {
          currentValue = Double.parseDouble(textField.getText());
          setValue(0.0);
        } else {
          setValue(currentValue);
        }
      });
      checkBox.setSelected(true);

      JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
      separator.setPreferredSize(new Dimension(5,1));
      GridBagConstraints cgc = new GridBagConstraints();
      cgc.gridx = 1;
      cgc.gridheight = gridBagState.getY() + 1;
      cgc.fill = GridBagConstraints.VERTICAL;
      sidePanel.add(separator, cgc);
    }

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

  @Override
  public void addListener(Object listener) {
    if (listener instanceof ChangeListener) {
      slider.addChangeListener((ChangeListener) listener);
    }
  }
}
