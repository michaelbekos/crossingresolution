package util;

import com.yworks.yfiles.graph.IGraph;
import graphoperations.GraphOperations;
import layout.algo.utils.BestSolutionMonitor;
import util.graph2d.Intersection;

/**
 * Created by Jessica Wolz on 24.11.16.
 */
public class DisplayMessagesGui {

    public static String createNumberEdgesVertices(IGraph graph){
        return "Number of Vertices: " + graph.getNodes().size() + "     Number of Edges: " + graph.getEdges().size();
    }
    public static String createMinimumAngleMsg(Intersection currCross, int nodes, BestSolutionMonitor bestSolution) {

        String text = "Min. Angle: " + currCross.angle.toString();
        text += " | Best: " + bestSolution.getBestMinimumAngleForNodes(nodes).orElse(Double.POSITIVE_INFINITY);
        text += " | Nodes: " + currCross.segment1.n1.getLabels().first().getText();
        text += " , " +  currCross.segment1.n2.getLabels().first().getText();
        text += " | " +  currCross.segment2.n1.getLabels().first().getText();
        text += " , " +  currCross.segment2.n2.getLabels().first().getText();
        return text;
    }

    /**
     * Creates Message for Popup at end of iterations, which holds the maximal
     * minimum angle after x iterations.
     * @param angle - maximal minimum angle
     * @param iterations - after iterations which created maximal minimum angle
     * @return text - the generated text for the pop up message
     */
    public static String createMaxMinAngleMsg(double angle, double iterations) {
        String text = "Maximal Minimum Angle: " + angle +
                " after " + (iterations +1) + " iterations.";
        return text;
    }

    public static String createAngularResMsg(double angularRes, int nodes, BestSolutionMonitor bestSolution) {
        String text = "Angular Resolution: " + angularRes;
        text += " | Stored Best: " + bestSolution.getBestAngularResolutionForNodes(nodes).orElse(Double.POSITIVE_INFINITY);
        return text;
    }

    public static String createAspectRatioMsg(IGraph graph, GraphOperations.AspectRatio aspectRatio) {
        return "Aspect Ratio: " + aspectRatio.getValue() +
                " | Min: " + (int)aspectRatio.getShortestEdgeLength() +
                " | Max.: " + (int)aspectRatio.getLongestEdgeLength();
    }
}
