package io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import layout.algo.utils.PositionMap;
import util.BoundingBox;

import javax.swing.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Handles the contest format files
 */
public class Contest2018IOHandler {

    private static final int MAX_X_DIMENSION = 1000000;
    private static final int MAX_Y_DIMENSION = 1000000;

    private JSONFile openFile = null;

    public class Node {
        int id;
        int x;
        int y;

        private Node(int id, int x, int y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return id + " : "+ "("+x+","+y+")";
        }
    }

    public class Edge {
        int source;
        int target;

        private Edge(int source, int target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public String toString() {
            return source + "->" + target;
        }
    }

    private class JSONFile {
        private ArrayList<Node> nodes;
        private ArrayList<Edge> edges;
        private Integer xdimension;
        private Integer ydimension;

        private JSONFile(ArrayList<Node> nodes, ArrayList<Edge> edges, Integer xdimension, Integer ydimension) {
            this.nodes = nodes;
            this.edges = edges;
            this.xdimension = xdimension;
            this.ydimension = ydimension;
        }

        @Override
        public String toString() {
            StringBuilder outString = new StringBuilder();
            for (Node n : nodes) {
                outString.append(n);
            }
            for (Edge e : edges) {
                outString.append(e);
            }
            outString.append("Dim: ").append("(").append(xdimension).append(",").append(ydimension).append(")\n");
            return outString.toString();
        }
    }

    /**
     * Parsing inputFile in contest 2018 JSON format
     */
    public void read(IGraph g, String inputFileName) throws IOException {
        openFile = null;
        g.clear();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JSONFile inFile = gson.fromJson(new FileReader(inputFileName), JSONFile.class);

        if (inFile == null) {
            throw new IOException();
        }

        openFile = inFile;

        if (openFile.xdimension == null) {
            openFile.xdimension = MAX_X_DIMENSION;
        }
        if (openFile.ydimension == null) {
            openFile.ydimension = MAX_Y_DIMENSION;
        }

        //Create Nodes
        INode[] nodes = new INode[inFile.nodes.size()];
        for (int i = 0; i < inFile.nodes.size(); i++) {
            INode node = g.createNode(new PointD(inFile.nodes.get(i).x, inFile.nodes.get(i).y));
            node.setTag(i);
            nodes[i] = node;
        }

        //Create Edges
        for (int i = 0; i < inFile.edges.size(); i++) {
            g.createEdge(nodes[inFile.edges.get(i).source], nodes[inFile.edges.get(i).target]);
        }

    }

    /**
     * Writing graph in JSON file with contest 2018 format
     */
    public void write(IGraph graph, String outputFileName, JTextArea outputTextArea) throws IOException {
        //Get Nodes
        ArrayList<Contest2018IOHandler.Node> nodes = new ArrayList<>(graph.getNodes().size());
        StringBuilder errorText = new StringBuilder();
        for(int i = 0; i < graph.getNodes().size(); i++){
            for(INode n: graph.getNodes()){
                if(i == n.getTag().hashCode()) {
                    int x, y;
                    PointD pos = n.getLayout().getCenter();
                    if (pos.getY() % 1 != 0 || pos.getX() % 1 != 0) {
                        System.err.println("Graph not Gridded! Rounding Non-Integer Values: x " + pos.getX() + " y " + pos.getY());
                        errorText.append("Graph not Gridded! Rounding Non-Integer Values:\nx ").append(pos.getX()).append("\ny ").append(pos.getY()).append("\n");
                    }
                    if (pos.getY() < 0 || pos.getX() < 0) {
                        System.err.println("Graph has Negative Values! x: " + pos.getX() + " y: " + pos.getY());
                        errorText.append("Graph has negative Values!\n");
                    }
                    x = (int)Math.round(pos.getX());
                    y = (int)Math.round(pos.getY());
                    nodes.add(new Contest2018IOHandler.Node(Integer.parseInt(n.getTag().toString()), x,y));
                }
            }
        }

        int max_x_dim, max_y_dim;
        //Get Graph Size
        RectD bounds = BoundingBox.from(PositionMap.FromIGraph(graph));
        int graph_width = (int)Math.ceil(bounds.getWidth() < 1 ? 0 : bounds.getWidth());   //smaller than 1 is not a graph
        int graph_height = (int)Math.ceil(bounds.getHeight() < 1 ? 0 : bounds.getHeight());
        //Get Edges
        ArrayList<Contest2018IOHandler.Edge> edges = new ArrayList<>(graph.getEdges().size());
        if (fileOpen()) {
            //from input file
            edges = openFile.edges;
            max_x_dim = openFile.xdimension;
            max_y_dim = openFile.ydimension;
            if (graph_width > openFile.xdimension || graph_height > openFile.ydimension) {
                System.err.println("=====================================================");
                System.err.println("Graph is larger than the maximum allowed dimensions!");
                System.err.println("=====================================================");
                errorText.append("=====================================================\n");
                errorText.append("Graph is larger than allowed!\n");
                errorText.append("=====================================================\n");
                //TODO Scale Graph to fit bounds
            }

        } else {
            //from graph
            for (IEdge e : graph.getEdges()) {
                edges.add(new Contest2018IOHandler.Edge(
                        Integer.parseInt(e.getSourceNode().getTag().toString()),
                        Integer.parseInt(e.getTargetNode().getTag().toString())));
            }
            max_x_dim = MAX_X_DIMENSION;
            max_y_dim = MAX_Y_DIMENSION;
        }

        if (outputTextArea != null) {
            outputTextArea.setText(errorText.toString());
        }


        try(Writer writer = new FileWriter(outputFileName)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(new JSONFile(nodes, edges, max_x_dim, max_y_dim),writer);
        }

        openFile = null;
    }

    /**
     * @return true if a file is currently open in the JSON Contest 2018 format
     */
    public boolean fileOpen(){
        return (openFile != null);
    }


}