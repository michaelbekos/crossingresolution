package io;

import util.GridPositioning;

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
    GridPositioning.gridGraph(graph);
    BufferedWriter out = Files.newBufferedWriter(Paths.get(outputFileName));
    out.write("# First value is number of nodes (N)");
    out.newLine();
    long size = graph.getNodes().size();
    out.write(size + " ");
    out.newLine();
    out.write("# Next N numbers describe the node locations");
    out.newLine();
    for(INode n: graph.getNodes()){
      long x, y;
      PointD pos = n.getLayout().getCenter();
      x = Math.round(pos.getX());
      y = Math.round(pos.getY());
      out.write(x + " " + y);
      out.newLine();
    }
    out.write("# Remaining lines are the edges.");
    out.newLine();
    out.write("# The first value is the source node index.");
    out.newLine();
    out.write("# The second value is the target node index.");
    out.newLine();
    for(IEdge e: graph.getEdges()){
      int source, target;
      // hack: get index of node by doing toString, then parsing, since nodes have their index as label by default.
      source = Integer.parseInt(e.getSourceNode().toString());
      target = Integer.parseInt(e.getTargetNode().toString());
      
      out.write(source + " " + target);
      out.newLine();
    }
    out.close();
  }
}