package graphoperations;

import java.util.*;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.utils.IListEnumerable;

import algorithms.graphs.MinimumAngle;
import layout.algo.utils.PositionMap;
import util.graph2d.Intersection;


public final class Chains {
  private final IGraph graph;
  private final Set<Set<INode>> chains = new HashSet<>();

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

  private void analyze() {
    Set<INode> nodesInChain = new HashSet<>();

    for (INode node : graph.getNodes()) {
      followChain(node, chains, nodesInChain, null);
    }
  }

  private void followChain(INode node, Set<Set<INode>> chains, Set<INode> nodesInChain, HashSet<INode> currentChain) {
    if (graph.degree(node) != 2) {
      return;
    }

    if (nodesInChain.contains(node)) {
      return;
    }

    nodesInChain.add(node);

    if (currentChain == null) {
      currentChain = new HashSet<>();
      chains.add(currentChain);
    }

    currentChain.add(node);

    for (INode neighbor : graph.neighbors(INode.class, node)) {
      followChain(neighbor, chains, nodesInChain, currentChain);
    }
  }
}
