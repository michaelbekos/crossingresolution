package graphoperations;

import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.view.ISelectionModel;

import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

public final class RemovedNodes {
  private final IGraph graph;
  private final Deque<RemovedNodesSet> removedNodes = new LinkedList<>();

  public RemovedNodes(IGraph graph) {
    this.graph = graph;
  }

  public void removeHighestDegree(int n) {
    removeByComparator(n, Comparator.<INode>comparingInt(graph::degree).reversed());
  }

  public void removeLowestDegree(int n) {
    removeByComparator(n, Comparator.comparingInt(graph::degree));
  }

  public void removeSelected(ISelectionModel<INode> selection) {
    Set<INode> nodesToRemove = graph.getNodes().stream()
        .filter(selection::isSelected)
        .collect(Collectors.toSet());

    removeNodes(nodesToRemove);
  }

  private void removeByComparator(int n, Comparator<INode> comparator) {
    Set<INode> nodesToRemove = graph.getNodes().stream()
        .sorted(comparator)
        .limit(n)
        .collect(Collectors.toSet());

    removeNodes(nodesToRemove);
  }

  private void removeNodes(Set<INode> nodesToRemove) {
    RemovedNodesSet removedNodesSet = new RemovedNodesSet(graph);
    removedNodesSet.removeNodes(nodesToRemove);
    removedNodes.push(removedNodesSet);
  }

  public void reinsertOne() {
    removedNodes.pop().reinsertNodes();
  }

  public void reinsert(int n) {
    for (int i = 0; i < n && !removedNodes.isEmpty(); i++) {
      reinsertOne();
    }
  }

  public void reinsertAll() {
    while (!removedNodes.isEmpty()) {
      reinsertOne();
    }
  }

  public void reinsertSingleNodes(int numVertices) {
    while (numVertices > 0 && !removedNodes.isEmpty()) {
      RemovedNodesSet removedNodesSet = removedNodes.pop();
      Set<INode> insertedNodes = removedNodesSet.reinsertNodes();
      numVertices -= insertedNodes.size();

      if (numVertices < 0) {
        // remove some nodes again if we reinserted too many
        removeNodes(insertedNodes.stream()
            .limit(-numVertices)
            .collect(Collectors.toSet()));
      }
    }
  }

  public boolean isEmpty() {
    return removedNodes.isEmpty();
  }

  public int size() {
    return removedNodes.size();
  }

  public void clear() {
    removedNodes.clear();
  }
}
