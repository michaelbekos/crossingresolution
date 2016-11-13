package util.graph2d;

import com.yworks.yfiles.geometry.PointD;

public class Intersection{
  public PointD intersectionPoint;
  public Double angle;
  @Override 
  public String toString(){
    return "I(" + intersectionPoint + ", " + angle + ")";
  }
  public Intersection(PointD i, Double a){
    intersectionPoint = i;
    angle = a;
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
}