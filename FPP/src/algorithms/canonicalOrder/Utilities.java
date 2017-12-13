package algorithms.canonicalOrder;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.geom.YPoint;
import y.geom.YVector;
import y.layout.planar.FaceCursor;
import y.layout.planar.PlanarInformation;
import y.view.Bend;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.NodeRealizer;

import java.util.Comparator;

public class Utilities {

	public static void setOuterFace(PlanarInformation plan, Graph2D graph) {
		// take leftMost node and check his faces
		Node leftMost = null;
		Node rightMost = null;
		Node topMost = null;
		Node bottomMost = null;
		double currentXMin = Double.MAX_VALUE;
		double currentXMax = -Double.MAX_VALUE;
		double currentYMin = Double.MAX_VALUE;
		double currentYMax = -Double.MAX_VALUE;
		for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
			YPoint c = getPointForNode(nc.node(), graph);
			if (c.x < currentXMin) {
				currentXMin = c.x;
				leftMost = nc.node();
			}
			if (c.x > currentXMax) {
				currentXMax = c.x;
				rightMost = nc.node();
			}
			if (c.y < currentYMin) {
				currentYMin = c.y;
				topMost = nc.node();
			}
			if (c.y > currentYMax) {
				currentYMax = c.y;
				bottomMost = nc.node();
			}
		}
		// now compute extreme bends
		Bend leftMostB = null;
		Bend rightMostB = null;
		Bend topMostB = null;
		Bend bottomMostB = null;
		for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
			EdgeRealizer r = graph.getRealizer(ec.edge());
			for (int i = 0; i < r.bendCount(); i++) {
				Bend b = r.getBend(i);
				YPoint p = getBendPoint(b);
				if (p.x < currentXMin) {
					currentXMin = p.x;
					leftMostB = b;
				}
				if (p.x > currentXMax) {
					currentXMax = p.x;
					rightMostB = b;
				}
				if (p.y < currentYMin) {
					currentYMin = p.y;
					topMostB = b;
				}
				if (p.y > currentYMax) {
					currentYMax = p.y;
					bottomMostB = b;
				}
			}
		}
		// if one of the extreme bends is != null then one of the vertices of
		// the corresponding edge has to be chosen instead of the extreme vertex
		if (leftMostB != null) {
			leftMost = leftMostB.getEdge().source();
		}
		if (rightMostB != null) {
			rightMost = rightMostB.getEdge().source();
		}
		if (topMostB != null) {
			topMost = topMostB.getEdge().source();
		}
		if (bottomMostB != null) {
			bottomMost = bottomMostB.getEdge().source();
		}
		// System.out.println("leftMost: " + leftMost + " rightMost: " +
		// rightMost + " bottomMost: " + bottomMost + " topMost: " + topMost);
		for (FaceCursor fc = plan.faces(); fc.ok(); fc.next()) {
			if (fc.face().contains(bottomMost) && fc.face().contains(topMost)
					&& fc.face().contains(leftMost)
					&& fc.face().contains(rightMost)) {

				plan.setOuterFace(fc.face());
			}
		}
	}

	public static YPoint getPointForNode(Node node, Graph2D graph) {
		NodeRealizer realizer = graph.getRealizer(node);
		return new YPoint(realizer.getCenterX(), realizer.getCenterY());
	}

	public static YPoint getBendPoint(Bend b) {
		return new YPoint(b.getX(), b.getY());
	}

}
