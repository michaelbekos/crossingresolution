import algorithms.graphs.CachedMinimumAngle;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.view.GraphComponent;
import layout.algo.ForceAlgorithm;
import layout.algo.ForceAlgorithmConfigurator;
import layout.algo.forces.CrossingForce;
import layout.algo.forces.IncidentEdgesForce;
import layout.algo.forces.NodeNeighbourForce;
import layout.algo.forces.NodePairForce;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

/**
 * Created by Jessica Wolz on 10.12.16.
 */
public abstract class InitForceAlgorithm {
  public static ForceAlgorithm defaultForceAlgorithm(GraphComponent view, ILayoutInterfaceItemFactory sidePanelItemFactory) {
    ForceAlgorithmConfigurator configurator = new ForceAlgorithmConfigurator();
    configurator.init(sidePanelItemFactory);

    CachedMinimumAngle cMinimumAngle = new CachedMinimumAngle();
    ForceAlgorithm fd = new ForceAlgorithm(configurator, view, cMinimumAngle);

    IGraph graph = view.getGraph();

    fd.forces.add(new NodeNeighbourForce(configurator, graph));
    fd.forces.add(new NodePairForce(configurator, graph));
    fd.forces.add(new CrossingForce(configurator, graph, cMinimumAngle));
    fd.forces.add(new IncidentEdgesForce(configurator, graph));

    return fd;
  }
}
