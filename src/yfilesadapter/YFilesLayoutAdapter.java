package yfilesadapter;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.LayoutUtilities;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.layout.ILayoutAlgorithm;
import layout.algo.ILayout;
import layout.algo.utils.PositionMap;

public class YFilesLayoutAdapter implements ILayout {
  private IGraph graph;
  private ILayoutAlgorithm layout;

  public YFilesLayoutAdapter(IGraph graph, ILayoutAlgorithm layout) {
    this.graph = graph;
    this.layout = layout;
  }

  @Override
  public void init() {}

  @Override
  public boolean executeStep(int iteration, int maxIterations) {
    LayoutUtilities.applyLayout(graph, layout);
    return true;
  }

  @Override
  public Mapper<INode, PointD> getNodePositions() {
    return PositionMap.FromIGraph(graph);
  }

  @Override
  public void showDebug() {}

  @Override
  public void clearDebug() {}
}
