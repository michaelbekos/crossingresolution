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
  private double bestCrossingResolution;
  private double bestAngularResolution;
  private double bestTotalResolution;
  private double currentCrossingResolution;
  private double currentAngularResolution;
  private double currentTotalResolution;
  private boolean useCrossingResolution;
  private boolean useAngularResolution;
  private boolean useAspectRatio;

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
    this.bestCrossingResolution = 0;
    this.bestAngularResolution = 0;
    this.bestTotalResolution = 0;
    this.currentCrossingResolution = 0;
    this.currentAngularResolution = 0;
    this.currentTotalResolution = 0;
    this.useCrossingResolution = true;
    this.useAngularResolution = false;
    this.useAspectRatio = true;
  }

  public Optional<Intersection> computeCrossingResolution() {
    Mapper<INode, PointD> nodePositions = PositionMap.FromIGraph(graph);
    MinimumAngle.resetHighlighting(graph);
    Optional<Intersection> minAngleCr = MinimumAngle.getMinimumAngleCrossing(graph);

    if (minAngleCr.isPresent()){
      currentCrossingResolution = minAngleCr.get().angle;
      if (bestCrossingResolution <= minAngleCr.get().angle){
        bestCrossingResolution = minAngleCr.get().angle;

        bestSolution.setBestMinimumAngle(bestCrossingResolution, graph.getNodes().size());
        bestSolution.setBestSolutionMapping(nodePositions, graph.getNodes().size());
      }
    }
    return  minAngleCr;
  }

  public void showCrossingResolution(IGraph graph, GraphComponent view, JLabel infoLabel, boolean viewCenter) {
    if (!bestSolution.getBestMinimumAngleForNodes(graph.getNodes().size()).isPresent()) {
      bestCrossingResolution = 0;
    }

    Optional<Intersection> minAngleCr = computeCrossingResolution();

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
      if (bestAngularResolution <= angularRes){
        bestAngularResolution = angularRes;

        bestSolution.setBestAngularResolution(bestAngularResolution, graph.getNodes().size());
        bestSolution.setBestSolutionAngularResolutionMapping(nodePositions, graph.getNodes().size());
      }
    }
    return angularRes;
  }

  public void showAngularResolution(IGraph graph, GraphComponent view, JLabel infoLabel, boolean viewCenter) {
    if (!bestSolution.getBestAngularResolutionForNodes(graph.getNodes().size()).isPresent()) {
      bestAngularResolution = 0;
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


  public void updateCrossingResolutionInfoBar() {
    showCrossingResolution(graph, view, infoLabel, false);
  }

  public void updateAngularResolutionInfoBar() {
    showAngularResolution(graph, view, infoLabel, false);
  }

  public void updateAngleInfoBar() {
    computeCrossingResolution();
    computeAngularResolution();
    if (useAspectRatio) {   //TODO
        String text = DisplayMessagesGui.createAspectRatioMsg(graph);
        infoLabel.setText(text);
        return;
    }
    if (useAngularResolution && useCrossingResolution) {
      if (currentAngularResolution < currentCrossingResolution) {
        showAngularResolution(graph, view, infoLabel, false);
      } else {
        showCrossingResolution(graph, view, infoLabel, false);
      }
    } else if (useAngularResolution) {
      showAngularResolution(graph, view, infoLabel, false);
    } else {
      showCrossingResolution(graph, view, infoLabel, false);
    }

  }

  public double computeTotalResolution() {
    computeCrossingResolution();
    computeAngularResolution();

    System.out.println("COMPUTE Total Resolution:  cross "  + this.currentCrossingResolution);
    System.out.println("COMPUTE Total Resolution:   andular  "  +  this.currentAngularResolution );
    if (useAngularResolution && useCrossingResolution) {
      if (currentAngularResolution < currentCrossingResolution) {
        this.currentTotalResolution = currentAngularResolution;
      } else {
        this.currentTotalResolution = currentCrossingResolution;
      }
    } else if (useAngularResolution) {
      this.currentTotalResolution = currentAngularResolution;
    } else {
      this.currentTotalResolution = currentCrossingResolution;
    }

    System.out.println("COMPUTE Total Resolution:   total   "  +  this.currentTotalResolution);

    if(bestTotalResolution <= this.currentTotalResolution){
      this.bestTotalResolution = this.currentTotalResolution;
    }

    System.out.println("COMPUTE Total Resolution:   old crossing"  + this.bestCrossingResolution);
    System.out.println("COMPUTE Total Resolution:   old angular"  +  this.bestAngularResolution);
    System.out.println("COMPUTE Total Resolution:   old total"  + this.bestTotalResolution);

    return this.currentTotalResolution;
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

  public double getBestCrossingResolution() {
    return bestCrossingResolution;
  }

  public double getBestAngularResolution() {return bestAngularResolution;}

  public double getBestTotalResolution() {
    return bestTotalResolution;
  }

  public void setUseCrossingResolution(boolean value) {
    this.useCrossingResolution= value;
  }

  public void setUseAngularResolution(boolean value) {
    this.useAngularResolution = value;
  }

  public void setUseAspectRatio(boolean value) {
      this.useAspectRatio = value;
  }

  public void setBestCrossingResolution(double bestCrossingResolution) {
    this.bestCrossingResolution = bestCrossingResolution;
  }

  public void setBestAngularResolution(double bestAngularResolution) {
    this.bestAngularResolution = bestAngularResolution;
  }

  public void setBestTotalResolution(double bestTotalResolution) {
    this.bestTotalResolution = bestTotalResolution;
  }

  public double getCurrentCrossingResolution() {
    computeCrossingResolution();
    return currentCrossingResolution;
  }

  public double getCurrentAngularResolution() {
    computeAngularResolution();
    return currentAngularResolution;
  }

  public double getCurrentTotalResolution() {
    computeAngularResolution();
    computeCrossingResolution();
    return Math.min(currentAngularResolution, currentCrossingResolution);
  }
}
