import com.sun.istack.internal.Nullable;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.SizeD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.graph.styles.PolylineEdgeStyle;
import com.yworks.yfiles.graph.styles.ShinyPlateNodeStyle;
import com.yworks.yfiles.graph.styles.DefaultLabelStyle;
import com.yworks.yfiles.layout.YGraphAdapter;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.layout.orthogonal.OrthogonalLayout;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.view.*;
import com.yworks.yfiles.view.input.*;
import layout.algo.ForceAlgorithmApplier;
import layout.algo.GeneticAlgorithm;
import util.GraphOperations;
import util.Tuple4;
import util.interaction.ThresholdSliders;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

import algorithms.fpp.FraysseixPachPollack;

import static layout.algo.ForceAlgorithmApplier.bestSolution;


/**
 * Created by michael on 28.10.16.
 */
public class MainFrame extends JFrame {
	
	/* Box related issue*/
	private static double boxsize= 10000;
	
	
    /* Graph Drawing related objects */
    private GraphComponent view;
    private IGraph graph;
    private OrganicLayout defaultLayouter;
    private GraphEditorInputMode graphEditorInputMode;
    private GridVisualCreator gridVisualCreator;
    private GraphSnapContext graphSnapContext;

    /* Default Styles */
    private ShinyPlateNodeStyle defaultNodeStyle;
    private PolylineEdgeStyle defaultEdgeStyle;
    private DefaultLabelStyle defaultLabelStyle;

    /* Central gui elements */
    private JLabel infoLabel;
    private JProgressBar progressBar;

    private MinimumAngleMonitor minimumAngleMonitor;

    private JSlider[] sliders;

    private final Double[] springThresholds = {0.01, 0.01, 0.01, 0.1};
    private final Boolean[] algoModifiers = {false, false};
    private int faaRunningTimeGenetic = 250;

    @Nullable
    private ForceAlgorithmApplier faa = null;

    private void finalizeFAA (@Nullable ForceAlgorithmApplier faa) {
        if (faa != null) {
            faa.running = false;
            faa.clearDrawables();
        }
    }

    // for this class, we can instantiate defaultForceAlgorithmApplier and do some post-initializing
    private ForceAlgorithmApplier defaultForceAlgorithmApplier(int iterations) {
        ForceAlgorithmApplier fd = InitForceAlgorithm.defaultForceAlgorithmApplier(iterations, view, progressBar, infoLabel);
        springThresholds[1] = 50 * Math.log(graph.getNodes().size());
        fd.modifiers = springThresholds.clone();
        fd.switches = algoModifiers.clone();
        return fd;
    }

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        this.initComponents();
        this.initMenuBar();

        super.setTitle("Graph Drawing Tool");
        super.setMinimumSize(new Dimension(400, 300));
        super.setExtendedState(MAXIMIZED_BOTH);
        super.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        super.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });
    }

    /**
     * This method is called within the constructor to initialize the form.
     */
    private void initComponents() {

        JPanel progressBarPanel = new JPanel();
        progressBarPanel.setLayout(new GridLayout(1, 2, 10, 10));

        this.infoLabel = new JLabel();
        this.infoLabel.setText("Number of Vertices: 0     Number of Edges: 0");
        progressBarPanel.add(infoLabel);

        this.progressBar = new JProgressBar();
        this.progressBar.setPreferredSize(new Dimension(250, 20));
        this.progressBar.setStringPainted(true);
        progressBarPanel.add(this.progressBar);

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setPreferredSize(new Dimension(300, 300));
        //mainPanel.setLayout(new BorderLayout(0, 10));
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 2;
        c.insets = new Insets(5, 0, 5, 0);
        mainPanel.add(progressBarPanel, c);
        //mainPanel.add(progressBarPanel, BorderLayout.PAGE_END);

        this.view = new GraphComponent();
        this.view.setSize(330, 330);
        this.view.requestFocus();
        view.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        view.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        c.fill = GridBagConstraints.BOTH;
        c.gridy = 1;
        c.weightx = 0.8;
        mainPanel.add(this.view, c);
        //mainPanel.add(this.view, BorderLayout.CENTER);

        this.graph = this.view.getGraph();
        this.graph.setUndoEngineEnabled(true);

        this.graphEditorInputMode = new GraphEditorInputMode();
        this.graphEditorInputMode.setCreateNodeAllowed(true);
        this.graphEditorInputMode.setCreateEdgeAllowed(true);
        this.graphEditorInputMode.getCreateEdgeInputMode().setCreateBendAllowed(false);
        this.graphEditorInputMode.setCreateBendAllowed(false);
        this.graphEditorInputMode.setEditLabelAllowed(true);
        this.graphEditorInputMode.addPopulateItemPopupMenuListener((o, iModelItemPopulateItemPopupMenuEventArgs) -> {
            if (iModelItemPopulateItemPopupMenuEventArgs.getItem() instanceof INode) {
                populateNodePopupMenu(iModelItemPopulateItemPopupMenuEventArgs);
            }
            if (iModelItemPopulateItemPopupMenuEventArgs.getItem() instanceof IEdge) {
                populateEdgePopupMenu(iModelItemPopulateItemPopupMenuEventArgs);
            }
        });
        this.view.setInputMode(this.graphEditorInputMode);

        /* Add four listeners two the graph */
        this.graph.addNodeCreatedListener((o, iNodeItemEventArgs) -> {
            graph.addLabel(iNodeItemEventArgs.getItem(), Integer.toString(graph.getNodes().size() - 1));
            this.graph.setStyle(iNodeItemEventArgs.getItem(), this.defaultNodeStyle.clone());
            this.graph.setStyle(iNodeItemEventArgs.getItem().getLabels().first(), this.defaultLabelStyle.clone());
            infoLabel.setText("Number of Vertices: " + graph.getNodes().size() + "     Number of Edges: " + graph.getEdges().size());
        });

        this.graph.addEdgeCreatedListener((o, iNodeItemEventArgs) -> {
            this.graph.setStyle(iNodeItemEventArgs.getItem(), this.defaultEdgeStyle.clone());
            infoLabel.setText("Number of Vertices: " + graph.getNodes().size() + "     Number of Edges: " + graph.getEdges().size());
        });
        this.graph.addNodeRemovedListener((o, iNodeItemEventArgs) ->
            infoLabel.setText("Number of Vertices: " + graph.getNodes().size() + "     Number of Edges: " + graph.getEdges().size()
        ));

        this.graph.addEdgeRemovedListener((o, iNodeItemEventArgs) ->
            infoLabel.setText("Number of Vertices: " + graph.getNodes().size() + "     Number of Edges: " + graph.getEdges().size()
        ));

        this.graph.addNodeLayoutChangedListener((o, u, iNodeItemEventArgs) -> {
            synchronized(movedNodes){
                movedNodes.add(u);
            }
        });
        //this.graph.addNodeStyleChangedListener((o, iNodeItemEventArgs) -> {
            //view.updateUI();
       // });
        this.view.addUpdatingListener((o, args) -> {
          //  MinimumAngle.resetHighlighting(this.graph);
            Set<INode> movedNodesCP;
            synchronized(movedNodes){
                if(movedNodes.size() <= 0) return;
                movedNodesCP = new HashSet<>(movedNodes);
                movedNodes.clear();
            }
            if (faa != null) {
                faa.resetNodePositions(movedNodesCP);
            }
        });
        this.view.addZoomChangedListener((o, zoomItemEventArgs) -> {
            double scaleValue = 1/this.view.getZoom();
            for(INode u : this.graph.getNodes()){
                this.graph.setNodeLayout(u, new RectD(u.getLayout().getX(),u.getLayout().getY(),this.graph.getNodeDefaults().getSize().width*scaleValue,this.graph.getNodeDefaults().getSize().height*scaleValue));
            }
        });


        /* Add two listeners two the graph */
        this.graphSnapContext = new GraphSnapContext();
        this.graphEditorInputMode.setSnapContext(this.graphSnapContext);
        GridInfo gridInfo = new GridInfo();
        this.gridVisualCreator = new GridVisualCreator(gridInfo);
        this.view.getBackgroundGroup().addChild(this.gridVisualCreator, ICanvasObjectDescriptor.ALWAYS_DIRTY_INSTANCE);
        this.graphSnapContext.setGridSnapType(GridSnapTypes.GRID_POINTS);
        this.graphSnapContext.setNodeGridConstraintProvider(new GridConstraintProvider<>(gridInfo));
        //this.graphSnapContext.setBendGridConstraintProvider(new GridConstraintProvider<>(gridInfo));

        /* Default Node Styling */
        this.defaultNodeStyle = new ShinyPlateNodeStyle();
        this.defaultNodeStyle.setPaint(Color.GRAY);
        this.defaultNodeStyle.setPen(new Pen(Color.GRAY, 1));
        this.defaultNodeStyle.setShadowDrawingEnabled(false);
        this.graph.getNodeDefaults().setStyle(defaultNodeStyle);
        this.graph.getDecorator().getNodeDecorator().getFocusIndicatorDecorator().hideImplementation();
        this.graph.getNodeDefaults().setSize(new SizeD(15,15));

        /* Default Edge Styling */
        this.defaultEdgeStyle = new PolylineEdgeStyle();
        this.defaultEdgeStyle.setPen(Pen.getBlack());
        this.graph.getEdgeDefaults().setStyle(this.defaultEdgeStyle);

        /* Default Label Styling */
        this.defaultLabelStyle = new DefaultLabelStyle();
        this.defaultLabelStyle.setFont(new Font("Dialog", Font.PLAIN, 0));
        this.defaultLabelStyle.setTextPaint(Colors.WHITE);
        this.graph.getNodeDefaults().getLabelDefaults().setStyle(this.defaultLabelStyle);
        this.graph.getEdgeDefaults().getLabelDefaults().setStyle(this.defaultLabelStyle);

        super.getContentPane().setLayout(new java.awt.BorderLayout(20, 20));
        super.getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);
        //super.getContentPane().add(mainPanel, c);

        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c. insets = new Insets(5, 0, 10, 0);
        //mainPanel.add(toolBar, BorderLayout.PAGE_START);

        this.defaultLayouter = new OrganicLayout();
        this.defaultLayouter.setPreferredEdgeLength(100);
        this.defaultLayouter.setMinimumNodeDistance(100);

        this.minimumAngleMonitor = new MinimumAngleMonitor(view, graph, infoLabel);

        initSidePanel(mainPanel, c);
    }

    private final Set<INode> movedNodes = new HashSet<>();

    private void initSidePanel(JPanel mainPanel, GridBagConstraints c) {
        Tuple4<JPanel, JSlider[], JSpinner[], Integer> slidPanelSlidersCount = ThresholdSliders.create(springThresholds, new String[]{"Electric force", " ", "Crossing force", "Incident edges force"});
        JPanel sidePanel = slidPanelSlidersCount.a;
        this.sliders = slidPanelSlidersCount.b;
        slidPanelSlidersCount.c[1].setVisible(false);
        sliders[1].setVisible(false);
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
        geneticSlider.setValue(faaRunningTimeGenetic);
        geneticSlider.addChangeListener(e-> {
            JSlider source = (JSlider) e.getSource();
            faaRunningTimeGenetic = source.getValue();
            System.out.println("magic number:" + faaRunningTimeGenetic);
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
// FPP start
        JButton startFPP = new JButton("Start De Fraysseix Pach Pollack");
               // stopFPP  = new JButton("Stop De Fraysseix Pach Pollack");
        startFPP.addActionListener(this::startFPPClicked);
      //  stopFPP.addActionListener(this::stopFPPClicked);

        sidePanel.add(startFPP, cSidePanel);
        cSidePanel.gridx = 1;
       // sidePanel.add(stopFPP, cSidePanel);

        cSidePanel.gridy = sidePanelNextY++;
        cSidePanel.gridx = 0;
// FPP ende
        JButton showForces = new JButton("Show forces");
        showForces.addActionListener(e -> {
            if (faa == null) {
                faa = defaultForceAlgorithmApplier(0);
            }
            faa.showForces();
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
            ForceAlgorithmApplier.applyNodePositionsToGraph(graph, nodePositions);
            String msg = minCrossingAngle.map(d -> "Minimum crossing angle: " + d.toString()).orElse("No crossings!");
            msg += "\n";
            msg += "Modifiers:\n";
            for(int i = 0; i < mods.length; i++){
                Double d = mods[i];
                sliders[i].setValue((int) (1000 * d));
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
        showForceAlgoState.addActionListener(e -> faa.showNodePositions());
        sidePanel.add(showForceAlgoState, cSidePanel);

        JButton scaleToBox = new JButton("Scale me to the box");
        cSidePanel.gridx = 1;
        sidePanel.add(scaleToBox, cSidePanel);
        scaleToBox.addActionListener(e -> scalingToBox());
        scaleToBox.setSelected(false);


        cSidePanel.gridy = sidePanelNextY++;

        JCheckBox enableMinimumAngleDisplay = new JCheckBox("Show minimum angle");
        cSidePanel.gridx = 0;
        cSidePanel.gridy = sidePanelNextY;
        sidePanel.add(enableMinimumAngleDisplay, cSidePanel);
        enableMinimumAngleDisplay.addItemListener(this::minimumAngleDisplayEnabled);
        enableMinimumAngleDisplay.setSelected(false);

        

        JCheckBox allowClickCreateNodeEdge = new JCheckBox("Manual Mode");  //No new nodes or edges on click, can't select ports and edges, for manual tuning
        cSidePanel.gridx = 1;
        sidePanel.add(allowClickCreateNodeEdge, cSidePanel);
        allowClickCreateNodeEdge.addItemListener(this::allowClickCreateNodeEdgeActionPerformed);
        allowClickCreateNodeEdge.setSelected(true);
    }


    private void initMenuBar() {
        InitMenuBar menuBar = new InitMenuBar(
            this,
            graph,
            infoLabel,
            view,
            progressBar,
            graphEditorInputMode,
            defaultLayouter,
            graphSnapContext,
            gridVisualCreator,
            minimumAngleMonitor
        );
        super.setJMenuBar(menuBar.initMenuBar());
    }


    /*********************************************************************
     * Popup Menus
     ********************************************************************/
    private void populateEdgePopupMenu(PopulateItemPopupMenuEventArgs<IModelItem> args) {
        ISelectionModel<IEdge> selection = this.view.getSelection().getSelectedEdges();
        if (args.getItem() instanceof IEdge) {
            IEdge edge = (IEdge) args.getItem();
            selection.clear();
            selection.setSelected(edge, true);
            this.view.setCurrentItem(edge);

            JPopupMenu popupMenu = (JPopupMenu) args.getMenu();

            javax.swing.JMenuItem removeVertex = new javax.swing.JMenuItem("Delete");
            removeVertex.setIcon(new ImageIcon(getClass().getResource("/resources/delete-16.png")));
            removeVertex.addActionListener(evt -> {
                graph.remove(edge);
                view.updateUI();
            });

            popupMenu.add(removeVertex);
            args.setHandled(true);
        }
    }

    private void populateNodePopupMenu(PopulateItemPopupMenuEventArgs<IModelItem> args) {
        ISelectionModel<INode> selection = this.view.getSelection().getSelectedNodes();
            if (args.getItem() instanceof INode) {
            INode node = (INode) args.getItem();
            selection.clear();
            selection.setSelected(node, true);
            this.view.setCurrentItem(node);

            JPopupMenu popupMenu = (JPopupMenu) args.getMenu();

            JMenuItem editLabel = new JMenuItem(node.getLabels().first().getText().equals("") ? "Add Label" : "Edit Label");
            editLabel.setIcon(new ImageIcon(getClass().getResource("/resources/star-16.png")));
            editLabel.addActionListener(evt -> {
                JTextField labelTextField = new JTextField(node.getLabels().first().getText());
                int result = JOptionPane.showOptionDialog(null, new Object[]{"Label: ", labelTextField}, node.getLabels().first().getText().equals("") ? "Add Label" : "Edit Label", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (result == JOptionPane.OK_OPTION) {
                    graph.setLabelText(node.getLabels().first(), labelTextField.getText());
                }
            });

            javax.swing.JMenuItem removeVertex = new javax.swing.JMenuItem("Delete");
            removeVertex.setIcon(new ImageIcon(getClass().getResource("/resources/delete-16.png")));
            removeVertex.addActionListener(evt -> {
                graph.remove(node);
                view.updateUI();
            });

            popupMenu.add(editLabel);
            popupMenu.add(removeVertex);
            args.setHandled(true);
        }
    }

    /*********************************************************************
     * Implementation of actions
     ********************************************************************/

    private void startGeneticClicked(@SuppressWarnings("unused") ActionEvent evt){
        if(geneticAlgorithm == null || !geneticAlgorithm.running){
            ForceAlgorithmApplier.init();
            initializeGeneticAlgorithm();
            this.graphEditorInputMode.setCreateNodeAllowed(false);
            geneticAlgorithmThread.start();
        }
    }

    private void stopGeneticClicked(@SuppressWarnings("unused") ActionEvent evt){
        geneticAlgorithm.running = false;
        this.graphEditorInputMode.setCreateNodeAllowed(true);
    }

    private void startForceClicked(@SuppressWarnings("unused") ActionEvent evt){
        if(faa == null || !faa.running){
            ForceAlgorithmApplier.init();
            ForceAlgorithmApplier fd = defaultForceAlgorithmApplier(-1);
            fd.modifiers = springThresholds;
            fd.switches = algoModifiers;
            finalizeFAA(faa);
            faa = fd;
            Thread thread = new Thread(fd);
            this.graphEditorInputMode.setCreateNodeAllowed(false);
            thread.start();
            this.view.updateUI();
        }
    }

    private void stopForceClicked(@SuppressWarnings("unused") ActionEvent evt){
        if (faa != null) {
            faa.running = false;
            this.graphEditorInputMode.setCreateNodeAllowed(true);
        }
    }

    private void startFPPClicked(@SuppressWarnings("unused") ActionEvent evt){

        YGraphAdapter graphAdapter = new YGraphAdapter(this.view.getGraph());
        if (com.yworks.yfiles.algorithms.PlanarEmbedding.isPlanar(graphAdapter.getYGraph())) {
            FraysseixPachPollack fpp = new FraysseixPachPollack(view.getGraph(), new FraysseixPachPollack.FPPSettings());
            //fpp.run();
            this.view.fitContent();
            this.view.updateUI();
        } else {
            this.infoLabel.setText("The input graph is not planar");
        }

    }

  /*  private void stopFPPClicked(@SuppressWarnings("unused") ActionEvent evt){
        if (faa != null) {
            faa.running = false;
            this.graphEditorInputMode.setCreateNodeAllowed(true);
        }
    }
*/

    void springEmbedderItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        JTextField iterationsTextField = new JTextField("1000");
        int iterations = 1000;

        int result = JOptionPane.showOptionDialog(null, new Object[]{"Number of Iterations: ", iterationsTextField}, "Algorithm Properties", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

        if (result == JOptionPane.OK_OPTION) {
            try {
                iterations = Integer.parseInt(iterationsTextField.getText());
            } catch (NumberFormatException exc) {
                JOptionPane.showMessageDialog(null, "Incorrect input.\nThe number of iterations will be set to 5000.", "Incorrect Input", JOptionPane.ERROR_MESSAGE);
            }
        }
        else return;

        ForceAlgorithmApplier fd = defaultForceAlgorithmApplier(iterations);
        finalizeFAA(faa);
        faa = fd;


        Thread thread = new Thread(fd);
        thread.start();
        this.view.updateUI();
    }

    private void minimumAngleDisplayEnabled(ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            minimumAngleMonitor.registerGraphChangedListeners();
        } else if (evt.getStateChange() == ItemEvent.DESELECTED) {
            minimumAngleMonitor.removeGraphChangedListeners();
        }
    }
    
    private void scalingToBox(){
    Mapper<INode, PointD> nodePositions = ForceAlgorithmApplier.initPositionMap(graph);
    double maxX=0, maxY=0, minY=boxsize;
    for(INode u : graph.getNodes()){
    	if(u.getLayout().getCenter().getX()>maxX){
    		maxX=u.getLayout().getCenter().getX();
    	}
    	if(u.getLayout().getCenter().getY()<minY){
    		minY=u.getLayout().getCenter().getY();
    	}
    	if(u.getLayout().getCenter().getY()>maxY){
    		maxY=u.getLayout().getCenter().getY();
    	}
    }
    nodePositions = GraphOperations.scaleUpProcess(graph,nodePositions, Math.min((int)(boxsize/maxX), (int)(boxsize/maxY)));
    this.graph =  ForceAlgorithmApplier.applyNodePositionsToGraph(graph, nodePositions);
    this.view.fitGraphBounds();
    }    

    private void allowClickCreateNodeEdgeActionPerformed(ItemEvent evt) {
        this.graphEditorInputMode.setCreateNodeAllowed((evt.getStateChange() == ItemEvent.DESELECTED));     //no new nodes
        this.graphEditorInputMode.setCreateEdgeAllowed((evt.getStateChange() == ItemEvent.DESELECTED));     //no new edges
        this.graphEditorInputMode.setEditLabelAllowed((evt.getStateChange() == ItemEvent.DESELECTED));      //no editing of labels
        this.graphEditorInputMode.setShowHandleItems((evt.getStateChange() == ItemEvent.DESELECTED) ? GraphItemTypes.ALL : GraphItemTypes.NONE); //no resizing of nodes nor selection of ports
        this.graphEditorInputMode.setDeletableItems((evt.getStateChange() == ItemEvent.DESELECTED) ? GraphItemTypes.ALL : GraphItemTypes.NONE);  //no deleting of nodes
        this.graphEditorInputMode.setSelectableItems((evt.getStateChange() == ItemEvent.DESELECTED) ? GraphItemTypes.ALL : GraphItemTypes.NODE); //no selecting of edges (only nodes)
    }

    private void forceDirectionPerpendicularActionPerformed(@SuppressWarnings("unused") ActionEvent evt){    this.algoModifiers[0] = true; }
    private void forceDirectionNonPerpendicularActionPerformed(@SuppressWarnings("unused") ActionEvent evt){ this.algoModifiers[0] = false; }

    private void optimizingAngleNintyActionPerformed(@SuppressWarnings("unused") ActionEvent evt) { this.algoModifiers[1] = true; }
    private void optimizingAngleSixtyActionPerformed(@SuppressWarnings("unused") ActionEvent evt) { this.algoModifiers[1] = false; }


    private GeneticAlgorithm<ForceAlgorithmApplier> geneticAlgorithm;
    private Thread geneticAlgorithmThread;
    private void initializeGeneticAlgorithm(){
        LinkedList<ForceAlgorithmApplier> firstFAAs = new LinkedList<>();
        firstFAAs.add(defaultForceAlgorithmApplier(faaRunningTimeGenetic));
        LayoutUtilities.applyLayout(graph, new OrthogonalLayout());
        firstFAAs.add(defaultForceAlgorithmApplier(faaRunningTimeGenetic));
        LayoutUtilities.applyLayout(graph, new OrganicLayout());
        firstFAAs.add(defaultForceAlgorithmApplier(faaRunningTimeGenetic));
        geneticAlgorithm = InitGeneticAlgorithm.defaultGeneticAlgorithm(firstFAAs, graph);
        geneticAlgorithmThread = new Thread(geneticAlgorithm);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            // check for 'os.name == Windows 7' does not work, since JDK 1.4 uses the compatibility mode
            if (!"com.sun.java.swing.plaf.motif.MotifLookAndFeel".equals(UIManager.getSystemLookAndFeelClassName()) && !"com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(UIManager.getSystemLookAndFeelClassName()) && !UIManager.getSystemLookAndFeelClassName().equals(UIManager.getLookAndFeel().getClass().getName()) && !(System.getProperty("java.version").startsWith("1.4") && System.getProperty("os.name").startsWith("Windows") && "6.1".equals(System.getProperty("os.version")))) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new MainFrame().setVisible(true));

    }


}
