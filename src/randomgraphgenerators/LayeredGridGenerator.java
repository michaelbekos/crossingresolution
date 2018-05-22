package randomgraphgenerators;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;

/**
 * Created by Jessica Wolz on 03.02.17.
 */
public class LayeredGridGenerator {

    public static void generate(IGraph g, int xCount, int yCount, int layers){
        int offset = 100;
        int o = offset;
        int gridOffset = offset / layers;
        int go = gridOffset;
        g.clear();
        INode[][][] nodes = new INode[layers][][];
        for(int l = 0; l < layers; l++) {
            nodes[l] = new INode[xCount][];
            // create nodes in x & y dimension
            for (int x = 0; x < xCount; x++) {
                nodes[l][x] = new INode[yCount];
                for (int y = 0; y < yCount; y++) {
                    PointD pos = new PointD(500 + o * x + go * l, 500 + o * y - go * l);
                    INode n = g.createNode(pos);
                    nodes[l][x][y] = n;
                }
            }
            // create edges in x dimension
            for (int x = 0; x < xCount - 1; x++) {
                for (int y = 0; y < yCount; y++) {
                    INode source = nodes[l][x][y],
                            target = nodes[l][x+1][y];
                    g.createEdge(source, target);
                }
            }
            // create edges in y dimension
            for (int x = 0; x < xCount; x++) {
                for (int y = 0; y < yCount - 1; y++) {
                    INode source = nodes[l][x][y],
                            target = nodes[l][x][y+1];
                    g.createEdge(source, target);
                }
            }
        }

        for(INode n : g.getNodes()){
            System.out.println(n + " " + n.getLayout().getCenter());
        }

        for(int l = 0; l < layers - 1; l++) {
                // create edges between layers
                for (int x = 0; x < xCount; x++) {
                    for (int y = 0; y < yCount; y++) {
                        INode source = nodes[l][x][y],
                                target = nodes[l+1][x][y];
                        g.createEdge(source, target);

                    }
                }
            
        }

    }
}
