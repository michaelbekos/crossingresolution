package util.graph2d;

import com.yworks.yfiles.geometry.PointD;

public class Intersection implements Comparable<Intersection> {
  public PointD intersectionPoint;
  public Double angle;
  public Double orientedAngle;
  public LineSegment segment1;
  public LineSegment segment2;

  @Override 
  public String toString(){
    return "(I" + intersectionPoint + ", " + angle + ")";
  }
  public Intersection(PointD i, Double a, LineSegment segment1, LineSegment segment2){
    intersectionPoint = i;
    orientedAngle = a;
    angle = orientedAngle;
    if(angle > 90){
      angle = 180 - angle;
    }
    this.segment1 = segment1;
    this.segment2 = segment2;
  }
  @Override
  public boolean equals(Object o){
    if(o instanceof Intersection){
      return 
        ((Intersection) o).intersectionPoint.equals(intersectionPoint) && 
        ((Intersection) o).angle.equals(angle);
    }
    return false;
  }
  @Override
  public int compareTo(Intersection o){
    return orientedAngle.compareTo(o.orientedAngle);
  }
}