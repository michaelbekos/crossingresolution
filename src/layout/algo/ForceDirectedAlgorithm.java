package layout.algo;

import com.yworks.yfiles.algorithms.YPoint;
import com.yworks.yfiles.algorithms.YVector;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.IMapper;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.ICanvasObject;
import com.yworks.yfiles.view.ICanvasObjectDescriptor;
import layout.algo.event.AlgorithmEvent;
import layout.algo.event.AlgorithmListener;
import view.visual.VectorVisual;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

/**
 * This abstract class is implemented a framework for force-directed algorithms, in which several different
 * forces can be present through the method calculateForces(). The implementation is done using the Runnable
 * interface, where AlgorithmListeners are notified during the execution.
 *
 * @author Michael A. Bekos
 */
public abstract class ForceDirectedAlgorithm  implements Runnable
{
    protected GraphComponent view;
    protected IGraph graph;
    protected IMapper<INode, List<YVector>> map;           //Here the forces are saved.
    protected int maxNoOfIterations;                //The maximum number of iterations.
    protected static List<ICanvasObject> canvasObjects = new ArrayList<>();

    //Graph Listeners.
    // A listener can, e.g., interrupt the iteration process, if it detects converge by setting maxNoOfIterations to -1.
    protected List<AlgorithmListener> algorithmListeners;

    /**
     * Constructor of Objects of type ForceDirectedAlgorithm
     * @param view - an object of type Graph2DView
     * @param maxNoOfIterations - the maximum number of iterations
     */
    public ForceDirectedAlgorithm(GraphComponent view, int maxNoOfIterations)
    {
        this.view = view;
        this.graph = view.getGraph();
        this.maxNoOfIterations = maxNoOfIterations;
        this.algorithmListeners = new ArrayList<AlgorithmListener>();
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
        AlgorithmEvent evt = new AlgorithmEvent(this, 0);

        //Notify the listeners that the algorithms is started.
        for (Iterator<AlgorithmListener> it = this.algorithmListeners.iterator(); it.hasNext(); )
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
            catch (InterruptedException exc)
            {
                //Do nothing...
            }
            this.reset();

            //Notify the listeners that the algorithms changed its status.
            for (Iterator<AlgorithmListener> it = this.algorithmListeners.iterator(); it.hasNext(); )
            {
                evt.currentStatus(Math.round(100*i/this.maxNoOfIterations));
                it.next().algorithmStateChanged(evt);
            }
        }

        //Notify the listeners that the algorithms is finished.
        for (Iterator<AlgorithmListener> it = this.algorithmListeners.iterator(); it.hasNext(); )
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
        for (INode u : graph.getNodes())
        {
            YVector vector = new YVector(0,0);
            List<YVector> vectors = (List<YVector>) this.map.getValue(u);

            Iterator<YVector> it = vectors.iterator();
            while (it.hasNext())
            {
                vector.add(it.next());
            }

            double u_x = u.getLayout().getCenter().x;
            double u_y = u.getLayout().getCenter().y;

            YPoint p_u = new YPoint(u_x, u_y);

            p_u = YVector.add(p_u, vector);

            this.view.getGraph().setNodeCenter(u, new PointD(p_u.getX(),p_u.getY()));
        }
        this.view.updateUI();
    }



    /**
     * A method implemented for debugging purposes, which displays the vectors at each vertex.
     */
    protected void displayVectors()
    {
        for (INode u : graph.getNodes())
        {
            YVector vector = new YVector(0,0);
            List<YVector> vectors = (List<YVector>) this.map.getValue(u);

            Iterator<YVector> it = vectors.iterator();
            while (it.hasNext())
            {
                YVector temp = it.next();
                PointD temp2 = new PointD(temp.getX(), temp.getY());
                this.canvasObjects.add(this.view.getBackgroundGroup().addChild(new VectorVisual(this.view, temp2, u, Color.RED), ICanvasObjectDescriptor.VISUAL));
                vector.add(temp);
            }
        }
        this.view.updateUI();
    }

    /**
     * Initiates a run.
     */
    protected void init()
    {
        if (this.map == null)
        {
            this.map = new Mapper<>(new WeakHashMap<>());
            for (INode u : graph.getNodes())
            {
                this.map.setValue(u, new java.util.ArrayList<YVector>());
            }
        }
        this.clearDrawables();
    }

    /**
     * Resets the algorithm after each iteration.
     */
    protected void reset()
    {
        for (INode u : graph.getNodes())
        {
            List<YVector> vectors = (List<YVector>) this.map.getValue(u);
            vectors.clear();
        }
        this.clearDrawables();
    }

    /**
     * Adds a new Algorithm Listener.
     * @param algorithmListener - an algorithm listener
     */
    public void addAlgorithmListener(AlgorithmListener algorithmListener)
    {
        this.algorithmListeners.add(algorithmListener);
    }

    /**
     * Remove an Algorithm Listener.
     * @param algorithmListener - an algorithm listener
     */
    public void removeAlgorithmListener(AlgorithmListener algorithmListener)
    {
        this.algorithmListeners.remove(algorithmListener);
    }

    /**
     * Returns the NodeMap in which the forces are stored.
     * @return - the NodeMap in which the forces are stored.
     */
    public IMapper<INode, List<YVector>> getMap()
    {
        return this.map;
    }

    /**
     * Clear drawables.
     */
    private void clearDrawables()
    {
        for (ICanvasObject o : canvasObjects)
        {
            o.remove();
        }
        canvasObjects.clear();
        this.view.updateUI();
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

