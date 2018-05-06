package graphoperations;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is meant for single use only!
 */
final class RemovedNodesSet {
  private final IGraph graph;
  private final Map<String, Set<String>> edges = new HashMap<>();
  private final LinkedHashSet<String> nodes = new LinkedHashSet<>();
  private final Map<String, PointD> positions = new HashMap<>();

  RemovedNodesSet(IGraph graph) {
    this.graph = graph;
  }

  public void removeNodes(Set<INode> nodesToRemove) {
    for (INode node : nodesToRemove) {
      String tag = node.getTag().toString();
      nodes.add(tag);
      positions.put(tag, node.getLayout().getCenter());

      Set<String> neighbors = graph.neighbors(INode.class, node).stream()
          .map(n -> n.getTag().toString())
          .collect(Collectors.toSet());
      edges.put(tag, neighbors);

      graph.edgesAt(node).stream()
          .collect(Collectors.toList()) // we need a copy since we're removing from the original collection
          .forEach(graph::remove);
      graph.remove(node);
    }
  }

  public Set<INode> reinsertNodes() {
    Set<INode> insertedNodes = new HashSet<>();
    Map<String, INode> allNodes = new HashMap<>();

    graph.getNodes().forEach(node -> allNodes.put(node.getTag().toString(), node));

    for (String tag : nodes) {
      INode node = graph.createNode();
      node.setTag(tag);
      graph.setNodeCenter(node, positions.get(tag));
      insertedNodes.add(node);
      allNodes.put(tag, node);
    }

    for (String tag : nodes) {
      INode node = allNodes.get(tag);
      for (String neighborTag : edges.get(tag)) {
        INode neighbor = allNodes.get(neighborTag);

        if (graph.getEdge(node, neighbor) == null) {
          graph.createEdge(node, neighbor);
        }
      }
    }

    return insertedNodes;
  }

  public Set<INode> reinsertNodesLine(ArrayList<INode> startEnd) {
    Set<INode> insertedNodes = new HashSet<>();
    Map<String, INode> allNodes = new HashMap<>();

    graph.getNodes().forEach(node -> allNodes.put(node.getTag().toString(), node));

    int i = 0;
    INode first = startEnd.get(1);
    INode end = startEnd.get(2);

    for (String tag : nodes) {
      double diff_x = first.getLayout().getCenter().getX() - end.getLayout().getCenter().getX();
      double diff_y = first.getLayout().getCenter().getY() - end.getLayout().getCenter().getY();
      INode node = graph.createNode();
      node.setTag(tag);
      graph.setNodeCenter(node, new PointD(end.getLayout().getCenter().getX()+(diff_x / (nodes.size() + 1)) * i, end.getLayout().getCenter().getY()+(diff_y / (nodes.size() + 1)) * i));
      insertedNodes.add(node);
      allNodes.put(tag, node);
      i++;
    }

    for (String tag : nodes) {
      INode node = allNodes.get(tag);
      for (String neighborTag : edges.get(tag)) {
        INode neighbor = allNodes.get(neighborTag);

        if (graph.getEdge(node, neighbor) == null) {
          graph.createEdge(node, neighbor);
        }
      }
    }

    return insertedNodes;
  }

  int getSize() {
    return nodes.size();
  }
}
