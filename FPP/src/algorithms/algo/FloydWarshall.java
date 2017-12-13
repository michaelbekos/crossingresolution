/*
 for each edge (u,v)
    dist[u][v] ← w(u,v)  // the weight of the edge (u,v)
for k from 1 to |V|
    for i from 1 to |V|
        for j from 1 to |V|
            if dist[i][k] + dist[k][j] < dist[i][j] then
                dist[i][j] ← dist[i][k] + dist[k][j]
 */
package algorithms.algo;

/**
 *
 * @author Michael
 */
public class FloydWarshall 
{
    private y.base.Graph graph;
    private double[] cost;
    
    public FloydWarshall(y.base.Graph graph)
    {
        this(graph, new double[graph.edgeCount()]);
        for (int i=0; i<this.cost.length; i++) 
        {
            cost[i] = 1;
        }
    }
    
    public FloydWarshall(y.base.Graph graph, double[] cost)
    {
        this.graph = graph;
        this.cost = cost;
    }
    
    public double[][] calculateDistances() throws Exception
    {
        if (this.graph.nodeCount()==0 || this.graph.edgeCount() == 0)
        {
            throw new Exception("The input graph is empty");
        }
        
        double[][] dist = new double[this.graph.nodeCount()][this.graph.nodeCount()];
        
        for (int i=0; i<dist.length; i++)
        {
            for (int j=0; j<dist[i].length; j++)
            {
                dist[i][j] = Double.MAX_VALUE;
            }
        }
        
        for (y.base.EdgeCursor ec = this.graph.edges(); ec.ok(); ec.next())
        {
            dist[ec.edge().source().index()][ec.edge().target().index()] = cost[ec.edge().index()];
            dist[ec.edge().target().index()][ec.edge().source().index()] = cost[ec.edge().index()]; 
        }
        
        for (y.base.NodeCursor k = this.graph.nodes(); k.ok(); k.next())
        {
            for (y.base.NodeCursor i = this.graph.nodes(); i.ok(); i.next())
            {
                for (y.base.NodeCursor j = this.graph.nodes(); j.ok(); j.next())
                {
                    if (dist[i.node().index()][k.node().index()] + dist[k.node().index()][j.node().index()] < dist[i.node().index()][j.node().index()])
                    {
                        dist[i.node().index()][j.node().index()] = dist[i.node().index()][k.node().index()] + dist[k.node().index()][j.node().index()];                        
                    }
                }
            }
        }
        return dist;
    }    
}
