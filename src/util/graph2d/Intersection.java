package util.graph2d;

import com.yworks.yfiles.geometry.PointD;

public class Intersection implements Comparable<Intersection> {
  public PointD intersectionPoint;
  public Double angle, orientedAngle;
  @Override 
  public String toString(){
    return "I(" + intersectionPoint + ", " + angle + ")";
  }
  public Intersection(PointD i, Double a){
    intersectionPoint = i;
    orientedAngle = a;
    angle = orientedAngle;
    if(angle > 90){
      angle = 180 - angle;
    }
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