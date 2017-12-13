/*
 * BFS.java
 *
 * Created on 15 November 2007
 */
package algorithms.algo;


/**
 * This class provides services that center around breadth first search (BFS).
 *
 * @author Michael A. Bekos
 */

public class BFS 
{   
    public static y.base.NodeList[] getLayers(y.base.Graph graph, y.base.Node coreNode)
    {
        boolean isdirected = false;
        y.base.NodeMap visitingTimes = graph.createNodeMap();
        y.base.NodeMap finishingTimes = graph.createNodeMap();
        y.base.NodeMap layerIDMap = graph.createNodeMap();
        y.base.NodeMap parentNodes = graph.createNodeMap();
        y.base.NodeList[] result = BFS.getLayers(graph, coreNode, isdirected, visitingTimes, finishingTimes, layerIDMap, parentNodes);
        graph.disposeNodeMap(visitingTimes);
        graph.disposeNodeMap(finishingTimes);
        graph.disposeNodeMap(layerIDMap);
        graph.disposeNodeMap(parentNodes);
        return result;
    }
    
    public static y.base.NodeList[] getLayers(y.base.Graph graph, y.base.Node coreNode, y.base.NodeMap layerIDMap, y.base.NodeMap parentNodes)
    {
        boolean isdirected = false;
        y.base.NodeMap visitingTimes = graph.createNodeMap();
        y.base.NodeMap finishingTimes = graph.createNodeMap();
        y.base.NodeList[] result = BFS.getLayers(graph, coreNode, isdirected, visitingTimes, finishingTimes, layerIDMap, parentNodes);
        graph.disposeNodeMap(visitingTimes);
        graph.disposeNodeMap(finishingTimes);
        return result;
    }
    
    public static y.base.NodeList[] getLayers(y.base.Graph graph, y.base.Node coreNode, boolean isdirected, y.base.NodeMap layerIDMap, y.base.NodeMap parentNodes)
    {
        y.base.NodeMap visitingTimes = graph.createNodeMap();
        y.base.NodeMap finishingTimes = graph.createNodeMap();
        y.base.NodeList[] result = BFS.getLayers(graph, coreNode, isdirected, visitingTimes, finishingTimes, layerIDMap, parentNodes);
        graph.disposeNodeMap(visitingTimes);
        graph.disposeNodeMap(finishingTimes);
        return result;
    }
    
    public static y.base.NodeList[] getLayers(y.base.Graph graph, y.base.Node coreNode, boolean isdirected, y.base.NodeMap visitingTimes, y.base.NodeMap finishingTimes, y.base.NodeMap layerIDMap, y.base.NodeMap parentNodes)
    {       
        y.base.NodeMap nodeColors = graph.createNodeMap();
        for (y.base.NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
        {
            visitingTimes.setInt(nc.node(), -1);
            finishingTimes.setInt(nc.node(), -1);
            layerIDMap.setInt(nc.node(), -1);
            parentNodes.set(nc.node(), null);
            nodeColors.set(nc.node(), java.awt.Color.WHITE);
        }
        int timer = -1;
        visitingTimes.setInt(coreNode, ++timer);
        nodeColors.set(coreNode, java.awt.Color.GRAY);
        layerIDMap.setInt(coreNode, 0);
        
        //q is a FIFO queue
        java.util.LinkedList<y.base.Node> q = new java.util.LinkedList<y.base.Node>();
        q.add(coreNode);
        
        while (!q.isEmpty())
        {
            y.base.Node u = q.removeFirst();
            for (y.base.NodeCursor nc = (isdirected ? u.successors() : u.neighbors()); nc.ok(); nc.next())
            {                
                if (nodeColors.get(nc.node()) == java.awt.Color.WHITE)
                {
                    nodeColors.set(nc.node(), java.awt.Color.GRAY);
                    visitingTimes.setInt(nc.node(),  ++timer);
                    layerIDMap.setInt(nc.node(), layerIDMap.getInt(u)+1);
                    parentNodes.set(nc.node(), u);
                    
                    q.add(nc.node());
                }
            }
            nodeColors.set(u, java.awt.Color.BLACK);
            finishingTimes.setInt(u, ++timer);
        }
        graph.disposeNodeMap(nodeColors);
        
        int maxLayers = 0;
        for (y.base.NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) 
        {
            maxLayers = Math.max(maxLayers, layerIDMap.getInt(nc.node()));
        }        
        y.base.NodeList[] result = new y.base.NodeList[maxLayers+1];
        for (int i=0; i<result.length; i++)
        {
            result[i] = new y.base.NodeList();
        }
        for (y.base.NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) 
        {
            if (nc.node() == coreNode || parentNodes.get(nc.node())!= null)
            {
                result[layerIDMap.getInt(nc.node())].add(nc.node());
            }
        }
        return result;
    }
}
