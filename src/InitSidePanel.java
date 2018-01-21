import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.GraphItemTypes;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.LayoutUtilities;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.layout.orthogonal.OrthogonalLayout;
import layout.algo.ForceAlgorithmApplier;
import layout.algo.GeneticAlgorithm;
import layout.algo.IGraphLayoutExecutor;
import layout.algo.TrashCan;
import layout.algo.utils.PositionMap;
import util.GraphOperations;
import util.Tuple4;
import util.interaction.ThresholdSliders;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.LinkedList;
import java.util.Optional;

import static layout.algo.TrashCan.bestSolution;

public class InitSidePanel {
    private MainFrame mainFrame;

    public InitSidePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public JPanel initSidePanel(JPanel mainPanel, GridBagConstraints c) {
        Tuple4<JPanel, JSlider[], JSpinner[], Integer> slidPanelSlidersCount = ThresholdSliders.create(mainFrame.springThresholds, new String[]{"Electric force", " ", "Crossing force", "Incident edges force"});
        JPanel sidePanel = slidPanelSlidersCount.a;
        mainFrame.sliders = slidPanelSlidersCount.b;
        slidPanelSlidersCount.c[1].setVisible(false);
        mainFrame.sliders[1].setVisible(false);
        int sidePanelNextY = slidPanelSlidersCount.d;
        c.gridy = 1;
        c.gridx = 1;
        c.weighty = 1;
        c.weightx = 0.2;
        c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.VERTICAL;
        mainPanel.add(sidePanel, c);
        //mainPanel.add(sliders, BorderLayout.LINE_END);
        GridBagConstraints cSidePanel = new GridBagConstraints();
        //WARNING: POST-INCREMENT!
        cSidePanel.gridy = sidePanelNextY++;
        cSidePanel.gridx = 0;
        sidePanel.add(new JLabel("Genetic FAA round time"), cSidePanel);
        JSlider geneticSlider = new JSlider(0, 1000);
        geneticSlider.setSize(0, 500);
        geneticSlider.setValue(mainFrame.faaRunningTimeGenetic);
        geneticSlider.addChangeListener(e-> {
            JSlider source = (JSlider) e.getSource();
            mainFrame.faaRunningTimeGenetic = source.getValue();
            System.out.println("magic number:" + mainFrame.faaRunningTimeGenetic);
        });
        cSidePanel.gridx = 0;
        cSidePanel.gridy = sidePanelNextY++;
        sidePanel.add(geneticSlider, cSidePanel);
        cSidePanel.gridy = sidePanelNextY++;
        JButton startGenetic = new JButton("Start genetic algo"),
                stopGenetic  = new JButton("Stop genetic algo");
        startGenetic.addActionListener(this::startGeneticClicked);
        stopGenetic.addActionListener(this::stopGeneticClicked);
        sidePanel.add(startGenetic, cSidePanel);
        cSidePanel.gridx = 1;
        sidePanel.add(stopGenetic, cSidePanel);
        cSidePanel.gridy = sidePanelNextY++;

        cSidePanel.gridx = 0;
        cSidePanel.gridy = sidePanelNextY++;

        JRadioButton forceDirectionPerpendicular = new JRadioButton("Perpendicular"),
                forceDirectionNonPerpendicular = new JRadioButton("Non Perpendicular");
        cSidePanel.gridx = 0;
        sidePanel.add(forceDirectionPerpendicular,cSidePanel);
        forceDirectionPerpendicular.setSelected(true);
        forceDirectionPerpendicular.addActionListener(this::forceDirectionPerpendicularActionPerformed);
        cSidePanel.gridx = 1;
        sidePanel.add(forceDirectionNonPerpendicular,cSidePanel);
        forceDirectionNonPerpendicular.setSelected(false);
        forceDirectionNonPerpendicular.addActionListener(this::forceDirectionNonPerpendicularActionPerformed);

        ButtonGroup group = new ButtonGroup();
        group.add(forceDirectionNonPerpendicular);
        group.add(forceDirectionPerpendicular);

        JRadioButton optimizingAngleNinty = new JRadioButton("Optimizing Angle Crossing: 90°"),
                optimizingAngleSixty = new JRadioButton("Optimizing Angle Crossing: 60°");

        cSidePanel.gridy = sidePanelNextY++;
        cSidePanel.gridx = 0;
        sidePanel.add(optimizingAngleNinty,cSidePanel);
        optimizingAngleNinty.setSelected(true);
        optimizingAngleNinty.addActionListener(this::optimizingAngleNintyActionPerformed);
        cSidePanel.gridx = 1;
        sidePanel.add(optimizingAngleSixty,cSidePanel);
        optimizingAngleSixty.setSelected(false);
        optimizingAngleSixty.addActionListener(this::optimizingAngleSixtyActionPerformed);

        ButtonGroup angleGroup = new ButtonGroup();
        angleGroup.add(optimizingAngleNinty);
        angleGroup.add(optimizingAngleSixty);

        cSidePanel.gridy = sidePanelNextY++;
        cSidePanel.gridx = 0;
        JButton startForce = new JButton("Start force algo"),
                stopForce  = new JButton("Stop force algo");
        startForce.addActionListener(this::startForceClicked);
        stopForce.addActionListener(this::stopForceClicked);

        sidePanel.add(startForce, cSidePanel);
        cSidePanel.gridx = 1;
        sidePanel.add(stopForce, cSidePanel);

        cSidePanel.gridy = sidePanelNextY++;
        cSidePanel.gridx = 0;
        JButton showForces = new JButton("Show forces");
        showForces.addActionListener(e -> {
            if (mainFrame.faa == null) {
                mainFrame.faa = mainFrame.defaultForceAlgorithmApplier(0);
            }
            mainFrame.faa.showForces();
        });
        sidePanel.add(showForces, cSidePanel);

        cSidePanel.gridx = 1;
        JButton showBestSolution = new JButton("Show best");
        showBestSolution.addActionListener(e -> {
            if (bestSolution == null) {
                return;
            }

            Mapper<INode, PointD> nodePositions = bestSolution.a;
            Optional<Double> minCrossingAngle = bestSolution.b;
            Double[] mods = bestSolution.c;
            Boolean[] switchs = bestSolution.d;
            PositionMap.applyToGraph(mainFrame.graph, nodePositions);
            String msg = minCrossingAngle.map(d -> "Minimum crossing angle: " + d.toString()).orElse("No crossings!");
            msg += "\n";
            msg += "Modifiers:\n";
            for(int i = 0; i < mods.length; i++){
                Double d = mods[i];
                mainFrame.sliders[i].setValue((int) (1000 * d));
                //noinspection StringConcatenationInLoop
                msg += "\t" + d.toString() + "\n";
            }
            msg += "\n";
            msg += "Switches:\n";
            for(Boolean b: switchs){
                //noinspection StringConcatenationInLoop
                msg += "\n\t" + b.toString() + "\n";
            }
            JOptionPane.showMessageDialog(null, msg);
        });
        sidePanel.add(showBestSolution, cSidePanel);

        cSidePanel.gridy = sidePanelNextY++;
        cSidePanel.gridx = 0;
        JButton showForceAlgoState = new JButton("Show state");
        showForceAlgoState.addActionListener(e -> PositionMap.applyToGraph(mainFrame.graph, mainFrame.faa.getNodePositions()));
        sidePanel.add(showForceAlgoState, cSidePanel);

        JButton scaleToBox = new JButton("Scale me to the box");
        cSidePanel.gridx = 1;
        sidePanel.add(scaleToBox, cSidePanel);
        scaleToBox.addActionListener(e -> scalingToBox());
        scaleToBox.setSelected(false);


        JCheckBox enableMinimumAngleDisplay = new JCheckBox("Show minimum angle");
        cSidePanel.gridx = 0;
        cSidePanel.gridy = sidePanelNextY++;
        sidePanel.add(enableMinimumAngleDisplay, cSidePanel);
        enableMinimumAngleDisplay.addItemListener(this::minimumAngleDisplayEnabled);
        enableMinimumAngleDisplay.setSelected(false);


        JCheckBox allowClickCreateNodeEdge = new JCheckBox("Manual Mode");  //No new nodes or edges on click, can't select ports and edges, for manual tuning
        cSidePanel.gridx = 1;
        sidePanel.add(allowClickCreateNodeEdge, cSidePanel);
        allowClickCreateNodeEdge.addItemListener(this::allowClickCreateNodeEdgeActionPerformed);
        allowClickCreateNodeEdge.setSelected(true);

        return sidePanel;
    }

    /*********************************************************************
     * Implementation of actions
     ********************************************************************/

    private void startGeneticClicked(@SuppressWarnings("unused") ActionEvent evt){
        if(geneticAlgorithm == null || !geneticAlgorithm.running){
            TrashCan.init();
            initializeGeneticAlgorithm();
            mainFrame.graphEditorInputMode.setCreateNodeAllowed(false);
            geneticAlgorithmThread.start();
        }
    }

    private void stopGeneticClicked(@SuppressWarnings("unused") ActionEvent evt){
        geneticAlgorithm.running = false;
        mainFrame.graphEditorInputMode.setCreateNodeAllowed(true);
    }

    private void startForceClicked(@SuppressWarnings("unused") ActionEvent evt){
        if(mainFrame.faa == null || !mainFrame.faa.running){
            TrashCan.init();
            ForceAlgorithmApplier fd = mainFrame.defaultForceAlgorithmApplier(-1);
            fd.modifiers = mainFrame.springThresholds;
            fd.switches = mainFrame.algoModifiers;
            mainFrame.finalizeFAA(mainFrame.faa);
            mainFrame.faa = fd;
            mainFrame.graphEditorInputMode.setCreateNodeAllowed(false);
            IGraphLayoutExecutor executor =
                new IGraphLayoutExecutor(fd, mainFrame.view.getGraph(), mainFrame.progressBar, mainFrame.sidePanel, -1, 20);
            executor.run();
            mainFrame.view.updateUI();
        }
    }

    private void stopForceClicked(@SuppressWarnings("unused") ActionEvent evt){
        if (mainFrame.faa != null) {
            mainFrame.faa.running = false;
            mainFrame.graphEditorInputMode.setCreateNodeAllowed(true);
        }
    }




    private void minimumAngleDisplayEnabled(ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            mainFrame.minimumAngleMonitor.registerGraphChangedListeners();
        } else if (evt.getStateChange() == ItemEvent.DESELECTED) {
            mainFrame.minimumAngleMonitor.removeGraphChangedListeners();
        }
    }

    private void scalingToBox(){
        Mapper<INode, PointD> nodePositions = PositionMap.FromIGraph(mainFrame.graph);
        double maxX=0, maxY=0;
        for(INode u : mainFrame.graph.getNodes()){
            if(u.getLayout().getCenter().getX()>maxX){
                maxX=u.getLayout().getCenter().getX();
            }
            if(u.getLayout().getCenter().getY()>maxY){
                maxY=u.getLayout().getCenter().getY();
            }
        }
        nodePositions = GraphOperations.scaleUpProcess(mainFrame.graph,nodePositions, Math.min((int)(MainFrame.BOX_SIZE/maxX), (int)(MainFrame.BOX_SIZE/maxY)));
        mainFrame.graph =  PositionMap.applyToGraph(mainFrame.graph, nodePositions);
        mainFrame.view.fitGraphBounds();
    }

    private void allowClickCreateNodeEdgeActionPerformed(ItemEvent evt) {
        mainFrame.graphEditorInputMode.setCreateNodeAllowed((evt.getStateChange() == ItemEvent.DESELECTED));     //no new nodes
        mainFrame.graphEditorInputMode.setCreateEdgeAllowed((evt.getStateChange() == ItemEvent.DESELECTED));     //no new edges
        mainFrame.graphEditorInputMode.setEditLabelAllowed((evt.getStateChange() == ItemEvent.DESELECTED));      //no editing of labels
        mainFrame.graphEditorInputMode.setShowHandleItems((evt.getStateChange() == ItemEvent.DESELECTED) ? GraphItemTypes.ALL : GraphItemTypes.NONE); //no resizing of nodes nor selection of ports
        mainFrame.graphEditorInputMode.setDeletableItems((evt.getStateChange() == ItemEvent.DESELECTED) ? GraphItemTypes.ALL : GraphItemTypes.NONE);  //no deleting of nodes
        mainFrame.graphEditorInputMode.setSelectableItems((evt.getStateChange() == ItemEvent.DESELECTED) ? GraphItemTypes.ALL : GraphItemTypes.NODE); //no selecting of edges (only nodes)
    }

    private void forceDirectionPerpendicularActionPerformed(@SuppressWarnings("unused") ActionEvent evt){    mainFrame.algoModifiers[0] = true; }
    private void forceDirectionNonPerpendicularActionPerformed(@SuppressWarnings("unused") ActionEvent evt){ mainFrame.algoModifiers[0] = false; }

    private void optimizingAngleNintyActionPerformed(@SuppressWarnings("unused") ActionEvent evt) { mainFrame.algoModifiers[1] = true; }
    private void optimizingAngleSixtyActionPerformed(@SuppressWarnings("unused") ActionEvent evt) { mainFrame.algoModifiers[1] = false; }


    private GeneticAlgorithm<ForceAlgorithmApplier> geneticAlgorithm;
    private Thread geneticAlgorithmThread;
    private void initializeGeneticAlgorithm(){
        LinkedList<ForceAlgorithmApplier> firstFAAs = new LinkedList<>();
        firstFAAs.add(mainFrame.defaultForceAlgorithmApplier(mainFrame.faaRunningTimeGenetic));
        LayoutUtilities.applyLayout(mainFrame.graph, new OrthogonalLayout());
        firstFAAs.add(mainFrame.defaultForceAlgorithmApplier(mainFrame.faaRunningTimeGenetic));
        LayoutUtilities.applyLayout(mainFrame.graph, new OrganicLayout());
        firstFAAs.add(mainFrame.defaultForceAlgorithmApplier(mainFrame.faaRunningTimeGenetic));
        geneticAlgorithm = InitGeneticAlgorithm.defaultGeneticAlgorithm(firstFAAs, mainFrame.graph);
        geneticAlgorithmThread = new Thread(geneticAlgorithm);
    }
}
