package layout.algo;

import algorithms.graphs.CachedMinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import layout.algo.utils.PositionMap;
import util.Tuple4;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Serves as an immediate class to put everything static that should be refactored to something that is worth to be
 * called code
 */
public class TrashCan {

  // keep track of a bestSolution across all ForceAlgorithms
  public static Tuple4<Mapper<INode, PointD>, Optional<Double>, Double[], Boolean[]> bestSolution = null;

  // call this whenever the underlying graph changes structurally!
  public static void init(){
    bestSolution = null;
  }

  /**
   * improveSolution: check current node positions and last best positions (metric: minimum crossing angle). If better, update.
   */
  public static void improveSolution(CachedMinimumAngle cMinimumAngle, IGraph graph, Double[] modifiers, Mapper<INode, PointD> nodePositions, Boolean[] switches){
    Mapper<INode, PointD> sol = nodePositions;
    Optional<Double> solutionAngle = cMinimumAngle.getMinimumAngle(graph, sol);
    /* best solution is a tuple of:
     * - nodePositions :: Mapper (INode, PointD)
     * - crossingAngle (if any) :: Optional Double
     * - force parameters :: [Double]
     * - force switches :: [Bool]
     */
    // do it lazy, since we might not need to
    Supplier<Tuple4<Mapper<INode, PointD>, Optional<Double>, Double[], Boolean[]>> thisSol
      = (() -> new Tuple4<>(PositionMap.copy(sol), solutionAngle, modifiers.clone(), switches.clone()));
    // if we hadn't had a previous best yet, update with our
    if (bestSolution == null) {
      bestSolution = thisSol.get();
      return;
    }
    // otherwise: previous best exists. get it...
    Tuple4<Mapper<INode, PointD>, Optional<Double>, Double[], Boolean[]> prevBest = bestSolution;
    // ... if previous one had no crossings, we can't be better, so we stop.
    if(prevBest.b == null || !prevBest.b.isPresent()) return;
    // ... otherwise, if this one has no crossings, it must be better, so we update.
    if(!solutionAngle.isPresent()){
      bestSolution = thisSol.get();
      return;
    }
    // ... if the previous angle was better, we return.
    if(prevBest.b.get() >= solutionAngle.get()) return;
    // ... otherwise, ours is better, so we update.
    bestSolution = thisSol.get();

  }
}
