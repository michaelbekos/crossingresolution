import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.SizeD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.graph.styles.INodeStyle;
import com.yworks.yfiles.graph.styles.PolylineEdgeStyle;
import com.yworks.yfiles.graph.styles.ShinyPlateNodeStyle;
import com.yworks.yfiles.graph.styles.SimpleLabelStyle;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.view.*;
import com.yworks.yfiles.view.input.*;
import com.yworks.yfiles.layout.orthogonal.OrthogonalLayout;

import layout.algo.*;
import algorithms.graphs.MinimumAngle;
import layout.algo.NodeSwapper;
import util.*;
import util.interaction.*;
import util.graph2d.*;
import util.graph2d.LineSegment;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.*;


/**
 * Created by michael on 28.10.16.
 */
public class MainFrame extends JFrame {

    /* Graph Drawing related objects */
    private GraphComponent view;
    private IGraph graph;
    private OrganicLayout defaultLayouter;
    private GraphEditorInputMode graphEditorInputMode;
    private GridVisualCreator gridVisualCreator;
    private GraphSnapContext graphSnapContext;
    private boolean isGridVisible;

    /* Default Styles */
    private ShinyPlateNodeStyle defaultNodeStyle;
    private PolylineEdgeStyle defaultEdgeStyle;
    private SimpleLabelStyle defaultLabelStyle;

    /* Object that keeps track of the latest open/saved file */
    private String fileNamePath;
    private String fileNamePathFolder;

    /* Central gui elements */
    private JLabel infoLabel;
    private JProgressBar progressBar;
    private JMenu editMenu = new JMenu();
    public JMenuBar mainMenuBar = new JMenuBar();

    private JPanel sidePanel;
    private int sidePanelFirstY = 0, sidePanelNextY;


    private boolean perpendicular = true;
    private boolean createNodeAllowed = true;
    private boolean optimizingNinty = true;
    JSlider[] sliders;
    
    public final Double[] springThreshholds = new Double[]{0.01, 0.01, 0.01, 0.1};
    public final Boolean[] algoModifiers = new Boolean[]{false, false};
    private int faaRunningTimeGenetic = 250;

    private Maybe<ForceAlgorithmApplier> faa = Maybe.nothing();

    /* Object that tracks removed/replaced Vertices */
    private INode[][] removedVertices;

    public static final Consumer<Maybe<ForceAlgorithmApplier>> finalizeFAA = Maybe.lift(f -> {
        f.running = false;
        f.clearDrawables();
    });

    // for this class, we can instantiate defaultForceAlgorithmApplier and do some post-initializing
    public ForceAlgorithmApplier defaultForceAlgorithmApplier(int iterations){
        ForceAlgorithmApplier fd = InitForceAlgorithm.defaultForceAlgorithmApplier(iterations, view, Maybe.just(progressBar), Maybe.just(infoLabel));
        springThreshholds[1] = 50 * Math.log(graph.getNodes().size());
        fd.modifiers = springThreshholds.clone();
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
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
        this.graph.addNodeRemovedListener((o, iNodeItemEventArgs) -> {
            infoLabel.setText("Number of Vertices: " + graph.getNodes().size() + "     Number of Edges: " + graph.getEdges().size());
        });

        this.graph.addEdgeRemovedListener((o, iNodeItemEventArgs) -> {
            infoLabel.setText("Number of Vertices: " + graph.getNodes().size() + "     Number of Edges: " + graph.getEdges().size());
        });

        this.graph.addNodeLayoutChangedListener((o, u, iNodeItemEventArgs) -> {
            synchronized(movedNodes){
                movedNodes.add(u);
            }
        });
        //this.graph.addNodeStyleChangedListener((o, iNodeItemEventArgs) -> {
            //view.updateUI();
       // });
        this.view.addUpdatingListener((o, args) -> {
            MinimumAngle.resetHighlighting(this.graph);
            Set<INode> movedNodesCP;
            synchronized(movedNodes){
                if(movedNodes.size() <= 0) return;
                movedNodesCP = new HashSet<>(movedNodes);
                movedNodes.clear();
            }
            faa.andThen(f -> {
                //f.clearDrawables();
                f.resetNodePositions(movedNodesCP);
            });
        });

        /* Add two listeners two the graph */
        this.graphSnapContext = new GraphSnapContext();
        this.graphEditorInputMode.setSnapContext(this.graphSnapContext);
        GridInfo gridInfo = new GridInfo();
        this.gridVisualCreator = new GridVisualCreator(gridInfo);
        this.view.getBackgroundGroup().addChild(this.gridVisualCreator, ICanvasObjectDescriptor.ALWAYS_DIRTY_INSTANCE);
        this.isGridVisible = true;
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
        this.defaultLabelStyle = new SimpleLabelStyle();
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

        initSidePanel(mainPanel, c);
    }

    Set<INode> movedNodes = new HashSet<>();

    private void initSidePanel(JPanel mainPanel, GridBagConstraints c) {
        Tuple4<JPanel, JSlider[], JSpinner[], Integer> slidPanelSlidersCount = ThresholdSliders.create(springThreshholds, new String[]{"Electric force", " ", "Crossing force", "Incident edges force"});
        this.sidePanel = slidPanelSlidersCount.a;
        this.sliders = slidPanelSlidersCount.b;
        slidPanelSlidersCount.c[1].setVisible(false);
        sliders[1].setVisible(false);
        sidePanelNextY = slidPanelSlidersCount.d;
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
        JButton showForces = new JButton("Show forces");
        showForces.addActionListener(e -> {
            if(!faa.hasValue()){
                faa = Maybe.just(defaultForceAlgorithmApplier(0));
            }
            faa.get().showForces();
        });
        sidePanel.add(showForces, cSidePanel);

        cSidePanel.gridx = 1;
        JButton showBestSolution = new JButton("Show best");
        showBestSolution.addActionListener(e -> {
            ForceAlgorithmApplier.bestSolution.andThen(nm_mca_da_ba -> {
                Mapper<INode, PointD> nodePositions = nm_mca_da_ba.a;
                Maybe<Double> minCrossingAngle = nm_mca_da_ba.b;
                Double[] mods = nm_mca_da_ba.c;
                Boolean[] switchs = nm_mca_da_ba.d;
                ForceAlgorithmApplier.applyNodePositionsToGraph(graph, nodePositions);
                String msg = minCrossingAngle.fmap(d -> "Minimum crossing angle: " + d.toString()).getDefault("No crossings!");
                msg += "\n";
                msg += "Modifiers:\n";
                for(int i = 0; i < mods.length; i++){
                    Double d = mods[i];
                    sliders[i].setValue((int) (1000 * d));
                    msg += "\t" + d.toString() + "\n";
                }
                msg += "\n";
                msg += "Switches:\n";
                for(Boolean b: switchs){
                    msg += "\n\t" + b.toString() + "\n";
                }
                JOptionPane.showMessageDialog(null, msg);
            });
        });
        sidePanel.add(showBestSolution, cSidePanel);

        cSidePanel.gridy = sidePanelNextY++;
        cSidePanel.gridx = 0;
        JButton showForceAlgoState = new JButton("Show state");
        showForceAlgoState.addActionListener(e -> {
            faa.andThen(fa -> fa.showNodePositions());
        });
        sidePanel.add(showForceAlgoState, cSidePanel);
    }

    private void initMenuBar() {

        JMenu layoutMenu = new JMenu();
        JMenu viewMenu = new JMenu();
        JMenu editMenu = new JMenu();

        InitMenuBar menuBar = new InitMenuBar(mainMenuBar, layoutMenu, editMenu, viewMenu, this.graph, this.infoLabel, this.view, this.progressBar, this.graphEditorInputMode,
                this.defaultLayouter, this.fileNamePathFolder, this.fileNamePath, this.removedVertices);
        mainMenuBar = menuBar.initMenuBar();
        JMenuItem springEmbedderItem = new JMenuItem();
        springEmbedderItem.setIcon(new ImageIcon(getClass().getResource("/resources/layout-16.png")));
        springEmbedderItem.setText("Spring Embedder");
        springEmbedderItem.addActionListener(this::springEmbedderItemActionPerformed);
        layoutMenu.add(springEmbedderItem);

        JMenuItem jitterItem = new JMenuItem();
        jitterItem.setIcon(new ImageIcon(getClass().getResource("/resources/layout-16.png")));
        jitterItem.setText("Jitter");
        jitterItem.addActionListener(this::jitterItemActionPerformed);
        layoutMenu.add(jitterItem);


        JMenuItem swapperItem = new JMenuItem();
        swapperItem.setIcon(new ImageIcon(getClass().getResource("/resources/layout-16.png")));
        swapperItem.setText("Nodes Swapper");
        swapperItem.addActionListener(this::swapperItemActionPerformed);
        layoutMenu.add(swapperItem);

        JMenuItem gridPositioningItem = new JMenuItem();
        gridPositioningItem.setIcon(new ImageIcon(getClass().getResource("/resources/layout-16.png")));
        gridPositioningItem.setText("Respective Crossing Angle Gridding");
        gridPositioningItem.addActionListener(this::gridCrossingItemActionPerformed);
        layoutMenu.add(gridPositioningItem);
        JMenuItem graphGridItem = new JMenuItem();
        graphGridItem.setIcon(new ImageIcon(getClass().getResource("/resources/layout-16.png")));
        graphGridItem.setText("Graph Gridding");
        graphGridItem.addActionListener(this::graphGridItemActionPerformed);
        layoutMenu.add(graphGridItem);

        JMenuItem dirtyGridPositioningItem = new JMenuItem();
        dirtyGridPositioningItem.setIcon(new ImageIcon(getClass().getResource("/resources/layout-16.png")));
        dirtyGridPositioningItem.setText("Quick and Dirty Gridding");
        dirtyGridPositioningItem.addActionListener(this::quickAndDirtyGridItemActionPerformed);
        layoutMenu.add(dirtyGridPositioningItem);

        JMenuItem gridItem = new JMenuItem();
        gridItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));
        gridItem.setIcon(new ImageIcon(getClass().getResource("/resources/grid-16.png")));
        gridItem.setText("Grid On/Off");
        gridItem.addActionListener(this::gridItemActionPerformed);
        viewMenu.add(gridItem);

        JMenu analyzeMenu = new JMenu();
        analyzeMenu.setIcon(new ImageIcon(getClass().getResource("/resources/star-16.png")));
        analyzeMenu.setText("Analyze Graph");

        JMenuItem minimumCrossingAngleMenu = new JMenuItem();
        minimumCrossingAngleMenu.setIcon(new ImageIcon(getClass().getResource("/resources/star-16.png")));
        minimumCrossingAngleMenu.setText("Minimum Angle");
        minimumCrossingAngleMenu.addActionListener(this::minimumCrossingAngleMenuActionPerformed);

        JMenuItem overlappingNodesMenu = new JMenuItem();
        overlappingNodesMenu.setIcon(new ImageIcon(getClass().getResource("/resources/star-16.png")));
        overlappingNodesMenu.setText("Show Overlapping Nodes");
        overlappingNodesMenu.addActionListener(this::overlappingNodesMenuActionPerformed);

        analyzeMenu.add(minimumCrossingAngleMenu);
        analyzeMenu.add(overlappingNodesMenu);
        viewMenu.add(analyzeMenu);
        viewMenu.add(new JSeparator());
        mainMenuBar.add(layoutMenu);
        super.setJMenuBar(mainMenuBar);
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
    private void gridCrossingItemActionPerformed(ActionEvent evt) {
        boolean gridding = false;
        if(ForceAlgorithmApplier.class != null) {
            while (gridding == false) {
                GridPositioning.gridGraph(this.graph);
                //grid.removeOverlapsOrganic();
                GridPositioning.removeOverlaps(this.graph, 0.1);
                if(GridPositioning.isGridGraph(this.graph)){
                    gridding = true;
                }
            }
        }
        System.out.println("Graph is gridded: " + GridPositioning.isGridGraph(this.graph));
        this.view.updateUI();
    }

    private void quickAndDirtyGridItemActionPerformed(ActionEvent evt){
        if(ForceAlgorithmApplier.class != null){
            GridPositioning.gridQuickAndDirty(this.graph);
            if(!GridPositioning.isGridGraph(this.graph)){
                System.out.println("Error occured with the gridding of the graph");
            }
        }
        System.out.println("Graph is gridded: " + GridPositioning.isGridGraph(this.graph));
        this.view.updateUI();
    }

    private void graphGridItemActionPerformed(ActionEvent evt) {
        boolean gridding = false;
        if(ForceAlgorithmApplier.class != null) {
            while (gridding == false) {
                GridPositioning.gridGraphFast(this.graph);
                //grid.removeOverlapsOrganic();
                GridPositioning.removeOverlaps(this.graph, 0.1);
                if(GridPositioning.isGridGraph(this.graph)){
                    gridding = true;
                }
            }
        }
        System.out.println("Graph is gridded: " + GridPositioning.isGridGraph(this.graph));
        this.view.updateUI();
    }

    public void startGeneticClicked(ActionEvent e){
        if(geneticAlgorithm == null || geneticAlgorithm.running == false){
            ForceAlgorithmApplier.init();
            initializeGeneticAlgorithm();
            this.graphEditorInputMode.setCreateNodeAllowed(false);
            geneticAlgorithmThread.start();
        }
    }

    public void stopGeneticClicked(ActionEvent e){
        geneticAlgorithm.running = false;
        this.graphEditorInputMode.setCreateNodeAllowed(true);
    }

    public void startForceClicked(ActionEvent e){
        if(!faa.hasValue() || faa.get().running == false){
            ForceAlgorithmApplier.init();
            ForceAlgorithmApplier fd = defaultForceAlgorithmApplier(-1);
            fd.modifiers = springThreshholds;
            fd.switches = algoModifiers;
            MainFrame.finalizeFAA.accept(faa);
            faa = Maybe.just(fd);
            Thread thread = new Thread(fd);
            this.graphEditorInputMode.setCreateNodeAllowed(false);
            thread.start();
            this.view.updateUI();
        }
    }

    public void stopForceClicked(ActionEvent e){
        faa.andThen(f -> {
            f.running = false;
            this.graphEditorInputMode.setCreateNodeAllowed(true);
        });

    }


    private void springEmbedderItemActionPerformed(ActionEvent evt) {
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
        MainFrame.finalizeFAA.accept(faa);
        faa = Maybe.just(fd);


        Thread thread = new Thread(fd);
        thread.start();
        this.view.updateUI();
    }

    private void gridItemActionPerformed(ActionEvent evt) {
        if (this.isGridVisible) {
            this.isGridVisible = false;
            this.graphSnapContext.setGridSnapType(GridSnapTypes.NONE);
        } else {
            this.isGridVisible = true;
            this.graphSnapContext.setGridSnapType(GridSnapTypes.GRID_POINTS);
        }
        this.gridVisualCreator.setVisible(this.isGridVisible);
        this.view.updateUI();
    }

    private void swapperItemActionPerformed(ActionEvent evt){
        JTextField nodesTextField = new JTextField("2");
        int nodes = 2;

        JCheckBox checkbox = new JCheckBox("Nodes from Minimum Crossing");
        boolean crossing;

        int result = JOptionPane.showOptionDialog(null, new Object[]{"Number of Nodes to swap: ", nodesTextField, checkbox}, "Swapping Algorithm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        crossing = checkbox.isSelected();
        if (result == JOptionPane.OK_OPTION) {
            try {
                nodes = Integer.parseInt(nodesTextField.getText());
                if(nodes > 4 && crossing) {
                    JOptionPane.showMessageDialog(null, "No more than four nodes contained in Crossing.\nThe number of nodes to swap will be set to 2. ", "Incorrect Input", JOptionPane.ERROR_MESSAGE);
                    nodes = 2;
                }
            } catch (NumberFormatException exc) {
                JOptionPane.showMessageDialog(null, "Incorrect input.\nThe number of nodes to swap will be set to 2.", "Incorrect Input", JOptionPane.ERROR_MESSAGE);
            }
        }
        else return;

        NodeSwapper.swapNodes(this.graph, nodes, crossing);
        this.view.updateUI();

    }

    private void jitterItemActionPerformed(ActionEvent evt) {
        GridPositioning.removeOverlaps(this.graph, 5);
        this.view.updateUI();
    }

    private void minimumCrossingAngleMenuActionPerformed(ActionEvent evt){
        Maybe<Tuple3<LineSegment, LineSegment, Intersection>>
                minAngleCr = MinimumAngle.getMinimumAngleCrossing(graph, Maybe.nothing());
        Maybe<String> labText = minAngleCr.fmap(cr -> {
            String text = "Minimum Angle: " + cr.c.angle.toString();
            if(cr.a.n1.hasValue() && cr.b.n1.hasValue()){
                text += " | Nodes: " + cr.a.n1.get().getLabels().first().getText();
                text += " , " +  cr.a.n2.get().getLabels().first().getText();
                text += " | " +  cr.b.n1.get().getLabels().first().getText();
                text += " , " +  cr.b.n2.get().getLabels().first().getText();
            }
            MinimumAngle.resetHighlighting(this.graph);
            MinimumAngle.highlightCrossing(cr);
            view.updateUI();
            return text;
        });
        infoLabel.setText(labText.getDefault("Graph has no crossings."));
    }

    private void overlappingNodesMenuActionPerformed(ActionEvent evt){
        Set<Double> seenCoordinatesX = new HashSet<>();
        Set<Double> seenCoordinatesY = new HashSet<>();
        
        for(INode u : this.graph.getNodes()){
            double u_x = u.getLayout().getCenter().getX();
            double u_y = u.getLayout().getCenter().getY();

            if(seenCoordinatesX.contains(u_x) && seenCoordinatesY.contains(u_y)){
                INodeStyle s = u.getStyle();
                if (s instanceof ShinyPlateNodeStyle) {
                    ((ShinyPlateNodeStyle) s).setPaint(Color.MAGENTA);
                    ((ShinyPlateNodeStyle) s).setPen(new Pen(Color.MAGENTA,1));

                } else {
                    System.out.println(s.getClass());
                }
            } else {
                seenCoordinatesX.add(u_x);
                seenCoordinatesY.add(u_y);
            }
        }

        view.updateUI();
    }

    private void forceDirectionPerpendicularActionPerformed(ActionEvent evt){    this.algoModifiers[0] = true; }
    private void forceDirectionNonPerpendicularActionPerformed(ActionEvent evt){ this.algoModifiers[0] = false; }

    private void optimizingAngleNintyActionPerformed(ActionEvent actionEvent) { this.algoModifiers[1] = true; }
    private void optimizingAngleSixtyActionPerformed(ActionEvent actionEvent) { this.algoModifiers[1] = false; }

    
    public GeneticAlgorithm<ForceAlgorithmApplier> geneticAlgorithm;
    public Thread geneticAlgorithmThread;
    public void initializeGeneticAlgorithm(){
        LinkedList<ForceAlgorithmApplier> firstFAAs = new LinkedList<>();
        firstFAAs.add(defaultForceAlgorithmApplier(faaRunningTimeGenetic));
        LayoutUtilities.applyLayout(graph, new OrthogonalLayout());
        firstFAAs.add(defaultForceAlgorithmApplier(faaRunningTimeGenetic));
        LayoutUtilities.applyLayout(graph, new OrganicLayout());
        firstFAAs.add(defaultForceAlgorithmApplier(faaRunningTimeGenetic));
        geneticAlgorithm = InitGeneticAlgorithm.defaultGeneticAlgorithm(firstFAAs, graph, view, Maybe.just(infoLabel));
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
