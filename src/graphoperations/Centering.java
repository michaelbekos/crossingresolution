package graphoperations;

import java.util.Map;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;

import util.BoundingBox;

public class Centering {

	public static void moveToCenter(double boxSizeX, double boxSizeY, Mapper<INode, PointD> nodePositions) {
	    RectD bounds = BoundingBox.from(nodePositions);
	    double moveX = boxSizeX/2-bounds.getCenterX();
	    double moveY = boxSizeY/2-bounds.getCenterY();
	    for (Map.Entry<INode, PointD> entry : nodePositions.getEntries()) {
	        INode node = entry.getKey();
	        PointD center = entry.getValue();
	        nodePositions.setValue(node, new PointD(
	            (center.getX() + moveX),
	            (center.getY() + moveY)
	        ));
	      }
	}

}
