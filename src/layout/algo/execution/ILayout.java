package layout.algo.execution;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;

import java.util.Set;

/**
 * This is the basic interface for algorithms that work on an {@link com.yworks.yfiles.graph.IGraph} and shall be
 * executed by a {@link BasicIGraphLayoutExecutor}.
 *
 * Note that, although an instance of {@link com.yworks.yfiles.graph.IGraph} may be passed to the constructor, you
 * should NOT directly update the positions of the nodes of the IGraph. Instead, you should work on an instance of
 * {@link Mapper} and return it in the {@link #getNodePositions()} method.
 */
public interface ILayout {
  /**
   * Initializes the algorithm. Will be called before the algorithm starts and before any other method of this interface.
   * It may be called multiple times, so ensure that this method completely resets every state left over from a previous
   * run. You should also put all memory-allocating code in here (and not in the constructor), as some time may pass
   * between creating an instance of the algorithm and actually running it.
   */
  void init();

  /**
   * Sets a set of nodes that shall NOT be moved by the algorithm but might be taken into consideration while computing
   * positions for the other nodes.
   * @param fixNodes a set of {@link INode}s that shall not be moved by the algorithm
   */
  void setFixNodes(Set<INode> fixNodes);

  /**
   * Performs one iteration of the algorithm. Make sure you keep the operation as short as possible.
   * @param iteration The current iteration
   * @return true iff the algorithm is finished
   */
  boolean executeStep(int iteration);

  /**
   * Returns a map of the current positions of the nodes.
   * @return a map of the current positions of the nodes
   */
  Mapper<INode, PointD> getNodePositions();

  /**
   * Performs some cleanup actions or final computations. Will be called once after the last call to
   * {@link #executeStep(int)}. The default implementation is empty.
   * @param lastIteration the number of the last iteration (the same that was passed to the last call of
   *                      {@link #executeStep(int)})
   */
  default void finish(int lastIteration) {}

  /**
   * Shows some debug information.
   */
  default void showDebug() {}

  /**
   * Clears the debug information.
   */
  default void clearDebug() {}
}
