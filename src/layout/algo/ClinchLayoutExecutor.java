package layout.algo;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.view.GraphComponent;

import javax.swing.*;
import java.util.Map;

public class ClinchLayoutExecutor extends GenericAlgorithmExecutor{

    private final ILayout layout;
    private final int numberOfCyclesBetweenViewUpdates;


    public ClinchLayoutExecutor (GraphComponent view, JProgressBar progressBar, int maxIterations, ILayout layout, int numberOfCyclesBetweenViewUpdates) {
        super(view, progressBar, maxIterations);
        this.layout = layout;
        this.numberOfCyclesBetweenViewUpdates = numberOfCyclesBetweenViewUpdates;
    }

    private void updateViewAlgo(Mapper<INode, PointD> nodePositions) {
        synchronized (super.graph) {
            for (Map.Entry<INode, PointD> entry : nodePositions.getEntries()) {
                super.graph.setNodeCenter(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void initializeAlgorithm() {
        layout.init();
    }

    @Override
    public void updateStepAlgorithm() {
        boolean finished = layout.executeStep(super.currentIteration);
        if (finished) {
            super.stopRunning();
        }

        if (super.currentIteration % numberOfCyclesBetweenViewUpdates == 0) {
            updateViewAlgo(layout.getNodePositions());
        }
    }

    @Override
    public void finishAlgorithm() {
        updateViewAlgo(layout.getNodePositions());
    }


}
