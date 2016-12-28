package io;

import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.geometry.PointD;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;

public class ContestIOHandler extends GraphIOHandler {
  public static void read(IGraph g, String inputFileName) throws IOException {
    Path path = Paths.get(inputFileName);
    List<String> lines = Files.readAllLines(path);

    g.clear();
    int phase = 0;
    int nodesCount = -1;
    int nodesAdded = 0;
    // I HATE THE JAVA COMPILER "mimimi nodes might not have been initialized"
    INode[] nodes = new INode[0];
    for(String line: lines){
      line = line.trim();
      // comment
      if(line.charAt(0) == '#') continue;
      if(phase == 0){
        nodesCount = Integer.parseInt(line);
        nodes = new INode[nodesCount];
        phase = 1;
      }
      else if(phase == 1){
        try {
          int x, y;
          String[] sxy = line.split("\\s+");
          x = Integer.parseInt(sxy[0]);
          y = Integer.parseInt(sxy[1]);
          INode node = g.createNode(new PointD(x, y));
          nodes[nodesAdded] = node;
          nodesAdded++;
          if(nodesAdded >= nodesCount) phase = 2;
        } catch(Exception e){}
      }
      else if(phase == 2){
        try {
          int i1, i2;
          String[] si1i2 = line.split("\\s+");
          i1 = Integer.parseInt(si1i2[0]);
          i2 = Integer.parseInt(si1i2[1]);
          g.createEdge(nodes[i1], nodes[i2]);
        } catch(Exception e){}
      }
    }
  }

  public static void write(IGraph graph, String outputFileName) throws IOException {
    
  }
}