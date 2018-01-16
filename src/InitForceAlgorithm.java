import com.sun.istack.internal.Nullable;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.view.GraphComponent;
import layout.algo.ForceAlgorithmApplier;
import layout.algo.forces.CrossingForce;
import layout.algo.forces.IncidentEdgesForce;
import layout.algo.forces.NodeNeighbourForce;
import layout.algo.forces.NodePairForce;

import javax.swing.*;

/**
 * Created by Jessica Wolz on 10.12.16.
 */
public abstract class InitForceAlgorithm {
  public static ForceAlgorithmApplier defaultForceAlgorithmApplier(int iterations, GraphComponent view) {
    return defaultForceAlgorithmApplier(iterations, view, null, null);
  }

  public static ForceAlgorithmApplier defaultForceAlgorithmApplier(int iterations, GraphComponent view, @Nullable JProgressBar progressBar, @Nullable JLabel infoLabel) {
    ForceAlgorithmApplier fd = new ForceAlgorithmApplier(view, iterations, progressBar, infoLabel);

    IGraph graph = view.getGraph();

    fd.algos.add(new NodeNeighbourForce(graph, fd));
    fd.algos.add(new NodePairForce(graph, fd));
    fd.algos.add(new CrossingForce(fd, fd.cMinimumAngle, graph));
    fd.algos.add(new IncidentEdgesForce(fd, graph));

    return fd;
  }
}
