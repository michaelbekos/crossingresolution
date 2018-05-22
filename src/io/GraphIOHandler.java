package io;

import com.yworks.yfiles.algorithms.Graph;
import java.io.IOException;


/**
 * Created by michael on 7/21/2015.
 */
public abstract class GraphIOHandler {

    /**
     * Reads the contents of the given file and returns a book embedding
     * @param inputFileName the input file
     * @return the graph
     * @throws IOException
     */
    public static Graph read(String inputFileName) throws IOException
    {
        return null;
    }

    /**
     * Writes the contents of the given graph to a stream
     * @param graph the graph
     * @param outputFileName the output file
     * @throws IOException
     */
    public static void write(Graph graph, String outputFileName) throws IOException
    {
        //StringBuffer buffer = new StringBuffer();
        //PrintWriter out = new PrintWriter(outputFileName);
        //out.println(buffer);
        //out.close();
    }
}
