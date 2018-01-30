package sidepanel;

import com.yworks.yfiles.view.IGraphSelection;
import com.yworks.yfiles.view.input.GraphEditorInputMode;
import layout.algo.layoutinterface.AbstractLayoutInterfaceItem;

public class SelectionItem extends AbstractLayoutInterfaceItem<IGraphSelection> {
  private GraphEditorInputMode graphEditorInputMode;

  SelectionItem(String name, GraphEditorInputMode graphEditorInputMode) {
    super(name);
    this.graphEditorInputMode = graphEditorInputMode;
  }

  @Override
  public IGraphSelection getValue() {
    return graphEditorInputMode.getGraphSelection();
  }
}
