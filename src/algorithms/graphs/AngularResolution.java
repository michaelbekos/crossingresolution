package algorithms.graphs;

import com.yworks.yfiles.algorithms.YPoint;
import com.yworks.yfiles.algorithms.YVector;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.utils.PositionMap;

import java.util.ArrayList;

public class AngularResolution {

    public static double getAngularResolution(IGraph graph) {
        ArrayList<INode> nodeList = new ArrayList<>();
        for (INode x : graph.getNodes()) {
            nodeList.add(x);
        }

        return getAngle(null, nodeList, graph);
    }


    public static double getAngularResolutionForNode(Mapper<INode, PointD> nodePositions, INode node, IGraph graph) {
        ArrayList<INode> nodeList = new ArrayList<>();

        nodeList.add(node);
        for (INode n : graph.neighbors(INode.class, node)) {
            nodeList.add(n);
        }

        return getAngle(nodePositions, nodeList, graph);
    }

    private static double getAngle(Mapper<INode, PointD> nodePositions, ArrayList<INode> nodeList, IGraph graph) {
        if (nodePositions == null) {
            nodePositions = PositionMap.FromIGraph(graph);
        }

        double angle = Double.MAX_VALUE;
        final Mapper<INode, PointD> positions = nodePositions;

        for (INode v : nodeList) {
            //Sort in cyclic order the adjacent edges
            ArrayList<IEdge> edgeList = new ArrayList<>();
            for (IEdge e : graph.edgesAt(v)) {
                edgeList.add(e);
            }

            if (edgeList.size() == 1) continue;

            //Cyclic Edge Comparator - Compares two edges that must share a common end point.
            edgeList.sort(((IEdge e1, IEdge e2) -> {
                INode c;
                INode w;    //u
                INode x;    //v

                if (e1.getSourceNode() == e2.getSourceNode()) {
                    c = e1.getSourceNode();
                    w = e1.getTargetNode();
                    x = e2.getTargetNode();
                } else if (e1.getSourceNode() == e2.getTargetNode()) {
                    c = e1.getSourceNode();
                    w = e1.getTargetNode();
                    x = e2.getSourceNode();
                } else if (e1.getTargetNode() == e2.getSourceNode()) {
                    c = e1.getTargetNode();
                    w = e1.getSourceNode();
                    x = e2.getTargetNode();
                } else if (e1.getTargetNode() == e2.getTargetNode()) {
                    c = e1.getTargetNode();
                    w = e1.getSourceNode();
                    x = e2.getSourceNode();
                } else {
                    return -1;
                }

                YVector cVector = new YVector(positions.getValue(c).getX() + 1, positions.getValue(c).getY(), positions.getValue(c).getX(), positions.getValue(c).getY());
                YVector uVector = new YVector(positions.getValue(w).getX(), positions.getValue(w).getY(), positions.getValue(c).getX(), positions.getValue(c).getY());
                YVector vVector = new YVector(positions.getValue(x).getX(), positions.getValue(x).getY(), positions.getValue(c).getX(), positions.getValue(c).getY());

                double tu = YVector.angle(uVector, cVector);
                double tv = YVector.angle(vVector, cVector);

                if (tu == tv) {
                    return 0;
                } else if (tu > tv) {
                    if (e1.getSourceNode() == w || e1.getTargetNode() == w) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    if (e1.getSourceNode() == w || e1.getTargetNode() == w) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }));

            for (int i = 0; i < edgeList.size(); i++) {
                IEdge e1 = edgeList.get(i);
                IEdge e2 = edgeList.get((i + 1) % edgeList.size());

                INode u1 = (v == e1.getSourceNode()) ? e1.getTargetNode() : e1.getSourceNode();
                INode u2 = (v == e2.getSourceNode()) ? e2.getTargetNode() : e2.getSourceNode();

                YPoint p_v = new YPoint(positions.getValue(v).getX(), positions.getValue(v).getY());
                YPoint p_u1 = new YPoint(positions.getValue(u1).getX(), positions.getValue(u1).getY());
                YPoint p_u2 = new YPoint(positions.getValue(u2).getX(), positions.getValue(u2).getY());

                YVector v_u1 = new YVector(p_u1, p_v);
                YVector v_u2 = new YVector(p_u2, p_v);

                angle = Math.min(angle, YVector.angle(v_u2, v_u1));
            }
        }
        return (180 * angle / Math.PI);
    }
    public static INode[] getCriticalNodes(IGraph graph) {  //TODO: refactor static so min angular doesnt get computed twice
        ArrayList<INode> nodeList = new ArrayList<>();
        for (INode x : graph.getNodes()) {
            nodeList.add(x);
        }

        Mapper<INode, PointD> nodePositions = PositionMap.FromIGraph(graph);

        double angle = Double.MAX_VALUE;
        final Mapper<INode, PointD> positions = nodePositions;
        INode nodes[] = new INode[3];

        for (INode v : nodeList) {
            //Sort in cyclic order the adjacent edges
            ArrayList<IEdge> edgeList = new ArrayList<>();
            for (IEdge e : graph.edgesAt(v)) {
                edgeList.add(e);
            }

            if (edgeList.size() == 1) continue;

            //Cyclic Edge Comparator - Compares two edges that must share a common end point.
            edgeList.sort(((IEdge e1, IEdge e2) -> {
                INode c;
                INode w;    //u
                INode x;    //v

                if (e1.getSourceNode() == e2.getSourceNode()) {
                    c = e1.getSourceNode();
                    w = e1.getTargetNode();
                    x = e2.getTargetNode();
                } else if (e1.getSourceNode() == e2.getTargetNode()) {
                    c = e1.getSourceNode();
                    w = e1.getTargetNode();
                    x = e2.getSourceNode();
                } else if (e1.getTargetNode() == e2.getSourceNode()) {
                    c = e1.getTargetNode();
                    w = e1.getSourceNode();
                    x = e2.getTargetNode();
                } else if (e1.getTargetNode() == e2.getTargetNode()) {
                    c = e1.getTargetNode();
                    w = e1.getSourceNode();
                    x = e2.getSourceNode();
                } else {
                    return -1;
                }

                YVector cVector = new YVector(positions.getValue(c).getX() + 1, positions.getValue(c).getY(), positions.getValue(c).getX(), positions.getValue(c).getY());
                YVector uVector = new YVector(positions.getValue(w).getX(), positions.getValue(w).getY(), positions.getValue(c).getX(), positions.getValue(c).getY());
                YVector vVector = new YVector(positions.getValue(x).getX(), positions.getValue(x).getY(), positions.getValue(c).getX(), positions.getValue(c).getY());

                double tu = YVector.angle(uVector, cVector);
                double tv = YVector.angle(vVector, cVector);

                if (tu == tv) {
                    return 0;
                } else if (tu > tv) {
                    if (e1.getSourceNode() == w || e1.getTargetNode() == w) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    if (e1.getSourceNode() == w || e1.getTargetNode() == w) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }));

            for (int i = 0; i < edgeList.size(); i++) {
                IEdge e1 = edgeList.get(i);
                IEdge e2 = edgeList.get((i + 1) % edgeList.size());

                INode u1 = (v == e1.getSourceNode()) ? e1.getTargetNode() : e1.getSourceNode();
                INode u2 = (v == e2.getSourceNode()) ? e2.getTargetNode() : e2.getSourceNode();

                YPoint p_v = new YPoint(positions.getValue(v).getX(), positions.getValue(v).getY());
                YPoint p_u1 = new YPoint(positions.getValue(u1).getX(), positions.getValue(u1).getY());
                YPoint p_u2 = new YPoint(positions.getValue(u2).getX(), positions.getValue(u2).getY());

                YVector v_u1 = new YVector(p_u1, p_v);
                YVector v_u2 = new YVector(p_u2, p_v);

                angle = Math.min(angle, YVector.angle(v_u2, v_u1));
                if (angle ==  YVector.angle(v_u2, v_u1)) {
                    nodes = new INode[] {v, u1, u2};
                }
            }
        }
        return nodes;
    }
}
