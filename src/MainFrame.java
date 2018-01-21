import com.sun.istack.internal.Nullable;
import com.yworks.yfiles.geometry.SizeD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.graph.styles.PolylineEdgeStyle;
import com.yworks.yfiles.graph.styles.ShinyPlateNodeStyle;
import com.yworks.yfiles.graph.styles.SimpleLabelStyle;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.view.*;
import com.yworks.yfiles.view.input.*;
import layout.algo.ForceAlgorithm;
import layout.algo.layoutinterface.SidePanelItemFactory;


import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;



/**
 * Created by michael on 28.10.16.
 */
public class MainFrame extends JFrame {
	
	/* Box related issue*/
	public static final double BOX_SIZE= 10000;
	
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
    private SimpleLabelStyle defaultLabelStyle;

    /* Central gui elements */
    private JLabel infoLabel;
    public JProgressBar progressBar;

    public MinimumAngleMonitor minimumAngleMonitor;

    public JSlider[] sliders;

    public int faRunningTimeGenetic = 250;

    @Nullable
    public ForceAlgorithm forceAlgorithm = null;
    public JPanel sidePanel;
    public SidePanelItemFactory sidePanelItemFactory;


    // for this class, we can instantiate defaultForceAlgorithm and do some post-initializing
    public ForceAlgorithm defaultForceAlgorithm() {
        return InitForceAlgorithm.defaultForceAlgorithm(view, sidePanelItemFactory);
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
            // TODO: do we really need this?
            /*if (forceAlgorithm != null) {
                forceAlgorithm.resetNodePositions(movedNodesCP);
            }*/
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

        this.minimumAngleMonitor = new MinimumAngleMonitor(view, graph, infoLabel);

        InitSidePanel newSidePanel = new InitSidePanel(this);
        sidePanel = newSidePanel.initSidePanel(mainPanel,c);
        sidePanelItemFactory = new SidePanelItemFactory(sidePanel, view);
//        initSidePanel(mainPanel, c);
    }

    private final Set<INode> movedNodes = new HashSet<>();




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
