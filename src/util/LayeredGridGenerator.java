package util;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;

/**
 * Created by Jessica Wolz on 03.02.17.
 */
public class LayeredGridGenerator {

    public static void generate(IGraph g, int xCount, int yCount, int layers){
        g.clear();
        layers=2;
        INode[][] nodes = new INode[xCount*layers][];
        for(int l = 1; l <= layers; l++) {
            // create nodes in x & y dimension
            for (int x = 0; x < xCount; x++) {
                nodes[x*l] = new INode[yCount];
                for (int y = 0; y < yCount; y++) {
                    PointD pos = new PointD(500 + 50 * x - 25 * l, 500 + 50 * y + 25 * l);
                    INode n = g.createNode(pos);
                    nodes[x*l][y] = n;
                }
            }
            // create edges in x dimension
            for (int x = 0; x < xCount - 1; x++) {
                for (int y = 0; y < yCount; y++) {
                    INode source = nodes[x*l][y],
                            target = nodes[(x + 1)*l][y];
                    g.createEdge(source, target);
                }
            }
            // create edges in y dimension
            for (int x = 0; x < xCount; x++) {
                for (int y = 0; y < yCount - 1; y++) {
                    INode source = nodes[x*l][y],
                            target = nodes[x*l][(y + 1)];
                    g.createEdge(source, target);
                }
            }
        }

        for(INode n : g.getNodes()){
            System.out.println(n + " " + n.getLayout().getCenter());
        }

        for(int l = 1; l < layers; l++) {
            if (layers > 1) {
                // create edges between layers
                for (int x = 0; x < xCount; x++) {
                    for (int y = 0; y < yCount; y++) {
                        INode source = nodes[x * l][y],
                                target = nodes[x * (l + 1)][y];
                        g.createEdge(source, target);

                    }
                }
            }
        }

    }
}
