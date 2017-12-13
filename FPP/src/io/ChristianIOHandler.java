package io;

import y.base.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Michael on 25/6/2016.
 */
public class ChristianIOHandler extends GraphIOHandler
{

    /**
     * Reads the contents of the given file and returns a book embedding
     * @param inputFileName the input file
     * @return the graph
     * @throws java.io.IOException
     */
    public static Graph read(String inputFileName) throws IOException
    {
        String line;
        StringBuffer content = new StringBuffer();
        BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
        while((line = reader.readLine()) != null)
        {
            content.append(line).append("\n");
        }
        reader.close();

        Graph g = new Graph();
        Node[] nodes = new Node[0];
        int[][] edges = new int[0][0];

        String[] lines = content.toString().split("\\n+");
        for (int l =0; l < lines.length; l++) {
            String[] source = lines[l].split("\\s+");
            if (l == 0) {
                nodes = new Node[Integer.parseInt(source[2])];
                edges = new int[Integer.parseInt(source[3])][Integer.parseInt(source[3])];
                for (int i = 0; i < nodes.length; i++) {
                    nodes[i] = g.createNode();
                }
            }
            else
            {

                String[] target = source[1].split(",");
                for (int i = 0; i < target.length; i++)
                {
                    //Symmetry
                    edges[Integer.parseInt(source[0])-1][Integer.parseInt(target[i])-1] = 1;
                    edges[Integer.parseInt(target[i])-1][Integer.parseInt(source[0])-1] = 1;
                }
            }
        }
        for (int i=0; i<edges.length; i++)
        {
            for (int j=edges.length-1; j>i; j--)
            {
                if (edges[i][j] == 1)
                {
                    g.createEdge(nodes[i], nodes[j]);
                }
            }
        }
        return g;
    }

    /**
     * Writes the contents of the given graph to a stream
     * @param graph the graph
     * @param outputFileName the output file
     * @throws java.io.IOException
     */
    public static void write(Graph graph, String outputFileName) throws IOException
    {
        StringBuffer output = new StringBuffer();
        output.append("p be " + graph.N() + " " + graph.E() + "\n");
        y.base.NodeMap map = graph.createNodeMap();
        int k = 1;
        for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
        {
            map.setInt(nc.node(), k);
            k++;
        }
        boolean[][] edges = new boolean[graph.N()+1][graph.N()+1];
        for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
        {
            edges[map.getInt(ec.edge().source())][map.getInt(ec.edge().target())] = true;
            edges[map.getInt(ec.edge().target())][map.getInt(ec.edge().source())] = true;
        }

        for (int i=1; i<edges.length; i++)
        {
            for (int j=edges.length-1; j>i; j--)
            {
                if (edges[i][j]) {
                    output.append(i + " " + j + "\n");
                }
            }
        }
        PrintWriter out = new PrintWriter(outputFileName);
        out.println(output);
        out.close();
    }
}
