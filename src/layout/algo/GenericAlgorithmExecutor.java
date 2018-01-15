package layout.algo;

import com.yworks.yfiles.graph.ICompoundEdit;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.view.GraphComponent;

import javax.swing.*;

public abstract class GenericAlgorithmExecutor{
    private final GraphComponent view;
    public final IGraph graph;
    private final JProgressBar progressBar;
    private final int maxIterations;
    private boolean running;
    private boolean finished;
    public int currentIteration;
    private ICompoundEdit compoundEdit;
    public IGraph bestSolution; //TODO

    /*  example constructor for class ExampleAlgorithmExecutor (extends GenericAlgorithmExecutor) and implements initializeAlgorithm(), updateStepAlgorithm(), finishAlgorithm():

            public ExampleAlgorithmExecutor (GraphComponent view, JProgressBar progressBar, int maxIterations) {
                super(view, progressBar, maxIterations);
            }

        example running the ExampleAlgorithm:
            new ExampleAlgorithmExecutor(this.view, this.progressBar, 1000).run();
     */

    public GenericAlgorithmExecutor(GraphComponent view, JProgressBar progressBar, int maxIterations) {
        this.view = view;
        this.graph = this.view.getGraph();
        this.progressBar = progressBar;
        this.maxIterations = maxIterations;
        this.currentIteration = 0;
        this.running = false;
        this.finished = false;
    }

    public abstract void initializeAlgorithm();     //initialization step for algorithm (before running)

    public abstract void updateStepAlgorithm();     //one iteration (update step) in the algorithm

    public abstract void finishAlgorithm();         //cleanup after algorithm has finished/stopped

    public void startRunning() {
        if (!this.running) {
            this.running = true;
            synchronized (this.graph) {
                this.compoundEdit = this.graph.beginEdit("Undo layout", "Redo layout");
            }
        }
    }

    public void stopRunning() {
        if (this.running) {
            synchronized (this.graph) {
                this.compoundEdit.commit();
            }
            updateProgressBar(0);
            finishAlgorithm();
            this.running = false;
            this.finished = true;
        }
    }

    public void pauseRunning() {
        this.running = !this.running;
    }

    private void updateProgressBar(int iteration) {
        synchronized (progressBar) {
            progressBar.setValue((int) (100 * iteration / (float) maxIterations));
        }
    }

    private void update() {
        updateProgressBar(this.currentIteration);
        updateStepAlgorithm();
    }

    public void run() {
        //iterations < 0 infinite loop
        //iterations > 0 runs for # of iterations
        new Thread(() -> {
            startRunning();     //remove when start/stop button exists
            initializeAlgorithm();
            while (!this.finished) {
                while (this.running) {
                    if (maxIterations < 0) {
                        update();
                    } else if (maxIterations > 0) {
                        currentIteration++;
                        update();
                        if (currentIteration == maxIterations) {
                            stopRunning();
                        }
                    }
                }
            }
        }).start();
    }

    //best result

}
