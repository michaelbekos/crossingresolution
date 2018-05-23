package layout.algo.utils;

import java.util.Iterator;
import java.util.Map.Entry;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;

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

public static Boolean overlap(PointD position, Mapper<INode, PointD> positions) {
	Boolean value=true;
	Iterator<Entry<INode, PointD>> It = positions.getEntries().iterator();
	PointD tmp;
		while(It.hasNext()){
			tmp=It.next().getValue();
			if (position.hits(tmp, 0.5) || position.equals(tmp)) {
				value= false;
			}
		}
	return value;
}
}
