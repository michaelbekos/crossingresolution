package util.graph2d;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.*;
import util.*;

public class LineSegment{
  public PointD p1, p2, ve;
  public Maybe<IEdge> e = new Nothing<>();
  public Maybe<INode> n1 = new Nothing<>(), 
                      n2 = new Nothing<>();
  public Double key;
  @Override
  public String toString(){
    return "LS(" + p1 + ", " + p2 + ")";
  }
  public LineSegment(PointD p11, PointD p21){
    p1 = p11;
    p2 = p21;
    ve = PointD.subtract(p2, p1);
  }
  public LineSegment(INode n1, INode n2){
    this(n1.getLayout().getCenter(), n2.getLayout().getCenter());
    this.n1 = Maybe.just(n1);
    this.n2 = Maybe.just(n2);
  }
  public LineSegment(IEdge e){
    this(e.getSourceNode(), e.getTargetNode());
    this.e = Maybe.just(e);
  }
  public Maybe<Intersection> intersects(LineSegment o, boolean skipEqualEndpoints){
    PointD p3, p4;
    p3 = o.p1;
    p4 = o.p2;
    // skip equal endpoints
    if(skipEqualEndpoints && 
      (p1.equals(p3) || p1.equals(p4) ||
       p2.equals(p3) || p2.equals(p4))) return Maybe.nothing();
    // http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
    PointD r = ve;
    PointD s = o.ve;
    double rTimesS = crossProduct(r, s);
    // lines parallel
    if(rTimesS == 0) return Maybe.nothing();
    double t, u;
    t = crossProduct(PointD.subtract(p3, p1), s) / rTimesS;
    u = crossProduct(PointD.subtract(p3, p1), r) / rTimesS;
    // intersection not on line segments
    if(t < 0 || u < 0 || t > 1 || u > 1) return new Nothing<>();
    PointD crossingPoint = PointD.add(p1, PointD.times(t, r));
    Double crossingsAngle = Math.toDegrees(Math.acos(PointD.scalarProduct(r, s) / (r.getVectorLength() * s.getVectorLength())));
    return Maybe.just(new Intersection(crossingPoint, crossingsAngle));
  }
  public static double crossProduct(PointD p1, PointD p2){
    return p1.getX() * p2.getY() - p1.getY() * p2.getX();
  }
}