package layout.algo.utils;

import java.util.Hashtable;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;


public class BestSolution {
    private Hashtable<Integer, Mapper<INode, PointD>> bestSolutionMapping= new Hashtable<Integer, Mapper<INode, PointD>>();
    private static Hashtable<Integer, Double> bestSolutionMinimumAngle= new Hashtable<Integer, Double>();

    public BestSolution() {
        reset();
    }

    //call init to reset the BestSolution (e.g. loading new graph)
    public void reset() {
    	if (bestSolutionMapping.size()!=0){
        bestSolutionMapping.clear();
    		}
    	if (bestSolutionMinimumAngle.size()!=0){
            bestSolutionMinimumAngle.clear();
        	}
    }

    public void setBestMinimumAngle(double minAngle, int nodes) {
        bestSolutionMinimumAngle.put(nodes,minAngle);
    }

    public void setBestSolutionMapping(Mapper<INode, PointD> bestSolution, int nodes) {
        bestSolutionMapping.put(nodes,bestSolution);
    }

    public static Double getBestMinimumAngle(int nodes) {
        return bestSolutionMinimumAngle.get(nodes);
    }

    public Mapper<INode, PointD> getBestSolutionMapping(int nodes) {
        return bestSolutionMapping.get(nodes);
    }
}
