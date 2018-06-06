package sidepanel;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.IGraphSelection;
import com.yworks.yfiles.view.input.GraphEditorInputMode;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public class SidePanelItemFactory implements ILayoutInterfaceItemFactory {
  private JPanel sidePanel;
  private GraphComponent view;
  private GraphEditorInputMode graphEditorInputMode;
  private JTextArea outputTextArea;
  private GridBagState gridBagState;

  SidePanelItemFactory(JPanel sidePanel, GraphComponent view, GraphEditorInputMode graphEditorInputMode, JTextArea outputTextArea, GridBagState gridBagState) {
    this.sidePanel = sidePanel;
    this.view = view;
    this.graphEditorInputMode = graphEditorInputMode;
    this.outputTextArea = outputTextArea;
    this.gridBagState = gridBagState;
  }

  @Override
  public AbstractLayoutInterfaceItem<Double> doubleParameter(String name, double minValue, double maxValue) {
    return new DoubleSidePanelItem(name, minValue, maxValue, sidePanel, gridBagState);
  }

  @Override
  public AbstractLayoutInterfaceItem<Integer> intParameter(String name, int minValue, int maxValue) {
    return new IntegerSidePanelItem(name, minValue, maxValue, sidePanel, gridBagState);
  }

  @Override
  public AbstractLayoutInterfaceItem<Boolean> booleanParameter(String name) {
    return new BoolSidePanelItem(name, sidePanel, gridBagState);
  }

  @Override
  public AbstractLayoutInterfaceItem<Boolean> toggleableParameter(AbstractLayoutInterfaceItem<?> parameter) {
    return new SidePanelToggleableItem(parameter, sidePanel, gridBagState);
  }

  @Override
  public AbstractLayoutInterfaceItem<Boolean> masterToggle(String name, Collection<AbstractLayoutInterfaceItem<Boolean>> parameters) {
    return new MasterToggleSidePanelItem(name, parameters, sidePanel, gridBagState);
  }

  @Override
  public AbstractLayoutInterfaceItem<IGraphSelection> selection(String name) {
    return new SelectionItem(name, graphEditorInputMode);
  }

  @Override
  public AbstractLayoutInterfaceItem<Mapper<INode, PointD>> debugVectors(String name) {
    return new DebugVectorsItem(name, view);
  }

  @Override
  public AbstractLayoutInterfaceItem<String> statusMessage(String name) {
    return new SidePanelStatusMessageItem(name, outputTextArea);
  }

  @Override
  public AbstractLayoutInterfaceItem<List<Double>> slopesParameter(String name) {
    return new SidePanelSlopesParameter(name, sidePanel, gridBagState, view);
  }
}
