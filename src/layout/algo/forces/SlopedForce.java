package layout.algo.forces;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

import java.util.ArrayList;
import java.util.List;

public class SlopedForce implements IForce {
  private double threshold;
  private int numberOfSlopes;
  private double initialAngleDeg;
  private IGraph graph;

  public SlopedForce(IGraph graph, int numberOfSlopes, double initialAngleDeg, double threshold) {
    this.threshold = threshold;
    this.numberOfSlopes = numberOfSlopes;
    this.initialAngleDeg = initialAngleDeg;
    this.graph = graph;
  }

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {

  }

  @Override
  public Mapper<INode, PointD> calculate(Mapper<INode, PointD> forces, Mapper<INode, PointD> nodePositions) {
    List<Double> slopeAngles = new ArrayList<>();
    double stepSize = (2 * Math.PI)/numberOfSlopes;
    double pos = 2 * Math.PI*(initialAngleDeg/360.0);
    for (int i = 0; i < numberOfSlopes; i++) {
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
          u1_x - (sign * dy + u1_x),
          u1_y + (sign * dx + u1_y)
      );
      PointD targetVector = new PointD(
          u2_x + (sign * dy + u2_x),
          u2_y - (sign * dx + u2_y)
      );



      sourceVector = sourceVector.getNormalized();
      sourceVector = PointD.times(threshold * Math.abs(fittedSlopeAngle - edgeSlopeAngle), sourceVector);
      forces.setValue(sourceNode, PointD.add(forces.getValue(sourceNode), sourceVector));

      targetVector = targetVector.getNormalized();
      targetVector = PointD.times(threshold * Math.abs(fittedSlopeAngle - edgeSlopeAngle), targetVector);
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

  @Override
  public ArrayList<AbstractLayoutInterfaceItem> getAbstractLayoutInterfaceItems() {
    ArrayList<AbstractLayoutInterfaceItem> parameterList = new ArrayList<>();
    return parameterList;
  }
}
