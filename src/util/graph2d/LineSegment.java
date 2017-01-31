package util.graph2d;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.algorithms.IPlaneObject;
import com.yworks.yfiles.algorithms.YRectangle;
import com.yworks.yfiles.graph.*;
import util.*;

public class LineSegment implements IPlaneObject {
  YRectangle bb;
  public PointD p1, p2, ve;
  public Maybe<IEdge>  e = Maybe.nothing();
  public Maybe<INode> n1 = Maybe.nothing(), 
                      n2 = Maybe.nothing();

  public YRectangle getBoundingBox(){
    return bb;
  }

  /**
   * Calculate the Bounding Box of each Line Segment
   */
  public void calcBB(){
    double x1, y1;
    x1 = p1.getX();
    y1 = p1.getY();
    double x2, y2;
    x2 = p2.getX();
    y2 = p2.getY();
    bb = new YRectangle(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
  }

  /**
   * Initialize LineSegment with two PointDs p11 & p21
   */
  public LineSegment(PointD p11, PointD p21){
    p1 = p11;
    p2 = p21;
    ve = PointD.subtract(p2, p1);
    calcBB();
  }

  /**
   * Initialize LineSegment with two Nodes n1 & n2
   */
  public LineSegment(INode n1, INode n2){
    this(n1.getLayout().getCenter(), n2.getLayout().getCenter());
    this.n1 = Maybe.just(n1);
    this.n2 = Maybe.just(n2);
  }

  /**
   * Initialize LineSegment with Edge e
   */
  public LineSegment(IEdge e){
    this(e.getSourceNode(), e.getTargetNode());
    this.e = Maybe.just(e);
  }

  /**
   * Initialize LineSegment with Edge e and node Positions np
   */
  public LineSegment(IEdge e, IMapper<INode, PointD> np){
    this(e);
    p1 = np.getValue(n1.get());
    p2 = np.getValue(n2.get());
    ve = PointD.subtract(p2, p1);
    calcBB();
  }

  /**
   * Compute intersections of this line segment with another line segment
   * @param o - other line segment
   * @param skipEqualEndpoints - true if endpoints can be equal
   * @return Return Intersection of there is one
   */
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
    if(t < 0 || u < 0 || t > 1 || u > 1) return Maybe.nothing();
    PointD crossingPoint = PointD.add(p1, PointD.times(t, r));
    Double crossingsAngle = Math.toDegrees(Math.acos(PointD.scalarProduct(r, s) / (r.getVectorLength() * s.getVectorLength())));
    return Maybe.just(new Intersection(crossingPoint, crossingsAngle));
  }

  // compute cross product of two points
  public static double crossProduct(PointD p1, PointD p2){
    return p1.getX() * p2.getY() - p1.getY() * p2.getX();
  }

  @Override
  public String toString(){
    return "(LS " + p1 + " " + p2 + ")";
  }

  /**
   * Compare if two linesegments are equal
   * @return true if linesegments are equal
   */
  @Override
  public boolean equals(Object o){
    if(o instanceof LineSegment){
      LineSegment l = (LineSegment) o;
      if((p1.distanceTo(l.p1) <= G.Epsilon && p2.distanceTo(l.p2) <= G.Epsilon) ||
              (p1.distanceTo(l.p2) <= G.Epsilon && p2.distanceTo(l.p1) <= G.Epsilon))
        return true;
      else return false;
    }
    return false;
  }


}