package layout.algo.forcealgorithm.forces;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.ICanvasObject;
import com.yworks.yfiles.view.ICanvasObjectDescriptor;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;
import view.visual.VectorVisual;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SlopedForce implements IForce {
  private AbstractLayoutInterfaceItem<Double> weight;
  private AbstractLayoutInterfaceItem<Boolean> activated;
  private AbstractLayoutInterfaceItem<Integer> numberOfSlopes;
  private AbstractLayoutInterfaceItem<Double> initialAngleDeg;
  private AbstractLayoutInterfaceItem<Boolean> showSlopesEnabled;
  private IGraph graph;
  private GraphComponent view;

  public SlopedForce(IGraph graph, GraphComponent view) {
    this.graph = graph;
    this.view = view;
  }

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory, Collection<AbstractLayoutInterfaceItem<Boolean>> toggleableParameters) {
    weight = itemFactory.doubleParameter("Slope Force", 0.0, 0.5);
    weight.setValue(0.05);

    activated = itemFactory.toggleableParameter(weight);
    activated.setValue(true);
    toggleableParameters.add(activated);

    numberOfSlopes = itemFactory.intParameter("Slope Force: Num. Slopes",0,360);
    numberOfSlopes.setValue(1);
    numberOfSlopes.addListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent changeEvent) {
        if (showSlopesEnabled.getValue()) {
          drawSlopes(numberOfSlopes.getValue(), initialAngleDeg.getValue());
        }
      }
    });
    initialAngleDeg = itemFactory.doubleParameter("Slope Force: Initial Angle", 0, 360);
    initialAngleDeg.setValue(0.0);
    initialAngleDeg.addListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent changeEvent) {
        if (showSlopesEnabled.getValue()) {
          drawSlopes(numberOfSlopes.getValue(), initialAngleDeg.getValue());
        }
      }
    });
    showSlopesEnabled = itemFactory.booleanParameter("Slope Force: Show Slopes");
    showSlopesEnabled.setValue(false);
    showSlopesEnabled.addListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent itemEvent) {
        if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
          //draw
          drawSlopes(numberOfSlopes.getValue(),initialAngleDeg.getValue());
        } else {
          //clear
          clearSlopes();
        }
      }
    });
  }

  @Override
  public Mapper<INode, PointD> calculate(Mapper<INode, PointD> forces, Mapper<INode, PointD> nodePositions) {
    if (!activated.getValue()) {
      return forces;
    }
    List<Double> slopeAngles = new ArrayList<>();
    double stepSize = (2 * Math.PI)/numberOfSlopes.getValue();
    double pos = 2 * Math.PI*(initialAngleDeg.getValue()/360.0);
    for (int i = 0; i < numberOfSlopes.getValue(); i++) {
      double x = 10 * Math.cos(pos);
      double y = 10 * Math.sin(pos);

      double slopeAngle = Math.atan2(y, x);
      if (slopeAngle < 0) {
        slopeAngle += Math.PI;
      }
      slopeAngles.add(slopeAngle);
      pos += stepSize;
      if (pos > 2 * Math.PI) {
        pos -= 2 * Math.PI;
      }
    }


    for (IEdge e : graph.getEdges()) {
      double edgeSlopeAngle = calculateSlopeAngle(e);
      double fittedSlopeAngle = slopeAngles.get(0);
      if (edgeSlopeAngle < 0) {
        edgeSlopeAngle += Math.PI;
      }
      if (fittedSlopeAngle < 0) {
        fittedSlopeAngle += Math.PI;
      }
      for (double s : slopeAngles) {
        if (Math.abs(s - edgeSlopeAngle) < Math.abs(fittedSlopeAngle - edgeSlopeAngle)) {
          fittedSlopeAngle = s;
        }
      }


      INode sourceNode = e.getSourceNode();
      double u1_x = sourceNode.getLayout().getCenter().x;
      double u1_y = sourceNode.getLayout().getCenter().y;

      INode targetNode = e.getTargetNode();
      double u2_x = targetNode.getLayout().getCenter().x;
      double u2_y = targetNode.getLayout().getCenter().y;

      double dx = u2_x - u1_x;
      double dy = u2_y - u1_y;


      if (edgeSlopeAngle < 0) {
        edgeSlopeAngle += Math.PI;
      }
      if (fittedSlopeAngle < 0) {
        fittedSlopeAngle += Math.PI;
      }

      int sign;
      if ((edgeSlopeAngle - fittedSlopeAngle) < 0) {
        sign = -1;
      } else {
        sign = 1;
      }

      PointD sourceVector = new PointD(
              (-sign * dy),
              (sign * dx)
      );
      PointD targetVector = new PointD(
              (sign * dy),
              (-sign * dx)
      );


      sourceVector = sourceVector.getNormalized();
      sourceVector = PointD.times(weight.getValue() * Math.abs(fittedSlopeAngle - edgeSlopeAngle), sourceVector);
      forces.setValue(sourceNode, PointD.add(forces.getValue(sourceNode), sourceVector));

      targetVector = targetVector.getNormalized();
      targetVector = PointD.times(weight.getValue() * Math.abs(fittedSlopeAngle - edgeSlopeAngle), targetVector);
      forces.setValue(targetNode, PointD.add(forces.getValue(targetNode), targetVector));
    }

    return forces;
  }

  private static double calculateSlopeAngle(IEdge e) {
    INode u1 = e.getSourceNode();
    double u1_x = u1.getLayout().getCenter().getX();
    double u1_y = u1.getLayout().getCenter().getY();

    INode u2 = e.getTargetNode();
    double u2_x = u2.getLayout().getCenter().getX();
    double u2_y = u2.getLayout().getCenter().getY();

    double dx = u2_x - u1_x;
    double dy = u2_y - u1_y;

    return Math.atan2(dy,dx);
  }


  //helper function
  private List<ICanvasObject> canvasObjects = new ArrayList<>();
  private void clearSlopes() {
    for (ICanvasObject o : canvasObjects) {
      o.remove();
    }
    canvasObjects.clear();
  }
  private void drawSlopes(double numSlopes, double initAngleDeg) {
    clearSlopes();
    numSlopes *=2;
    double stepSize = (2* Math.PI)/numSlopes;
    double pos = 2 * Math.PI*(initAngleDeg/360);
    INode tmpNode = graph.createNode(view.getCenter());
    double x_val;
    double y_val;
    double zoom = 1/view.getZoom() * 3;
    for (int i = 0; i < numSlopes; i++) {
      x_val = zoom * Math.cos(pos);
      y_val = zoom * Math.sin(pos);
      canvasObjects.add(view.getBackgroundGroup().addChild(new VectorVisual(view, new PointD(x_val,y_val), tmpNode, Color.GREEN,(int)(5/view.getZoom())), ICanvasObjectDescriptor.VISUAL));
      pos += stepSize;
      if (pos > 2 * Math.PI) {
        pos -= 2 * Math.PI;
      }
    }
    graph.remove(tmpNode);
  }


}
