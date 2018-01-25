package sidepanel;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.view.GraphComponent;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import javax.swing.*;

public class SidePanelItemFactory implements ILayoutInterfaceItemFactory {
  private JTabbedPane sidePanel;
  private GraphComponent view;

  public SidePanelItemFactory(JTabbedPane sidePanel, GraphComponent view) {
    this.sidePanel = sidePanel;
    this.view = view;
  }

  @Override
  public AbstractLayoutInterfaceItem<Double> doubleParameter(String name, double minValue, double maxValue, double threshold) {
    return new DoubleSidePanelItem(name, sidePanel, minValue, maxValue, threshold);
  }

  @Override
  public AbstractLayoutInterfaceItem<Integer> intParameter(String name, int minValue, int maxValue, int threshold) {
    return new IntegerSidePanelItem(name, sidePanel, minValue, maxValue, threshold);
  }

  @Override
  public AbstractLayoutInterfaceItem<Boolean> booleanParameter(String name) {
    return new BoolSidePanelItem(name, sidePanel);
  }

  @Override
  public AbstractLayoutInterfaceItem<Mapper<INode, PointD>> debugVectors(String name) {
    return new DebugVectorsItem(name, view);
  }
}
