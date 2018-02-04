package sidepanel;

import algorithms.graphs.CachedMinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.layout.ILayoutAlgorithm;
import com.yworks.yfiles.layout.LayoutExecutor;
import com.yworks.yfiles.layout.circular.CircularLayout;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.layout.orthogonal.OrthogonalLayout;
import com.yworks.yfiles.layout.partial.PartialLayout;
import com.yworks.yfiles.layout.partial.SubgraphPlacement;
import com.yworks.yfiles.layout.tree.TreeLayout;
import com.yworks.yfiles.view.IGraphSelection;
import com.yworks.yfiles.view.ISelectionModel;
import layout.algo.*;
import layout.algo.forces.*;
import layout.algo.genetic.GeneticForceAlgorithmConfigurator;
import layout.algo.genetic.GeneticForceAlgorithmLayout;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.utils.PositionMap;
import main.MainFrame;
import util.GraphOperations;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;

public class InitSidePanel {
    private MainFrame mainFrame;
    private JTabbedPane tabbedSidePane;

    //Default Controls
    private ArrayList<JCheckBox> defaultControlEnableMinimumAngleDisplay;
    private JCheckBox masterEnableMinimumAngle;
    private boolean stateEnableMinimumAngle;
    private ArrayList<JCheckBox> defaultControlAllowClickCreateNodeEdge;
    private JCheckBox masterAllowClickCreateNodeEdge;
    private boolean stateAllowClickCreateNoteEdge;


    public InitSidePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public JTabbedPane initSidePanel(JPanel mainPanel) {
        tabbedSidePane = new JTabbedPane();
        initDefault();
        GridBagConstraints cc = new GridBagConstraints();
        cc.gridx = 1;
        cc.gridy = 1;
        cc.weighty = 1;
        cc.weightx = 0.2;
        cc.insets = new Insets(0, 0, 0, 0);
        cc.fill = GridBagConstraints.BOTH;
        mainPanel.add(tabbedSidePane, cc);

        TrashCan.init();
        IGraph graph = mainFrame.view.getGraph();

        addRandomMovementAlgorithm(graph);
        addForceAlgorithm(graph);
        addGeneticAlgorithm(graph);
        addSpringEmbedderAlgorithm(graph);
        addClinchLayout(graph);

        // TODO:
        addAlgorithm("Sloped Spring Embedder", null, null);

        addMiscAlgorithms();

        //min angle and manual mode default on
        defaultControlEnableMinimumAngleDisplay.get(0).setSelected(true);
        defaultControlAllowClickCreateNodeEdge.get(0).setSelected(true);

        return tabbedSidePane;
    }

    private void initDefault() {
        masterEnableMinimumAngle = new JCheckBox("Show minimum angle");
        masterEnableMinimumAngle.addItemListener(this::masterMinAngleDisplayEnabled);
        masterEnableMinimumAngle.setSelected(false);
        stateEnableMinimumAngle = false;

        masterAllowClickCreateNodeEdge = new JCheckBox("Manual Mode");
        masterAllowClickCreateNodeEdge.addItemListener(this::masterAllowClickCreateNodeEdgeActionPerformed);
        masterAllowClickCreateNodeEdge.setSelected(false);
        stateAllowClickCreateNoteEdge = false;

        defaultControlEnableMinimumAngleDisplay = new ArrayList<>();
        defaultControlAllowClickCreateNodeEdge = new ArrayList<>();
        tabbedSidePane.addChangeListener(changeEvent -> {
            defaultControlEnableMinimumAngleDisplay.get(tabbedSidePane.getSelectedIndex()).setSelected(masterEnableMinimumAngle.isSelected());
            defaultControlAllowClickCreateNodeEdge.get(tabbedSidePane.getSelectedIndex()).setSelected(masterAllowClickCreateNodeEdge.isSelected());
        });
    }

    private void addClinchLayout(IGraph graph) {
        ClinchLayoutConfigurator configurator = new ClinchLayoutConfigurator();
        ClinchLayout clinchLayout = new ClinchLayout(configurator, graph);
        addAlgorithm("Clinch Nodes", configurator, clinchLayout);
    }

    private void addSpringEmbedderAlgorithm(IGraph graph) {
        ForceAlgorithmConfigurator configurator = new ForceAlgorithmConfigurator()
            .addForce(new SpringForce(graph, 100, 0.01, 150))
            .addForce(new ElectricForce(graph, 0.01, 50000));

        ForceAlgorithm forceAlgorithm = new ForceAlgorithm(configurator, graph, new CachedMinimumAngle());
        addAlgorithm("Spring Embedder", configurator, forceAlgorithm);
    }

    private void addGeneticAlgorithm(IGraph graph) {
        GeneticForceAlgorithmConfigurator geneticConfigurator = new GeneticForceAlgorithmConfigurator();
        GeneticForceAlgorithmLayout geneticAlgo = new GeneticForceAlgorithmLayout(geneticConfigurator, graph);
        addAlgorithm("Genetic Algorithm", geneticConfigurator, geneticAlgo);
    }

    private void addForceAlgorithm(IGraph graph) {
        CachedMinimumAngle cMinimumAngle = new CachedMinimumAngle();
        ForceAlgorithmConfigurator configurator = new ForceAlgorithmConfigurator();
        configurator.addForce(new NodeNeighbourForce(graph))
                .addForce(new NodePairForce(graph))
                .addForce(new IncidentEdgesForce(graph))
                .addForce(new CrossingForce(graph, cMinimumAngle));

        ForceAlgorithm forceAlgorithm = new ForceAlgorithm(configurator, mainFrame.graph, cMinimumAngle);
        addAlgorithm("Force Algorithm", configurator, forceAlgorithm);
    }

    private void addRandomMovementAlgorithm(IGraph graph) {
        RandomMovementConfigurator config = new RandomMovementConfigurator();
        RandomMovementLayout layout = new RandomMovementLayout(graph, config);
        addAlgorithm("Random Movement", config, layout);
    }

    /**
     * Adds an algorithm in a new tab, top panel is defined by the configurator, bottom is the default panel
     * @param algorithmName - name of tab
     * @param configurator - which and what parameters
     * @param layout - interface to algorithm for start/pause/stop buttons and controls for above parameters
     */
    private void addAlgorithm(String algorithmName, ILayoutConfigurator configurator, ILayout layout) {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new GridBagLayout());
        GridBagConstraints cSidePanel = new GridBagConstraints();

        //algorithm configurator specific controls
        JPanel custom = new JPanel();
        custom.setLayout(new GridBagLayout());
        GridBagConstraints cCustomPanel = new GridBagConstraints();
        cCustomPanel.gridx = 0;
        cCustomPanel.gridy = 1;

        GridBagState gridBagState = new GridBagState();
        gridBagState.increaseY();

        if (configurator != null){
            configurator.init(new SidePanelItemFactory(custom, mainFrame.view, mainFrame.graphEditorInputMode, gridBagState));
        }

        cSidePanel.fill = GridBagConstraints.BOTH;
        cSidePanel.gridx = 0;
        cSidePanel.gridy = 0;
        cSidePanel.weighty = 1;
        cSidePanel.anchor = GridBagConstraints.FIRST_LINE_START;
        sidePanel.add(custom, cSidePanel);


        //default controls (start, pause, manual mode, min angle, output, etc)
        cSidePanel.fill = GridBagConstraints.HORIZONTAL;
        cSidePanel.gridx = 0;
        cSidePanel.gridy = 1;
        cSidePanel.weightx = 0;
        cSidePanel.weighty = 0;
        sidePanel.add(getDefaultPanel(layout), cSidePanel);

        tabbedSidePane.addTab(algorithmName, sidePanel);
    }

    /**
     * Adds all algorithms that do not need a separate tab
     */
    private void addMiscAlgorithms() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new GridBagLayout());
        GridBagConstraints cSidePanel = new GridBagConstraints();

        //misc algorithms
        JPanel custom = new JPanel();
        custom.setLayout(new GridBagLayout());
        GridBagConstraints cCustomPanel = new GridBagConstraints();
        cCustomPanel.gridx = 0;
        int cCustomPanelY = 1;


        JButton orthogonalItem = new JButton("Orthogonal Layout");
        cCustomPanel.fill = GridBagConstraints.HORIZONTAL;
        cCustomPanel.gridx = 0;
        cCustomPanel.gridy = cCustomPanelY;
        orthogonalItem.addActionListener(this::orthogonalItemActionPerformed);
        custom.add(orthogonalItem, cCustomPanel);

        JButton circularItem = new JButton("Circular Layout");
        cCustomPanel.fill = GridBagConstraints.HORIZONTAL;
        cCustomPanel.gridx = 1;
        cCustomPanel.gridy = cCustomPanelY;
        circularItem.addActionListener(this::circularItemActionPerformed);
        custom.add(circularItem, cCustomPanel);

        JButton treeItem = new JButton("Tree Layout");
        cCustomPanel.fill = GridBagConstraints.HORIZONTAL;
        cCustomPanel.gridx = 0;
        cCustomPanel.gridy = ++cCustomPanelY;
        treeItem.addActionListener(this::treeItemActionPerformed);
        custom.add(treeItem, cCustomPanel);

        JButton organicItem = new JButton("Organic Layout");
        cCustomPanel.fill = GridBagConstraints.HORIZONTAL;
        cCustomPanel.gridx = 1;
        cCustomPanel.gridy = cCustomPanelY;
        organicItem.addActionListener(this::organicItemActionPerformed);
        custom.add(organicItem, cCustomPanel);

        cSidePanel.fill = GridBagConstraints.HORIZONTAL;
        cSidePanel.gridx = 0;
        cSidePanel.gridy = 0;
        cSidePanel.weighty = 1;
        sidePanel.add(custom, cSidePanel);

        //default controls (manual mode, min angle, output, etc)
        cSidePanel.fill = GridBagConstraints.HORIZONTAL;
        cSidePanel.gridx = 0;
        cSidePanel.gridy = 1;
        cSidePanel.weightx = 0;
        cSidePanel.weighty = 0;
        sidePanel.add(getLeanDefaultPanel(), cSidePanel);

        tabbedSidePane.addTab("Misc.", sidePanel);
    }

    /**
     * returns default panel without start/stop controls
     * @return - lean default panel
     */
    private JPanel getLeanDefaultPanel() {
        //lean default panel
        JPanel defaultPanel = new JPanel();
        defaultPanel.setLayout(new GridBagLayout());
        GridBagConstraints cDefaultPanel = new GridBagConstraints();
        addDefaultControls(defaultPanel, cDefaultPanel, 0);

        return defaultPanel;
    }

    /**
     * returns the default panel for all algorithms (start/pause, stop, manual mode, min angle, output)
     * @param layout - interface start/stop etc to algorithm
     * @return default panel
     */
    private JPanel getDefaultPanel(ILayout layout) {
        IGraphLayoutExecutor executor =
            new IGraphLayoutExecutor(layout, mainFrame.graph, mainFrame.progressBar, -1, 20);

        JPanel defaultPanel = new JPanel();
        defaultPanel.setLayout(new GridBagLayout());
        GridBagConstraints cDefaultPanel = new GridBagConstraints();
        int cDefaultPanelY = 0;

        JButton button1 = new JButton("Start");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = cDefaultPanelY;
        cDefaultPanel.weighty = 0;
        button1.addActionListener(actionEvent -> {
            if (button1.getText().equals("Start") || button1.getText().equals("Continue")) {
                if (executor.isRunning()) {
                    button1.setText("Pause");
                    executor.unpause();
                } else {
                    if (executor.isFinished()) {
                        button1.setText("Start");
                    } else {
                        button1.setText("Pause");
//                        button1.setBackground(Color.green);
                        executor.start();
                    }
                }
            } else if (button1.getText().equals("Pause")) {
                button1.setText("Continue");
                executor.pause();
            }
        });
        defaultPanel.add(button1, cDefaultPanel);

        JButton button2 = new JButton("Stop");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 1;
        cDefaultPanel.gridy = cDefaultPanelY;
//        cDefaultPanel.weightx = 0.;
        cDefaultPanel.weighty = 0;
        button2.addActionListener(actionEvent -> {
            button1.setText("Start");
            executor.stop();
        });
        defaultPanel.add(button2, cDefaultPanel);

        addDefaultControls(defaultPanel, cDefaultPanel, cDefaultPanelY);

        return defaultPanel;
    }

    private void addDefaultControls(JPanel defaultPanel, GridBagConstraints cDefaultPanel, int cDefaultPanelY) {
        JButton showBestSolution = new JButton("Show best");    //TODO: fix best solution
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        showBestSolution.addActionListener(this::showBestSolution);
        defaultPanel.add(showBestSolution, cDefaultPanel);

        JButton scaleToBox = new JButton("Scale me to the box");
        cDefaultPanel.gridx = 1;
        cDefaultPanel.gridy = cDefaultPanelY;
        defaultPanel.add(scaleToBox, cDefaultPanel);
        scaleToBox.addActionListener(e -> scalingToBox());
        scaleToBox.setSelected(false);

        JCheckBox enableMinimumAngleDisplay = new JCheckBox("Show minimum angle");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        cDefaultPanel.weightx = 0.5;
        cDefaultPanel.weighty = 0;
        defaultPanel.add(enableMinimumAngleDisplay, cDefaultPanel);
        enableMinimumAngleDisplay.addItemListener(this::minimumAngleDisplayEnabled);
        enableMinimumAngleDisplay.setSelected(false);
        defaultControlEnableMinimumAngleDisplay.add(enableMinimumAngleDisplay);

        JCheckBox allowClickCreateNodeEdge = new JCheckBox("Manual Mode");  //No new nodes or edges on click, can't select ports and edges, for manual tuning
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 1;
        cDefaultPanel.gridy = cDefaultPanelY;
        cDefaultPanel.weightx = 0.5;
        cDefaultPanel.weighty = 0;
        defaultPanel.add(allowClickCreateNodeEdge, cDefaultPanel);
        allowClickCreateNodeEdge.addItemListener(this::allowClickCreateNodeEdgeActionPerformed);
        allowClickCreateNodeEdge.setSelected(false);
        defaultControlAllowClickCreateNodeEdge.add(allowClickCreateNodeEdge);

        JTextArea output = new JTextArea("Output");
        output.setLineWrap(true);
        output.setRows(5);
        JScrollPane test = new JScrollPane(output);
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        cDefaultPanel.weightx = 1;
        cDefaultPanel.weighty = 0;
        cDefaultPanel.insets = new Insets(10,10,10,10);
        cDefaultPanel.gridwidth = 3;
        defaultPanel.add(test, cDefaultPanel);
    }

    private void showBestSolution(@SuppressWarnings("unused") ActionEvent evt) {
        if (TrashCan.bestSolution == null) {
            return;
        }

        Mapper<INode, PointD> nodePositions = TrashCan.bestSolution.a;
        Optional<Double> minCrossingAngle = TrashCan.bestSolution.b;
        Double[] mods = TrashCan.bestSolution.c;
        Boolean[] switchs = TrashCan.bestSolution.d;
        PositionMap.applyToGraph(mainFrame.graph, nodePositions);
        String msg = minCrossingAngle.map(d -> "Minimum crossing angle: " + d.toString()).orElse("No crossings!");
        msg += "\n";
        msg += "Modifiers:\n";
        for (int i = 0; i < mods.length; i++) {
            Double d = mods[i];
            mainFrame.sliders[i].setValue((int) (1000 * d));
            //noinspection StringConcatenationInLoop
            msg += "\t" + d.toString() + "\n";
        }
        msg += "\n";
        msg += "Switches:\n";
        for (Boolean b : switchs) {
            //noinspection StringConcatenationInLoop
            msg += "\n\t" + b.toString() + "\n";
        }
        JOptionPane.showMessageDialog(null, msg);
    }

    public void removeDefaultListeners() {
        stateEnableMinimumAngle = masterEnableMinimumAngle.isSelected();
        stateAllowClickCreateNoteEdge = masterAllowClickCreateNodeEdge.isSelected();
        masterEnableMinimumAngle.setSelected(false);
        masterAllowClickCreateNodeEdge.setSelected(false);
    }

    public void addDefaultListeners() {
        masterEnableMinimumAngle.setSelected(stateEnableMinimumAngle);
        masterAllowClickCreateNodeEdge.setSelected(stateAllowClickCreateNoteEdge);
    }

    /*********************************************************************
     * Implementation of actions
     ********************************************************************/


    private void organicItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        removeDefaultListeners();
        applyLayoutToSelection(new OrganicLayout());
        addDefaultListeners();
    }

    private void circularItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        removeDefaultListeners();
        applyLayoutToSelection(new CircularLayout());
        addDefaultListeners();
    }

    private void orthogonalItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        removeDefaultListeners();
        applyLayoutToSelection(new OrthogonalLayout());
        addDefaultListeners();

    }

    private void treeItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        removeDefaultListeners();
        try {
            applyLayoutToSelection(new TreeLayout());
        } catch (Exception exc) {
            mainFrame.infoLabel.setText("The input graph is not a tree or a forest.");
        } finally {
            addDefaultListeners();
        }
    }

    private void applyLayoutToSelection(ILayoutAlgorithm layout) {
        IGraphSelection selection = mainFrame.graphEditorInputMode.getGraphSelection();
        ISelectionModel<INode> selectedNodes = selection.getSelectedNodes();

        if (selectedNodes.getCount() == 0) {
            LayoutUtilities.morphLayout(mainFrame.view, layout, Duration.ofSeconds(1), null);
            return;
        }

        FilteredGraphWrapper selectedGraph = new FilteredGraphWrapper(mainFrame.graph, selectedNodes::isSelected,
                iEdge -> selectedNodes.isSelected(iEdge.getSourceNode()) || selectedNodes.isSelected(iEdge.getTargetNode()));

        PartialLayout partialLayout = new PartialLayout(layout);
        partialLayout.setSubgraphPlacement(SubgraphPlacement.FROM_SKETCH);

        LayoutExecutor executor = new LayoutExecutor(mainFrame.view, selectedGraph, partialLayout);
        executor.setDuration(Duration.ofSeconds(1));
        executor.setViewportAnimationEnabled(true);
        executor.setEasedAnimationEnabled(true);
        executor.setContentRectUpdatingEnabled(true);

        executor.start();
    }

    private void masterMinAngleDisplayEnabled(ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            mainFrame.minimumAngleMonitor.registerGraphChangedListeners();
        } else if (evt.getStateChange() == ItemEvent.DESELECTED) {
            mainFrame.minimumAngleMonitor.removeGraphChangedListeners();
        }
    }

    private void minimumAngleDisplayEnabled(ItemEvent evt) {
        masterEnableMinimumAngle.setSelected(evt.getStateChange() == ItemEvent.SELECTED);
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

    private void masterAllowClickCreateNodeEdgeActionPerformed(ItemEvent evt) {
        mainFrame.graphEditorInputMode.setCreateNodeAllowed(evt.getStateChange() == ItemEvent.DESELECTED);     //no new nodes
        mainFrame.graphEditorInputMode.setCreateEdgeAllowed(evt.getStateChange() == ItemEvent.DESELECTED);     //no new edges
        mainFrame.graphEditorInputMode.setEditLabelAllowed(evt.getStateChange() == ItemEvent.DESELECTED);      //no editing of labels
        mainFrame.graphEditorInputMode.setShowHandleItems(evt.getStateChange() == ItemEvent.DESELECTED ? GraphItemTypes.ALL : GraphItemTypes.NONE); //no resizing of nodes nor selection of ports
        mainFrame.graphEditorInputMode.setDeletableItems(evt.getStateChange() == ItemEvent.DESELECTED ? GraphItemTypes.ALL : GraphItemTypes.NONE);  //no deleting of nodes
        mainFrame.graphEditorInputMode.setSelectableItems(evt.getStateChange() == ItemEvent.DESELECTED ? GraphItemTypes.ALL : GraphItemTypes.NODE); //no selecting of edges (only nodes)
    }

    private void allowClickCreateNodeEdgeActionPerformed(ItemEvent evt) {
        masterAllowClickCreateNodeEdge.setSelected(evt.getStateChange() == ItemEvent.SELECTED);
    }
}
