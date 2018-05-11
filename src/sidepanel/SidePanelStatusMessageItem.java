package sidepanel;

import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;

import javax.swing.*;

class SidePanelStatusMessageItem extends AbstractLayoutInterfaceItem<String> {
  private final JTextArea outputTextArea;

  SidePanelStatusMessageItem(String name, JTextArea outputTextArea) {
    super(name);
    this.outputTextArea = outputTextArea;
  }

  @Override
  public void setValue(String value) {
    super.setValue(value);
    outputTextArea.setText(value);
  }
}
