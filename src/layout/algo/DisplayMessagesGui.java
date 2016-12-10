package layout.algo;

import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import util.Maybe;
import util.Tuple3;
import util.graph2d.Intersection;
import util.graph2d.LineSegment;

import javax.sound.sampled.Line;

/**
 * Created by khokhi on 24.11.16.
 */
public class DisplayMessagesGui {

    public static String createNumberEdgesVertices(IGraph graph){
        return "Number of Vertices: " + graph.getNodes().size() + "     Number of Edges: " + graph.getEdges().size();
    }
    public static String createMinimumAngleMsg(Tuple3<LineSegment, LineSegment, Intersection> currCross) {

        String text = "Minimum Angle: " + currCross.c.angle.toString();
        if(currCross.a.n1.hasValue() && currCross.b.n1.hasValue()){
            text += " | Nodes: " + currCross.a.n1.get().getLabels().first().getText();
            text += " , " +  currCross.a.n2.get().getLabels().first().getText();
            text += " | " +  currCross.b.n1.get().getLabels().first().getText();
            text += " , " +  currCross.b.n2.get().getLabels().first().getText();
        }
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

    /**
     * Creates Message for Minimal Edge length of Graph
     * @param length - holds minimal edge length
     */
    public static String createEdgeLengthMsg(double length, LineSegment edge){
        String text = "Minimal Edgelength: " + length
                        + "| Nodes " + edge.p1 + "," + edge.p2;
        return text;
    }
}
