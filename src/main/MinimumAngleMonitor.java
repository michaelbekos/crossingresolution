package main;

import algorithms.graphs.MinimumAngle;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.utils.IEventListener;
import com.yworks.yfiles.utils.ItemEventArgs;
import com.yworks.yfiles.view.GraphComponent;
import layout.algo.utils.BestSolutionMonitor;
import layout.algo.utils.PositionMap;
import util.DisplayMessagesGui;
import util.graph2d.Intersection;

import javax.swing.*;
import java.util.Optional;

public class MinimumAngleMonitor {
  private IGraph graph;
  private JLabel infoLabel;
  private GraphComponent view;
  private double oldAngle;

  private BestSolutionMonitor bestSolution;

  private IEventListener<ItemEventArgs<IEdge>> minimumAngleEdgeCreatedListener = (o, ItemEventArgs) -> showMinimumAngle(graph, view, infoLabel, false);
  private IEventListener<EdgeEventArgs> minimumAngleEdgeRemovedListener = (o, EdgeEventArgs) -> showMinimumAngle(graph, view, infoLabel, false);
  private IEventListener<ItemEventArgs<INode>> minimumAngleNodeCreatedListener = (o, ItemEventArgs) -> showMinimumAngle(graph, view, infoLabel, false);
  private IEventListener<NodeEventArgs> minimumAngleNodeRemovedListener = (o, NodeEventArgs) -> showMinimumAngle(graph, view, infoLabel, false);
  private INodeLayoutChangedHandler minimumAngleLayoutChangedHandler = (o, iNode, rectD) -> showMinimumAngle(graph, view, infoLabel, false);

  MinimumAngleMonitor(GraphComponent view, IGraph graph, JLabel infoLabel, BestSolutionMonitor bestSolution) {
    this.graph = graph;
    this.infoLabel = infoLabel;
    this.view = view;
    this.bestSolution = bestSolution;
    this.oldAngle = 0;
  }

  void showMinimumAngle(IGraph graph, GraphComponent view, JLabel infoLabel, boolean viewCenter) {
    if (!bestSolution.getBestMinimumAngleForNodes(graph.getNodes().size()).isPresent()) {
      oldAngle = 0;
    }
    Mapper<INode, PointD> nodePositions = PositionMap.FromIGraph(graph);
    MinimumAngle.resetHighlighting(graph);
    Optional<Intersection> minAngleCr = MinimumAngle.getMinimumAngleCrossing(graph);

    if (minAngleCr.isPresent()){
      if (oldAngle <= minAngleCr.get().angle){
        oldAngle = minAngleCr.get().angle;

        bestSolution.setBestMinimumAngle(oldAngle, graph.getNodes().size());
        bestSolution.setBestSolutionMapping(nodePositions, graph.getNodes().size());
      }
    }

    Optional<String> labText = minAngleCr.map(cr -> {
      String text = DisplayMessagesGui.createMinimumAngleMsg(cr, graph.getNodes().size(), bestSolution);

      if (viewCenter) {
        view.setCenter(cr.intersectionPoint);
      }
      MinimumAngle.resetHighlighting(graph);
      MinimumAngle.highlightCrossing(cr);

      view.updateUI();

      return text;
    });

    infoLabel.setText(labText.orElse("Graph has no crossings."));
  }

  public void updateMinimumAngleInfoBar() {
    showMinimumAngle(graph, view, infoLabel, false);
  }

  public void registerGraphChangedListeners() {
    graph.addNodeLayoutChangedListener(minimumAngleLayoutChangedHandler);
    graph.addEdgeCreatedListener(minimumAngleEdgeCreatedListener);
    graph.addEdgeRemovedListener(minimumAngleEdgeRemovedListener);
    graph.addNodeCreatedListener(minimumAngleNodeCreatedListener);
    graph.addNodeRemovedListener(minimumAngleNodeRemovedListener);
  }

  public void removeGraphChangedListeners() {
    graph.removeNodeLayoutChangedListener(minimumAngleLayoutChangedHandler);
    graph.removeEdgeCreatedListener(minimumAngleEdgeCreatedListener);
    graph.removeEdgeRemovedListener(minimumAngleEdgeRemovedListener);
    graph.removeNodeCreatedListener(minimumAngleNodeCreatedListener);
    graph.removeNodeRemovedListener(minimumAngleNodeRemovedListener);

    MinimumAngle.resetHighlighting(graph);
  }

  public double getMinimumAngle() {
    return oldAngle;
  }
}
