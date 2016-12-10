package util;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.*;

import java.util.*;
import java.util.stream.*;

public class GridGenerator {
  public static Random rand = new Random();
  public static void generate(IGraph g, int xCount, int yCount, int rCount){
    g.clear();
    INode[][] nodes = new INode[xCount][];
    for(int x = 0; x < xCount; x++){
      nodes[x] = new INode[yCount];
      for(int y = 0; y < yCount; y++){
        PointD pos = new PointD(500 + 50 * x, 500 + 50 * y);
        INode n = g.createNode();
        nodes[x][y] = n;
        g.setNodeCenter(n, pos);
      }
    }
    for(int x = 0; x < xCount - 1; x++){
      for(int y = 0; y < yCount; y++){
        INode source = nodes[x][y],
              target = nodes[x + 1][y];
        g.createEdge(source, target);
      }
    }
    for(int x = 0; x < xCount; x++){
      for(int y = 0; y < yCount - 1; y++){
        INode source = nodes[x][y],
              target = nodes[x][y + 1];
        g.createEdge(source, target);
      }
    }
    for(int r = 0; r < rCount; r++){
      for(int tryC = 0; tryC < 100; tryC++){
        int sourceX = rand.nextInt(xCount);
        int sourceY = rand.nextInt(yCount);
        int targetX = rand.nextInt(xCount);
        int targetY = rand.nextInt(yCount);
        if(sourceX == targetX && sourceY == targetY) continue;
        INode source = nodes[sourceX][sourceY];
        INode target = nodes[targetX][targetY];
        if(g.getEdge(source, target) != null) continue;
        g.createEdge(source, target);
        break;
      }
    }
  }
}