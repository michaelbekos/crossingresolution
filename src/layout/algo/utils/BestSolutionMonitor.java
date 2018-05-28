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

    private Map<Integer, Mapper<INode, PointD>> bestSolutionAngularResolutionMapping = new Hashtable<>();
    private Map<Integer, Double> bestSolutionAngularResolution = new Hashtable<>();

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

        if (bestSolutionAngularResolutionMapping.size() != 0){
            bestSolutionAngularResolutionMapping.clear();
        }
        if (bestSolutionAngularResolution.size() != 0){
            bestSolutionAngularResolution.clear();
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

    public void setBestAngularResolution(double angularRes, int nodes) {
        bestSolutionAngularResolution.put(nodes,angularRes);
    }

    public void setBestSolutionAngularResolutionMapping(Mapper<INode, PointD> bestSolution, int nodes) {
        bestSolutionAngularResolutionMapping.put(nodes,bestSolution);
    }

    public Optional<Double> getBestAngularResolutionForNodes(int nodes) {
        return Optional.ofNullable(bestSolutionAngularResolution.get(nodes));
    }

    public Optional<Mapper<INode, PointD>> getBestSolutionAngularResolutionPositions(int nodes) {
        return Optional.ofNullable(bestSolutionAngularResolutionMapping.get(nodes));
    }

    public Optional<Double> getBestTotalResolutionForNodes(int nodes) {
        if (bestSolutionAngularResolution.get(nodes) < bestSolutionMinimumAngle.get(nodes)) {
            return Optional.ofNullable(bestSolutionAngularResolution.get(nodes));
        } else {
            return Optional.ofNullable(bestSolutionMinimumAngle.get(nodes));
        }
    }

    public Optional<Mapper<INode, PointD>> getBestSolutionTotalResolutionPositions(int nodes) {
        if (bestSolutionAngularResolution.get(nodes) < bestSolutionMinimumAngle.get(nodes)) {
            return Optional.ofNullable(bestSolutionAngularResolutionMapping.get(nodes));
        } else {
            return Optional.ofNullable(bestSolutionAngularResolutionMapping.get(nodes));
        }
    }
}
