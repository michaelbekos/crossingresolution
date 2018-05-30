package main;

import algorithms.graphs.AngularResolution;
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
  private double oldMinAngle;
  private double oldAngularRes;
  private double currentCrossingResolution;
  private double currentAngularResolution;
  private boolean useCrossingResolution;
  private boolean useAngularResolution;

  private BestSolutionMonitor bestSolution;

  private IEventListener<ItemEventArgs<IEdge>> minimumAngleEdgeCreatedListener = (o, ItemEventArgs) -> updateAngleInfoBar();
  private IEventListener<EdgeEventArgs> minimumAngleEdgeRemovedListener = (o, EdgeEventArgs) -> updateAngleInfoBar();
  private IEventListener<ItemEventArgs<INode>> minimumAngleNodeCreatedListener = (o, ItemEventArgs) -> updateAngleInfoBar();
  private IEventListener<NodeEventArgs> minimumAngleNodeRemovedListener = (o, NodeEventArgs) -> updateAngleInfoBar();
  private INodeLayoutChangedHandler minimumAngleLayoutChangedHandler = (o, iNode, rectD) -> updateAngleInfoBar();

  //Total Angle Monitor (Crossing+Angular Resolution)
  MinimumAngleMonitor(GraphComponent view, IGraph graph, JLabel infoLabel, BestSolutionMonitor bestSolution) {
    this.graph = graph;
    this.infoLabel = infoLabel;
    this.view = view;
    this.bestSolution = bestSolution;
    this.oldMinAngle = 0;
    this.oldAngularRes = 0;
    this.currentCrossingResolution = 0;
    this.currentAngularResolution = 0;
    this.useCrossingResolution = true;
    this.useAngularResolution = false;
  }

  public Optional<Intersection> computeMinimumAngle() {
    Mapper<INode, PointD> nodePositions = PositionMap.FromIGraph(graph);
    MinimumAngle.resetHighlighting(graph);
    Optional<Intersection> minAngleCr = MinimumAngle.getMinimumAngleCrossing(graph);

    if (minAngleCr.isPresent()){
      currentCrossingResolution = minAngleCr.get().angle;
      if (oldMinAngle <= minAngleCr.get().angle){
        oldMinAngle = minAngleCr.get().angle;

        bestSolution.setBestMinimumAngle(oldMinAngle, graph.getNodes().size());
        bestSolution.setBestSolutionMapping(nodePositions, graph.getNodes().size());
      }
    }
    return  minAngleCr;
  }

  public void showMinimumAngle(IGraph graph, GraphComponent view, JLabel infoLabel, boolean viewCenter) {
    if (!bestSolution.getBestMinimumAngleForNodes(graph.getNodes().size()).isPresent()) {
      oldMinAngle = 0;
    }

    Optional<Intersection> minAngleCr = computeMinimumAngle();

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

  public double computeAngularResolution() {
    Mapper<INode, PointD> nodePositions = PositionMap.FromIGraph(graph);
    double angularRes = AngularResolution.getAngularResolution(graph);
    if (Double.isFinite(angularRes)){
      currentAngularResolution = angularRes;
      if (oldAngularRes <= angularRes){
        oldAngularRes = angularRes;

        bestSolution.setBestAngularResolution(oldAngularRes, graph.getNodes().size());
        bestSolution.setBestSolutionAngularResolutionMapping(nodePositions, graph.getNodes().size());
      }
    }
    return angularRes;
  }

  public void showAngularResolution(IGraph graph, GraphComponent view, JLabel infoLabel, boolean viewCenter) {
    if (!bestSolution.getBestAngularResolutionForNodes(graph.getNodes().size()).isPresent()) {
      oldAngularRes = 0;
    }

    double angularRes = computeAngularResolution();

    Optional<Double> angularResOpt = Optional.of(angularRes);
    Optional<String> labText = angularResOpt.map(cr -> {
      String text = DisplayMessagesGui.createAngularResMsg(angularRes, graph.getNodes().size(), bestSolution);

      view.updateUI();

      return text;
    });

    infoLabel.setText(labText.orElse("Graph has no crossings."));
  }


  public void updateMinimumAngleInfoBar() {
    showMinimumAngle(graph, view, infoLabel, false);
  }

  public void updateAngularResolutionInfoBar() {
    showAngularResolution(graph, view, infoLabel, false);
  }

  public void updateAngleInfoBar() {
    computeMinimumAngle();
    computeAngularResolution();
    if (useAngularResolution && useCrossingResolution) {
      if (currentAngularResolution < currentCrossingResolution) {
        showAngularResolution(graph, view, infoLabel, false);
      } else {
        showMinimumAngle(graph, view, infoLabel, false);
      }
    } else if (useAngularResolution) {
      showAngularResolution(graph, view, infoLabel, false);
    } else {
      showMinimumAngle(graph, view, infoLabel, false);
    }

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
    return oldMinAngle;
  }

  public double getAngularResolution() {
    return oldAngularRes;
  }

  public void setCrossingResolution(boolean value) {
    this.useCrossingResolution= value;
  }

  public void setAngularResolution(boolean value) {
    this.useAngularResolution = value;
  }

  public double getCurrentCrossingResolution() {
    computeMinimumAngle();
    return currentCrossingResolution;
  }

  public double getCurrentAngularResolution() {
    computeAngularResolution();
    return currentAngularResolution;
  }
}
