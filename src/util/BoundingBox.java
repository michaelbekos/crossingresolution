package util;

import java.util.Map;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;

//public class BoundingBox {
//  public static RectD from(Mapper<INode, PointD> nodes) {
//    double minX = 0, maxX = MainFrame.BOX_SIZE[0];
//    double minY = 0, maxY = MainFrame.BOX_SIZE[1];
//
//    return new RectD(minX, minY, maxX - minX, maxY - minY);
//  }
//}


public class BoundingBox {
	  public static RectD from(Mapper<INode, PointD> nodes) {
	    double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
	    double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

	    for (Map.Entry<INode, PointD> node : nodes.getEntries()) {
	      PointD position = node.getValue();
	      minX = Math.min(position.x, minX);
	      maxX = Math.max(position.x, maxX);

	      minY = Math.min(position.y, minY);
	      maxY = Math.max(position.y, maxY);
	    }

	    return new RectD(minX, minY, maxX - minX, maxY - minY);
	  }
	}