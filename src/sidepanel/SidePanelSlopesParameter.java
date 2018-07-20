package sidepanel;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.ICanvasObject;
import com.yworks.yfiles.view.ICanvasObjectDescriptor;
import view.visual.VectorVisual;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

class SidePanelSlopesParameter extends SidePanelItem<List<Double>> {
  private JCheckBox showSlopes;
  private List<ICanvasObject> canvasObjects = new ArrayList<>();
  private IGraph graph;
  private GraphComponent view;

  private int numSlopes = 1;
  private double angle;

  SidePanelSlopesParameter(String name, JPanel sidePanel, GridBagState gridBagState, GraphComponent view) {
    super(name, sidePanel, gridBagState);
    this.view = view;
    this.graph = view.getGraph();
    init();
  }

  @Override
  void createComponents(JPanel sidePanel, GridBagState gridBagState) {
    IntSlider numSlopesSlider = Components.addIntSlider(getName() + ": Num. Slopes", 1, 360, sidePanel, gridBagState, true);
    numSlopesSlider.setValue(numSlopes);
    numSlopesSlider.setValueListener(value -> {
      numSlopes = value;
      drawSlopes();
    });

    DoubleSlider angleSlider = Components.addDoubleSlider(getName() + ": Initial Angle", 0, 360, sidePanel, gridBagState);
    angleSlider.setValue(angle);
    angleSlider.setValueListener(value -> {
      angle = value;
      drawSlopes();
    });

    showSlopes = Components.addCheckBox(getName() + ": Show Slopes", sidePanel, gridBagState, e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        drawSlopes();
      } else {
        clearSlopes();
      }
    }, true);
    showSlopes.setSelected(false);
  }

  @Override
  public List<Double> getValue() {
    return calculateAngles(numSlopes);
  }

  private List<Double> calculateAngles(int numSlopes) {
    ArrayList<Double> angles = new ArrayList<>(numSlopes);
    double stepSize = (2 * Math.PI) / numSlopes;
    double pos = 2 * Math.PI * (angle / 360);

    for (int i = 0; i < numSlopes; i++) {
      angles.add(pos);
      pos += stepSize;
      if (pos > 2 * Math.PI) {
        pos -= 2 * Math.PI;
      }
    }

    return angles;
  }

  private void clearSlopes() {
    for (ICanvasObject o : canvasObjects) {
      o.remove();
    }
    canvasObjects.clear();
  }

  private void drawSlopes() {
    if (!showSlopes.isSelected()) {
      return;
    }

    clearSlopes();

    double zoom = 1 / view.getZoom() * 3;
    INode tmpNode = graph.createNode(view.getCenter());

    List<Double> angles = calculateAngles(numSlopes * 2);

    for (Double angle : angles) {
      double x = zoom * Math.cos(angle);
      double y = zoom * Math.sin(angle);
      VectorVisual visual = new VectorVisual(view, new PointD(x, y), tmpNode, Color.GREEN, (int) (5 / view.getZoom()));
      canvasObjects.add(view.getBackgroundGroup().addChild(visual, ICanvasObjectDescriptor.VISUAL));
    }

    graph.remove(tmpNode);
  }
}
