package layout.algo.layoutinterface;

import java.util.ArrayList;

public interface ILayoutConfigurator {
  void init(ILayoutInterfaceItemFactory itemFactory);
  ArrayList<AbstractLayoutInterfaceItem> getAbstractLayoutInterfaceItems();
}
