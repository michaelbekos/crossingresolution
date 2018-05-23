package layout.algo.forcealgorithm.forces;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.view.GraphComponent;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SlopedForce implements IForce {
  private AbstractLayoutInterfaceItem<Double> weight;
  private AbstractLayoutInterfaceItem<Boolean> activated;
  private IGraph graph;
  private AbstractLayoutInterfaceItem<List<Double>> slopes;
  ArrayList<AbstractLayoutInterfaceItem> itemList;

  public SlopedForce(IGraph graph) {
    this.graph = graph;
  }

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory, Collection<AbstractLayoutInterfaceItem<Boolean>> toggleableParameters) {
    itemList = new ArrayList<>();

    weight = itemFactory.doubleParameter("Slope Force", 0.0, 0.5);
    weight.setValue(0.05);
    itemList.add(weight);

    activated = itemFactory.toggleableParameter(weight);
    activated.setValue(true);
    itemList.add(activated);
    toggleableParameters.add(activated);

    slopes = itemFactory.slopesParameter("Slope Force");
    itemList.add(slopes);
  }

  @Override
  public ArrayList<AbstractLayoutInterfaceItem> getItems(){
    return itemList;
  }

  @Override
  public Mapper<INode, PointD> calculate(Mapper<INode, PointD> forces, Mapper<INode, PointD> nodePositions) {
    if (!activated.getValue()) {
      return forces;
    }

    List<Double> slopeAngles = slopes.getValue().stream()
        .map(angle -> {
          double x = 10 * Math.cos(angle);
          double y = 10 * Math.sin(angle);

          double slopeAngle = Math.atan2(y, x);
          if (slopeAngle < 0) {
            slopeAngle += Math.PI;
          }

          return slopeAngle;
        })
        .collect(Collectors.toList());

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
}
