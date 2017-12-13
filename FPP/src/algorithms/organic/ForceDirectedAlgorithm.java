package algorithms.organic;

/**
 * This abstract class is implemented a framework for force-directed algorithms, in which several different
 * forces can be present through the method calculateForces(). The implementation is done using the Runnable
 * interface, where AlgorithmListeners are notified during the execution.
 *
 * @author Michael A. Bekos
 */
public abstract class ForceDirectedAlgorithm  implements Runnable
{
    protected y.view.Graph2DView view;
    protected y.view.Graph2D graph;
    protected y.base.NodeMap map;           //Here the forces are saved.
    protected int maxNoOfIterations;        //The maximum number of iterations.

    //Graph Listeners.
    // A listener can, e.g., interrupt the iteration process, if it detects converge by setting maxNoOfIterations to -1.
    protected java.util.ArrayList<event.AlgorithmListener> algorithmListeners;

    /**
     * Constructor of Objects of type ForceDirectedAlgorithm
     * @param view - an object of type Graph2DView
     * @param maxNoOfIterations - the maximum number of iterations
     */
    public ForceDirectedAlgorithm(y.view.Graph2DView view, int maxNoOfIterations)
    {
        this.view = view;
        this.graph = view.getGraph2D();
        this.maxNoOfIterations = maxNoOfIterations;
        this.algorithmListeners = new java.util.ArrayList<event.AlgorithmListener>();
    }

    /**
     * Abstract method to calculate the vectors.
     * Subclasses must implement this method.
     */
    public abstract void calculateVectors();

    /**
     * Execute the algorithm
     */
    public void run()
    {
        event.AlgorithmEvent evt = new event.AlgorithmEvent(this, 0);

        //Notify the listeners that the algorithms is started.
        for (java.util.Iterator<event.AlgorithmListener> it = this.algorithmListeners.iterator(); it.hasNext(); )
        {
            it.next().algorithmStarted(evt);
        }

        // Just for debugging purposes, to display the vectors.
        if (this.maxNoOfIterations == 0)
        {
            this.init();
            this.calculateVectors();
            this.displayVectors();
        }

        for (int i=0; i<this.maxNoOfIterations; i++)
        {
            this.init();
            this.calculateVectors();
            this.draw();

            try
            {
                Thread.sleep(1);
            }
            catch (java.lang.InterruptedException exc)
            {
                //Do nothing...
            }
            this.reset();

            //Notify the listeners that the algorithms changed its status.
            for (java.util.Iterator<event.AlgorithmListener> it = this.algorithmListeners.iterator(); it.hasNext(); )
            {
                evt.currentStatus(Math.round(100*i/this.maxNoOfIterations));
                it.next().algorithmStateChanged(evt);
            }
        }

        //Notify the listeners that the algorithms is finished.
        for (java.util.Iterator<event.AlgorithmListener> it = this.algorithmListeners.iterator(); it.hasNext(); )
        {
            evt.currentStatus(100);
            it.next().algorithmFinished(evt);
        }
    }

    /**
     * Draw the result by adding the vectors of each node.
     */
    protected void draw()
    {
        for (y.base.NodeCursor u = graph.nodes(); u.ok(); u.next())
        {
            y.geom.YVector vector = new y.geom.YVector(0,0);
            java.util.ArrayList<y.geom.YVector> vectors = (java.util.ArrayList<y.geom.YVector>) this.map.get(u.node());
            
            java.util.Iterator<y.geom.YVector> it = vectors.iterator();
            while (it.hasNext())
            {
                vector.add(it.next());
            }

            double u_x = this.graph.getRealizer(u.node()).getCenterX();
            double u_y = this.graph.getRealizer(u.node()).getCenterY();

            y.geom.YPoint p_u = new y.geom.YPoint(u_x, u_y);

            p_u = y.geom.YVector.add(p_u, vector);

            this.graph.getRealizer(u.node()).setCenterX(p_u.x);
            this.graph.getRealizer(u.node()).setCenterY(p_u.y);
        }
        this.view.updateView();
    }



    /**
     * A method implemented for debugging purposes, which displays the vectors at each vertex.
     */
    protected void displayVectors()
    {
        for (y.base.NodeCursor u = graph.nodes(); u.ok(); u.next())
        {
            y.geom.YVector vector = new y.geom.YVector(0,0);
            java.util.ArrayList<y.geom.YVector> vectors = (java.util.ArrayList<y.geom.YVector>) this.map.get(u.node());

            java.util.Iterator<y.geom.YVector> it = vectors.iterator();
            while (it.hasNext())
            {
                y.geom.YVector temp = it.next();               
                view.addDrawable(new layout.VectorDrawable(this.view, temp, u.node(), java.awt.Color.RED));

                vector.add(temp);
            }
        }
        this.view.updateView();
    }

    /**
     * Initiates a run.
     */
    protected void init()
    {
        if (this.map == null)
        {
            this.map = this.graph.createNodeMap();
            for (y.base.NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
            {
                this.map.set(nc.node(), new java.util.ArrayList<y.geom.YVector>());
            }
        }
        this.clearDrawables();
    }

    /**
     * Resets the algorithm after each iteration.
     */
    protected void reset()
    {
        for (y.base.NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
        {
            java.util.ArrayList<y.geom.YVector> vectors = (java.util.ArrayList<y.geom.YVector>) this.map.get(nc.node());
            vectors.clear();
        }
    }

    /**
     * Adds a new Algorithm Listener.
     * @param algorithmListener - an algorithm listener
     */
    public void addAlgorithmListener(event.AlgorithmListener algorithmListener)
    {
        this.algorithmListeners.add(algorithmListener);
    }

    /**
     * Remove an Algorithm Listener.
     * @param algorithmListener - an algorithm listener
     */
    public void removeAlgorithmListener(event.AlgorithmListener algorithmListener)
    {
        this.algorithmListeners.remove(algorithmListener);
    }

    /**
     * Returns the NodeMap in which the forces are stored.
     * @return - the NodeMap in which the forces are stored.
     */
    public y.base.NodeMap getMap()
    {
        return this.map;
    }

   /**
    * Clear drawables.
    */
   private void clearDrawables()
   {
       for (java.util.Iterator<y.view.Drawable> it = this.view.getDrawables().iterator(); it.hasNext() ;)
        {
            this.view.removeDrawable(it.next());
        }
        this.view.updateView();
   }

    /**
     * Returns the maximum number of iterations
     * @return - the maximum number of iterations
     */
    public int getMaxNoOfIterations() {
        return maxNoOfIterations;
    }

    /**
     * Sets the maximum number of iterations
     * @param maxNoOfIterations - the maximum number of iterations
     */
    public void setMaxNoOfIterations(int maxNoOfIterations) {
        this.maxNoOfIterations = maxNoOfIterations;
    }
}
