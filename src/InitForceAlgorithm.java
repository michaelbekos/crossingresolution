import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.view.GraphComponent;
import layout.algo.ForceAlgorithmApplier;
import layout.algo.forces.CrossingForce;
import layout.algo.forces.IncidentEdgesForce;
import layout.algo.forces.NodeNeighbourForce;
import layout.algo.forces.NodePairForce;

/**
 * Created by Jessica Wolz on 10.12.16.
 */
public abstract class InitForceAlgorithm {
  public static ForceAlgorithmApplier defaultForceAlgorithmApplier(int iterations, GraphComponent view) {
    ForceAlgorithmApplier fd = new ForceAlgorithmApplier(view, iterations);

    IGraph graph = view.getGraph();

    fd.algos.add(new NodeNeighbourForce(graph, fd));
    fd.algos.add(new NodePairForce(graph, fd));
    fd.algos.add(new CrossingForce(fd, fd.cMinimumAngle, graph));
    fd.algos.add(new IncidentEdgesForce(fd, graph));

    return fd;
  }
}
