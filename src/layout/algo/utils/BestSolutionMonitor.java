package layout.algo.utils;

import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;


public class BestSolutionMonitor {
    private Map<Integer, Mapper<INode, PointD>> bestSolutionMapping = new Hashtable<>();
    private Map<Integer, Double> bestSolutionMinimumAngle = new Hashtable<>();

    public BestSolutionMonitor() {
        reset();
    }

    //call to reset the BestSolutionMonitor (e.g. loading new graph)
    public void reset() {
        if (bestSolutionMapping.size() != 0){
            bestSolutionMapping.clear();
        }
        if (bestSolutionMinimumAngle.size() != 0){
            bestSolutionMinimumAngle.clear();
        }
    }

    public void setBestMinimumAngle(double minAngle, int nodes) {
        bestSolutionMinimumAngle.put(nodes,minAngle);
    }

    public void setBestSolutionMapping(Mapper<INode, PointD> bestSolution, int nodes) {
        bestSolutionMapping.put(nodes,bestSolution);
    }

    public Optional<Double> getBestMinimumAngleForNodes(int nodes) {
        return Optional.ofNullable(bestSolutionMinimumAngle.get(nodes));
    }

    public Optional<Mapper<INode, PointD>> getBestSolutionPositions(int nodes) {
        return Optional.ofNullable(bestSolutionMapping.get(nodes));
    }
}
