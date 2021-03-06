package graphoperations;

import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;

public final class RemovedChains {
  private final IGraph graph;
  private final Deque<RemovedNodesSet> removedChains = new LinkedList<>();

  public RemovedChains(IGraph graph) {
    this.graph = graph;
  }

  void remove(Set<Set<INode>> chains) {
    for (Set<INode> chain : chains) {
      RemovedNodesSet removedNodesSet = new RemovedNodesSet(graph);
      removedNodesSet.removeNodes(chain);
      removedChains.push(removedNodesSet);
    }
  }

  public void remove(Set<Set<INode>> chains, int chainNum) {
    int counter = 0;
    for (Set<INode> chain : chains) {
      if (counter == chainNum) {
        break;
      }
      RemovedNodesSet removedNodesSet = new RemovedNodesSet(graph);
      removedNodesSet.removeNodes(chain);
      removedChains.push(removedNodesSet);
      counter++;
    }
  }

  public void reinsert(int chainNum) {
    for (int i = 0; i < chainNum; i++) {
        removedChains.pop().reinsertNodes();
    }
  }

  public void reinsertOne() {
    removedChains.pop().reinsertNodes();
  }

  public void reinsertAll() {
    while (!removedChains.isEmpty()) {
      reinsertOne();
    }
  }

  public int number() {
    return removedChains.size();
  }

  public boolean isEmpty() {
    return removedChains.isEmpty();
  }

  public void clear() {
    removedChains.clear();
  }
}
