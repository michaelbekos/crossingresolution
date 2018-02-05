package util;

import com.yworks.yfiles.graph.IGraph;

import layout.algo.utils.BestSolution;
import util.graph2d.Intersection;

/**
 * Created by Jessica Wolz on 24.11.16.
 */
public class DisplayMessagesGui {

    public static String createNumberEdgesVertices(IGraph graph){
        return "Number of Vertices: " + graph.getNodes().size() + "     Number of Edges: " + graph.getEdges().size();
    }
    public static String createMinimumAngleMsg(Intersection currCross, int nodes) {

        String text = "Minimum Angle: " + currCross.angle.toString();
        text += " | Stored Best: " + BestSolution.getBestMinimumAngle(nodes);
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
}
