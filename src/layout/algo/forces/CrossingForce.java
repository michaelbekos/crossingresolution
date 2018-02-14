package layout.algo.forces;

import algorithms.graphs.CachedMinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;
import util.G;
import util.Tuple2;
import util.Util;
import util.graph2d.LineSegment;

public class CrossingForce implements IForce {
  private CachedMinimumAngle cMinimumAngle;
  private IGraph graph;
  private AbstractLayoutInterfaceItem<Double> weight;
  private AbstractLayoutInterfaceItem<Boolean> isPerpendicular;

  public CrossingForce(IGraph graph, CachedMinimumAngle cMinimumAngle) {
    this.cMinimumAngle = cMinimumAngle;
    this.graph = graph;
  }

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    weight = itemFactory.doubleParameter("Crossings Force", 0.0, 1, true);
    weight.setValue(0.01);
    isPerpendicular = itemFactory.booleanParameter("Perpendicular", false);
    isPerpendicular.setValue(false);
  }

  @Override
  public Mapper<INode, PointD> calculate(Mapper<INode, PointD> forces, Mapper<INode, PointD> nodePositions) {
    if (weight.getValue() == 0) {
      return forces;
    }

    cMinimumAngle.getCrossings(graph, nodePositions).parallelStream().forEach(intersection -> {
      LineSegment l1 = intersection.segment1,
          l2 = intersection.segment2;
      INode n1 = l1.n1,
          n2 = l1.n2,
          n3 = l2.n1,
          n4 = l2.n2;
      PointD p1 = l1.p1,
          p2 = l1.p2,
          p3 = l2.p1,
          p4 = l2.p2,
          f1 = new PointD(0, 0),
          f2 = new PointD(0, 0),
          f3 = new PointD(0, 0),
          f4 = new PointD(0, 0),
          v1 = PointD.add(p1, PointD.negate(p2)),
          v2 = PointD.add(p3, PointD.negate(p4));
      // apply cosinus force
      Tuple2<PointD, PointD> f = calculateSomethingWithCosinuses(v1, v2, intersection.orientedAngle);
        PointD force1 = f.a, force2 = f.b;
        f1 = PointD.add(f1, force1);
        f2 = PointD.add(f2, PointD.negate(force1));
        f3 = PointD.add(f3, force2);
        f4 = PointD.add(f4, PointD.negate(force2));
      synchronized(forces){
        PointD f1_1 = forces.getValue(n1),
            f2_1 = forces.getValue(n2),
            f3_1 = forces.getValue(n3),
            f4_1 = forces.getValue(n4);
        forces.setValue(n1, PointD.add(f1, f1_1));
        forces.setValue(n2, PointD.add(f2, f2_1));
        forces.setValue(n3, PointD.add(f3, f3_1));
        forces.setValue(n4, PointD.add(f4, f4_1));
      }
    });
    return forces;
  }

  private Tuple2<PointD, PointD> calculateSomethingWithCosinuses(PointD e1, PointD e2, double angle) {
    double threshold = weight.getValue();
    if(e1.getVectorLength() <= G.Epsilon ||
        e2.getVectorLength() <= G.Epsilon){
      return new Tuple2<>(new PointD(0, 0), new PointD(0, 0));
    }
    PointD t1 = e1.getNormalized();
    PointD t2 = e2.getNormalized();
    PointD t1Neg = PointD.negate(t1);
    PointD t2Neg = PointD.negate(t2);
    PointD t1_ = new PointD(0,0),
        t2_ = new PointD(0,0);

    t1_ = PointD.times(t2Neg, threshold * Math.cos(Math.toRadians(angle)));
    t2_ = PointD.times(t1Neg, threshold * Math.cos(Math.toRadians(angle)));
    t1 = PointD.times(t1, threshold * Math.cos(Math.toRadians(angle)));
    t2 = PointD.times(t2, threshold * Math.cos(Math.toRadians(angle)));

    t1 = Util.rotate90DegreesClockwise(PointD.negate(t1));
    t2 = Util.rotate90DegreesClockwise(t2);
    if(isPerpendicular.getValue()) {
      return new Tuple2<>(t1, t2);
    }
    // else direction of other edge
    else{
      return new Tuple2<>(t1_, t2_);
    }
            /*if(angle > 60 && angle < 120){
                return new Tuple2<>(new PointD(0, 0), new PointD(0, 0));
            }
            t1 = PointD.times(t1, threshold * Math.cos(2.0 / 3.0 * Math.toRadians(angle)));
            t2 = PointD.times(t2, threshold * Math.cos(2.0 / 3.0 * Math.toRadians(angle)));
        */
  }

  public void toggleCheckbox(boolean value) {
    weight.toggleCheckbox(value);
  }
}