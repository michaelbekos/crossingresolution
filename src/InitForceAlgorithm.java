import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.view.GraphComponent;
import layout.algo.ForceAlgorithm;
import layout.algo.forces.CrossingForce;
import layout.algo.forces.IncidentEdgesForce;
import layout.algo.forces.NodeNeighbourForce;
import layout.algo.forces.NodePairForce;

/**
 * Created by Jessica Wolz on 10.12.16.
 */
public abstract class InitForceAlgorithm {
  public static ForceAlgorithm defaultForceAlgorithm(int iterations, GraphComponent view) {
    ForceAlgorithm fd = new ForceAlgorithm(view, iterations);

    IGraph graph = view.getGraph();

    fd.algos.add(new NodeNeighbourForce(graph, fd));
    fd.algos.add(new NodePairForce(graph, fd));
    fd.algos.add(new CrossingForce(fd, fd.cMinimumAngle, graph));
    fd.algos.add(new IncidentEdgesForce(fd, graph));

    return fd;
  }
}
