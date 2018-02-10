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

public class SidePanelItemFactory implements ILayoutInterfaceItemFactory {
  private JPanel sidePanel;
  private GraphComponent view;
  private GraphEditorInputMode graphEditorInputMode;
  private GridBagState gridBagState;

  SidePanelItemFactory(JPanel sidePanel, GraphComponent view, GraphEditorInputMode graphEditorInputMode, GridBagState gridBagState) {
    this.sidePanel = sidePanel;
    this.view = view;
    this.graphEditorInputMode = graphEditorInputMode;
    this.gridBagState = gridBagState;
  }

  @Override
  public AbstractLayoutInterfaceItem<Double> doubleParameter(String name, double minValue, double maxValue, double threshold, boolean enableCheckbox) {
    return new DoubleSidePanelItem(name, minValue, maxValue, threshold, sidePanel, gridBagState, enableCheckbox);
  }

  @Override
  public AbstractLayoutInterfaceItem<Integer> intParameter(String name, int minValue, int maxValue, int threshold) {
    return new IntegerSidePanelItem(name, minValue, maxValue, threshold, sidePanel, gridBagState);
  }

  @Override
  public AbstractLayoutInterfaceItem<Boolean> booleanParameter(String name) {
    return new BoolSidePanelItem(name, sidePanel, gridBagState);
  }

  @Override
  public AbstractLayoutInterfaceItem<IGraphSelection> selection(String name) {
    return new SelectionItem(name, graphEditorInputMode);
  }

  @Override
  public AbstractLayoutInterfaceItem<Mapper<INode, PointD>> debugVectors(String name) {
    return new DebugVectorsItem(name, view);
  }
}
