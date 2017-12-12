import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.utils.IEventListener;
import com.yworks.yfiles.utils.ItemEventArgs;
import com.yworks.yfiles.view.GraphComponent;
import util.DisplayMessagesGui;
import util.graph2d.Intersection;

import javax.swing.*;
import java.util.Optional;

public class MinimumAngleMonitor {
  private IGraph graph;
  private JLabel infoLabel;
  private GraphComponent view;

  private IEventListener<ItemEventArgs<IEdge>> minimumAngleEdgeCreatedListener = (o, ItemEventArgs) -> showMinimumAngle(graph, view, infoLabel);
  private IEventListener<EdgeEventArgs> minimumAngleEdgeRemovedListener = (o, EdgeEventArgs) -> showMinimumAngle(graph, view, infoLabel);
  private IEventListener<ItemEventArgs<INode>> minimumAngleNodeCreatedListener = (o, ItemEventArgs) -> showMinimumAngle(graph, view, infoLabel);
  private IEventListener<NodeEventArgs> minimumAngleNodeRemovedListener = (o, NodeEventArgs) -> showMinimumAngle(graph, view, infoLabel);
  private INodeLayoutChangedHandler minimumAngleLayoutChangedHandler = (o, iNode, rectD) -> showMinimumAngle(graph, view, infoLabel);

  MinimumAngleMonitor(GraphComponent view, IGraph graph, JLabel infoLabel) {
    this.graph = graph;
    this.infoLabel = infoLabel;
    this.view = view;
  }

  void showMinimumAngle(IGraph graph, GraphComponent view, JLabel infoLabel) {
    MinimumAngle.resetHighlighting(graph);
    Optional<Intersection>
        minAngleCr = MinimumAngle.getMinimumAngleCrossing(graph);

    Optional<String> labText = minAngleCr.map(cr -> {
      String text = DisplayMessagesGui.createMinimumAngleMsg(cr);

      MinimumAngle.resetHighlighting(graph);
      MinimumAngle.highlightCrossing(cr);

      view.updateUI();

      return text;
    });

    infoLabel.setText(labText.orElse("Graph has no crossings."));
  }

  void registerGraphChangedListeners() {
    graph.addNodeLayoutChangedListener(minimumAngleLayoutChangedHandler);
    graph.addEdgeCreatedListener(minimumAngleEdgeCreatedListener);
    graph.addEdgeRemovedListener(minimumAngleEdgeRemovedListener);
    graph.addNodeCreatedListener(minimumAngleNodeCreatedListener);
    graph.addNodeRemovedListener(minimumAngleNodeRemovedListener);
  }

  void removeGraphChangedListeners() {
    graph.removeNodeLayoutChangedListener(minimumAngleLayoutChangedHandler);
    graph.removeEdgeCreatedListener(minimumAngleEdgeCreatedListener);
    graph.removeEdgeRemovedListener(minimumAngleEdgeRemovedListener);
    graph.removeNodeCreatedListener(minimumAngleNodeCreatedListener);
    graph.removeNodeRemovedListener(minimumAngleNodeRemovedListener);

    MinimumAngle.resetHighlighting(graph);
  }
}
