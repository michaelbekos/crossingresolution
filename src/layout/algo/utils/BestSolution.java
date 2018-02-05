package layout.algo.utils;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;

public class BestSolution {
    private Mapper<INode, PointD> bestSolutionMapping;
    private Double bestSolutionMinimumAngle;

    public BestSolution() {
        reset();
    }

    //call init to reset the BestSolution (e.g. loading new graph)
    public void reset() {
        bestSolutionMapping = null;
        bestSolutionMinimumAngle = null;
    }

    public void setBestMinimumAngle(double minAngle) {
        bestSolutionMinimumAngle = minAngle;
    }

    public void setBestSolutionMapping(Mapper<INode, PointD> bestSolution) {
        bestSolutionMapping = bestSolution;
    }

    public Double getBestMinimumAngle() {
        return bestSolutionMinimumAngle;
    }

    public Mapper<INode, PointD> getBestSolutionMapping() {
        return bestSolutionMapping;
    }
}
