import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.LayoutUtilities;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.input.GraphEditorInputMode;
import util.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

/**
 * Created by khokhi on 10.12.16.
 */
public class InitMenuBar {

    private JMenu fileMenu = new JMenu();
    private JMenuBar mainMenuBar = new JMenuBar();
    private JMenuItem newMenuItem = new JMenu();
    private JMenu editMenu = new JMenu();
    private JMenu layoutMenu = new JMenu();
    private IGraph graph;
    private JLabel infoLabel = new JLabel();
    private GraphComponent view;
    private GraphEditorInputMode graphEditorInputMode = new GraphEditorInputMode();
    private JMenu viewMenu = new JMenu();
    private OrganicLayout defaultLayouter = new OrganicLayout();

    /* Object that keeps track of the latest open/saved file */
    private String fileNamePath;
    private String fileNamePathFolder;

    public InitMenuBar(JMenuBar mainMenuBar, JMenu layoutMenu, JMenu viewMenu, IGraph graph, JLabel infoLabel, GraphComponent view,
                       GraphEditorInputMode graphEditorInputMode, OrganicLayout defaultLayouter, String filePathFolder, String filePath) {
        this.mainMenuBar = mainMenuBar;
        this.layoutMenu = layoutMenu;
        this.viewMenu = viewMenu;
        this.graph = graph;
        this.infoLabel = infoLabel;
        this.view = view;
        this.graphEditorInputMode = graphEditorInputMode;
        this.defaultLayouter = defaultLayouter;
        this.fileNamePathFolder = filePathFolder;
        this.fileNamePath = filePath;
    }


    public JMenuBar initMenuBar() {

        /* File Menu */
        fileMenu.setText("File");
        mainMenuBar.add(fileMenu);

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
                HypercubeGenerator.generate(graph, dimensions);
            }
        });
        newMenuItem.add(hypercubeGraphItem);

        JMenuItem gridGraphItem = new JMenuItem();
        gridGraphItem.setIcon(new ImageIcon(getClass().getResource("/resources/new-document-16.png")));
        gridGraphItem.setText("Grid graph");
        gridGraphItem.addActionListener(e -> {
            JTextField xCount = new JTextField("5");
            JTextField yCount = new JTextField("5");
            int result = JOptionPane.showOptionDialog(null, new Object[]{"xCount: ", xCount, "yCount: ", yCount}, "Graph Properties", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (result == JOptionPane.OK_OPTION) {
                int xC = Integer.parseInt(xCount.getText());
                int yC = Integer.parseInt(yCount.getText());
                GridGenerator.generate(graph, xC, yC);
            }
        });
        newMenuItem.add(gridGraphItem);

        fileMenu.add(newMenuItem);
        fileMenu.add(new JSeparator());

        JMenuItem openItem = new JMenuItem();
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        openItem.setIcon(new ImageIcon(getClass().getResource("/resources/open-16.png")));
        openItem.setText("Open");
        openItem.addActionListener(this::openItemActionPerformed);
        fileMenu.add(openItem);

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

        JMenuItem quitItem = new JMenuItem();
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
        quitItem.setIcon(new ImageIcon(getClass().getResource("/resources/exit-16.png")));
        quitItem.setText("Quit");
        quitItem.addActionListener(evt -> System.exit(0));
        fileMenu.add(quitItem);

        mainMenuBar.add(fileMenu);

        /* Edit Menu */
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

        mainMenuBar.add(editMenu);

        /* View Menu */

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
        mainMenuBar.add(viewMenu);

       /* View Menu */

        layoutMenu.setText("Layout");



    /*JMenuItem minimumAngleImprovementItem = new JMenuItem();
    minimumAngleImprovementItem.setIcon(new ImageIcon(getClass().getResource("/resources/layout-16.png")));
    minimumAngleImprovementItem.setText("Testing Improvement of Minimal Crossing Angle");
    minimumAngleImprovementItem.addActionListener(this::minimumAngleImprovementItemActionPerformed);
    layoutMenu.add(minimumAngleImprovementItem);*/

        return this.mainMenuBar;
    }


    private void fitContentItemActionPerformed(ActionEvent evt) {
        this.view.fitGraphBounds();
    }

    private void zoomOutItemActionPerformed(ActionEvent evt) {
        this.view.setZoom(this.view.getZoom() - 0.2);
    }

    private void zoomInItemActionPerformed(ActionEvent evt) {
        this.view.setZoom(this.view.getZoom() + 0.2);
    }

    private void deselectAllItemActionPerformed(ActionEvent evt) {
        this.graphEditorInputMode.clearSelection();
    }

    private void selectAllItemActionPerformed(ActionEvent evt) {
        this.graphEditorInputMode.selectAll();
    }

    private void clearAllItemActionPerformed(ActionEvent evt) {
        this.graph.clear();
    }

    private void clearSelectedItemActionPerformed(ActionEvent evt) {
        if (this.graphEditorInputMode.isClearSelectionAllowed()) {
            this.graphEditorInputMode.clearSelection();
        }
    }

    private void pasteItemActionPerformed(ActionEvent evt) {
        this.graphEditorInputMode.paste();
    }

    private void copyItemActionPerformed(ActionEvent evt) {
        this.graphEditorInputMode.copy();
    }

    private void cutItemActionPerformed(ActionEvent evt) {
        this.graphEditorInputMode.cut();
    }

    private void redoItemActionPerformed(ActionEvent evt) {
        if (this.graphEditorInputMode.isUndoOperationsAllowed()) {
            this.graphEditorInputMode.redo();
        }
    }

    private void undoItemActionPerformed(ActionEvent evt) {
        if (this.graphEditorInputMode.isUndoOperationsAllowed()) {
            this.graphEditorInputMode.undo();
        }
    }

    private void blankGraphItemGraphItemActionPerformed(ActionEvent evt) {
        this.graph.clear();
        this.view.updateUI();
    }

    /*private void minimumAngleImprovementItemActionPerformed(ActionEvent evt) {

        JTextField movementsTextField = new JTextField("0.1");
        double movements = 0.1;

        int result = JOptionPane.showOptionDialog(null, new Object[]{"Steps of Movement to the Left/Right: ", movementsTextField}, "Algorithm Properties", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

        if (result == JOptionPane.OK_OPTION) {
            try {
                movements = Double.parseDouble(movementsTextField.getText());
            } catch (NumberFormatException exc) {
                JOptionPane.showMessageDialog(null, "Incorrect input.\nThe steps of movement to the left/right will be set to 0.1.", "Incorrect Input", JOptionPane.ERROR_MESSAGE);
            }
        }

        MinimumAngleImprovement mc = new MinimumAngleImprovement(this.view.getGraph());
        mc.minimumAngleImprovement(movements);

    }
    */

    private void randomGraphItemActionPerformed(ActionEvent evt) {
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
                rgg.generate(this.graph);
                LayoutUtilities.applyLayout(this.graph, this.defaultLayouter);
                this.view.fitGraphBounds();
                this.view.updateUI();
            }
        }
    }

    private void openItemActionPerformed(ActionEvent evt) {
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
                this.fileNamePathFolder = chooser.getSelectedFile().getParent();

            } catch (IOException ioe) {
                this.infoLabel.setText("An error occured while reading the input file.");
            }
        }
    }

    private void reloadItemActionPerformed(ActionEvent evt) {
        if (this.fileNamePath != null) {
            try {
                this.graph.clear();
                this.view.importFromGraphML(this.fileNamePath);
                this.view.fitGraphBounds();
                this.view.updateUI();
            } catch (IOException ioe) {
                this.infoLabel.setText("An error occured while reading the input file.");
            }
        } else {
            infoLabel.setText("No file was recently opened.");
        }
    }

    private void saveItemActionPerformed(ActionEvent evt) {
        if (this.fileNamePath != null) {
            try {
                this.view.exportToGraphML(this.fileNamePath);
            } catch (IOException ioe) {
                this.infoLabel.setText("An error occured while exporting the graph.");
            }
        } else {
            JFileChooser chooser = new JFileChooser();
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
    }

    private void saveAsItemActionPerformed(ActionEvent evt) {
        JFileChooser chooser = new JFileChooser(this.fileNamePathFolder);
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


}
