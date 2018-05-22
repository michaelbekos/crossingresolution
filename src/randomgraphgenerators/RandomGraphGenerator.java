package randomgraphgenerators;

/*
 * RandomGraphGenerator.java
 *
 * Created on May 6, 2010, 1:05:55 PM
 */

/**
 *
 * @author michael
 */
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.layout.YGraphAdapter;
import com.yworks.yfiles.algorithms.Cycles;

import java.util.Random;


/**
 * A class that creates random graphs. The size of the graph and other options
 * may be specified. These options influence the properties of the created
 * graph.
 *
 */
public class RandomGraphGenerator
{
    private int nodeCount;
    private int edgeCount;
    private boolean allowCycles;
    private boolean allowSelfLoops;
    private boolean allowMultipleEdges;
    private Random random;

    /** Constructs a new random graph generator */
    public RandomGraphGenerator()
    {
        this(System.currentTimeMillis());
    }

    /**
     * Constructs a new random graph generator that uses
     * the given random seed to initialize.
     */
    public RandomGraphGenerator(long seed)
    {
        this.random = new Random(seed);
        this.nodeCount = 30;
        this.edgeCount = 40;
        this.allowSelfLoops = false;
        this.allowCycles = true;
        this.allowMultipleEdges = false;
    }

    /**
     * Sets the random seed for this generator.
     */
    public void setSeed(long seed)
    {
        this.random.setSeed(seed);
    }

    /**
     * Sets the node count of the graph to be generated.
     * The default value is 30.
     */
    public void setNodeCount(int nodeCount)
    {
        this.nodeCount = nodeCount;
    }

    /**
     * Sets the edge count of the graph to be generated.
     * The default value is 40.
     */
    public void setEdgeCount(int edgeCount)
    {
        this.edgeCount = edgeCount;
    }

    /**
     * Returns the edge count of the graph to be generated.
     */
    public int getEdgeCount()
    {
        return this.edgeCount;
    }

    /**
     * Returns the node count of the graph to be generated.
     */
    public int getNodeCount()
    {
        return this.nodeCount;
    }

    /**
     * Whether or not to allow the generation of cyclic graphs, i.e.
     * graphs that contain directed cyclic paths. If allowed
     * it still could happen by chance that the generated
     * graph is acyclic. By default allowed.
     */
    public void allowCycles(boolean allow)
    {
        this.allowCycles = allow;
    }

    /**
     * Returns whether or not to allow the generation of cyclic graphs.
     */
    public boolean allowCycles()
    {
        return allowCycles;
    }

    /**
     * Whether or not to allow the generation of selfloops, i.e.
     * edges with same source and target nodes.
     * If allowed it still could happen by chance that
     * the generated graph contains no selfloops.
     * By default disallowed.
     */
    public void allowSelfLoops(boolean allow)
    {
        this.allowSelfLoops = allow;
    }

    /**
     * Returns whether or not to allow the generation of selfloops.
     */
    public boolean allowSelfLoops()
    {
        return this.allowSelfLoops;
    }

    /**
     * Whether or not to allow the generation of graphs that contain multiple
     * edges.
     */
    public void allowMultipleEdges(boolean allow)
    {
        this.allowMultipleEdges = allow;
    }

    /**
     * Returns whether or not to allow the generation of graphs that contain
     * multiple edges.
     */
    public boolean allowMultipleEdges()
    {
        return this.allowMultipleEdges;
    }

    /**
     * Random graph generator in case multiple edges are allowed.
     */
    public void generate(IGraph graph)
    {
        graph.clear();

        INode[] nodes = new INode[this.nodeCount];

        for (int i = 0; i < this.nodeCount; i++)
        {
            nodes[i] = graph.createNode();
        }

        int noOfFailures = 0;
        int noOfEdges = 0;

        for (;;)
        {
            if (noOfEdges == this.edgeCount || noOfFailures == 1000 * Math.pow(this.nodeCount * this.edgeCount,5))
            {
                break;
            }

            INode source = nodes[this.random.nextInt(this.nodeCount)];
            INode target = nodes[this.random.nextInt(this.nodeCount)];

            if (!this.allowSelfLoops && source == target)
            {
                noOfFailures++;
                continue;
            }

            if (!this.allowMultipleEdges && (graph.getEdge(source, target) != null || graph.getEdge(target, source) != null))
            {
                noOfFailures++;
                continue;
            }

            IEdge e = graph.createEdge(source, target);

            if(!this.allowCycles && ! Cycles.findCycle(new YGraphAdapter(graph).getYGraph(), false).isEmpty())
            {
                noOfFailures++;
                graph.remove(e);
                continue;
            }

            noOfFailures = 0;
            noOfEdges++;

        }
    }
}



