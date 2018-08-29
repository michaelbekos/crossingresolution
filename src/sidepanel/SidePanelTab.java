package sidepanel;

import algorithms.graphs.GridGraph;
import com.yworks.yfiles.algorithms.GraphChecker;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.layout.*;
import com.yworks.yfiles.layout.circular.CircularLayout;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.layout.organic.RemoveOverlapsStage;
import com.yworks.yfiles.layout.orthogonal.OrthogonalLayout;
import com.yworks.yfiles.layout.partial.PartialLayout;
import com.yworks.yfiles.layout.partial.SubgraphPlacement;
import com.yworks.yfiles.layout.tree.TreeLayout;
import com.yworks.yfiles.utils.IEventListener;
import com.yworks.yfiles.view.IGraphSelection;
import com.yworks.yfiles.view.ISelectionModel;
import graphoperations.Centering;
import graphoperations.Chains;
import graphoperations.RemovedChains;
import graphoperations.Scaling;
import layout.algo.FraysseixPachPollack;
import layout.algo.NodeSwapper;
import layout.algo.execution.BasicIGraphLayoutExecutor;
import layout.algo.execution.ILayout;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.utils.LayoutUtils;
import layout.algo.utils.PositionMap;
import main.MainFrame;
import util.BoundingBox;
import util.GraphModifier;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

public class SidePanelTab {
    private static final double scaleFactorWhenCentering = 0.9;
	public JPanel sidePanelTab;
    public String algorithmName;
    public ILayoutConfigurator configurator;
    public ILayout layout;

    private InitSidePanel initSidePanel;

    private ProgressBarLayoutExecutor executor;
    private JButton startPauseButton;
    private JButton stopButton;
    private JCheckBox enableMinimumAngleDisplay;
    private JCheckBox allowClickCreateNodeEdge;
    private JCheckBox allowClickGraphEditor;
    private JCheckBox enableCrossingResolution;
    private JCheckBox enableAngularResolution;
    private JCheckBox enableAspectRatio;
    private JTextArea outputTextArea;
    private boolean verbose;
    
    private int depth;

    public SidePanelTab(InitSidePanel initSidePanel) {
        //default empty
        this.initSidePanel = initSidePanel;
        this.outputTextArea = new JTextArea("Output");
    }

    public SidePanelTab(InitSidePanel initSidePanel, String algorithmName, ILayoutConfigurator configurator, ILayout layout) {
        this.initSidePanel = initSidePanel;
        this.algorithmName = algorithmName;
        this.configurator = configurator;
        this.layout = layout;
        this.verbose = true;
        init();
    }

    private void init() {
    	depth=0;
        sidePanelTab = new JPanel();
        sidePanelTab.setLayout(new GridBagLayout());
        GridBagConstraints cSidePanel = new GridBagConstraints();

        //algorithm configurator specific controls
        JPanel custom = new JPanel();
        custom.setLayout(new GridBagLayout());
        GridBagConstraints cCustomPanel = new GridBagConstraints();
        cCustomPanel.gridx = 0;
        cCustomPanel.gridy = 1;

        GridBagState gridBagState = new GridBagState();
        gridBagState.increaseY();

        outputTextArea = new JTextArea("Output");
        SidePanelItemFactory itemFactory = new SidePanelItemFactory(custom, initSidePanel.mainFrame.view, initSidePanel.mainFrame.graphEditorInputMode, outputTextArea, gridBagState);
        executor = new ProgressBarLayoutExecutor(layout, initSidePanel.mainFrame.graph, initSidePanel.mainFrame.progressBar, -1, 20, itemFactory);
        if (configurator != null) {
            configurator.init(itemFactory);
        }


        cSidePanel.fill = GridBagConstraints.BOTH;
        cSidePanel.gridx = 0;
        cSidePanel.gridy = 0;
        cSidePanel.weighty = 1;
        cSidePanel.anchor = GridBagConstraints.FIRST_LINE_START;
        sidePanelTab.add(custom, cSidePanel);


        //default controls (start, pause, manual mode, min angle, output, etc)
        cSidePanel.fill = GridBagConstraints.HORIZONTAL;
        cSidePanel.gridx = 0;
        cSidePanel.gridy = 1;
        cSidePanel.weightx = 0;
        cSidePanel.weighty = 0;
        sidePanelTab.add(getDefaultPanel(), cSidePanel);
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
     * @return default panel
     */
    private JPanel getDefaultPanel() {
        JPanel defaultPanel = new JPanel();
        defaultPanel.setLayout(new GridBagLayout());
        GridBagConstraints cDefaultPanel = new GridBagConstraints();
        int cDefaultPanelY = 0;

        startPauseButton = new JButton("Start");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = cDefaultPanelY;
        cDefaultPanel.insets = new Insets(0,0,5,0);
        startPauseButton.addActionListener(this::startPauseActionPerformed);
        defaultPanel.add(startPauseButton, cDefaultPanel);

        stopButton = new JButton("Stop");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 1;
        cDefaultPanel.gridy = cDefaultPanelY;
        cDefaultPanel.insets = new Insets(0,0,5,0);
        stopButton.addActionListener(this::stopActionPerformed);
        defaultPanel.add(stopButton, cDefaultPanel);
        executor.addPropertyChangeListener(this::finishedPropertyChanged);

        JSeparator separator = new JSeparator();
        GridBagConstraints cgc = new GridBagConstraints();
        cgc.gridy = ++cDefaultPanelY;
        cgc.fill = GridBagConstraints.HORIZONTAL;
        cgc.gridwidth = 2;
        defaultPanel.add(separator, cgc);

        addDefaultControls(defaultPanel, cDefaultPanel, cDefaultPanelY);

        return defaultPanel;
    }

    private void addDefaultControls(JPanel defaultPanel, GridBagConstraints cDefaultPanel, int cDefaultPanelY) {
        JButton showBestSolution = new JButton("Show Best");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.weightx = 0.5;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        cDefaultPanel.insets = new Insets(5,0,0,0);
        showBestSolution.addActionListener(this::showBestSolution);
        defaultPanel.add(showBestSolution, cDefaultPanel);

        JButton scaleToBox = new JButton("Center");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 1;
        cDefaultPanel.gridy = cDefaultPanelY;
        cDefaultPanel.insets = new Insets(5,0,0,0);
        defaultPanel.add(scaleToBox, cDefaultPanel);
        scaleToBox.addActionListener(this::scalingToBox);
        scaleToBox.setSelected(false);

        JButton removeChains = new JButton("Remove All Chains");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        cDefaultPanel.insets = new Insets(0,0,0,0);
        defaultPanel.add(removeChains, cDefaultPanel);
        removeChains.addActionListener(this::removeChainsItemActionPerformed);

        JButton reinsertChain = new JButton("Reinsert One Chain");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 1;
        cDefaultPanel.gridy = cDefaultPanelY;
        defaultPanel.add(reinsertChain, cDefaultPanel);
        reinsertChain.addActionListener(this::reinsertChainItemActionPerformed);

        JButton showGraphInfo = new JButton("Graph Info");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        cDefaultPanel.insets = new Insets(0,0,0,0);
        defaultPanel.add(showGraphInfo, cDefaultPanel);
        showGraphInfo.addActionListener(this::showGraphInfoActionPerformed);

        JButton reinsertAllChains = new JButton("Reinsert All Chains");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 1;
        cDefaultPanel.gridy = cDefaultPanelY;
        defaultPanel.add(reinsertAllChains, cDefaultPanel);
        reinsertAllChains.addActionListener(this::reinsertAllChainsItemActionPerformed);

        if (MainFrame.CONTEST_MODE) {
            JButton organicItem = new JButton("Organic Layout");
            cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
            cDefaultPanel.gridx = 0;
            cDefaultPanel.gridy = ++cDefaultPanelY;
            organicItem.addActionListener(this::organicItemActionPerformed);
            defaultPanel.add(organicItem, cDefaultPanel);

            JButton fppItem = new JButton("FPP");
            cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
            cDefaultPanel.gridx = 1;
            cDefaultPanel.gridy = cDefaultPanelY;
            fppItem.addActionListener(this::fppItemActionPerformed);
            defaultPanel.add(fppItem, cDefaultPanel);

            JButton jitterItem = new JButton("Jitter");
            cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
            cDefaultPanel.gridx = 0;
            cDefaultPanel.gridy = ++cDefaultPanelY;
            jitterItem.addActionListener(this::jitterItemActionPerformed);
            defaultPanel.add(jitterItem, cDefaultPanel);
        }

        enableMinimumAngleDisplay = new JCheckBox("Show Angle");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        cDefaultPanel.weightx = 0.5;
        cDefaultPanel.weighty = 0;
        if (!MainFrame.CONTEST_MODE) {defaultPanel.add(enableMinimumAngleDisplay, cDefaultPanel);}
        enableMinimumAngleDisplay.addItemListener(this::minimumAngleDisplayEnabled);
        enableMinimumAngleDisplay.setSelected(true);

        allowClickCreateNodeEdge = new JCheckBox("Manual Mode");  //No new nodes or edges on click, can't select ports and edges, for manual tuning
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 1;
        cDefaultPanel.gridy = cDefaultPanelY;
        cDefaultPanel.weightx = 0.5;
        cDefaultPanel.weighty = 0;
        if (!MainFrame.CONTEST_MODE) { defaultPanel.add(allowClickCreateNodeEdge, cDefaultPanel); }
        allowClickCreateNodeEdge.addItemListener(this::allowClickCreateNodeEdgeActionPerformed);
        allowClickCreateNodeEdge.setSelected(true);

        allowClickGraphEditor = new JCheckBox("User Mode");  //No new nodes or edges on click, can't select ports and edges, for manual tuning
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        cDefaultPanel.weightx = 0.5;
        cDefaultPanel.weighty = 0;
        defaultPanel.add(allowClickGraphEditor, cDefaultPanel);
        allowClickGraphEditor.addItemListener(this::allowClickGraphEditorActionPerformed);
        allowClickGraphEditor.setSelected(true);

        enableCrossingResolution = new JCheckBox("Crossing Resolution");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        cDefaultPanel.weightx = 0.5;
        cDefaultPanel.weighty = 0;
        enableCrossingResolution.addItemListener(this::enableCrossingResolutionActionPerformed);
        if (!MainFrame.CONTEST_MODE) {
            defaultPanel.add(enableCrossingResolution, cDefaultPanel);
        }
        enableCrossingResolution.setSelected(false);

        enableAngularResolution = new JCheckBox("Angular Resolution");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 1;
        cDefaultPanel.gridy = cDefaultPanelY;
        cDefaultPanel.weightx = 0.5;
        cDefaultPanel.weighty = 0;
        enableAngularResolution.addItemListener(this::enableAngularResolutionActionPerformed);
        if (!MainFrame.CONTEST_MODE) {
            defaultPanel.add(enableAngularResolution, cDefaultPanel);
        }
        enableAngularResolution.setSelected(false);

        enableAspectRatio = new JCheckBox("Aspect Ratio");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        cDefaultPanel.weightx = 0.5;
        cDefaultPanel.weighty = 0;
        enableAspectRatio.addItemListener(this::enableAspectRatioActionPerformed);
        if (!MainFrame.CONTEST_MODE) {
            defaultPanel.add(enableAspectRatio, cDefaultPanel);
        }
        enableAspectRatio.setSelected(false);


        outputTextArea.setLineWrap(true);
        outputTextArea.setRows(10);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        cDefaultPanel.weightx = 1;
        cDefaultPanel.weighty = 0;
        cDefaultPanel.insets = new Insets(10,10,10,10);
        cDefaultPanel.gridwidth = 3;
        defaultPanel.add(scrollPane, cDefaultPanel);
    }


    public JPanel getMiscAlgorithmTab() {
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

        JButton fppItem = new JButton("FPP");
        cCustomPanel.fill = GridBagConstraints.HORIZONTAL;
        cCustomPanel.gridx = 0;
        cCustomPanel.gridy = ++cCustomPanelY;
        fppItem.addActionListener(this::fppItemActionPerformed);
        custom.add(fppItem, cCustomPanel);
        
        JButton jitterItem = new JButton("Jitter");
        cCustomPanel.fill = GridBagConstraints.HORIZONTAL;
        cCustomPanel.gridx = 1;
        cCustomPanel.gridy = cCustomPanelY;
        jitterItem.addActionListener(this::jitterItemActionPerformed);
        custom.add(jitterItem, cCustomPanel);
        
        JButton swapperItem = new JButton("Node Swapper");
        cCustomPanel.fill = GridBagConstraints.HORIZONTAL;
        cCustomPanel.gridx = 0;
        cCustomPanel.gridy = ++cCustomPanelY;
        swapperItem.addActionListener(this::swapperItemActionPerformed);
        custom.add(swapperItem, cCustomPanel);

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

        return sidePanel;
    }

    public boolean getEnableMinimumAngleDisplay() {
        return this.enableMinimumAngleDisplay.isSelected();
    }

    public void setEnableMinimumAngleDisplay(boolean value) {
        this.enableMinimumAngleDisplay.setSelected(value);
    }

    public boolean getAllowClickCreateNodeEdge() {
        return this.allowClickCreateNodeEdge.isSelected();
    }

    public void setEnableCrossingResolution(boolean value) {
        this.enableCrossingResolution.setSelected(value);
    }

    public boolean getEnableCrossingResolution() {
        return this.enableCrossingResolution.isSelected();
    }

    public void setEnableAngularResolution(boolean value) {
        this.enableAngularResolution.setSelected(value);
    }

    public boolean getEnableAngularResolution() {
        return this.enableAngularResolution.isSelected();
    }

    public void setEnableAspectRazio(boolean value) {
        this.enableAspectRatio.setSelected(value);
    }

    public boolean getEnableAspectRatio(){return this.enableAspectRatio.isSelected();}

    public void setAllowClickCreateNodeEdge(boolean value) {
        this.allowClickCreateNodeEdge.setSelected(value);
    }

    public void setAllowClickGraphEditor(boolean value) {
        this.allowClickGraphEditor.setSelected(value);
    }

    public void setOutputTextArea(String outputText) {
        outputTextArea.setText(outputText);
    }

    public JTextArea getOutputTextArea() {
        return  outputTextArea;
    }


    /*********************************************************************
     * Implementation of actions
     ********************************************************************/

    private void showBestSolution(@SuppressWarnings("unused") ActionEvent evt) {
        int nodes= initSidePanel.mainFrame.graph.getNodes().size();
        Optional<Mapper<INode, PointD>> bestPositions;
        //Total Resolution
        if (getEnableAngularResolution() && getEnableCrossingResolution()) {
                bestPositions = initSidePanel.mainFrame.bestSolution.getBestSolutionTotalResolutionPositions(nodes);
        } else if (getEnableAngularResolution()) {
            bestPositions = initSidePanel.mainFrame.bestSolution.getBestSolutionAngularResolutionPositions(nodes);
        }
        else {
            bestPositions = initSidePanel.mainFrame.bestSolution.getBestSolutionPositions(nodes);
        }
        if (!bestPositions.isPresent()) {
            outputTextArea.setText("No Graph Loaded.");
            return;
        }

        Mapper<INode, PointD> nodePositions = bestPositions.get();

        modifyGraph(() -> {
            initSidePanel.removeDefaultListeners();
            PositionMap.applyToGraph(initSidePanel.mainFrame.graph, nodePositions);
            initSidePanel.addDefaultListeners();
        });

        initSidePanel.mainFrame.minimumAngleMonitor.updateAngleInfoBar();
    }

    private void startPauseActionPerformed(ActionEvent evt) {
        startPauseExecution();
    }

    private void stopActionPerformed(ActionEvent evt) {
        stopExecution();
    }

    public void startPauseExecution() {
        if (startPauseButton.getText().equals("Start") || startPauseButton.getText().equals("Continue")) {
            if (executor.isPaused()) {
                startPauseButton.setText("Pause");
                startPauseButton.setBackground(Color.yellow);
                stopButton.setBackground(Color.red);
                executor.unpause();
            } else {
                if (executor.isFinished()) {
                    startPauseButton.setText("Start");
                    startPauseButton.setBackground(null);
                    stopButton.setBackground(null);
                } else {
                    startPauseButton.setText("Pause");
                    startPauseButton.setBackground(Color.yellow);
                    stopButton.setBackground(Color.red);
                    outputTextArea.setText("");
                    executor.start();
                }
            }
        } else if (startPauseButton.getText().equals("Pause")) {
            startPauseButton.setText("Continue");
            startPauseButton.setBackground(Color.green);
            stopButton.setBackground(Color.red);
            executor.pause();
        }
    }

    public void stopExecution() {
        startPauseButton.setText("Start");
        startPauseButton.setBackground(null);
        stopButton.setBackground(null);
        executor.stop();
    }

    private void finishedPropertyChanged(PropertyChangeEvent evt) {
        if ("finished".equals(evt.getPropertyName()) && (boolean)evt.getNewValue()) {
            if (verbose) {
                if (executor.getMaxIterations() >= 0) {
                    outputTextArea.setText("Finished After " + executor.getMaxIterations() + " Iterations.");
                } else {
                    outputTextArea.setText(algorithmName + " has been Stopped.");
                }
            }
            stopExecution();
        } else if ("removeListeners".equals(evt.getPropertyName())) {
            if ((boolean)evt.getNewValue()) {
                initSidePanel.removeDefaultListeners();
            } else {
                initSidePanel.addDefaultListeners();
            }
        }
    }

    private void minimumAngleDisplayEnabled(ItemEvent evt) {
        initSidePanel.masterEnableMinimumAngle.setSelected(evt.getStateChange() == ItemEvent.SELECTED);
    }

    private void allowClickCreateNodeEdgeActionPerformed(ItemEvent evt) {
        initSidePanel.masterAllowClickCreateNodeEdge.setSelected(evt.getStateChange() == ItemEvent.SELECTED);
    }

    private void allowClickGraphEditorActionPerformed(ItemEvent evt) {
        initSidePanel.masterAllowClickGraphEditor.setSelected(evt.getStateChange() == ItemEvent.SELECTED);
    }

    private void enableCrossingResolutionActionPerformed(ItemEvent evt) {
        initSidePanel.masterEnableCrossingResolution.setSelected(evt.getStateChange() == ItemEvent.SELECTED);
    }

    private void enableAngularResolutionActionPerformed(ItemEvent evt) {
        initSidePanel.masterEnableAngularResolution.setSelected(evt.getStateChange() == ItemEvent.SELECTED);
    }

    private void enableAspectRatioActionPerformed(ItemEvent evt) {
        initSidePanel.masterEnableAspectRatio.setSelected(evt.getStateChange() == ItemEvent.SELECTED);
    }

    private void scalingToBox(@SuppressWarnings("unused") ActionEvent evt){
        modifyGraph(() -> {
            initSidePanel.removeDefaultListeners();

            IGraph graph = initSidePanel.mainFrame.graph;
            Mapper<INode, PointD> nodePositions = PositionMap.FromIGraph(graph);
            RectD bounds = BoundingBox.from(nodePositions);
            Scaling.scaleBy(Math.max(1, Math.min((int) (MainFrame.BOX_SIZE[0] / bounds.getWidth() * scaleFactorWhenCentering),
             (int) (MainFrame.BOX_SIZE[1] / bounds.getHeight()*scaleFactorWhenCentering))), nodePositions);
//            Scaling.scaleBy(Math.min(MainFrame.BOX_SIZE[0] / bounds.getWidth() * scaleFactorWhenCentering,
//                   MainFrame.BOX_SIZE[1] / bounds.getHeight()*scaleFactorWhenCentering), nodePositions);
            Centering.moveToCenter(MainFrame.BOX_SIZE[0], MainFrame.BOX_SIZE[1], nodePositions);
            PositionMap.applyToGraph(graph, nodePositions);
            initSidePanel.mainFrame.view.fitGraphBounds();



            bounds = BoundingBox.from(nodePositions);
            outputTextArea.setText("Scaled to Box with Size: " + bounds.getWidth() + "x" + bounds.getHeight());

            initSidePanel.addDefaultListeners();
            Scaling.scaleEdgeSizes(graph,scaleFactorWhenCentering);
        });
    }

    private LinkedList<RemovedChains>  removedChains = new LinkedList<RemovedChains>();
    private void removeChainsItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        if (initSidePanel.mainFrame.graph.getNodes().size() == 0) {
            return;
        }

        modifyGraph(() -> {
            initSidePanel.removeDefaultListeners();
//--------------------------------------------------------------------
//            removedChains.add(depth, Chains.analyze(initSidePanel.mainFrame.graph).remove());
//            outputTextArea.setText("Removed " + removedChains.get(depth).number() + " chains.");
//            if (removedChains.get(depth).number()!=0) {
//            	depth++;
//            }
//====================================================================
            Chains chains = Chains.analyze(initSidePanel.mainFrame.graph);
            initSidePanel.mainFrame.removedChains = chains.remove(chains.number(), initSidePanel.mainFrame.removedChains);
//--------------------------------------------------------------------
            initSidePanel.addDefaultListeners();
        });
        System.out.println("rmeoved "+removedChains.size());
    }

    private void reinsertChainItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        reinsertChain();
    }

    private void reinsertChain() {
        if (initSidePanel.mainFrame.removedChains.number() == 0) {
            return;
        }

        modifyGraph(() -> {
            initSidePanel.removeDefaultListeners();

//--------------------------------------------------------------------
//            //TODO reinsert chains (currently regular reinsert 1 chain)
//            removedChains.get(depth-1).reinsertOne();
//            if (removedChains.get(depth-1).number()!=0) {
//                outputTextArea.setText(removedChains.get(depth-1).number() + " Chains Left in the Stack on level: "+ (depth-1));
//            }
//            else {
//                if (depth-1==0){
//                outputTextArea.setText("All chains were reinserted.");
//            }
//            else {
//                depth--;
//                outputTextArea.setText(removedChains.get(depth-1).number() + " Chains Left in the Stack on level: "+ (depth-1));
//            }
//            }
//====================================================================
            reinsertVerticesItem();
//--------------------------------------------------------------------

            Scaling.scaleNodeSizes(initSidePanel.mainFrame.view);
            Scaling.scaleEdgeSizes(initSidePanel.mainFrame.view);

            initSidePanel.addDefaultListeners();
        });
    }


    private void reinsertAllChainsItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
//--------------------------------------------------------------------
//        if (depth==0 || removedChains.get(depth-1).number() == 0) {
//            removedChains.clear();
//            return;
//        }
//
//        modifyGraph(() -> {
//            initSidePanel.removeDefaultListeners();
//
//            while(depth>0) {
//                depth--;
//                removedChains.get(depth).reinsertAll();
//                outputTextArea.setText(removedChains.get(depth).number() + " Chains Left in the Stack.");
//            }
//
//            Scaling.scaleNodeSizes(initSidePanel.mainFrame.view);
//
//            initSidePanel.addDefaultListeners();
//        });
//====================================================================

        if (initSidePanel.mainFrame.removedChains.number() == 0) {
            return;
        }
        Thread automaticInsertion = new Thread(() -> {
                double startingAngle = initSidePanel.mainFrame.minimumAngleMonitor.getBestCrossingResolution();
                double epsilon = startingAngle/100;
                if (!algorithmName.equals("Random Movement")) {
                    setOutputTextArea("Recommended to use Random Movement!");
                    System.out.println("Recommended to use Random Movement!");
                }
                if(executor.isPaused() || executor.isFinished() || !executor.isRunning()) {
                    startPauseExecution();
                }

                long iterations = 0;
                while(initSidePanel.mainFrame.removedChains.number() > 0) {
                    if (executor.isPaused()) {
                        continue;
                    }
                    if (executor.isFinished()) {
                        return;
                    }
                    double minAngle = initSidePanel.mainFrame.minimumAngleMonitor.getBestCrossingResolution();
                    if (minAngle >= (startingAngle - epsilon)) {
                        //remove chain
                        reinsertVerticesItem();
                        Scaling.scaleNodeSizes(initSidePanel.mainFrame.view);
                        Scaling.scaleEdgeSizes(initSidePanel.mainFrame.view);
                        setOutputTextArea("Reinserting Chains... Chains left: "+initSidePanel.mainFrame.removedChains.number());
                        System.out.println("Reinserting Chains... "+initSidePanel.mainFrame.removedChains.number()+" angle: "+minAngle);
                        iterations = 0;
                        epsilon = startingAngle/100;
                    }
                    if (iterations == Math.pow(10,7)) {
                        epsilon +=0.1;
                        iterations = 0;
                    }

                    iterations++;
                }
                stopExecution();
        });
        automaticInsertion.start();

//--------------------------------------------------------------------
    }
    
    private void showGraphInfoActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        ArrayList<Integer> verticesDegree = new ArrayList<>();

        IGraph graph = initSidePanel.mainFrame.graph;
        for (INode u : graph.getNodes()) {
            int deg = graph.degree(u);
            while(deg >= verticesDegree.size()) {
                verticesDegree.add(0);
            }
            verticesDegree.set(deg, verticesDegree.get(deg) + 1);
        }
        StringBuilder graphInfo = new StringBuilder();
        graphInfo.append("Deg. Num.\n");
        for (int i = 0 ; i < verticesDegree.size(); i++) {
            if (verticesDegree.get(i) > 0) {
                graphInfo.append("  ").append(i).append("   :   ").append(verticesDegree.get(i).toString()).append("\n");
            }
        }
        graphInfo.append("\nTotal Vertices: ").append(graph.getNodes().size())
            .append("\nTotal Edges:    ").append(graph.getEdges().size()).append("\n");

        RectD bounds = BoundingBox.from(PositionMap.FromIGraph(graph));
        double width = bounds.getWidth() < 1 ? 0 : bounds.getWidth();   //smaller than 1 is not a graph
        double height = bounds.getHeight() < 1 ? 0 : bounds.getHeight();
        graphInfo.append("\nCurrent Graph Size: \nX: ").append(width).append("\nY: ").append(height).append("\n\n");

        double minMaxXY[] = new double[4];
        try {
            minMaxXY[0] = graph.getNodes().first().getLayout().getCenter().getX();
            minMaxXY[1] = graph.getNodes().first().getLayout().getCenter().getX();
            minMaxXY[2] = graph.getNodes().first().getLayout().getCenter().getY();
            minMaxXY[3] = graph.getNodes().first().getLayout().getCenter().getY();
        } catch (IllegalArgumentException e) {}
        for (INode u : graph.getNodes()) {
            if (u.getLayout().getCenter().getX() < minMaxXY[0]) {
                minMaxXY[0] = u.getLayout().getCenter().getX();
            } else if (u.getLayout().getCenter().getX() > minMaxXY[1]) {
                minMaxXY[1] = u.getLayout().getCenter().getX();
            }
            if (u.getLayout().getCenter().getY() < minMaxXY[2]) {
                minMaxXY[2] = u.getLayout().getCenter().getY();
            } else if (u.getLayout().getCenter().getY() > minMaxXY[3]) {
                minMaxXY[3] = u.getLayout().getCenter().getY();
            }
        }
        graphInfo.append("Min/Max X Value: ").append(minMaxXY[0]).append(" / ").append(minMaxXY[1]).append("\n");
        graphInfo.append("Min/Max Y Value: ").append(minMaxXY[2]).append(" / ").append(minMaxXY[3]).append("\n\n");

        graphInfo.append("Node Node Overlap: ").append(!LayoutUtils.nodeOverlapFree(graph)).append("\n");
        graphInfo.append("Node Edge Overlap: ").append(!LayoutUtils.edgeOverlapFree(graph)).append("\n");
        graphInfo.append("Negative Nodes: ").append(LayoutUtils.negativeNodes(graph)).append("\n");
        graphInfo.append("Planar Graph: ").append(GraphChecker.isPlanar(new YGraphAdapter(graph).getYGraph())).append("\n");

        graphInfo.append("\nGridded: ").append(GridGraph.isGridGraph(graph)).append("\n");

        outputTextArea.setText(graphInfo.toString());
    }

    private void organicItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        applyLayoutToSelection(new OrganicLayout());
    }

    private void circularItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        applyLayoutToSelection(new CircularLayout());
    }

    private void orthogonalItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        applyLayoutToSelection(new OrthogonalLayout());

    }

    private void treeItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        try {
            applyLayoutToSelection(new TreeLayout());
        } catch (Exception exc) {
            initSidePanel.mainFrame.infoLabel.setText("The input graph is not a tree or a forest.");
        }
    }

    private void fppItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        YGraphAdapter graphAdapter = new YGraphAdapter(initSidePanel.mainFrame.graph);
        if( com.yworks.yfiles.algorithms.GraphChecker.isBiconnected(graphAdapter.getYGraph())) {
            outputTextArea.setText("Graph is biconnected.");
            initSidePanel.removeDefaultListeners();
            ICompoundEdit compoundEdit = initSidePanel.mainFrame.graph.beginEdit("Undo layout", "Redo layout");
            FraysseixPachPollack.FPPSettings fppSettings = new FraysseixPachPollack.FPPSettings();
            fppSettings.boxSizeX = MainFrame.BOX_SIZE[0];
            fppSettings.boxSizeY = MainFrame.BOX_SIZE[1];
            FraysseixPachPollack fpp = new FraysseixPachPollack(initSidePanel.mainFrame.graph, fppSettings);
            fpp.getFFPResult();
            compoundEdit.commit();
            initSidePanel.addDefaultListeners();
        }else{
            outputTextArea.setText("Graph is not biconnected!");
        }

    }
    
    private void jitterItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        initSidePanel.removeDefaultListeners();
        RemoveOverlapsStage removal = new RemoveOverlapsStage((double) 5);
        LayoutGraphAdapter adap = new LayoutGraphAdapter(initSidePanel.mainFrame.graph);
        CopiedLayoutGraph g2 = adap.createCopiedLayoutGraph();
        removal.applyLayout(g2);
        LayoutUtilities.applyLayout(initSidePanel.mainFrame.graph, removal);
        initSidePanel.mainFrame.view.updateUI();
        initSidePanel.addDefaultListeners();
    }

    private void swapperItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        initSidePanel.removeDefaultListeners();
        JTextField nodesTextField = new JTextField("2");
        int nodes = 2;

        JCheckBox checkbox = new JCheckBox("Nodes from Minimum Crossing");
        boolean crossing;

        int result = JOptionPane.showOptionDialog(null, new Object[]{"Number of Nodes to swap: ", nodesTextField, checkbox}, "Swapping Algorithm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        crossing = checkbox.isSelected();

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            nodes = Integer.parseInt(nodesTextField.getText());
            if (nodes > 4 && crossing) {
                JOptionPane.showMessageDialog(null, "No more than four nodes contained in Crossing.\nThe number of nodes to swap will be set to 2. ", "Incorrect Input", JOptionPane.ERROR_MESSAGE);
                nodes = 2;
            }
        } catch (NumberFormatException exc) {
            JOptionPane.showMessageDialog(null, "Incorrect input.\nThe number of nodes to swap will be set to 2.", "Incorrect Input", JOptionPane.ERROR_MESSAGE);
        }

        NodeSwapper.swapNodes(initSidePanel.mainFrame.graph, nodes, crossing);
        initSidePanel.mainFrame.view.updateUI();
        initSidePanel.addDefaultListeners();
    }

    private void applyLayoutToSelection(ILayoutAlgorithm layout) {
        initSidePanel.removeDefaultListeners();

        IGraphSelection selection = initSidePanel.mainFrame.graphEditorInputMode.getGraphSelection();
        ISelectionModel<INode> selectedNodes = selection.getSelectedNodes();

        String layoutName = layout.getClass().getSimpleName().substring(0, layout.getClass().getSimpleName().length() - 6);
        outputTextArea.setText("Performing " + layoutName + " Layout...");
        IEventListener<LayoutEventArgs> doneHandler = ((o, layoutEventArgs) -> {
            outputTextArea.setText(layoutName + " Layout Finished.");
            initSidePanel.addDefaultListeners();
        });

        if (selectedNodes.size() == 0) {
            LayoutUtilities.morphLayout(initSidePanel.mainFrame.view, layout, Duration.ofSeconds(1), doneHandler);
            return;
        }

        FilteredGraphWrapper selectedGraph = new FilteredGraphWrapper(initSidePanel.mainFrame.graph, selectedNodes::isSelected,
                iEdge -> selectedNodes.isSelected(iEdge.getSourceNode()) || selectedNodes.isSelected(iEdge.getTargetNode()));

        PartialLayout partialLayout = new PartialLayout(layout);
        partialLayout.setSubgraphPlacement(SubgraphPlacement.FROM_SKETCH);

        LayoutExecutor executor = new LayoutExecutor(initSidePanel.mainFrame.view, selectedGraph, partialLayout);
        executor.setDuration(Duration.ofSeconds(1));
        executor.setViewportAnimationEnabled(true);
        executor.setEasedAnimationEnabled(true);
        executor.setContentRectUpdatingEnabled(true);
        executor.addLayoutFinishedListener(doneHandler);
        executor.start();
    }

    private void modifyGraph(GraphModifier graphModifier) {
        if (executor == null) {
            graphModifier.modify();
        } else {
            executor.modifyGraph(graphModifier);
        }
    }

    private void reinsertVerticesItem() {
        modifyGraph(() -> {
            initSidePanel.mainFrame.initSidePanel.removeDefaultListeners();
            initSidePanel.mainFrame.removedChains.reinsert(1);
            initSidePanel.mainFrame.initSidePanel.addDefaultListeners();
        });
    }

    public BasicIGraphLayoutExecutor getExecutor() {
        return executor;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
