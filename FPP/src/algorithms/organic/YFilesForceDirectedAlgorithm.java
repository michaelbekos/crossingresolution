/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package algorithms.organic;

/**
 *
 * @author fouli
 */
public class YFilesForceDirectedAlgorithm
{
    protected y.view.Graph2DView view;
    protected y.view.Graph2D graph;

    public YFilesForceDirectedAlgorithm(y.view.Graph2DView view)
    {
        this.view = view;
        this.graph = view.getGraph2D();
    }

    public void start()
    {
        y.layout.organic.SmartOrganicLayouter sol = new y.layout.organic.SmartOrganicLayouter();  
        sol.setNodeSizeAware(false);
        sol.setPreferredEdgeLength(150);

        new y.layout.BufferedLayouter(sol).doLayout(graph);
        view.fitContent();
        view.updateView();
    }

}
