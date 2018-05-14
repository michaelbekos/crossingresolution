package main;

import com.sun.istack.internal.Nullable;
import com.yworks.yfiles.geometry.SizeD;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.IModelItem;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.styles.DefaultLabelStyle;
import com.yworks.yfiles.graph.styles.PolylineEdgeStyle;
import com.yworks.yfiles.graph.styles.ShinyPlateNodeStyle;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.view.*;
import com.yworks.yfiles.view.input.*;
import graphoperations.Scaling;
import io.ContestIOHandler;
import io.SimpleGraphmlIOHandler;
import layout.algo.execution.ILayout;
import layout.algo.utils.BestSolutionMonitor;
import sidepanel.InitSidePanel;
import sidepanel.SidePanelTab;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Consumer;


/**
 * Created by michael on 28.10.16.
 */
public class MainFrame extends JFrame {

    /* Box related issue*/
    public static double BOX_SIZE = 10000;

    /* Graph Drawing related objects */
    public GraphComponent view;
    public IGraph graph;
    private OrganicLayout defaultLayouter;
    public GraphEditorInputMode graphEditorInputMode;
    private GridVisualCreator gridVisualCreator;
    private GraphSnapContext graphSnapContext;

    /* Default Styles */
    private ShinyPlateNodeStyle defaultNodeStyle;
    private PolylineEdgeStyle defaultEdgeStyle;
    private DefaultLabelStyle defaultLabelStyle;

    /* Visibility from the view */

    /* Central gui elements */
    public JLabel infoLabel;
    public JProgressBar progressBar;

    public MinimumAngleMonitor minimumAngleMonitor;
    public BestSolutionMonitor bestSolution;

    @Nullable
    public JTabbedPane sidePanel;
    public InitSidePanel initSidePanel;
    public InitMenuBar menuBar;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {}

    public void init() {
        this.initComponents();
        this.initMenuBar();

        super.setTitle("Graph Drawing Tool");
        super.setMinimumSize(new Dimension(400, 300));
        super.setExtendedState(MAXIMIZED_BOTH);
        super.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
        this.progressBar.setStringPainted(true);
        progressBarPanel.add(this.progressBar);

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 1;
        c.insets = new Insets(5, 0, 5, 0);
        mainPanel.add(progressBarPanel, c);

        this.view = new GraphComponent();
        this.view.requestFocus();
        this.view.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.view.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        c.fill = GridBagConstraints.BOTH;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        mainPanel.add(this.view, c);

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
            this.graph.addLabel(iNodeItemEventArgs.getItem(), Integer.toString(graph.getNodes().size() - 1));
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

        this.view.addZoomChangedListener((o, zoomItemEventArgs) -> {
            boolean removedListeners = this.initSidePanel.removeDefaultListeners();
            Scaling.scaleNodeSizes(view);
            if (this.graph.getNodes().size() > 100) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException x) {
                }
            }
            if (removedListeners) {

                this.initSidePanel.addDefaultListeners();
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

        this.defaultLayouter = new OrganicLayout();
        this.defaultLayouter.setPreferredEdgeLength(100);
        this.defaultLayouter.setMinimumNodeDistance(100);

        bestSolution = new BestSolutionMonitor();
        this.minimumAngleMonitor = new MinimumAngleMonitor(view, graph, infoLabel, bestSolution);

        this.initSidePanel = new InitSidePanel(this);
        this.sidePanel = this.initSidePanel.initSidePanel();

        GridBagConstraints cc = new GridBagConstraints();
        cc.gridx = 1;
        cc.gridy = 0;
        cc.weighty = 1;
        cc.weightx = 1;
        cc.insets = new Insets(0, 0, 0, 0);
        cc.fill = GridBagConstraints.BOTH;
        JPanel sidePanelPane = new JPanel();
        sidePanelPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 10));
        sidePanelPane.setLayout(new GridBagLayout());
        sidePanelPane.add(this.sidePanel, cc);

        JSplitPane mainSplit = new JSplitPane();
        mainSplit.setRightComponent(sidePanelPane);
        mainSplit.setLeftComponent(mainPanel);
        mainSplit.setContinuousLayout(true);
        mainSplit.setOneTouchExpandable(true);
        mainSplit.setResizeWeight(0.67);


        super.getContentPane().setLayout(new java.awt.BorderLayout(20, 20));
        super.getContentPane().add(mainSplit, java.awt.BorderLayout.CENTER);
    }


    private void initMenuBar() {
        this.menuBar = new InitMenuBar(
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

    public static void start(int closeOperation, @Nullable Consumer<MainFrame> onReady) {
        try {
            // check for 'os.name == Windows 7' does not work, since JDK 1.4 uses the compatibility mode
            if (!"com.sun.java.swing.plaf.motif.MotifLookAndFeel".equals(UIManager.getSystemLookAndFeelClassName()) && !"com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(UIManager.getSystemLookAndFeelClassName()) && !UIManager.getSystemLookAndFeelClassName().equals(UIManager.getLookAndFeel().getClass().getName()) && !(System.getProperty("java.version").startsWith("1.4") && System.getProperty("os.name").startsWith("Windows") && "6.1".equals(System.getProperty("os.version")))) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.init();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(closeOperation);
            if (onReady != null) {
                onReady.accept(frame);
            }
        });
    }

    public static void start(int closeOperation, boolean isVisible, @Nullable Consumer<MainFrame> onReady) {
        try {
            // check for 'os.name == Windows 7' does not work, since JDK 1.4 uses the compatibility mode
            if (!"com.sun.java.swing.plaf.motif.MotifLookAndFeel".equals(UIManager.getSystemLookAndFeelClassName()) && !"com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(UIManager.getSystemLookAndFeelClassName()) && !UIManager.getSystemLookAndFeelClassName().equals(UIManager.getLookAndFeel().getClass().getName()) && !(System.getProperty("java.version").startsWith("1.4") && System.getProperty("os.name").startsWith("Windows") && "6.1".equals(System.getProperty("os.version")))) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.init();
            frame.setVisible(isVisible);
            frame.setDefaultCloseOperation(closeOperation);
            if (onReady != null) {
                onReady.accept(frame);
            }
        });
    }

    public Optional<SidePanelTab> getTabForAlgorithm(Class<? extends ILayout> layoutClass) {
        return initSidePanel.getTabForAlgorithm(layoutClass);
    }

    public void openContestFile(String fileNamePath) {
        initSidePanel.removeDefaultListeners();
        try {
            ContestIOHandler.read(graph, fileNamePath);
            view.fitGraphBounds();
            view.updateUI();
            bestSolution.reset();
            setTitle(Paths.get(fileNamePath).getFileName().toString());
        } catch (IOException e) {
            infoLabel.setText("An error occured while reading the input file.");
        } finally {
            initSidePanel.addDefaultListeners();
        }
    }
    public void openFile(String fileNamePath) {
        initSidePanel.removeDefaultListeners();
        try {
          //  view.importFromGraphML(fileNamePath);
            SimpleGraphmlIOHandler.read(graph, fileNamePath);
            view.fitGraphBounds();
            view.updateUI();
            bestSolution.reset();
            setTitle(Paths.get(fileNamePath).getFileName().toString());
        } catch (IOException e) {
            infoLabel.setText("An error occured while reading the input file.");
        } finally {
            initSidePanel.addDefaultListeners();
        }
    }

}
