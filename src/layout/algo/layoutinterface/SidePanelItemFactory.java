package layout.algo.layoutinterface;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.view.GraphComponent;

import javax.swing.*;

public class SidePanelItemFactory implements ILayoutInterfaceItemFactory {
  private JPanel sidePanel;
  private GraphComponent view;

  public SidePanelItemFactory(JPanel sidePanel, GraphComponent view) {
    this.sidePanel = sidePanel;
    this.view = view;
  }

  @Override
  public AbstractLayoutInterfaceItem<Double> doubleParameter(String name, double minValue, double maxValue) {
    return new DoubleSidePanelItem(name, sidePanel, minValue, maxValue);
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
