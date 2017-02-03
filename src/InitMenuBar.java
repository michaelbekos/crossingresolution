import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.LayoutUtilities;
import com.yworks.yfiles.layout.circular.CircularLayout;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.layout.orthogonal.OrthogonalLayout;
import com.yworks.yfiles.layout.tree.TreeLayout;
import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.input.GraphEditorInputMode;

import io.ContestIOHandler;
import layout.algo.*;
import layout.algo.event.AlgorithmEvent;
import layout.algo.event.AlgorithmListener;
import util.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.time.Duration;

/**
 * Created by khokhi on 10.12.16.
 */
public class InitMenuBar {

    private JMenu fileMenu = new JMenu();
    private JMenuBar mainMenuBar = new JMenuBar();
    private JMenuItem newMenuItem = new JMenu();
    private JProgressBar progressBar;

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

    public InitMenuBar(JMenuBar mainMenuBar, JMenu layoutMenu, JMenu viewMenu, IGraph graph, JLabel infoLabel, GraphComponent view, JProgressBar progressBar,
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
        this.progressBar = progressBar;
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
                ForceAlgorithmApplier.init();
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
                ForceAlgorithmApplier.init();
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
                ForceAlgorithmApplier.init();
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

        mainMenuBar.add(fileMenu);



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

        /* View Menu */
        JMenu yFileslayoutMenu = new JMenu();
        yFileslayoutMenu.setText("yFiles Layout");

        JMenuItem orthogonalItem = new JMenuItem();
        orthogonalItem.setIcon(new ImageIcon(getClass().getResource("/resources/layout-16.png")));
        orthogonalItem.setText("Orthogonal");
        orthogonalItem.addActionListener(this::orthogonalItemActionPerformed);
        yFileslayoutMenu.add(orthogonalItem);

        JMenuItem circularItem = new JMenuItem();
        circularItem.setIcon(new ImageIcon(getClass().getResource("/resources/layout-16.png")));
        circularItem.setText("Circular");
        circularItem.addActionListener(this::circularItemActionPerformed);
        yFileslayoutMenu.add(circularItem);

        JMenuItem treeItem = new JMenuItem();
        treeItem.setIcon(new ImageIcon(getClass().getResource("/resources/layout-16.png")));
        treeItem.setText("Tree");
        treeItem.addActionListener(this::treeItemActionPerformed);
        yFileslayoutMenu.add(treeItem);

        JMenuItem organicItem = new JMenuItem();
        organicItem.setIcon(new ImageIcon(getClass().getResource("/resources/layout-16.png")));
        organicItem.setText("Organic");
        organicItem.addActionListener(this::organicItemActionPerformed);
        yFileslayoutMenu.add(organicItem);

        JMenuItem yFilesSpringEmbedderItem = new JMenuItem();
        yFilesSpringEmbedderItem.setIcon(new ImageIcon(getClass().getResource("/resources/layout-16.png")));
        yFilesSpringEmbedderItem.setText("yFiles Spring Embedder");
        yFilesSpringEmbedderItem.addActionListener(this::yFilesSpringEmbedderItemActionPerformed);
        yFileslayoutMenu.add(yFilesSpringEmbedderItem);

        mainMenuBar.add(yFileslayoutMenu);
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
                ForceAlgorithmApplier.init();
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

    private void openContestItemActionPerformed(ActionEvent evt) {
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

    private void exportItemActionPerformed(ActionEvent evt) {
        if (this.fileNamePath != null) {
            try {
                System.out.println(this.fileNamePath);
                ContestIOHandler.write(this.graph, this.fileNamePath);
            } catch (IOException ioe) {
                this.infoLabel.setText("An error occured while exporting the graph.");
            }
        } else {
            JFileChooser chooser = new JFileChooser();
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
    }

    private void yFilesSpringEmbedderItemActionPerformed(ActionEvent evt){
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

        ForceDirectedAlgorithm fd = new ForceDirectedAlgorithm(view, iterations) {
            public void calculateVectors() {
                ForceDirectedFactory.calculateSpringForcesEades(graph, 150, 100, 0.01, map);
                ForceDirectedFactory.calculateElectricForcesEades(graph, 50000, 0.01, map);
            }
        };
        fd.addAlgorithmListener(new AlgorithmListener() {
            public void algorithmStarted(AlgorithmEvent evt) {
            }

            public void algorithmFinished(AlgorithmEvent evt) {
                progressBar.setValue(0);
                view.fitContent();
                view.updateUI();
            }

            public void algorithmStateChanged(AlgorithmEvent evt) {
                progressBar.setValue(evt.currentStatus());
            }
        });
        Thread thread = new Thread(fd);
        thread.start();
        this.view.updateUI();
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

    private void organicItemActionPerformed(ActionEvent evt) {
        LayoutUtilities.morphLayout(this.view, new OrganicLayout(), Duration.ofSeconds(1), null);
    }

    private void circularItemActionPerformed(ActionEvent evt) {
        LayoutUtilities.morphLayout(this.view, new CircularLayout(), Duration.ofSeconds(1), null);
    }

    private void orthogonalItemActionPerformed(ActionEvent evt) {
        LayoutUtilities.morphLayout(this.view, new OrthogonalLayout(), Duration.ofSeconds(1), null);
    }

    private void treeItemActionPerformed(ActionEvent evt) {
        try {
            LayoutUtilities.morphLayout(this.view, new TreeLayout(), Duration.ofSeconds(1), null);
        } catch (Exception exc) {
            this.infoLabel.setText("The input graph is not a tree or a forest.");
        }
    }

}
