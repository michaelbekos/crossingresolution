/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.algo;

/**
 *
 * @author Michael
 */
public class ShortestCycle {
    
    private y.base.Graph graph;
    
    public ShortestCycle(y.base.Graph graph)
    {
        this.graph = graph;
    }
    
    public int computeShortestCycle()
    {
        int cycleLength = Integer.MAX_VALUE;
        for (y.base.NodeCursor nc = this.graph.nodes(); nc.ok(); nc.next())
        {
            y.base.NodeMap isCoreNode = graph.createNodeMap();
            isCoreNode.setBool(nc.node(), true);
            
            y.base.NodeList[] bfsLayers = y.algo.Bfs.getLayers(graph, isCoreNode);

            for (int i=0; i<bfsLayers.length; i++)
            {
                boolean found = false;
                for (y.base.NodeCursor nca = bfsLayers[i].nodes(); nca.ok() && !found; nca.next())
                {
                    for (y.base.NodeCursor ncb = bfsLayers[i].nodes(); ncb.ok() && !found; ncb.next())
                    {
                        if (graph.containsEdge(nca.node(), ncb.node()) || graph.containsEdge(ncb.node(), nca.node()))
                        {
                            cycleLength = Math.min(cycleLength, 2*i+1);
                            found = true;
                        }
                        if (i<bfsLayers.length-1 && !found)
                        {
                            for (y.base.NodeCursor ncc = bfsLayers[i+1].nodes(); ncc.ok(); ncc.next())
                            {
                                
                                if (nca.node()!=ncb.node() &&
                                    (graph.containsEdge(nca.node(), ncc.node()) || graph.containsEdge(ncc.node(), nca.node())) &&
                                    (graph.containsEdge(ncb.node(), ncc.node()) || graph.containsEdge(ncc.node(), ncb.node())))
                                {
                                    cycleLength = Math.min(cycleLength, 2*i+2);
                                    found = true;
                                }   
                            }
                        }
                    }
                }
            }
            graph.removeDataProvider(isCoreNode);
        }
        return cycleLength;
    }
    
}
