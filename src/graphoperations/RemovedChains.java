package graphoperations;

import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;

import java.util.*;

public final class RemovedChains {
  private final IGraph graph;
  private final Deque<RemovedNodesSet> removedChains = new LinkedList<>();
  private ArrayList<ArrayList<INode>> startEnd;

  public RemovedChains(IGraph graph) {
    this.graph = graph;
  }

  void remove(ArrayList<LinkedHashSet<INode>> chains) {
    for (Set<INode> chain : chains) {
      RemovedNodesSet removedNodesSet = new RemovedNodesSet(graph);
      removedNodesSet.removeNodes(chain);
      removedChains.push(removedNodesSet);
    }
  }

  public void remove(ArrayList<LinkedHashSet<INode>> chains, int chainNum) {
    int counter = 0;
    for (LinkedHashSet<INode> chain : chains) {
      if (counter == chainNum) {
        break;
      }
      RemovedNodesSet removedNodesSet = new RemovedNodesSet(graph);
      removedNodesSet.removeNodes(chain);
      removedChains.push(removedNodesSet);
      counter++;
    }
  }

  //lineMode: reinsert vertices on a line between the endpoint vertices
  public ArrayList<Set<INode>> reinsert(int chainNum, boolean lineMode) {
    ArrayList<Set<INode>> chains = new ArrayList<>();
    for (int i = 0; i < chainNum; i++) {
//        removedChains.pop().reinsertNodes();
      if (lineMode) {
        chains.add(removedChains.pop().reinsertNodesLine(startEnd.get(i)));
      } else {
        chains.add(removedChains.pop().reinsertNodes());
      }
    }
    return chains;
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

  public void setStartEnd(ArrayList<ArrayList<INode>> startEnd) {
    this.startEnd = startEnd;
  }

}
