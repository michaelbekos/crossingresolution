package sidepanel;

import algorithms.graphs.CachedMinimumAngle;
import com.yworks.yfiles.graph.GraphItemTypes;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.view.ICanvasObject;
import com.yworks.yfiles.view.ICanvasObjectDescriptor;
import layout.algo.*;
import layout.algo.forces.*;
import layout.algo.genetic.GeneticForceAlgorithmConfigurator;
import layout.algo.genetic.GeneticForceAlgorithmLayout;
import layout.algo.gridding.CombinedGridder;
import layout.algo.gridding.GridderConfigurator;
import layout.algo.gridding.IGridder;
import layout.algo.layoutinterface.ILayoutConfigurator;
import main.MainFrame;
import view.visual.DrawScale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Optional;

public class InitSidePanel {
    protected MainFrame mainFrame;
    private JTabbedPane tabbedSidePane;

    //Algorithm Tabs
    private ArrayList<SidePanelTab> sidePanelTabs;

    //Default Controls
    public JCheckBox masterEnableMinimumAngle;
    private boolean stateEnableMinimumAngle;
    public JCheckBox masterAllowClickCreateNodeEdge;


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

        IGraph graph = mainFrame.view.getGraph();

        addRandomMovementAlgorithm(graph);
        addForceAlgorithm(graph);
        addGeneticAlgorithm(graph);
        addClinchLayout(graph);
        addGriddingAlgorithm(graph);

        addMiscAlgorithms();

        //min angle and manual mode default on
        sidePanelTabs.get(0).setAllowClickCreateNodeEdge(true);
        sidePanelTabs.get(0).setEnableMinimumAngleDisplay(true);

        return tabbedSidePane;
    }


    public Optional<BasicIGraphLayoutExecutor> getExecutorForAlgorithm(Class<? extends ILayout> layoutClass) {
        return sidePanelTabs.stream()
            .map(SidePanelTab::getExecutor)
            .filter(executor -> layoutClass.isInstance(executor.getLayout()))
            .findFirst();
    }

    private void addGriddingAlgorithm(IGraph graph) {
        GridderConfigurator configurator = new GridderConfigurator();
        IGridder gridder = new CombinedGridder(graph, configurator);
        addAlgorithm("Gridding", configurator, gridder);
    }

    private void initDefault() {
        //show scale
        mainFrame.view.getBackgroundGroup().addChild(new DrawScale(mainFrame.view), ICanvasObjectDescriptor.VISUAL);

        masterEnableMinimumAngle = new JCheckBox("Show minimum angle");
        masterEnableMinimumAngle.addItemListener(this::masterMinAngleDisplayEnabled);
        masterEnableMinimumAngle.setSelected(false);

        masterAllowClickCreateNodeEdge = new JCheckBox("Manual Mode");
        masterAllowClickCreateNodeEdge.addItemListener(this::masterAllowClickCreateNodeEdgeActionPerformed);
        masterAllowClickCreateNodeEdge.setSelected(false);

        sidePanelTabs = new ArrayList<>();

        tabbedSidePane.addChangeListener(changeEvent -> {
            int selectedTab = tabbedSidePane.getSelectedIndex();
            sidePanelTabs.get(selectedTab).setEnableMinimumAngleDisplay(masterEnableMinimumAngle.isSelected());
            sidePanelTabs.get(selectedTab).setAllowClickCreateNodeEdge(masterAllowClickCreateNodeEdge.isSelected());
            for (int i = 0; i < sidePanelTabs.size() - 1; i++) {    //exclude misc
                if (i != selectedTab) {
                    sidePanelTabs.get(i).stopExecution();
                }
            }
        });
    }

    private void addClinchLayout(IGraph graph) {
        ClinchLayoutConfigurator configurator = new ClinchLayoutConfigurator();
        ClinchLayout clinchLayout = new ClinchLayout(configurator, graph);
        addAlgorithm("Clinch Nodes", configurator, clinchLayout);
    }

    private void addGeneticAlgorithm(IGraph graph) {
        GeneticForceAlgorithmConfigurator geneticConfigurator = new GeneticForceAlgorithmConfigurator();
        GeneticForceAlgorithmLayout geneticAlgo = new GeneticForceAlgorithmLayout(geneticConfigurator, graph);
        addAlgorithm("Genetic Algorithm", geneticConfigurator, geneticAlgo);
    }

    private void addForceAlgorithm(IGraph graph) {
        mainFrame.bestSolution.getBestMinimumAngle();
        CachedMinimumAngle cMinimumAngle = new CachedMinimumAngle();
        ForceAlgorithmConfigurator configurator = new ForceAlgorithmConfigurator()
                .addForce(new NodeNeighbourForce(graph))
                .addForce(new NodePairForce(graph))
                .addForce(new IncidentEdgesForce(graph))
                .addForce(new SpringForce(graph, 100, 0.01))
                .addForce(new ElectricForce(graph, 0.01))
                .addForce(new CrossingForce(graph, cMinimumAngle))
                .addForce(new SlopedForce(graph, mainFrame.view));

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
        SidePanelTab sidePanel = new SidePanelTab(this, algorithmName, configurator, layout);
        sidePanelTabs.add(sidePanel);
//        tabbedSidePane.addTab(sidePanel.algorithmName, sidePanel.sidePanelTab);
        tabbedSidePane.addTab(sidePanel.algorithmName, new JScrollPane(sidePanel.sidePanelTab));
    }

    /**
     * Adds all algorithms that do not need a separate tab
     */
    private void addMiscAlgorithms() {
        SidePanelTab miscTab = new SidePanelTab(this);
        sidePanelTabs.add(miscTab);
        tabbedSidePane.addTab("Misc.", miscTab.getMiscAlgorithmTab());
    }


    private boolean removedListeners = false;
    public void removeDefaultListeners() {
        if (stateEnableMinimumAngle && !removedListeners) {
            removedListeners = true;
            mainFrame.minimumAngleMonitor.removeGraphChangedListeners();
        }
    }

    public void addDefaultListeners() {
        if (stateEnableMinimumAngle && removedListeners) {
            removedListeners = false;
            mainFrame.minimumAngleMonitor.registerGraphChangedListeners();
            mainFrame.minimumAngleMonitor.updateMinimumAngleInfoBar();
        }
    }

    /*********************************************************************
     * Implementation of actions
     ********************************************************************/

    private void masterMinAngleDisplayEnabled(ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            stateEnableMinimumAngle = true;
            mainFrame.minimumAngleMonitor.registerGraphChangedListeners();
        } else if (evt.getStateChange() == ItemEvent.DESELECTED) {
            stateEnableMinimumAngle = false;
            mainFrame.minimumAngleMonitor.removeGraphChangedListeners();
        }
    }

    private void masterAllowClickCreateNodeEdgeActionPerformed(ItemEvent evt) {
        mainFrame.graphEditorInputMode.setCreateNodeAllowed(evt.getStateChange() == ItemEvent.DESELECTED);     //no new nodes
        mainFrame.graphEditorInputMode.setCreateEdgeAllowed(evt.getStateChange() == ItemEvent.DESELECTED);     //no new edges
        mainFrame.graphEditorInputMode.setEditLabelAllowed(evt.getStateChange() == ItemEvent.DESELECTED);      //no editing of labels
        mainFrame.graphEditorInputMode.setShowHandleItems(evt.getStateChange() == ItemEvent.DESELECTED ? GraphItemTypes.ALL : GraphItemTypes.NONE); //no resizing of nodes nor selection of ports
        mainFrame.graphEditorInputMode.setDeletableItems(evt.getStateChange() == ItemEvent.DESELECTED ? GraphItemTypes.ALL : GraphItemTypes.NONE);  //no deleting of nodes
        mainFrame.graphEditorInputMode.setSelectableItems(evt.getStateChange() == ItemEvent.DESELECTED ? GraphItemTypes.ALL : GraphItemTypes.NODE); //no selecting of edges (only nodes)
    }

    private ICanvasObject scale = null;
    private void masterShowScaleEnabled(@SuppressWarnings("unused") ItemEvent evt) {
        removeDefaultListeners();
        if (evt.getStateChange() == ItemEvent.SELECTED){
            scale = (mainFrame.view.getBackgroundGroup().addChild(new DrawScale(mainFrame.view), ICanvasObjectDescriptor.VISUAL));
        } else {
            scale.remove();
        }
        addDefaultListeners();
    }

}
