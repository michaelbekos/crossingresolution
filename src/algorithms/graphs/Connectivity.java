package algorithms.graphs;

import com.yworks.yfiles.graph.*;

import java.util.ArrayList;

/**
 * Created by Ama on 16.12.2017.
 */
public class Connectivity {

    //no need for instantiation...
    private Connectivity() {

    }

    public static ArrayList<IEdge> triangulatePlanarGraph(IGraph graph) throws IllegalArgumentException {

        // get start embedding to work with
        int n = graph.getNodes().size();
        PlanarInformation plan = new PlanarInformation(graph);
        CombinatorialEmbedder emb = new CombinatorialEmbedder();
        emb.setPlanarInformation(plan);
        emb.embed();
        if(n < graph.N()) {
            removeInsertedEdges(graph, plan);
            throw new IllegalArgumentException("Input Graph is not planar!");
        }
        ArrayList<IEdge> insertedEdges = makeGraphBiconnectedPlanar(graph, plan, true);

        // calculate new embedding

        emb.dispose();
        plan = new PlanarInformation(graph);
        emb.setPlanarInformation(plan);
        emb.embed();

        // for every face check if it is triangulated, if not triangulate it
        for (FaceCursor cursor = plan.faces(); cursor.ok(); cursor.next()) {
            triangulateFace(cursor.face(), graph, insertedEdges);
        }

        // clean up
        removeInsertedEdges(graph, plan);
        return insertedEdges;
    }
}


