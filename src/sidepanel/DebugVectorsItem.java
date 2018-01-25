package sidepanel;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.ICanvasObject;
import com.yworks.yfiles.view.ICanvasObjectDescriptor;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;
import view.visual.VectorVisual;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DebugVectorsItem extends AbstractLayoutInterfaceItem<Mapper<INode, PointD>> {
  private GraphComponent view;
  private List<ICanvasObject> canvasObjects = new LinkedList<>();

  DebugVectorsItem(String name, GraphComponent view) {
    super(name);
    this.view = view;
  }

  @Override
  public void setValue(Mapper<INode, PointD> vectors) {
    super.setValue(vectors);

    if (vectors == null) {
      for (ICanvasObject canvasObject: canvasObjects) {
        canvasObject.remove();
      }
      canvasObjects.clear();
    } else {
      for (Map.Entry<INode, PointD> vector : vectors.getEntries()) {
        VectorVisual vectorVisual = new VectorVisual(view, vector.getValue(), vector.getKey(), Color.GREEN);
        ICanvasObject canvasObject = view.getBackgroundGroup().addChild(vectorVisual, ICanvasObjectDescriptor.VISUAL);
        canvasObjects.add(canvasObject);
      }
    }

    this.view.updateUI();
  }
}
