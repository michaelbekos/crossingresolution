package layout.algo.forcealgorithm;

import algorithms.graphs.CachedMinimumAngle;
import com.yworks.yfiles.graph.IGraph;
import layout.algo.forcealgorithm.ForceAlgorithm;
import layout.algo.forcealgorithm.ForceAlgorithmConfigurator;
import layout.algo.forcealgorithm.forces.CrossingForce;
import layout.algo.forcealgorithm.forces.IncidentEdgesForce;
import layout.algo.forcealgorithm.forces.NodeNeighbourForce;
import layout.algo.forcealgorithm.forces.NodePairForce;
import layout.algo.layoutinterface.ILayoutInterfaceItemFactory;

/**
 * Created by Jessica Wolz on 10.12.16.
 */
public abstract class DefaultForceAlgorithm {
  public static ForceAlgorithm defaultForceAlgorithm(IGraph graph, ILayoutInterfaceItemFactory itemFactory) {
    CachedMinimumAngle cMinimumAngle = new CachedMinimumAngle();

    ForceAlgorithmConfigurator configurator = new ForceAlgorithmConfigurator();
    configurator.addForce(new NodeNeighbourForce(graph))
        .addForce(new NodePairForce(graph))
        .addForce(new CrossingForce(graph, cMinimumAngle))
        .addForce(new IncidentEdgesForce(graph));
    configurator.init(itemFactory);

    return new ForceAlgorithm(configurator, graph, cMinimumAngle);
  }
}
