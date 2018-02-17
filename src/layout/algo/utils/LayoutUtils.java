package layout.algo.utils;

import com.yworks.yfiles.geometry.PointD;

public class LayoutUtils {
  public static PointD stepInDirection(PointD oldPosition, PointD direction, double stepSize) {
    return new PointD(
        oldPosition.getX() + stepSize * direction.getX(),
        oldPosition.getY() + stepSize * direction.getY()
    );
  }

  public static PointD round(PointD p) {
    return new PointD(Math.round(p.getX()), Math.round(p.getY()));
  }
}
