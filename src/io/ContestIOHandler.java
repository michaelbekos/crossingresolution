package io;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Handles the contest format files
 */
public class ContestIOHandler extends GraphIOHandler {


  static List<String> lines;
   /**
   * Parsing inputFile in contest format
   * Ignore Comments & Whitespaces & Empty Lines
   */
  public static void read(IGraph g, String inputFileName) throws IOException {
    Path path = Paths.get(inputFileName);
    lines = Files.readAllLines(path);

    g.clear();
    int phase = 0;
    int nodesCount = -1;
    int nodesAdded = 0;
    // I HATE THE JAVA COMPILER "mimimi nodes might not have been initialized"
    INode[] nodes = new INode[0];
    for(String line: lines){
      line = line.trim();
      // ignore whitespaces or leading empty lines
      if(line.isEmpty() || line.equals("") || line.equals("\n")) { continue;}
      else if(line.charAt(0) == '#') {continue;}

      // phase 0 contains nodes count
      if(phase == 0){
        nodesCount = Integer.parseInt(line);
        nodes = new INode[nodesCount];
        phase = 1;
      }
      // phase 1 contains all the nodes in the graph
      else if(phase == 1){
        try {
          int x, y;
          String[] sxy = line.split("\\s+");
          x = Integer.parseInt(sxy[0]);
          y = Integer.parseInt(sxy[1]);
          INode node = g.createNode(new PointD(x, y));
          node.setTag(nodesAdded);
          nodes[nodesAdded] = node;
          nodesAdded++;
          if(nodesAdded >= nodesCount) phase = 2;
        } catch(Exception e){}
      }
      // phase 2 contains all the edges in the graph
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


  /**
   * Writing graph in file with contest format
   */
  public static void write(IGraph graph, String outputFileName, JTextArea outputTextArea) throws IOException {
    boolean isFromContestFile = true;

    if(lines == null){
      System.out.println("No line was read!");
      isFromContestFile = false;
    } else {
      for(INode n: graph.getNodes()){
        if(n.getTag() == null){
            isFromContestFile = false;
            System.out.println("Found node without tag!");
        }
      }
    }
    //allow saving of non-contest files in contest format
    int tagNum = 0;
    for (INode u : graph.getNodes()) {
      if (u.getTag() == null) {
        u.setTag(tagNum);
      }
      tagNum++;
    }
    if(isFromContestFile){
      // phase 0 contains number of nodes
      BufferedWriter out = Files.newBufferedWriter(Paths.get(outputFileName));
      out.write("# First value is number of nodes (N)");
      out.newLine();
      long size = graph.getNodes().size();
      out.write(size + " ");
      out.newLine();
      // phase 1 contains nodes contained in graph
      out.write("# Next N numbers describe the node locations");
      out.newLine();
      StringBuilder errorText = new StringBuilder();
      for(int i = 0; i < graph.getNodes().size(); i++){
        for(INode n: graph.getNodes()){
          if(i == n.getTag().hashCode()) {
            long x, y;
            PointD pos = n.getLayout().getCenter();
            if (pos.getY() % 1 != 0 || pos.getX() % 1 != 0) {
              System.err.println("Graph not Gridded! Rounding Non-Integer Values: x " + pos.getX() + " y " + pos.getY());
              errorText.append("Graph not Gridded! Rounding Non-Integer Values:\nx ").append(pos.getX()).append("\ny ").append(pos.getY()).append("\n");
            }
            x = Math.round(pos.getX());
            y = Math.round(pos.getY());
            out.write(x + " " + y);
            out.newLine();
          }
        }
      }
      if (outputTextArea != null) {
        outputTextArea.setText(errorText.toString());
      }
      // phase 2 contains edges contained in graph
      boolean reachedEdgeLine = false;
      for(String line: lines) {
        // line = line.trim();
        // ignore whitespaces or leading empty lines
        if (reachedEdgeLine|| line.equals("# Remaining lines are the edges.") ) {
          out.write(line);
          out.newLine();
          reachedEdgeLine = true;
        }
      }
      out.close();

    }else {
      // phase 0 contains number of nodes
      BufferedWriter out = Files.newBufferedWriter(Paths.get(outputFileName));
      out.write("# First value is number of nodes (N)");
      out.newLine();
      long size = graph.getNodes().size();
      out.write(size + " ");
      out.newLine();
      // phase 1 contains nodes contained in graph
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
      // phase 2 contains edges contained in graph
      out.write("# Remaining lines are the edges.");
      out.newLine();
      out.write("# The first value is the source node index.");
      out.newLine();
      out.write("# The second value is the target node index.");
      out.newLine();
      for(IEdge e: graph.getEdges()){
        int source, target;
        // hack: get index of node by doing toString, then parsing, since nodes have their index as label by default.
        source = Integer.parseInt(e.getSourceNode().getTag().toString());
        target = Integer.parseInt(e.getTargetNode().getTag().toString());

        out.write(source + " " + target);
        out.newLine();
      }
      out.close();
    }


  }
}