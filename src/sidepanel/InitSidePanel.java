package sidepanel;

import algorithms.graphs.CachedMinimumAngle;
import com.yworks.yfiles.graph.GraphItemTypes;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.view.ICanvasObjectDescriptor;
import com.yworks.yfiles.view.input.ClickInputMode;
import com.yworks.yfiles.view.input.HandleInputMode;
import layout.algo.clinchlayout.ClinchLayout;
import layout.algo.clinchlayout.ClinchLayoutConfigurator;
import layout.algo.execution.ILayout;
import layout.algo.forcealgorithm.ForceAlgorithm;
import layout.algo.forcealgorithm.ForceAlgorithmConfigurator;
import layout.algo.forcealgorithm.forces.*;
import layout.algo.genetic.GeneticForceAlgorithmConfigurator;
import layout.algo.genetic.GeneticForceAlgorithmLayout;
import layout.algo.gridding.CombinedGridder;
import layout.algo.gridding.GridderConfigurator;
import layout.algo.gridding.IGridder;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.randommovement.RandomMovementConfigurator;
import layout.algo.randommovement.RandomMovementLayout;
import main.MainFrame;
import view.visual.DrawBoundingBox;
import view.visual.DrawScale;

import javax.swing.*;
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
    public JCheckBox masterAllowClickGraphEditor;
    public JCheckBox masterEnableCrossingResolution;
    public JCheckBox masterEnableAngularResolution;
    public JCheckBox masterEnableAspectRatio;


    public InitSidePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public JTabbedPane initSidePanel() {
        tabbedSidePane = new JTabbedPane();
        initDefault();

        IGraph graph = mainFrame.view.getGraph();

        addRandomMovementAlgorithm(graph);
        if (!MainFrame.CONTEST_MODE) {
            addForceAlgorithm(graph);
            addGeneticAlgorithm(graph);
        }
        addClinchLayout(graph);
        addGriddingAlgorithm(graph);

        if (!MainFrame.CONTEST_MODE) {
            addMiscAlgorithms();
        }

        return tabbedSidePane;
    }


    public Optional<SidePanelTab> getTabForAlgorithm(Class<? extends ILayout> layoutClass) {
        return sidePanelTabs.stream()
            .filter(executor -> layoutClass.isInstance(executor.getExecutor().getLayout()))
            .findFirst();
    }

    private void initDefault() {
        //show scale
        mainFrame.view.getBackgroundGroup().addChild(new DrawScale(mainFrame.view), ICanvasObjectDescriptor.VISUAL);
        mainFrame.view.getBackgroundGroup().addChild(new DrawBoundingBox(mainFrame.view), ICanvasObjectDescriptor.VISUAL);

        masterEnableMinimumAngle = new JCheckBox("Show minimum angle");
        masterEnableMinimumAngle.addItemListener(this::masterMinAngleDisplayEnabled);
        masterEnableMinimumAngle.setSelected(false);

        masterAllowClickCreateNodeEdge = new JCheckBox("Manual Mode");
        masterAllowClickCreateNodeEdge.addItemListener(this::masterAllowClickCreateNodeEdgeActionPerformed);
        masterAllowClickCreateNodeEdge.setSelected(true);

        masterAllowClickGraphEditor = new JCheckBox("User Mode");
        masterAllowClickGraphEditor.addItemListener(this::masterAllowGraphEditorActionPerformed);
        masterAllowClickGraphEditor.setSelected(true);

        masterEnableCrossingResolution = new JCheckBox("Crossing Resolution");
        masterEnableCrossingResolution.addItemListener(this::masterEnableCrossingResolutionActionPerformed);
        masterEnableCrossingResolution.setSelected(true);

        masterEnableAngularResolution = new JCheckBox("Angular Resolution");
        masterEnableAngularResolution.addItemListener(this::masterEnableAngularResolutionActionPerformed);
        masterEnableAngularResolution.setSelected(false);

        masterEnableAspectRatio = new JCheckBox("Aspect Ratio");
        masterEnableAspectRatio.addItemListener(this::masterEnableAspectRatioActionPerformed);
        masterEnableAspectRatio.setSelected(false);

        sidePanelTabs = new ArrayList<>();

        tabbedSidePane.addChangeListener(changeEvent -> {
            int selectedTab = tabbedSidePane.getSelectedIndex();
            sidePanelTabs.get(selectedTab).setAllowClickGraphEditor(masterAllowClickGraphEditor.isSelected());
            if (!MainFrame.CONTEST_MODE) {
                sidePanelTabs.get(selectedTab).setEnableMinimumAngleDisplay(masterEnableMinimumAngle.isSelected());
                sidePanelTabs.get(selectedTab).setAllowClickCreateNodeEdge(masterAllowClickCreateNodeEdge.isSelected());
                sidePanelTabs.get(selectedTab).setEnableCrossingResolution(masterEnableCrossingResolution.isSelected());
                sidePanelTabs.get(selectedTab).setEnableAngularResolution(masterEnableAngularResolution.isSelected());
            }
            for (int i = 0; i < sidePanelTabs.size() - 1; i++) {    //exclude misc
                if (sidePanelTabs.get(i).getExecutor().isRunning()) {
                    sidePanelTabs.get(selectedTab).setOutputTextArea(sidePanelTabs.get(i).algorithmName + " is Still Running!");
                }
            }
        });
    }

    private void addClinchLayout(IGraph graph) {
        ClinchLayoutConfigurator configurator = new ClinchLayoutConfigurator();
        ClinchLayout clinchLayout = new ClinchLayout(configurator, graph);
        addAlgorithm("Clinching", configurator, clinchLayout);
    }

    private void addGeneticAlgorithm(IGraph graph) {
        GeneticForceAlgorithmConfigurator geneticConfigurator = new GeneticForceAlgorithmConfigurator();
        GeneticForceAlgorithmLayout geneticAlgo = new GeneticForceAlgorithmLayout(geneticConfigurator, graph);
        addAlgorithm("Genetic", geneticConfigurator, geneticAlgo);
    }

    private void addForceAlgorithm(IGraph graph) {
        CachedMinimumAngle cMinimumAngle = new CachedMinimumAngle();
        ForceAlgorithmConfigurator configurator = new ForceAlgorithmConfigurator()
                .addForce(new NodeNeighbourForce(graph))
                .addForce(new NodePairForce(graph))
                .addForce(new IncidentEdgesForce(graph))
                .addForce(new SpringForce(graph, 100, 0.01))
                .addForce(new ElectricForce(graph, 0.01))
                .addForce(new CrossingForce(graph, cMinimumAngle))
                .addForce(new SlopedForce(graph))
                .addForce(new TotalResolutionForce(graph));

        ForceAlgorithm forceAlgorithm = new ForceAlgorithm(configurator, mainFrame.graph, cMinimumAngle);
        addAlgorithm("Force", configurator, forceAlgorithm);
    }

    private void addRandomMovementAlgorithm(IGraph graph) {
        RandomMovementConfigurator config = new RandomMovementConfigurator();
        RandomMovementLayout layout = new RandomMovementLayout(graph, config);
        addAlgorithm("Random", config, layout);
    }


    private void addGriddingAlgorithm(IGraph graph) {
        GridderConfigurator configurator = new GridderConfigurator();
        IGridder gridder = new CombinedGridder(graph, configurator);
        SidePanelTab sidePanelTab = addAlgorithm("Gridding", configurator, gridder);
        sidePanelTab.setVerbose(false);
    }

    /**
     * Adds an algorithm in a new tab, top panel is defined by the configurator, bottom is the default panel
     * @param algorithmName - name of tab
     * @param configurator - which and what parameters
     * @param layout - interface to algorithm for start/pause/stop buttons and controls for above parameters
     */
    private SidePanelTab addAlgorithm(String algorithmName, ILayoutConfigurator configurator, ILayout layout) {
        SidePanelTab sidePanel = new SidePanelTab(this, algorithmName, configurator, layout);
        sidePanelTabs.add(sidePanel);
        tabbedSidePane.addTab(sidePanel.algorithmName, new JScrollPane(sidePanel.sidePanelTab));
        return sidePanel;
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
    public boolean removeDefaultListeners() {
        if (stateEnableMinimumAngle && !removedListeners) {
            removedListeners = true;
            mainFrame.minimumAngleMonitor.removeGraphChangedListeners();
            return true;
        }
        return false;
    }

    public void addDefaultListeners() {
        if (stateEnableMinimumAngle && removedListeners) {
            removedListeners = false;
            mainFrame.minimumAngleMonitor.registerGraphChangedListeners();
            mainFrame.minimumAngleMonitor.updateAngleInfoBar();
        }
    }

    public void setOutputTextArea(String outputText) {
        sidePanelTabs.get(tabbedSidePane.getSelectedIndex()).setOutputTextArea(outputText);
    }

    public JTextArea getOutputTextArea() {
        return sidePanelTabs.get(tabbedSidePane.getSelectedIndex()).getOutputTextArea();
    }

    /*********************************************************************
     * Implementation of actions
     ********************************************************************/

    private void masterMinAngleDisplayEnabled(ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            stateEnableMinimumAngle = true;
            mainFrame.initSidePanel.addDefaultListeners();
        } else if (evt.getStateChange() == ItemEvent.DESELECTED) {
            stateEnableMinimumAngle = false;
            mainFrame.initSidePanel.removeDefaultListeners();
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

    private void masterAllowGraphEditorActionPerformed(ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.SELECTED) {   //TODO: Performance - turn listeners off permanently
            mainFrame.initSidePanel.addDefaultListeners();
        } else {
            mainFrame.initSidePanel.removeDefaultListeners();
        }
        mainFrame.graphEditorInputMode.setEnabled(evt.getStateChange() == ItemEvent.SELECTED);
    }

    private void masterEnableCrossingResolutionActionPerformed(ItemEvent evt) { //TODO: maybe sync with random
        mainFrame.minimumAngleMonitor.setUseCrossingResolution(evt.getStateChange() == ItemEvent.SELECTED);
    }

    private void masterEnableAngularResolutionActionPerformed(ItemEvent evt) {
        mainFrame.minimumAngleMonitor.setUseAngularResolution(evt.getStateChange() == ItemEvent.SELECTED);
    }

    private void masterEnableAspectRatioActionPerformed(ItemEvent evt) {
        mainFrame.minimumAngleMonitor.setUseAspectRatio(evt.getStateChange() == ItemEvent.SELECTED);
    }
}
