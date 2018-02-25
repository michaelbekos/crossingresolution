package main;

import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.LayoutUtilities;
import com.yworks.yfiles.graph.Mapper;
import com.yworks.yfiles.graph.styles.INodeStyle;
import com.yworks.yfiles.graph.styles.ShinyPlateNodeStyle;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.GridVisualCreator;
import com.yworks.yfiles.view.Pen;
import com.yworks.yfiles.view.input.GraphEditorInputMode;
import com.yworks.yfiles.view.input.GraphSnapContext;
import com.yworks.yfiles.view.input.GridSnapTypes;
import graphoperations.Chains;
import graphoperations.RemovedChains;
import graphoperations.RemovedNodes;
import graphoperations.Scaling;
import io.ContestIOHandler;
import layout.algo.utils.PositionMap;
import randomgraphgenerators.GridGenerator;
import randomgraphgenerators.HypercubeGenerator;
import randomgraphgenerators.LayeredGridGenerator;
import randomgraphgenerators.RandomGraphGenerator;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
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

    private RemovedNodes removedNodes;
    private RemovedChains removedChains;

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

        this.removedNodes = new RemovedNodes(graph);
    }


    public JMenuBar initMenuBar() {
        JMenuBar mainMenuBar = new JMenuBar();

        mainMenuBar.add(createFileMenu());
        mainMenuBar.add(createEditMenu());
        mainMenuBar.add(createViewMenu());
        mainMenuBar.add(createGraphOpsMenu());

        return mainMenuBar;
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
        enforcelegal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
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
        fitContentItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_MASK));
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
                mainFrame.bestSolution.reset();
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
                mainFrame.bestSolution.reset();
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
                mainFrame.bestSolution.reset();
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
        mainFrame.initSidePanel.removeDefaultListeners();
        Mapper<INode, PointD> nodePositions = PositionMap.FromIGraph(graph);
        Scaling.scaleBy(2.0, nodePositions);
        PositionMap.applyToGraph(graph, nodePositions);
        this.view.fitGraphBounds();
        mainFrame.initSidePanel.addDefaultListeners();
    }

    private void scaleDownGraphItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        mainFrame.initSidePanel.removeDefaultListeners();
        Mapper<INode, PointD> nodePositions = PositionMap.FromIGraph(graph);
        Scaling.scaleBy(0.5, nodePositions);
        PositionMap.applyToGraph(graph, nodePositions);
        this.view.fitGraphBounds();
        mainFrame.initSidePanel.addDefaultListeners();
    }

    private void removeVerticesItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        mainFrame.initSidePanel.removeDefaultListeners();
        if (this.view.getSelection().getSelectedNodes().size() > 0) {
            removedNodes.removeSelected(view.getSelection().getSelectedNodes());
        } else if (this.graph.getNodes().size() > 0){
            JTextField vertexCount = new JTextField();
            vertexCount.setText(Integer.toString(1));
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            JRadioButton useHighest = new JRadioButton("Remove Highest Degree Vertices"),
                    useLowest = new JRadioButton("Remove Lowest Degree (0, 1, 2) Vertices"),
                    useChains = new JRadioButton("Remove Chains (Connected Vertices with Deg <= 2) ");
            ButtonGroup bg = new ButtonGroup();
            bg.add(useHighest);
            bg.add(useLowest);
            bg.add(useChains);
            JLabel label = new JLabel();
            String highDeg = "Number of highest degree vertices to remove: ";
            String lowDeg = "Number of lowest degree vertices to remove: ";
            String chainNumStr = "Number of chains to remove: ";
            label.setText(highDeg);
            panel.add(label);
            panel.add(vertexCount);
            panel.add(useHighest);
            panel.add(useLowest);
            panel.add(useChains);
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
                }
            });
            useLowest.addItemListener(itemEvent -> {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    label.setText(lowDeg);
                    vertexCount.setText(Integer.toString(numLowestFinal));
                }
            });


            Chains chains = Chains.analyze(graph);
            int chainNum = chains.number();
            useChains.addItemListener(itemEvent -> {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    label.setText(chainNumStr);
                    vertexCount.setText(Integer.toString(chainNum));
                }
            });
            useHighest.setSelected(false);
            useLowest.setSelected(false);
            useChains.setSelected(true);
            int result = JOptionPane.showOptionDialog(null, panel, "Remove Highest or Lowest Degree Vertices", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    int numVertices = Integer.parseInt(vertexCount.getText());
                    if(numVertices > graph.getNodes().size()){
                        numVertices = graph.getNodes().size();
                        if( 0 != JOptionPane.showConfirmDialog(null, "Input is greater than the number of all nodes.\n Remove all nodes?")){
                            numVertices = 0;
                        }
                    }

                    if (useChains.isSelected()) {
                        removedChains = chains.remove();
                    } else if (useHighest.isSelected()) {
                        removedNodes.removeHighestDegree(numVertices);
                    } else {
                        removedNodes.removeLowestDegree(numVertices);
                    }
                } catch (NumberFormatException exc) {
                    JOptionPane.showMessageDialog(null, "Incorrect input.\nNo vertex will be removed.", "Incorrect Input", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        mainFrame.initSidePanel.addDefaultListeners();
    }

    private void enforcelegal(@SuppressWarnings("unused") ActionEvent evt){
        mainFrame.initSidePanel.removeDefaultListeners();
        Mapper<INode, PointD> nodePositions = PositionMap.FromIGraph(graph);
        boolean change=true;
        while(change){
            change=false;
            for(INode u : graph.getNodes()){
                if (u.getLayout().getCenter().getX()<0 || u.getLayout().getCenter().getX() > MainFrame.BOX_SIZE || u.getLayout().getCenter().getY()<0 || u.getLayout().getCenter().getY() > MainFrame.BOX_SIZE){
                    Scaling.scaleBy(0.9, nodePositions);
                    PositionMap.applyToGraph(graph, nodePositions);
                    this.view.fitGraphBounds();
                    change=true;
                }
            }
        }
        mainFrame.initSidePanel.addDefaultListeners();
    }

    private void reinsertVerticesItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        mainFrame.initSidePanel.removeDefaultListeners();
        if (!removedNodes.isEmpty()){
            JTextField vertexComponentCount = new JTextField();
            vertexComponentCount.setText(Integer.toString(removedNodes.size()));
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
                    vertexComponentCount.setText(Integer.toString(1));
                } else if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
                    label.setText(component);
                    vertexComponentCount.setText(Integer.toString(removedNodes.size()));
                }
            });
            useVertices.setSelected(false);
            useComponents.setSelected(true);
            if (useVertices.isSelected()) {
                label.setText(vertex);
                vertexComponentCount.setText(Integer.toString(1));
            } else if (useComponents.isSelected()) {
                label.setText(component);
                vertexComponentCount.setText(Integer.toString(removedNodes.size()));
            }

            int result = JOptionPane.showOptionDialog(null, panel, "Reinsert Vertices or Components", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (result == JOptionPane.OK_OPTION) {
                int numVertices;
                try {
                    numVertices = Integer.parseInt(vertexComponentCount.getText());

                    if (!useVertices.isSelected()) {
                        removedNodes.reinsert(numVertices);
                    } else {
                        removedNodes.reinsertSingleNodes(numVertices);
                    }

                    Scaling.scaleNodeSizes(view);
                } catch (NumberFormatException exc) {
                    JOptionPane.showMessageDialog(null, "Incorrect input.\nNo vertex will be reinserted.", "Incorrect Input", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        mainFrame.initSidePanel.addDefaultListeners();
    }
    
    private void fitContentItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        mainFrame.initSidePanel.removeDefaultListeners();
        this.view.fitGraphBounds();
        mainFrame.initSidePanel.addDefaultListeners();
    }

    private void zoomOutItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        this.view.setZoom(this.view.getZoom() - 0.2);
    }

    private void zoomInItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        this.view.setZoom(this.view.getZoom() + 0.2);
    }



    private void blankGraphItemGraphItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        mainFrame.initSidePanel.removeDefaultListeners();
        this.graph.clear();
        this.removedNodes.clear();
        this.view.updateUI();
        mainFrame.bestSolution.reset();
        mainFrame.initSidePanel.addDefaultListeners();
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
            mainFrame.initSidePanel.removeDefaultListeners();
            try {
                rgg.setNodeCount(Integer.parseInt(nodeCount.getText()));
                rgg.setEdgeCount(Integer.parseInt(edgeCount.getText()));
                mainFrame.bestSolution.reset();
            } catch (NumberFormatException exc) {
                JOptionPane.showMessageDialog(null, "Incorrect input.\nThe graph will be created with 10 nodes and 10 edges.", "Incorrect Input", JOptionPane.ERROR_MESSAGE);
                rgg.setNodeCount(10);
                rgg.setEdgeCount(10);
            } finally {
                rgg.generate(this.graph);
                LayoutUtilities.applyLayout(this.graph, this.defaultLayouter);
                this.view.fitGraphBounds();
                this.view.updateUI();
                removedNodes.clear();
                mainFrame.initSidePanel.addDefaultListeners();
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
            mainFrame.initSidePanel.removeDefaultListeners();
            try {
                this.view.importFromGraphML(fileNamePath);
                this.view.fitGraphBounds();
                this.view.updateUI();
                this.removedNodes.clear();
                this.fileNamePathFolder = chooser.getSelectedFile().getParent();
                mainFrame.bestSolution.reset();
                mainFrame.setTitle(Paths.get(fileNamePath).getFileName().toString());
            } catch (IOException ioe) {
                this.infoLabel.setText("An error occured while reading the input file.");
            } finally {
                mainFrame.initSidePanel.addDefaultListeners();
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
            mainFrame.openContestFile(fileNamePath);
            removedNodes.clear();
            this.fileNamePathFolder = chooser.getSelectedFile().getParent();
        }
    }

    private void reloadItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        if (this.fileNamePath != null) {
            mainFrame.initSidePanel.removeDefaultListeners();
            try {
                this.graph.clear();
                if (this.fileNamePath.endsWith(".graphml")) {
                    this.view.importFromGraphML(this.fileNamePath);
                } else if (this.fileNamePath.endsWith(".txt")) {
                    ContestIOHandler.read(this.graph, this.fileNamePath);
                }
                this.view.fitGraphBounds();
                this.view.updateUI();
                this.removedNodes.clear();
                this.mainFrame.bestSolution.reset();
                this.mainFrame.minimumAngleMonitor.updateMinimumAngleInfoBar();
            } catch (IOException ioe) {
                this.infoLabel.setText("An error occured while reading the input file.");
            } finally {
                mainFrame.initSidePanel.addDefaultListeners();
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
                ContestIOHandler.write(this.graph, this.fileNamePath, mainFrame.initSidePanel.getOutputTextArea());
            } catch (IOException ioe) {
                this.infoLabel.setText("An error occured while exporting the graph.");
            }
        }
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
        mainFrame.initSidePanel.removeDefaultListeners();
        this.graph.clear();
        removedNodes.clear();
        mainFrame.initSidePanel.addDefaultListeners();
    }

    private void clearSelectedItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        if (this.graphEditorInputMode.isClearSelectionAllowed()) {
            mainFrame.initSidePanel.removeDefaultListeners();
            this.graphEditorInputMode.clearSelection();
            mainFrame.initSidePanel.addDefaultListeners();
        }
    }

    private void pasteItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        mainFrame.initSidePanel.removeDefaultListeners();
        this.graphEditorInputMode.paste();
        mainFrame.initSidePanel.addDefaultListeners();
    }

    private void copyItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        this.graphEditorInputMode.copy();
    }

    private void cutItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        mainFrame.initSidePanel.removeDefaultListeners();
        this.graphEditorInputMode.cut();
        mainFrame.initSidePanel.addDefaultListeners();
    }

    private void redoItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        if (this.graphEditorInputMode.isUndoOperationsAllowed()) {
            mainFrame.initSidePanel.removeDefaultListeners();
            this.graphEditorInputMode.redo();
            mainFrame.initSidePanel.addDefaultListeners();
        }
    }

    private void undoItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        if (this.graphEditorInputMode.isUndoOperationsAllowed()) {
            mainFrame.initSidePanel.removeDefaultListeners();
            this.graphEditorInputMode.undo();
            mainFrame.initSidePanel.addDefaultListeners();
        }
    }

    private void gridItemActionPerformed(@SuppressWarnings("unused") ActionEvent evt) {
        mainFrame.initSidePanel.removeDefaultListeners();
        if (this.isGridVisible) {
            this.isGridVisible = false;
            this.graphSnapContext.setGridSnapType(GridSnapTypes.NONE);
        } else {
            this.isGridVisible = true;
            this.graphSnapContext.setGridSnapType(GridSnapTypes.GRID_POINTS);
        }
        this.gridVisualCreator.setVisible(this.isGridVisible);
        this.view.updateUI();
        mainFrame.initSidePanel.addDefaultListeners();
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
