package util;

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

    for (int d = 1; d <= dimension; d++){
      PointD dir = (new PointD(2 * rand.nextDouble() - 1, 2 * rand.nextDouble() - 1)).getNormalized();
      dir = PointD.times(100, dir);
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
    }
  }
}