package util;

import com.yworks.yfiles.geometry.Matrix2D;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.*;

import java.util.*;
import java.util.stream.*;

public class HypercubeGenerator {
  public static Random rand = new Random();
  public static void generate(IGraph g, int dimension){
    g.clear();
    INode n0 = g.createNode();
    g.setNodeCenter(n0, new PointD(500, 500));
    PointD dir = new PointD(0, 100);
    Matrix2D rot = Matrix2D.createRotateInstance(Math.PI / dimension);
    for (int d = 1; d <= dimension; d++){
      List<INode> oldNodes = g.getNodes().stream().collect(Collectors.toList());
      List<IEdge> oldEdges = g.getEdges().stream().collect(Collectors.toList());
      INode[] newNodes = new INode[oldNodes.size()];
      int i = 0;
      for(INode nOld: oldNodes){
        INode nNew = g.createNode();
        newNodes[i] = nNew;
        PointD oldPos = nOld.getLayout().getCenter();
        PointD newPos = PointD.add(oldPos, dir);
        g.setNodeCenter(nNew, newPos);
        g.createEdge(nOld, nNew);
        i++;
      }
      for(IEdge e: oldEdges){
        INode source, target;
        source = newNodes[Integer.parseInt(e.getSourceNode().toString())];
        target = newNodes[Integer.parseInt(e.getTargetNode().toString())];
        g.createEdge(source, target);
      }
      dir = PointD.times(rot, dir);
    }
  }
}