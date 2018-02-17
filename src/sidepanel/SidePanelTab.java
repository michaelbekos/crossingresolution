package sidepanel;

import algorithms.fpp.FraysseixPachPollack;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.FilteredGraphWrapper;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.LayoutUtilities;
import com.yworks.yfiles.graph.Mapper;
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
import graphoperations.*;
import layout.algo.BasicIGraphLayoutExecutor;
import layout.algo.IGraphLayoutExecutor;
import layout.algo.ILayout;
import layout.algo.layoutinterface.ILayoutConfigurator;
import layout.algo.utils.PositionMap;
import main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;

public class SidePanelTab {
    public JPanel sidePanelTab;
    public String algorithmName;
    public ILayoutConfigurator configurator;
    public ILayout layout;

    private InitSidePanel initSidePanel;

    private IGraphLayoutExecutor executor;
    private JButton startPauseButton;
    private JButton stopButton;
    private JCheckBox enableMinimumAngleDisplay;
    private JCheckBox allowClickCreateNodeEdge;
    private JTextArea outputTextArea;

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
        init();
    }

    private void init() {
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
        executor = new IGraphLayoutExecutor(layout, initSidePanel.mainFrame.graph, initSidePanel.mainFrame.progressBar, -1, 20, itemFactory);
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
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        cDefaultPanel.insets = new Insets(5,0,0,0);
        showBestSolution.addActionListener(this::showBestSolution);
        defaultPanel.add(showBestSolution, cDefaultPanel);

        JButton scaleToBox = new JButton("Scale Me to the Box");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 1;
        cDefaultPanel.gridy = cDefaultPanelY;
        cDefaultPanel.insets = new Insets(5,0,0,0);
        defaultPanel.add(scaleToBox, cDefaultPanel);
        scaleToBox.addActionListener(e -> scalingToBox());
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

        enableMinimumAngleDisplay = new JCheckBox("Show Minimum Angle");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        cDefaultPanel.weightx = 0.5;
        cDefaultPanel.weighty = 0;
        defaultPanel.add(enableMinimumAngleDisplay, cDefaultPanel);
        enableMinimumAngleDisplay.addItemListener(this::minimumAngleDisplayEnabled);
        enableMinimumAngleDisplay.setSelected(false);

        allowClickCreateNodeEdge = new JCheckBox("Manual Mode");  //No new nodes or edges on click, can't select ports and edges, for manual tuning
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 1;
        cDefaultPanel.gridy = cDefaultPanelY;
        cDefaultPanel.weightx = 0.5;
        cDefaultPanel.weighty = 0;
        defaultPanel.add(allowClickCreateNodeEdge, cDefaultPanel);
        allowClickCreateNodeEdge.addItemListener(this::allowClickCreateNodeEdgeActionPerformed);
        allowClickCreateNodeEdge.setSelected(false);

        outputTextArea.setLineWrap(true);
        outputTextArea.setRows(5);
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

    public void setAllowClickCreateNodeEdge(boolean value) {
        this.allowClickCreateNodeEdge.setSelected(value);
    }



    /*********************************************************************
     * Implementation of actions
     ********************************************************************/

    private void showBestSolution(@SuppressWarnings("unused") ActionEvent evt) {
        int nodes= initSidePanel.mainFrame.graph.getNodes().size();
        Optional<Mapper<INode, PointD>> bestPositions = initSidePanel.mainFrame.bestSolution.getBestSolutionPositions(nodes);
        if (!bestPositions.isPresent()) {
            return;
        }

        Mapper<INode, PointD> nodePositions = bestPositions.get();
        initSidePanel.removeDefaultListeners();
        PositionMap.applyToGraph(initSidePanel.mainFrame.graph, nodePositions);
        initSidePanel.addDefaultListeners();
        initSidePanel.mainFrame.minimumAngleMonitor.updateMinimumAngleInfoBar();
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
            stopExecution();
        }
    }

    private void minimumAngleDisplayEnabled(ItemEvent evt) {
        initSidePanel.masterEnableMinimumAngle.setSelected(evt.getStateChange() == ItemEvent.SELECTED);
    }

    private void allowClickCreateNodeEdgeActionPerformed(ItemEvent evt) {
        initSidePanel.masterAllowClickCreateNodeEdge.setSelected(evt.getStateChange() == ItemEvent.SELECTED);
    }

    private void scalingToBox(){
        initSidePanel.removeDefaultListeners();
        Mapper<INode, PointD> nodePositions = PositionMap.FromIGraph(initSidePanel.mainFrame.graph);
        double maxX=0, maxY=0;
        for(INode u : initSidePanel.mainFrame.graph.getNodes()){
            if(u.getLayout().getCenter().getX()>maxX){
                maxX=u.getLayout().getCenter().getX();
            }
            if(u.getLayout().getCenter().getY()>maxY){
                maxY=u.getLayout().getCenter().getY();
            }
        }
        Scaling.scaleBy(Math.min((int)(MainFrame.BOX_SIZE/maxX), (int)(MainFrame.BOX_SIZE/maxY)), nodePositions);
        initSidePanel.mainFrame.graph =  PositionMap.applyToGraph(initSidePanel.mainFrame.graph, nodePositions);
        initSidePanel.mainFrame.view.fitGraphBounds();
        initSidePanel.addDefaultListeners();
    }

    //!!Separate vertex stack for chains (will not mesh with regular remove/reinsert vertexes e.g. chains removed here *have* to be reinserted using the button)!!
    private RemovedChains removedChains;
    private void removeChainsItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        initSidePanel.removeDefaultListeners();
        //lean chain-only version from InitMenuBar::removeVerticesItemActionPerformed
        if (initSidePanel.mainFrame.graph.getNodes().size() > 0){
            removedChains = Chains.analyze(initSidePanel.mainFrame.graph).remove();
        }
        initSidePanel.addDefaultListeners();
    }

    private void reinsertChainItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        initSidePanel.removeDefaultListeners();
        if (this.removedChains != null && this.removedChains.number() > 0) {

            //TODO reinsert chains (currently regular reinsert 1 chain)
            removedChains.reinsertOne();

            double scaleValue = 1 / initSidePanel.mainFrame.view.getZoom();  //scale reinserted nodes
            for (INode u : initSidePanel.mainFrame.graph.getNodes()) {
                initSidePanel.mainFrame.graph.setNodeLayout(u, new RectD(u.getLayout().getX(), u.getLayout().getY(), initSidePanel.mainFrame.graph.getNodeDefaults().getSize().width * scaleValue, initSidePanel.mainFrame.graph.getNodeDefaults().getSize().height * scaleValue));
            }


        }

        initSidePanel.addDefaultListeners();
    }

    private void showGraphInfoActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        ArrayList<Integer> verticesDegree = new ArrayList<>();
        double min_x = Double.MAX_VALUE;
        double min_y = Double.MAX_VALUE;
        double max_x = Double.MIN_VALUE;
        double max_y = Double.MIN_VALUE;
        for (INode u : initSidePanel.mainFrame.graph.getNodes()) {
            if (u.getLayout().getCenter().getX() < min_x) {
                min_x = u.getLayout().getCenter().getX();
            } else if (u.getLayout().getCenter().getX() > max_x) {
                max_x = u.getLayout().getCenter().getX();
            }
            if (u.getLayout().getCenter().getY() < min_y) {
                min_y = u.getLayout().getCenter().getY();
            } else if (u.getLayout().getCenter().getY() > max_y) {
                max_y = u.getLayout().getCenter().getY();
            }

            int deg = u.getPorts().size();
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
        graphInfo.append("\nTotal Vertices: " + initSidePanel.mainFrame.graph.getNodes().size() + "\nTotal Edges:    " + initSidePanel.mainFrame.graph.getEdges().size() + "\n");
        double width = (max_x - min_x) < 1 ? 0 : (max_x - min_x);   //smaller than 1 is not a graph
        double height = (max_y - min_y) < 1 ? 0 : (max_y - min_y);
        graphInfo.append("\nCurrent Graph Size: \nX: " + width + "\nY: " + height + "\n");
        outputTextArea.setText(graphInfo.toString());
    }

    private void organicItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        initSidePanel.removeDefaultListeners();
        applyLayoutToSelection(new OrganicLayout());
        initSidePanel.addDefaultListeners();
    }

    private void circularItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        initSidePanel.removeDefaultListeners();
        applyLayoutToSelection(new CircularLayout());
        initSidePanel.addDefaultListeners();
    }

    private void orthogonalItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        initSidePanel.removeDefaultListeners();
        applyLayoutToSelection(new OrthogonalLayout());
        initSidePanel.addDefaultListeners();

    }

    private void treeItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        initSidePanel.removeDefaultListeners();
        try {
            applyLayoutToSelection(new TreeLayout());
        } catch (Exception exc) {
            initSidePanel.mainFrame.infoLabel.setText("The input graph is not a tree or a forest.");
        } finally {
            initSidePanel.addDefaultListeners();
        }
    }

    private void fppItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        initSidePanel.removeDefaultListeners();
        FraysseixPachPollack fpp = new FraysseixPachPollack(initSidePanel.mainFrame.graph, new FraysseixPachPollack.FPPSettings());
        initSidePanel.addDefaultListeners();

    }

    private void applyLayoutToSelection(ILayoutAlgorithm layout) {
        IGraphSelection selection = initSidePanel.mainFrame.graphEditorInputMode.getGraphSelection();
        ISelectionModel<INode> selectedNodes = selection.getSelectedNodes();

        if (selectedNodes.size() == 0) {
            LayoutUtilities.morphLayout(initSidePanel.mainFrame.view, layout, Duration.ofSeconds(1));
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

        executor.start();
    }

    public BasicIGraphLayoutExecutor getExecutor() {
        return executor;
    }
}
