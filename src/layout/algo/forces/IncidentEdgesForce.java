package layout.algo.forces;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;
import util.G;
import util.Tuple2;
import util.Tuple3;
import util.Util;

import java.util.*;
import java.util.stream.Collectors;

public class IncidentEdgesForce implements IForce {
  private IGraph graph;
  public AbstractLayoutInterfaceItem<Double> weight;

  public IncidentEdgesForce(IGraph graph) {
    this.graph = graph;
  }

  @Override
  public void init(ILayoutInterfaceItemFactory itemFactory) {
    weight = itemFactory.doubleParameter("Incident Edges Force", 0.1, 1, 1);
    weight.setValue(0.1);
  }

  @Override
  public Mapper<INode, PointD> calculate(Mapper<INode, PointD> forces, Mapper<INode, PointD> nodePositions) {
//http://www.euclideanspace.com/maths/algebra/vectors/angleBetween/
    //for (INode n1 : graph.getNodes()) {
    graph.getNodes().parallelStream().forEach(n1 -> {
      PointD p1 = nodePositions.getValue(n1);
      Integer n1degree = graph.degree(n1);
      if(n1degree < 2) return;
      //nonStrictlyEqualPairs(graph.neighbors(INode.class, n1).stream(), graph.neighbors(INode.class, n1).stream()).forEach(n2n3 -> {
      List<Tuple3<INode, INode, Double>> neighboursWithAngle =
          Util.nonEqalPairs(graph.neighbors(INode.class, n1).stream(), graph.neighbors(INode.class, n1).stream()).map(
              n2n3 -> {
                INode n2 = n2n3.a,
                    n3 = n2n3.b;
                PointD p2 = nodePositions.getValue(n2),
                    p3 = nodePositions.getValue(n3);
                PointD v1 = PointD.subtract(p2, p1);
                PointD v2 = PointD.subtract(p3, p1);

                Double angle = Math.toDegrees(Math.atan2(v2.getY(), v2.getX()) - Math.atan2(v1.getY(), v1.getX()));
                return new Tuple3<>(n2n3, angle);
              })
              .map(n2n3d -> {
                if(n2n3d.c < 0)
                  return new Tuple3<>(n2n3d, 360 + n2n3d.c);
                else
                  return n2n3d;
              })
              .collect(Collectors.toList());
      Comparator<Tuple3<INode, INode, Double>> byAngle =
          (t1, t2) -> Double.compare(t1.c, t2.c);
      Collections.sort(neighboursWithAngle, byAngle);
      Tuple3<INode, INode, Double> n2n3 = neighboursWithAngle.get(0);
      neighboursWithAngle.remove(0);
      Set<INode> seenNodes = new HashSet<>();
      boolean nextFound = true;
      // go from node to node until all nodes have been visited
      // visit the first node twice --> don't add it before the first iteration
      while(nextFound) {
        INode n2 = n2n3.a,
            n3 = n2n3.b;
        seenNodes.add(n3);
        PointD p2 = nodePositions.getValue(n2),
            p3 = nodePositions.getValue(n3);
        PointD f2 = new PointD(0, 0),
            f3 = new PointD(0, 0);
        PointD v1 = PointD.subtract(p2, p1);
        PointD v2 = PointD.subtract(p3, p1);
        Double angle = n2n3.c;
        Tuple2<PointD, PointD> f = applyIncidentEdgeForce(v1, v2, angle, n1degree);
        f2 = PointD.add(f2, f.a);
        f3 = PointD.add(f3, f.b);
        synchronized(forces){
          PointD f2_1 = forces.getValue(n2),
              f3_1 = forces.getValue(n3);
          forces.setValue(n2, PointD.add(f2_1, f2));
          forces.setValue(n3, PointD.add(f3_1, f3));
        }
        nextFound = false;
        for(Tuple3<INode, INode, Double> next: neighboursWithAngle) {
          if(next.a.equals(n3) && !seenNodes.contains(next.b)){
            n2n3 = next;
            nextFound = true;
            break;
          }
        }
      }
    });
    return forces;
  }

  private Tuple2<PointD, PointD> applyIncidentEdgeForce(PointD e1, PointD e2, double angle, int deg) {
    if(deg <= 0) return new Tuple2<>(new PointD(0, 0), new PointD(0, 0));
    double threshold = weight.getValue(),
        optAngle = (360 / deg);
    if(e1.getVectorLength() <= G.Epsilon ||
        e2.getVectorLength() <= G.Epsilon){
      return new Tuple2<>(new PointD(0, 0), new PointD(0, 0));
    }
    PointD t1 = e1.getNormalized();
    PointD t2 = e2.getNormalized();
    Double neg = Math.signum(angle);

    t1 = PointD.times(t1, neg * threshold * Math.sin((Math.toRadians(optAngle - Math.abs(angle)))/2.0));
    t2 = PointD.times(t2, neg * threshold * Math.sin((Math.toRadians(optAngle - Math.abs(angle)))/2.0));
    t1 = Util.rotate90DegreesClockwise(t1);
    t2 = PointD.negate(t2);
    t2 = Util.rotate90DegreesClockwise(t2);
    return new Tuple2<>(t1, t2);
  }

  @Override
  public ArrayList<AbstractLayoutInterfaceItem> getAbstractLayoutInterfaceItems() {
    ArrayList<AbstractLayoutInterfaceItem> parameterList = new ArrayList<>();
    parameterList.add(weight);
    return parameterList;
  }
}