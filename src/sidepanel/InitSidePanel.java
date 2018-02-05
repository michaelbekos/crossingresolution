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
    public MainFrame mainFrame;
    private JTabbedPane tabbedSidePane;

    //Algorithm Tabs
    private ArrayList<SidePanelTab> sidePanelTabs;

    //Default Controls
    public JCheckBox masterEnableMinimumAngle;
    private boolean stateEnableMinimumAngle;
    public JCheckBox masterAllowClickCreateNodeEdge;
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
        sidePanelTabs.get(0).setAllowClickCreateNodeEdge(true);
        sidePanelTabs.get(0).setEnableMinimumAngleDisplay(true);

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

        sidePanelTabs = new ArrayList<>();

        tabbedSidePane.addChangeListener(changeEvent -> {
            sidePanelTabs.get(tabbedSidePane.getSelectedIndex()).setEnableMinimumAngleDisplay(masterEnableMinimumAngle.isSelected());
            sidePanelTabs.get(tabbedSidePane.getSelectedIndex()).setAllowClickCreateNodeEdge(masterAllowClickCreateNodeEdge.isSelected());
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
        SidePanelTab sidePanel = new SidePanelTab(this, algorithmName, configurator, layout);
        sidePanelTabs.add(sidePanel);
        tabbedSidePane.addTab(sidePanel.algorithmName, sidePanel.sidePanelTab);
    }

    /**
     * Adds all algorithms that do not need a separate tab
     */
    private void addMiscAlgorithms() {
        SidePanelTab miscTab = new SidePanelTab();
        tabbedSidePane.addTab("Misc.", miscTab.getMiscAlgorithmTab());
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

    private void masterMinAngleDisplayEnabled(ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            mainFrame.minimumAngleMonitor.registerGraphChangedListeners();
        } else if (evt.getStateChange() == ItemEvent.DESELECTED) {
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

}
