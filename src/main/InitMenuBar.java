package main;

import algorithms.graphs.CachedMinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.graph.styles.INodeStyle;
import com.yworks.yfiles.graph.styles.ShinyPlateNodeStyle;
import com.yworks.yfiles.layout.ILayoutAlgorithm;
import com.yworks.yfiles.layout.LayoutExecutor;
import com.yworks.yfiles.layout.circular.CircularLayout;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.layout.orthogonal.OrthogonalLayout;
import com.yworks.yfiles.layout.partial.PartialLayout;
import com.yworks.yfiles.layout.partial.SubgraphPlacement;
import com.yworks.yfiles.layout.tree.TreeLayout;
import com.yworks.yfiles.view.*;
import com.yworks.yfiles.view.input.GraphEditorInputMode;
import com.yworks.yfiles.view.input.GraphSnapContext;
import com.yworks.yfiles.view.input.GridSnapTypes;
import io.ContestIOHandler;
import layout.algo.*;
import layout.algo.forces.ElectricForce;
import layout.algo.forces.SlopedForce;
import layout.algo.forces.SpringForce;
import layout.algo.layoutinterface.VoidItemFactory;
import layout.algo.utils.PositionMap;
import util.*;
import view.visual.InitClinchLayout;
import view.visual.VectorVisual;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by khokhi on 10.12.16.
 */
public class InitMenuBar {
	
    private MainFrame mainFrame;

    private JProgressBar progressBar;
    private JLabel infoLabel;

    private GraphComponent view;
    private IGraph graph;
    private GraphEditorInputMode graphEditorInputMode;
    private OrganicLayout defaultLayouter;

    /* Object that keeps track of the latest open/saved file */
    private String fileNamePath;
    private String fileNamePathFolder;

    /* Object that tracks removed/replaced Vertices */
    private VertexStack removedVertices;

    private boolean isGridVisible = true;
    private GraphSnapContext graphSnapContext;
    private GridVisualCreator gridVisualCreator;

    private MinimumAngleMonitor minimumAngleMonitor;

    InitMenuBar(MainFrame mainFrame,
                IGraph graph,
                JLabel infoLabel,
                GraphComponent view,
                JProgressBar progressBar,
                GraphEditorInputMode graphEditorInputMode,
                OrganicLayout defaultLayouter,
                GraphSnapContext graphSnapContext,
                GridVisualCreator gridVisualCreator,
                MinimumAngleMonitor minimumAngleMonitor) {

        this.mainFrame = mainFrame;
        this.graph = graph;
        this.infoLabel = infoLabel;
        this.view = view;
        this.graphEditorInputMode = graphEditorInputMode;
        this.defaultLayouter = defaultLayouter;
        this.progressBar = progressBar;
        this.graphSnapContext = graphSnapContext;
        this.gridVisualCreator = gridVisualCreator;
        this.minimumAngleMonitor = minimumAngleMonitor;
        this.fileNamePathFolder = "contest-2017";
    }


    public JMenuBar initMenuBar() {
        JMenuBar mainMenuBar = new JMenuBar();

        mainMenuBar.add(createFileMenu());
        mainMenuBar.add(createEditMenu());
        mainMenuBar.add(createViewMenu());
        mainMenuBar.add(createGraphOpsMenu());
        mainMenuBar.add(createLayoutMenu());

        return mainMenuBar;
    }

    private JMenu createLayoutMenu() {
        JMenu layoutMenu = new JMenu();
        layoutMenu.setText("Layout");

        JMenuItem slopedSpringEmbedderItem = new JMenuItem();
        slopedSpringEmbedderItem.setIcon(new ImageIcon(getClass().getResource("/resources/layout-16.png")));
        slopedSpringEmbedderItem.setText("Sloped Spring Embedder");
        slopedSpringEmbedderItem.addActionListener(this::slopedSpringEmbedderItemActionPerformed);
        layoutMenu.add(slopedSpringEmbedderItem);

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

        JMenuItem clinchLayout = new JMenuItem();
        clinchLayout.setIcon(new ImageIcon(getClass().getResource("/resources/layout-16.png")));
        clinchLayout.setText("Clinch nodes");
        clinchLayout.addActionListener(this::clinchNodesActionPerformed);
        layoutMenu.add(clinchLayout);

        JMenuItem randomMovement = new JMenuItem();
        randomMovement.setIcon(new ImageIcon(getClass().getResource("/resources/layout-16.png")));
        randomMovement.setText("Random Movement");
        randomMovement.addActionListener(this::randomMovementActionPerformed);
        layoutMenu.add(randomMovement);

        return layoutMenu;
    }


    private JMenu createGraphOpsMenu() {
        /* Graph operation Menu */
        JMenu graphOpsMenu = new JMenu();
        graphOpsMenu.setText("Graph Ops.");
        /*
         *  Graph Scale
         */
        JMenuItem scaleUpItem = new JMenuItem();
        scaleUpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.ALT_MASK));
        scaleUpItem.setIcon(new ImageIcon(getClass().getResource("/resources/scaleUp.png"))); // test Image
        scaleUpItem.setText("Scale-up Graph");
        scaleUpItem.addActionListener(this::scaleUpGraphItemActionPerformed);
        graphOpsMenu.add(scaleUpItem);

        JMenuItem scaleDownItem = new JMenuItem();
        scaleDownItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.ALT_MASK));
        scaleDownItem.setIcon(new ImageIcon(getClass().getResource("/resources/scaleDown.png"))); // test Image
        scaleDownItem.setText("Scale-down Graph");
        scaleDownItem.addActionListener(this::scaleDownGraphItemActionPerformed);
        graphOpsMenu.add(scaleDownItem);

        /*
         * Remove/reinsert vertices
         */
        JMenuItem removeVerticesItem = new JMenuItem();
        removeVerticesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        removeVerticesItem.setIcon(new ImageIcon(getClass().getResource("/resources/removeNode.png"))); // test Image
        removeVerticesItem.setText("Remove Vertices");
        removeVerticesItem.addActionListener(this::removeVerticesItemActionPerformed);
        graphOpsMenu.add(removeVerticesItem);

        JMenuItem reinsertVerticesItem = new JMenuItem();
        reinsertVerticesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        reinsertVerticesItem.setIcon(new ImageIcon(getClass().getResource("/resources/reinsertNode.png"))); // test Image
        reinsertVerticesItem.setText("Reinsert Vertices");
        reinsertVerticesItem.addActionListener(this::reinsertVerticesItemActionPerformed);
        graphOpsMenu.add(reinsertVerticesItem);
        
        /*
         * Check legality
         */
        JMenuItem enforcelegal = new JMenuItem();
        enforcelegal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        enforcelegal.setIcon(new ImageIcon(getClass().getResource("/resources/exclamation.png"))); // test Image
        enforcelegal.setText("Enforce legality");
        enforcelegal.addActionListener(this::enforcelegal);
        graphOpsMenu.add(enforcelegal);
        
        return graphOpsMenu;
    }

    private JMenu createViewMenu() {
        JMenu viewMenu = new JMenu();
        viewMenu.setText("View");

        JMenuItem zoomInItem = new JMenuItem();
        zoomInItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_MASK));
        zoomInItem.setIcon(new ImageIcon(getClass().getResource("/resources/plus2-16.png")));
        zoomInItem.setText("Zoom In");
        zoomInItem.addActionListener(this::zoomInItemActionPerformed);
        viewMenu.add(zoomInItem);

        JMenuItem zoomOutItem = new JMenuItem();
        zoomOutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_MASK));
        zoomOutItem.setIcon(new ImageIcon(getClass().getResource("/resources/minus2-16.png")));
        zoomOutItem.setText("Zoom Out");
        zoomOutItem.addActionListener(this::zoomOutItemActionPerformed);
        viewMenu.add(zoomOutItem);

        JMenuItem fitContentItem = new JMenuItem();
        fitContentItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_MASK));
        fitContentItem.setIcon(new ImageIcon(getClass().getResource("/resources/zoom-original2-16.png")));
        fitContentItem.setText("Fit Content");
        fitContentItem.addActionListener(this::fitContentItemActionPerformed);
        viewMenu.add(fitContentItem);

        viewMenu.add(new JSeparator());

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
        minimumCrossingAngleMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_MASK));
        minimumCrossingAngleMenu.setText("Minimum Angle");
        minimumCrossingAngleMenu.addActionListener(this::minimumCrossingAngleMenuActionPerformed);
        analyzeMenu.add(minimumCrossingAngleMenu);

        JMenuItem overlappingNodesMenu = new JMenuItem();
        overlappingNodesMenu.setIcon(new ImageIcon(getClass().getResource("/resources/star-16.png")));
        overlappingNodesMenu.setText("Show Overlapping Nodes");
        overlappingNodesMenu.addActionListener(this::overlappingNodesMenuActionPerformed);

        analyzeMenu.add(overlappingNodesMenu);
        viewMenu.add(analyzeMenu);

        return viewMenu;
    }

    private JMenu createEditMenu() {
        /* Edit Menu */
        JMenu editMenu = new JMenu();
        editMenu.setText("Edit");

        JMenuItem undoItem = new JMenuItem();
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
        undoItem.setIcon(new ImageIcon(getClass().getResource("/resources/undo-16.png")));
        undoItem.setText("Undo");
        undoItem.addActionListener(this::undoItemActionPerformed);
        editMenu.add(undoItem);

        JMenuItem redoItem = new JMenuItem();
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
        redoItem.setIcon(new ImageIcon(getClass().getResource("/resources/redo-16.png")));
        redoItem.setText("Redo");
        redoItem.addActionListener(this::redoItemActionPerformed);
        editMenu.add(redoItem);
        editMenu.add(new JSeparator());

        JMenuItem cutItem = new JMenuItem();
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
        cutItem.setIcon(new ImageIcon(getClass().getResource("/resources/cut-16.png")));
        cutItem.setText("Cut");
        cutItem.addActionListener(this::cutItemActionPerformed);
        editMenu.add(cutItem);

        JMenuItem copyItem = new JMenuItem();
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
        copyItem.setIcon(new ImageIcon(getClass().getResource("/resources/copy-16.png")));
        copyItem.setText("Copy");
        copyItem.addActionListener(this::copyItemActionPerformed);
        editMenu.add(copyItem);

        JMenuItem pasteItem = new JMenuItem();
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
        pasteItem.setIcon(new ImageIcon(getClass().getResource("/resources/paste-16.png")));
        pasteItem.setText("Paste");
        pasteItem.addActionListener(this::pasteItemActionPerformed);
        editMenu.add(pasteItem);
        editMenu.add(new JSeparator());

        JMenuItem clearSelectedItem = new JMenuItem();
        clearSelectedItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        clearSelectedItem.setIcon(new ImageIcon(getClass().getResource("/resources/delete-16.png")));
        clearSelectedItem.setText("Clear Selected");
        clearSelectedItem.addActionListener(this::clearSelectedItemActionPerformed);
        editMenu.add(clearSelectedItem);


        JMenuItem clearAllItem = new JMenuItem();
        clearAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        clearAllItem.setIcon(new ImageIcon(getClass().getResource("/resources/delete-16.png")));
        clearAllItem.setText("Clear all");
        clearAllItem.addActionListener(this::clearAllItemActionPerformed);
        editMenu.add(clearAllItem);
        editMenu.add(new JSeparator());

        JMenuItem selectAllItem = new JMenuItem();
        selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
        selectAllItem.setIcon(new ImageIcon(getClass().getResource("/resources/group-16.png")));
        selectAllItem.setText("Select all");
        selectAllItem.addActionListener(this::selectAllItemActionPerformed);
        editMenu.add(selectAllItem);

        JMenuItem deselectAllItem = new JMenuItem();
        deselectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));
        deselectAllItem.setIcon(new ImageIcon(getClass().getResource("/resources/ungroup-16.png")));
        deselectAllItem.setText("Deselect all");
        deselectAllItem.addActionListener(this::deselectAllItemActionPerformed);
        editMenu.add(deselectAllItem);
        return editMenu;
    }

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu();
        fileMenu.setText("File");

        JMenuItem newMenuItem = new JMenu();
        newMenuItem.setIcon(new ImageIcon(getClass().getResource("/resources/new-document-16.png")));
        newMenuItem.setText("New");

        JMenuItem blankGraphItem = new JMenuItem();
        blankGraphItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        blankGraphItem.setIcon(new ImageIcon(getClass().getResource("/resources/delete-16.png")));
        blankGraphItem.setText("Blank Graph");
        blankGraphItem.addActionListener(this::blankGraphItemGraphItemActionPerformed);
        newMenuItem.add(blankGraphItem);
        newMenuItem.add(new JPopupMenu.Separator());

        JMenuItem randomGraphItem = new JMenuItem();
        randomGraphItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));
        randomGraphItem.setIcon(new ImageIcon(getClass().getResource("/resources/new-document-16.png")));
        randomGraphItem.setText("Random Graph");
        randomGraphItem.addActionListener(this::randomGraphItemActionPerformed);
        newMenuItem.add(randomGraphItem);

        JMenuItem hypercubeGraphItem = new JMenuItem();
        hypercubeGraphItem.setIcon(new ImageIcon(getClass().getResource("/resources/new-document-16.png")));
        hypercubeGraphItem.setText("Hypercube");
        hypercubeGraphItem.addActionListener(e -> {
            JTextField dim = new JTextField("3");
            int result = JOptionPane.showOptionDialog(null, new Object[]{"Dimensions: ", dim}, "Graph Properties", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (result == JOptionPane.OK_OPTION) {
                int dimensions = Integer.parseInt(dim.getText());
                TrashCan.init();
                HypercubeGenerator.generate(graph, dimensions);
                view.updateUI();
            }
        });
        newMenuItem.add(hypercubeGraphItem);

        JMenuItem gridGraphItem = new JMenuItem();
        gridGraphItem.setIcon(new ImageIcon(getClass().getResource("/resources/new-document-16.png")));
        gridGraphItem.setText("Grid graph");
        gridGraphItem.addActionListener(e -> {
            JTextField xCount = new JTextField("5");
            JTextField yCount = new JTextField("5");
            JTextField rCount = new JTextField("5");
            int result = JOptionPane.showOptionDialog(null, new Object[]{"xCount: ", xCount, "yCount: ", yCount, "rCount: ", rCount}, "Graph Properties", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (result == JOptionPane.OK_OPTION) {
                int xC = Integer.parseInt(xCount.getText());
                int yC = Integer.parseInt(yCount.getText());
                int rC = Integer.parseInt(rCount.getText());
                TrashCan.init();
                GridGenerator.generate(graph, xC, yC, rC);
                view.updateUI();
            }
        });
        newMenuItem.add(gridGraphItem);

        JMenuItem layeredGridGraphItem = new JMenuItem();
        layeredGridGraphItem.setIcon(new ImageIcon(getClass().getResource("/resources/new-document-16.png")));
        layeredGridGraphItem.setText("Layered Grid graph");
        layeredGridGraphItem.addActionListener(e -> {
            JTextField xCount = new JTextField("4");
            JTextField yCount = new JTextField("4");
            JTextField layers = new JTextField("2");
            int result = JOptionPane.showOptionDialog(null, new Object[]{"xCount: ", xCount, "yCount: ", yCount, "layersCount: ", layers}, "Graph Properties", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (result == JOptionPane.OK_OPTION) {
                int xC = Integer.parseInt(xCount.getText());
                int yC = Integer.parseInt(yCount.getText());
                int lC = Integer.parseInt(layers.getText());
                TrashCan.init();
                LayeredGridGenerator.generate(graph, xC, yC, lC);
                view.updateUI();
            }
        });
        newMenuItem.add(layeredGridGraphItem);

        fileMenu.add(newMenuItem);
        fileMenu.add(new JSeparator());

        JMenuItem openItem = new JMenuItem();
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        openItem.setIcon(new ImageIcon(getClass().getResource("/resources/open-16.png")));
        openItem.setText("Open");
        openItem.addActionListener(this::openItemActionPerformed);
        fileMenu.add(openItem);

        JMenuItem openContestItem = new JMenuItem();
        openContestItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK));
        openContestItem.setIcon(new ImageIcon(getClass().getResource("/resources/open-16.png")));
        openContestItem.setText("Open Contest File");
        openContestItem.addActionListener(this::openContestItemActionPerformed);
        fileMenu.add(openContestItem);

        JMenuItem reloadItem = new JMenuItem();
        reloadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
        reloadItem.setIcon(new ImageIcon(getClass().getResource("/resources/reload-16.png")));
        reloadItem.setText("Reload");
        reloadItem.addActionListener(this::reloadItemActionPerformed);
        fileMenu.add(reloadItem);
        fileMenu.add(new JSeparator());

        JMenuItem saveItem = new JMenuItem();
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        saveItem.setIcon(new ImageIcon(getClass().getResource("/resources/save-16.png")));
        saveItem.setText("Save");
        saveItem.addActionListener(this::saveItemActionPerformed);
        fileMenu.add(saveItem);

        JMenuItem saveAsItem = new JMenuItem();
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));
        saveAsItem.setIcon(new ImageIcon(getClass().getResource("/resources/save-16.png")));
        saveAsItem.setText("Save As...");
        saveAsItem.addActionListener(this::saveAsItemActionPerformed);
        fileMenu.add(saveAsItem);

        JMenuItem exportItem = new JMenuItem();
        exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
        exportItem.setIcon(new ImageIcon(getClass().getResource("/resources/save-16.png")));
        exportItem.setText("Export");
        exportItem.addActionListener(this::exportItemActionPerformed);
        fileMenu.add(exportItem);

        JMenuItem quitItem = new JMenuItem();
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
        quitItem.setIcon(new ImageIcon(getClass().getResource("/resources/exit-16.png")));
        quitItem.setText("Quit");
        quitItem.addActionListener(evt -> System.exit(0));
        fileMenu.add(quitItem);
        return fileMenu;
    }

    private void scaleUpGraphItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

        Mapper<INode, PointD> nodePositions = PositionMap.FromIGraph(graph);
        nodePositions = GraphOperations.scaleUpProcess(graph,nodePositions, 2.0);
        this.graph =  PositionMap.applyToGraph(graph, nodePositions);
        this.view.fitGraphBounds();
    }

    private void scaleDownGraphItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        Mapper<INode, PointD> nodePositions = PositionMap.FromIGraph(graph);
        nodePositions = GraphOperations.scaleUpProcess(graph,nodePositions, 0.5);
        this.graph =  PositionMap.applyToGraph(graph, nodePositions);
        this.view.fitGraphBounds();
    }

    private void removeVerticesItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

        if (this.view.getSelection().getSelectedNodes().getCount() > 0) {
            this.removedVertices = GraphOperations.removeVertices(this.graph, true, this.view.getSelection().getSelectedNodes().getCount(), this.view.getSelection().getSelectedNodes(), this.removedVertices);
        } else if (this.graph.getNodes().size() > 0){
            JTextField vertexCount = new JTextField();
            vertexCount.setText(Integer.toString(1));
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            JRadioButton useHighest = new JRadioButton("Remove Highest Degree Vertices"),
                    useLowest = new JRadioButton("Remove Lowest Degree (0, 1, 2) Vertices");
            ButtonGroup bg = new ButtonGroup();
            bg.add(useHighest);
            bg.add(useLowest);
            JLabel label = new JLabel();
            String highDeg = "Number of highest degree vertices to remove: ";
            String lowDeg = "Number of lowest degree vertices to remove: ";
            label.setText(highDeg);
            panel.add(label);
            panel.add(vertexCount);
            panel.add(useHighest);
            panel.add(useLowest);
            int numLowest = 0;
            for (INode u : this.graph.getNodes()) {
                if (u.getPorts().size() <= 2) {
                    numLowest++;
                }
            }
            int numLowestFinal = numLowest;
            useHighest.addItemListener(itemEvent -> {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    label.setText(highDeg);
                    vertexCount.setText(Integer.toString(1));
                } else if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
                    label.setText(lowDeg);
                    vertexCount.setText(Integer.toString(numLowestFinal));
                }
            });
            useHighest.setSelected(true);
            useLowest.setSelected(false);
            int result = JOptionPane.showOptionDialog(null, panel, "Remove Highest or Lowest Degree Vertices", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            int numVertices = useHighest.isSelected() ? 1 : numLowest;
            if (result == JOptionPane.OK_OPTION) {
                try {
                    numVertices = Integer.parseInt(vertexCount.getText());
                    if(numVertices > graph.getNodes().size()){
                        numVertices = graph.getNodes().size();
                        if( 0 != JOptionPane.showConfirmDialog(null, "Input is greater than the number of all nodes.\n Remove all nodes?")){
                            numVertices = 0;
                        }
                    }
                } catch (NumberFormatException exc) {
                    JOptionPane.showMessageDialog(null, "Incorrect input.\nNo vertex will be removed.", "Incorrect Input", JOptionPane.ERROR_MESSAGE);
                    numVertices = 0;
                }
                finally {
                    this.removedVertices = GraphOperations.removeVertices(this.graph, useHighest.isSelected(), numVertices, null, this.removedVertices);
                }
            }
        }
    }
    
    private void enforcelegal(@SuppressWarnings("unused") ActionEvent evt){
    	Mapper<INode, PointD> nodePositions = PositionMap.FromIGraph(graph);
    	boolean change=true;
    	while(change){
    		change=false;
	    	for(INode u : graph.getNodes()){
	        	if (u.getLayout().getCenter().getX()<0 || u.getLayout().getCenter().getX() > MainFrame.BOX_SIZE || u.getLayout().getCenter().getY()<0 || u.getLayout().getCenter().getY() > MainFrame.BOX_SIZE){
	        		nodePositions = GraphOperations.scaleUpProcess(graph,nodePositions, 0.9);
	        	    this.graph =  PositionMap.applyToGraph(graph, nodePositions);
	        	    this.view.fitGraphBounds();
	        	    change=true;
				}
	        }
    	}
    }

    private void reinsertVerticesItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        if (this.removedVertices != null && !this.removedVertices.isEmpty()){
            JTextField vertexComponentCount = new JTextField();
            vertexComponentCount.setText(Integer.toString(removedVertices.size()));
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            JRadioButton useVertices = new JRadioButton("Use Vertices"),
                    useComponents = new JRadioButton("Use Components");
            ButtonGroup bg = new ButtonGroup();
            bg.add(useVertices);
            bg.add(useComponents);
            JLabel label = new JLabel();
            String component = "Number of components to reinsert: ";
            String vertex = "Number of vertices to reinsert:        ";
            label.setText(vertex);
            panel.add(label);
            panel.add(vertexComponentCount);
            panel.add(useVertices);
            panel.add(useComponents);
            useVertices.addItemListener(itemEvent -> {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    label.setText(vertex);
                    vertexComponentCount.setText(Integer.toString(removedVertices.size()));
                } else if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
                    label.setText(component);
                    vertexComponentCount.setText(Integer.toString(removedVertices.componentStack.size()));
                }
            });
            useVertices.setSelected(true);
            useComponents.setSelected(false);

            int result = JOptionPane.showOptionDialog(null, panel, "Reinsert Vertices or Components", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            int numVertices = useVertices.isSelected() ? removedVertices.size() : removedVertices.componentStack.size();
            if (result == JOptionPane.OK_OPTION) {
                try {
                    numVertices = Integer.parseInt(vertexComponentCount.getText());
                    if(useVertices.isSelected() && numVertices > removedVertices.size()){
                        numVertices = removedVertices.size();
                    } else if (useComponents.isSelected() && numVertices > removedVertices.componentStack.size()) {
                        numVertices = removedVertices.componentStack.size();
                    }
                } catch (NumberFormatException exc) {
                    JOptionPane.showMessageDialog(null, "Incorrect input.\nNo vertex will be reinserted.", "Incorrect Input", JOptionPane.ERROR_MESSAGE);
                    numVertices = 0;
                }
                finally {
                    this.removedVertices = GraphOperations.reinsertVertices(this.graph, useVertices.isSelected(), numVertices, this.removedVertices);
                    double scaleValue = 1/this.view.getZoom();  //scale reinserted nodes
                    for(INode u : this.graph.getNodes()){
                        this.graph.setNodeLayout(u, new RectD(u.getLayout().getX(),u.getLayout().getY(),this.graph.getNodeDefaults().getSize().width*scaleValue,this.graph.getNodeDefaults().getSize().height*scaleValue));
                    }
                }
            }
        }
    }

    private void fitContentItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        this.view.fitGraphBounds();
    }

    private void zoomOutItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        this.view.setZoom(this.view.getZoom() - 0.2);
    }

    private void zoomInItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        this.view.setZoom(this.view.getZoom() + 0.2);
    }



    private void blankGraphItemGraphItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        this.graph.clear();
        this.removedVertices = null;
        this.view.updateUI();
    }

    private void randomGraphItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        RandomGraphGenerator rgg = new RandomGraphGenerator();
        rgg.allowMultipleEdges(false);
        rgg.allowCycles(true);
        rgg.allowSelfLoops(false);

        JTextField nodeCount = new JTextField("20");
        JTextField edgeCount = new JTextField("40");

        int result = JOptionPane.showOptionDialog(null, new Object[]{"Number of Nodes: ", nodeCount, "Number of Edges: ", edgeCount}, "Graph Properties", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

        if (result == JOptionPane.OK_OPTION) {
            try {
                rgg.setNodeCount(Integer.parseInt(nodeCount.getText()));
                rgg.setEdgeCount(Integer.parseInt(edgeCount.getText()));
            } catch (NumberFormatException exc) {
                JOptionPane.showMessageDialog(null, "Incorrect input.\nThe graph will be created with 10 nodes and 10 edges.", "Incorrect Input", JOptionPane.ERROR_MESSAGE);
                rgg.setNodeCount(10);
                rgg.setEdgeCount(10);
            } finally {
                TrashCan.init();
                rgg.generate(this.graph);
                LayoutUtilities.applyLayout(this.graph, this.defaultLayouter);
                this.view.fitGraphBounds();
                this.view.updateUI();
                this.removedVertices = null;
            }
        }
    }

    private void openItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        JFileChooser chooser = new JFileChooser(this.fileNamePathFolder);
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File file) {
                return (file.isDirectory() || file.toString().toLowerCase().endsWith("graphml"));
            }

            public String getDescription() {
                return "GraphML Files [.graphml]";
            }

        });

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            this.fileNamePath = chooser.getSelectedFile().toString();

            try {
                this.view.importFromGraphML(fileNamePath);
                this.view.fitGraphBounds();
                this.view.updateUI();
                this.removedVertices = null;
                this.fileNamePathFolder = chooser.getSelectedFile().getParent();

            } catch (IOException ioe) {
                this.infoLabel.setText("An error occured while reading the input file.");
            }
        }
    }

    private void openContestItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        JFileChooser chooser = new JFileChooser(this.fileNamePathFolder);
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File file) {
                return (file.isDirectory() || file.toString().toLowerCase().endsWith("txt"));
            }

            public String getDescription() {
                return "ASCII Files [.txt]";
            }

        });

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            this.fileNamePath = chooser.getSelectedFile().toString();

            try {
                ContestIOHandler.read(this.graph, this.fileNamePath);
                this.view.fitGraphBounds();
                this.view.updateUI();
                this.removedVertices = null;
                this.fileNamePathFolder = chooser.getSelectedFile().getParent();

            } catch (IOException ioe) {
                this.infoLabel.setText("An error occured while reading the input file.");
            }
        }
    }

    private void reloadItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        if (this.fileNamePath != null) {
            try {
                this.graph.clear();
                if (this.fileNamePath.endsWith(".graphml")) {
                    this.view.importFromGraphML(this.fileNamePath);
                } else if (this.fileNamePath.endsWith(".txt")) {
                    ContestIOHandler.read(this.graph, this.fileNamePath);
                }
                this.view.fitGraphBounds();
                this.view.updateUI();
                this.removedVertices = null;
            } catch (IOException ioe) {
                this.infoLabel.setText("An error occured while reading the input file.");
            }
        } else {
            infoLabel.setText("No file was recently opened.");
        }
    }

    private void saveItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        if (this.fileNamePath != null) {
            try {
                this.view.exportToGraphML(this.fileNamePath);
            } catch (IOException ioe) {
                this.infoLabel.setText("An error occured while exporting the graph.");
            }
        } else {
            showFileChooser(new JFileChooser());
        }
    }

    private void showFileChooser(JFileChooser chooser) {
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File file) {
                return (file.isDirectory() || file.toString().toLowerCase().endsWith(".graphml"));
            }

            public String getDescription() {
                return "GraphML Files [.graphml]";
            }

        });
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            this.fileNamePath = chooser.getSelectedFile().toString();
            if (!this.fileNamePath.toLowerCase().endsWith(".graphml")) {
                this.fileNamePath = this.fileNamePath + ".graphml";
            }
            this.fileNamePathFolder = chooser.getSelectedFile().getParent();

            try {
                this.view.exportToGraphML(this.fileNamePath);
            } catch (IOException ioe) {
                this.infoLabel.setText("An error occured while exporting the graph.");
            }
        }
    }

    private void exportItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        JFileChooser chooser = new JFileChooser(this.fileNamePathFolder);
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File file) {
                return (file.isDirectory() || file.toString().toLowerCase().endsWith(".txt"));

            }
            public String getDescription() {
                return "ASCII Files [.txt]";
            }
        });

        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            this.fileNamePath = chooser.getSelectedFile().toString();
            if (!this.fileNamePath.toLowerCase().endsWith(".txt")) {
                this.fileNamePath = this.fileNamePath + ".txt";
            }
            this.fileNamePathFolder = chooser.getSelectedFile().getParent();

            try {
                ContestIOHandler.write(this.graph, this.fileNamePath);
            } catch (IOException ioe) {
                this.infoLabel.setText("An error occured while exporting the graph.");
            }
        }
    }


    //helper function
    private List<ICanvasObject> canvasObjects = new ArrayList<>();
    private void drawSlopes(double numSlopes, double initAngleDeg) {
        numSlopes *=2;
        for (ICanvasObject o : canvasObjects) {
            o.remove();
        }
        canvasObjects.clear();

        double stepSize = (2* Math.PI)/numSlopes;
        double pos = 2 * Math.PI*(initAngleDeg/360);
        INode tmpNode = mainFrame.graph.createNode(mainFrame.view.getCenter());
        for (int i = 0; i < numSlopes; i++) {
            double x_val = 1/mainFrame.view.getZoom() * 3 * Math.cos(pos);
            double y_val = 1/mainFrame.view.getZoom() * 3 * Math.sin(pos);
            canvasObjects.add(mainFrame.view.getBackgroundGroup().addChild(new VectorVisual(mainFrame.view, new PointD(x_val,y_val), tmpNode, Color.GREEN,(int)(5/mainFrame.view.getZoom())), ICanvasObjectDescriptor.VISUAL));
            pos += stepSize;
            if (pos > 2 * Math.PI) {
                pos -= 2 * Math.PI;
            }
        }
        mainFrame.view.updateUI();
        mainFrame.graph.remove(tmpNode);
    }

    private int iterations = 1000;
    private int numSlopes = 1;
    private int initAngle = 0;
    void slopedSpringEmbedderItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        for (ICanvasObject o : canvasObjects) {
            o.remove();
        }
        canvasObjects.clear();

        JTextField iterationsTextField = new JTextField(Integer.toString(iterations));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel iterationLabel = new JLabel("Number of Iterations: ");
        panel.add(iterationLabel);
        panel.add(iterationsTextField);

        JLabel numSlopesLabel = new JLabel("Number of Slopes: ");
        JTextField numSlopesTextField = new JTextField(Integer.toString(numSlopes));

        panel.add(numSlopesLabel);
        panel.add(numSlopesTextField);

        JLabel initAngleLabel = new JLabel("Angle (°) of First Slope: "); //other slopes are equidistant to first slope, default angle is 0 (right), clockwise is positive
        panel.add(initAngleLabel);
        JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayout(1,2));
//        panel2.setMaximumSize(new Dimension(100,50));
        JTextField initAngleTextField = new JTextField(Integer.toString(initAngle));
        drawSlopes(Integer.parseInt(numSlopesTextField.getText()), Integer.parseInt(initAngleTextField.getText()));
        JSlider initAngleSlider = new JSlider(JSlider.HORIZONTAL, 0, 360, initAngle);
        initAngleSlider.addChangeListener(changeEvent -> {
            initAngleTextField.setText(Integer.toString(initAngleSlider.getValue()));
            drawSlopes(Integer.parseInt(numSlopesTextField.getText()), Integer.parseInt(initAngleTextField.getText()));
        });


        initAngleTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                try {
                    if (initAngleTextField.getText().matches("\\d+") && Integer.parseInt(initAngleTextField.getText()) >= 0 && Integer.parseInt(initAngleTextField.getText()) <= 360) { //checks is int
                        initAngleSlider.setValue(Integer.parseInt(initAngleTextField.getText()));
                        if (numSlopesTextField.getText().matches("\\d+") && Integer.parseInt(numSlopesTextField.getText()) > 0 && Integer.parseInt(numSlopesTextField.getText()) <= 180) {
                            drawSlopes(Integer.parseInt(numSlopesTextField.getText()), Integer.parseInt(initAngleTextField.getText()));
                        }
                    }
                } catch (NumberFormatException nfe) {
                    System.out.println("Invalid Input");
                }
            }
        });

        numSlopesTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                try {
                    if (numSlopesTextField.getText().matches("\\d+") && Integer.parseInt(numSlopesTextField.getText()) > 0 && Integer.parseInt(numSlopesTextField.getText()) <= 180) { //checks is int
                        if (initAngleTextField.getText().matches("\\d+") && Integer.parseInt(initAngleTextField.getText()) >= 0 && Integer.parseInt(initAngleTextField.getText()) <= 360) {
                            drawSlopes(Integer.parseInt(numSlopesTextField.getText()), Integer.parseInt(initAngleTextField.getText()));
                        }
                    }
                } catch (NumberFormatException nfe) {
                    System.out.println("Invalid Input");
                }
            }
        });

        panel2.add(initAngleSlider);
        panel2.add(initAngleTextField);
        panel.add(panel2);


        int result = JOptionPane.showOptionDialog(null, panel, "Algorithm Properties", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

        if (result == JOptionPane.OK_OPTION) {
            try {
                iterations = Integer.parseInt(iterationsTextField.getText());
                numSlopes = Integer.parseInt(numSlopesTextField.getText());
                initAngle = Integer.parseInt(initAngleTextField.getText());

            } catch (NumberFormatException exc) {
                JOptionPane.showMessageDialog(null, "Incorrect input.\nThe number of iterations will be set to 1000, slopes to 1 and inital angle to 0.", "Incorrect Input", JOptionPane.ERROR_MESSAGE);
                for (ICanvasObject o : canvasObjects) {
                    o.remove();
                }
                canvasObjects.clear();
                mainFrame.view.updateUI();
                return;
            }
        }
        else {
            for (ICanvasObject o : canvasObjects) {
                o.remove();
            }
            canvasObjects.clear();
            mainFrame.view.updateUI();
            return;
        }

        ForceAlgorithmConfigurator configurator = new ForceAlgorithmConfigurator()
            .addForce(new SlopedForce(mainFrame.graph, numSlopes * 2, initAngle, 0.7))
            .addForce(new ElectricForce(mainFrame.graph, 0.01, 30000))
            .addForce(new SpringForce(mainFrame.graph, 100, 0.01, 100));
        // TODO configurator.init(mainFrame.sidePanelItemFactory);

        ForceAlgorithm forceAlgorithm = new ForceAlgorithm(configurator, mainFrame.graph, new CachedMinimumAngle());

        IGraphLayoutExecutor executor =
            new IGraphLayoutExecutor(forceAlgorithm, mainFrame.graph, progressBar, iterations, 20);
        executor.start();

        for (ICanvasObject o : canvasObjects) {
            o.remove();
        }
        canvasObjects.clear();

        mainFrame.view.updateUI();
    }


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

        ForceAlgorithm fd = InitForceAlgorithm.defaultForceAlgorithm(graph, new VoidItemFactory());
        mainFrame.forceAlgorithm = fd;

        IGraphLayoutExecutor executor =
            new IGraphLayoutExecutor(fd, graph, progressBar, iterations, 20);
        executor.start();
        mainFrame.view.updateUI();
    }

    private void saveAsItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        showFileChooser(new JFileChooser(this.fileNamePathFolder));
    }

    //edit menu actions
    private void deselectAllItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        this.graphEditorInputMode.clearSelection();
    }

    private void selectAllItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        this.graphEditorInputMode.selectAll();
    }

    private void clearAllItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        this.graph.clear();
        this.removedVertices = null;
    }

    private void clearSelectedItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        if (this.graphEditorInputMode.isClearSelectionAllowed()) {
            this.graphEditorInputMode.clearSelection();
        }
    }

    private void pasteItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        this.graphEditorInputMode.paste();
    }

    private void copyItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        this.graphEditorInputMode.copy();
    }

    private void cutItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        this.graphEditorInputMode.cut();
    }

    private void redoItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        if (this.graphEditorInputMode.isUndoOperationsAllowed()) {
            this.graphEditorInputMode.redo();
        }
    }

    private void undoItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        if (this.graphEditorInputMode.isUndoOperationsAllowed()) {
            this.graphEditorInputMode.undo();
        }
    }

    private void jitterItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        GridPositioning.removeOverlaps(this.graph, 5);
        this.view.updateUI();
    }

    private void swapperItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
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

        NodeSwapper.swapNodes(this.graph, nodes, crossing);
        this.view.updateUI();

    }

    private void gridCrossingItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        do {
            GridPositioning.gridGraph(this.graph);
            GridPositioning.removeOverlaps(this.graph, 0.1);
        } while (!GridPositioning.isGridGraph(this.graph));

        System.out.println("Graph is gridded: " + GridPositioning.isGridGraph(this.graph));
        this.view.updateUI();
    }


    private void graphGridItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        do {
            GridPositioning.gridGraphFast(this.graph);
            GridPositioning.removeOverlaps(this.graph, 0.1);
        } while (!GridPositioning.isGridGraph(this.graph));

        System.out.println("Graph is gridded: " + GridPositioning.isGridGraph(this.graph));
        this.view.updateUI();
    }

    private void quickAndDirtyGridItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        GridPositioning.gridQuickAndDirty(this.graph);
        if (!GridPositioning.isGridGraph(this.graph)) {
            System.out.println("Error occured with the gridding of the graph");
        }
        System.out.println("Graph is gridded: " + GridPositioning.isGridGraph(this.graph));
        this.view.updateUI();
    }

    private void gridItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
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

    private void clinchNodesActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        IGraphSelection selection = graphEditorInputMode.getGraphSelection();
        InitClinchLayout.run(graph, selection, progressBar);
    }

    private void randomMovementActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        // TODO
//        RandomMovementConfigurator configurator = new RandomMovementConfigurator();
//        configurator.init(new SidePanelItemFactory(mainFrame.sidePanel, view, gridBagState));
//
//        RandomMovementLayout layout = new RandomMovementLayout(graph, configurator);
//        IGraphLayoutExecutor layoutExecutor =
//            new IGraphLayoutExecutor(layout, graph, progressBar, 1000000, 20);
//        layoutExecutor.start();
    }

    private void minimumCrossingAngleMenuActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        minimumAngleMonitor.showMinimumAngle(graph, view, infoLabel, true);
    }

    private void overlappingNodesMenuActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        Set<Double> seenCoordinatesX = new HashSet<>();
        Set<Double> seenCoordinatesY = new HashSet<>();

        for (INode u : this.graph.getNodes()) {
            double u_x = u.getLayout().getCenter().getX();
            double u_y = u.getLayout().getCenter().getY();

            if (seenCoordinatesX.contains(u_x) && seenCoordinatesY.contains(u_y)) {
                INodeStyle s = u.getStyle();
                if (s instanceof ShinyPlateNodeStyle) {
                    ((ShinyPlateNodeStyle) s).setPaint(Color.MAGENTA);
                    ((ShinyPlateNodeStyle) s).setPen(new Pen(Color.MAGENTA, 1));

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
}