package util;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.view.ISelectionModel;

import java.util.*;

public class GraphOperations {

    private static Comparator<INode> byPortSize = Comparator.<INode>comparingInt(p -> p.getPorts().size());

    /**
     * Removes numVertices number of nodes with the highest or lowest degree from graph g
     * @param g - underlying graph
     * @param numVertices - number of vertices to remove
     * @return - returns a stack of the removed vertices
     */
    public static VertexStack removeVertices(IGraph g, boolean removeChains, boolean removeHighestDegree, int numVertices, ISelectionModel<INode> selection, VertexStack removedVertices) {
        int tagNum = 0;
        for (INode u : g.getNodes()) {
            if (u.getTag() == null) {
                u.setTag(tagNum);
            }
            tagNum++;
        }
        if (numVertices == 0) {
            return removedVertices;
        }
        INode[] maxDegVertex = new INode[numVertices];


        if (selection != null) {
            int i = 0;
            for (INode u : g.getNodes()) {
                if (selection.isSelected(u)) {
                    maxDegVertex[i] = u;
                    i++;
                }
            }
        } else {
            int i = 0;
            for (INode u: g.getNodes()) {
                maxDegVertex[i] = u;
                i++;
                if(i >= numVertices){
                    break;
                }
            }
            if (removeChains) {
                ArrayList<ArrayList<INode>> chains = getChains(g);
                //adding chains to stack individually
                if (removedVertices == null) {
                    removedVertices = new VertexStack(g);
                }
                int k = 0;
                int stackedVerticesNum;
                for (ArrayList<INode> chain : chains) {
                    if (k >= numVertices) {
                        break;
                    }
                    k++;
                    stackedVerticesNum = chain.size();
                    for (INode chainNode : chain) {
                        removedVertices.push(chainNode, g);
                        g.remove(chainNode);
                    }
                    removedVertices.componentStack.add(stackedVerticesNum);
                }
                return removedVertices;

            } else if (removeHighestDegree) {
                //fill array maxDegVertex with n largest degree vertices (largest->smallest)
                Arrays.sort(maxDegVertex, byPortSize);
                while (i < g.getNodes().size()) {
                    if (g.getNodes().getItem(i).getPorts().size() > maxDegVertex[maxDegVertex.length - 1].getPorts().size()) {
                        maxDegVertex[maxDegVertex.length - 1] = g.getNodes().getItem(i);
                        Arrays.sort(maxDegVertex, byPortSize);
                    }
                    i++;
                }
            } else {
                //fill array maxDegVertex with n smallest degree vertices (smallest->largest), defaults degree 0,1,2 vertices
                Arrays.sort(maxDegVertex, byPortSize);
                while (i < g.getNodes().size()) {
                    if (g.getNodes().getItem(i).getPorts().size() < maxDegVertex[maxDegVertex.length - 1].getPorts().size()) {
                        maxDegVertex[maxDegVertex.length - 1] = g.getNodes().getItem(i);
                        Arrays.sort(maxDegVertex, byPortSize);
                    }
                    i++;
                }
            }
        }
        //Chains do this separately
        if (removedVertices == null) {
            removedVertices = new VertexStack(g);
        }

        for (int i = 0; i < numVertices; i++) {
            removedVertices.push(maxDegVertex[i], g);
            g.remove(maxDegVertex[i]);
        }
        removedVertices.componentStack.add(numVertices);

        return removedVertices;

    }

    /**
     * Helper function to compute chains
     * @param g - graph
     * @return  - returns a list of chains (nodes with deg <=2)
     */
    public static ArrayList<ArrayList<INode>> getChains(IGraph g) {
        ArrayList<ArrayList<INode>> chains = new ArrayList<>();
        for (INode u : g.getNodes()) {
            if (u.getPorts().size() == 2) {
                ArrayList<INode> newChain = new ArrayList<>();
                if (chains.size() > 0) {
                    boolean shouldContinue = false;
                    for (ArrayList<INode> chain : chains) {
                        if (chain.contains(u)) {
                            shouldContinue = true;
                            break;
                        }
                    }
                    if (shouldContinue) {
                        continue;
                    }
                } else {
                    if (newChain.contains(u)) {
                        continue;
                    }
                }
                newChain.add(u);
                INode neighbor = g.edgesAt(u.getPorts().getItem(0)).first().getSourceNode().equals(u) ?
                        g.edgesAt(u.getPorts().getItem(0)).first().getTargetNode() : g.edgesAt(u.getPorts().getItem(0)).first().getSourceNode();
                while (neighbor != null) {
                    if (neighbor.getPorts().size() == 2) {
                        //already added node in previous loop
                        if (chains.size() > 0) {
                            boolean shouldBreak = false;
                            for (ArrayList<INode> chain : chains) {
                                if (chain.contains(neighbor)) {
                                    shouldBreak = true;
                                    break;
                                }
                            }
                            if (!shouldBreak) {
                                shouldBreak = newChain.contains(neighbor);
                            }
                            if (shouldBreak) {
                                break;
                            }
                        } else {
                            if (newChain.contains(neighbor)) {
                                break;
                            }
                        }
                        newChain.add(neighbor);
                        neighbor = g.edgesAt(neighbor.getPorts().getItem(0)).first().getSourceNode().equals(neighbor) ?
                                g.edgesAt(neighbor.getPorts().getItem(0)).first().getTargetNode() : g.edgesAt(neighbor.getPorts().getItem(0)).first().getSourceNode();
                    } else if (neighbor.getPorts().size() == 1) {
                        newChain.add(neighbor);
                        break;
                    } else {
                        break;
                    }
                }
                //neighbor in the opposite direction
                INode neighbor2 = g.edgesAt(u.getPorts().getItem(1)).first().getSourceNode().equals(u) ?
                        g.edgesAt(u.getPorts().getItem(1)).first().getTargetNode() : g.edgesAt(u.getPorts().getItem(1)).first().getSourceNode();
                while (neighbor2 != null) {
                    if (neighbor2.getPorts().size() == 2) {
                        //already added node in previous loop
                        if (chains.size() > 0) {
                            boolean shouldBreak = false;
                            for (ArrayList<INode> chain : chains) {
                                if (chain.contains(neighbor2)) {
                                    shouldBreak = true;
                                    break;
                                }
                            }
                            if (!shouldBreak) {
                                shouldBreak = newChain.contains(neighbor2);
                            }
                            if (shouldBreak) {
                                break;
                            }
                        } else {
                            if (newChain.contains(neighbor2)) {
                                break;
                            }
                        }
                        newChain.add(neighbor2);
                        neighbor2 = g.edgesAt(neighbor2.getPorts().getItem(1)).first().getSourceNode().equals(neighbor2) ?
                                g.edgesAt(neighbor2.getPorts().getItem(1)).first().getTargetNode() : g.edgesAt(neighbor2.getPorts().getItem(1)).first().getSourceNode();
                    } else if (neighbor2.getPorts().size() == 1)  {
                        newChain.add(neighbor2);
                        break;
                    } else {
                        break;
                    }
                }
                chains.add(newChain);
            }
        }
        return chains;
    }

    /**
     * Reinserts the previously removed nodes
     * @param g - graph
     * @param removedVertices - stack of all removed vertices
     */
    public static VertexStack reinsertVertices(IGraph g, boolean useVertices, int numVerticesComponents, VertexStack removedVertices) {
        int numVertices = 0;
	        if (useVertices) {  //reinsert vertices
	            numVertices = numVerticesComponents;
	            int diff = -numVertices;
	            for (int i = removedVertices.componentStack.size(); i >= 0 ; --i) {         //fix component stack for vertices removed
	                diff += removedVertices.componentStack.get(removedVertices.componentStack.size() - 1);
	                if (diff < 0) {
	                    removedVertices.componentStack.remove(removedVertices.componentStack.size() - 1);
	                } else if (diff == 0) {
	                    removedVertices.componentStack.remove(removedVertices.componentStack.size() - 1);
	                    break;
	                } else {
	                    removedVertices.componentStack.set(removedVertices.componentStack.size() - 1, diff);
	                    break;
	                }
	            }
	        } else {            //reinsert components
	            for (int i = 0; i < numVerticesComponents; i++) {
	                numVertices += removedVertices.componentStack.get(removedVertices.componentStack.size() - 1);
	                removedVertices.componentStack.remove(removedVertices.componentStack.size() - 1);
	            }
	        }
	    

	        INode[] reinsertedNodes = new INode[numVertices];
	        for (int i = 0; i < numVertices; i++) {
	            INode removedNode = removedVertices.pop().vertex;
	            reinsertedNodes[(numVertices - 1) - i] =  g.createNode(removedNode.getLayout().toPointD(), removedNode.getStyle(), removedNode.getTag());
	        }
	
	        Mapper<Integer, INode> tagMap = new Mapper<>(new WeakHashMap<>());
	        for (INode n : g.getNodes()) {
	            tagMap.setValue(Integer.parseInt(n.getTag().toString()), n);
	        }
	        for (INode u : reinsertedNodes) {
	            int tag = Integer.parseInt(u.getTag().toString());
	            for (int i = 0; i < removedVertices.edgeList.length; i++) {
	                if (tag == removedVertices.edgeList[i][0]) {    //u = source node
	                    INode target = tagMap.getValue(removedVertices.edgeList[i][1]);  //find target node with tag
	                    if (target != null && g.getEdge(u, target) == null){
	                        g.createEdge(u, target);
	                    }
	                } else if (tag == removedVertices.edgeList[i][1]) { //u = target node
	                    INode source = tagMap.getValue(removedVertices.edgeList[i][0]);  //find source node with tag
	                    if (source != null && g.getEdge(source, u) == null ){
	                        g.createEdge(source, u);
	                    }
	                }
	            }
	        }
        return removedVertices;
    }




	  /**
     * Multiply the Coord. from each Node with the factor scaleValue
     */
    public static void scaleUpProcess(Mapper<INode, PointD> nodePose, double scaleValue){
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;

        for (Map.Entry<INode, PointD> entry : nodePose.getEntries()) {
            INode node = entry.getKey();
            if (node.getLayout().getCenter().getX() < minX) {
                minX = node.getLayout().getCenter().getX();
            }
            if (node.getLayout().getCenter().getY() < minY) {
                minY = node.getLayout().getCenter().getY();
            }
        }

        for (Map.Entry<INode, PointD> entry : nodePose.getEntries()) {
            INode node = entry.getKey();
            PointD center = node.getLayout().getCenter();
            nodePose.setValue(node, new PointD(
                (center.getX() - minX) * scaleValue,
                (center.getY() - minY) * scaleValue)
            );
        }
    }

}
