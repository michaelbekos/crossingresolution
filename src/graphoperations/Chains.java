package graphoperations;

import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


public final class Chains {
  private final IGraph graph;
  private final ArrayList<LinkedHashSet<INode>> chains = new ArrayList<>();
  private final ArrayList<ArrayList<INode>> startEnd = new ArrayList<>();

  private Chains(IGraph graph) {
    this.graph = graph;
    analyze();
  }

  public static Chains analyze(IGraph graph) {
    return new Chains(graph);
  }

  public int number() {
    return chains.size();
  }

  public RemovedChains remove() {
    RemovedChains removedChains = new RemovedChains(graph);
    removedChains.remove(chains);
    return removedChains;
  }

  public RemovedChains remove(int numChains, RemovedChains removedChains) {
    if (removedChains == null || removedChains.number() == 0) {
      removedChains = new RemovedChains(graph);
    }
    removedChains.remove(chains, numChains);
    return removedChains;
  }

  private void analyze() {
    Set<INode> nodesInChain = new HashSet<>();

    for (INode node : graph.getNodes()) {
      followChain(node, chains, nodesInChain, null, null);
    }
  }

  private void followChain(INode node, ArrayList<LinkedHashSet<INode>> chains, Set<INode> nodesInChain, LinkedHashSet<INode> currentChain, ArrayList<INode> startEndNode) {
    if (graph.degree(node) != 2) {
        if (!nodesInChain.contains(node) && currentChain != null) {
            startEndNode.add(node); //add starting and ending nodes of chain in graph
            startEnd.add(startEndNode);
        }
      return;
    }

    if (nodesInChain.contains(node)) {
      return;
    }

    nodesInChain.add(node);

    if (currentChain == null) {
      currentChain = new LinkedHashSet<>();
      chains.add(currentChain);
        startEndNode = new ArrayList<>();
        startEndNode.add(node);
    }

    currentChain.add(node);

    for (INode neighbor : graph.neighbors(INode.class, node)) {
      followChain(neighbor, chains, nodesInChain, currentChain, startEndNode);
    }
  }

  public ArrayList<ArrayList<INode>>  getStartEnd() {
      return startEnd;
  }
}
