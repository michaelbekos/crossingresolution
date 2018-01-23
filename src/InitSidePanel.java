import algorithms.graphs.CachedMinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.*;
import layout.algo.*;
import layout.algo.forces.CrossingForce;
import layout.algo.forces.IncidentEdgesForce;
import layout.algo.forces.NodeNeighbourForce;
import layout.algo.forces.NodePairForce;
import layout.algo.genetic.GeneticForceAlgorithmConfigurator;
import layout.algo.genetic.GeneticAlgorithm;
import layout.algo.genetic.GeneticForceAlgorithmLayout;
import layout.algo.layoutinterface.*;
import layout.algo.utils.PositionMap;
import util.GraphOperations;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Optional;

public class InitSidePanel {
    private MainFrame mainFrame;
    public JTabbedPane tabbedSidePane;

    public InitSidePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.tabbedSidePane = new JTabbedPane();
    }

    public JTabbedPane initSidePanel(JPanel mainPanel, GridBagConstraints c) {

        GridBagConstraints cc = new GridBagConstraints();
        cc.gridx = 1;
        cc.gridy = 1;
        cc.weighty = 1;
        cc.weightx = 0.2;
        cc.insets = new Insets(0, 0, 0, 0);
        cc.fill = GridBagConstraints.BOTH;

        TrashCan.init();

//----------------------------------------

        RandomMovementConfigurator config = new RandomMovementConfigurator();
        config.init(new SidePanelItemFactory(mainFrame.sidePanel, mainFrame.view));

        RandomMovementLayout layout = new RandomMovementLayout(mainFrame.graph, config);
        IGraphLayoutExecutor layoutExecutor = new IGraphLayoutExecutor(layout, mainFrame.graph, mainFrame.progressBar, -1, 20);

        addAlgorithm("Random Movement", config, layoutExecutor);
//----------------------------------------

//        ForceAlgorithm fd = mainFrame.defaultForceAlgorithm();

        CachedMinimumAngle cMinimumAngle = new CachedMinimumAngle();
        IGraph graph = mainFrame.view.getGraph();

        ForceAlgorithmConfigurator configurator = new ForceAlgorithmConfigurator();
        configurator.addForce(new NodeNeighbourForce(graph))
            .addForce(new NodePairForce(graph))
            .addForce(new CrossingForce(graph, cMinimumAngle))
            .addForce(new IncidentEdgesForce(graph));

        configurator.init(new SidePanelItemFactory(mainFrame.sidePanel, mainFrame.view));

        ForceAlgorithm fd = new ForceAlgorithm(configurator, mainFrame.graph, cMinimumAngle);

        IGraphLayoutExecutor forceExecutor = new IGraphLayoutExecutor(fd, mainFrame.view.getGraph(), mainFrame.progressBar, -1, 20);
        mainFrame.view.updateUI();

        addAlgorithm("Force Algorithm", configurator, forceExecutor);
//----------------------------------------

      //Config, layout, layoutexecutor for all other algos too

      GeneticForceAlgorithmConfigurator geneticConfigurator = new GeneticForceAlgorithmConfigurator();
      geneticConfigurator.init(mainFrame.sidePanelItemFactory);
      GeneticForceAlgorithmLayout geneticAlgo = new GeneticForceAlgorithmLayout(geneticConfigurator, graph);
      IGraphLayoutExecutor geneticExecutor = new IGraphLayoutExecutor(geneticAlgo, graph, mainFrame.progressBar, 1000, 20);
      addAlgorithm("Genetic Algorithm", geneticConfigurator, geneticExecutor);




        addAlgorithm("Clinch Nodes", null, null);
        addAlgorithm("Sloped Spring Embedder", null, null);


//----------------------------------------

////        ForceAlgorithm fd = mainFrame.defaultForceAlgorithm();
////        mainFrame.forceAlgorithm = fd;
//
//        ForceAlgorithmConfigurator springConfigurator = new ForceAlgorithmConfigurator();
//        configurator.init(new SidePanelItemFactory(mainFrame.sidePanel, mainFrame.view));
//
////        CachedMinimumAngle cMinimumAngle = new CachedMinimumAngle();
//        ForceAlgorithm springfd = new ForceAlgorithm(configurator, mainFrame.view, cMinimumAngle);
//
////        IGraph graph = mainFrame.view.getGraph();
//
//        springfd.forces.add(new NodeNeighbourForce(configurator, graph));
//        springfd.forces.add(new NodePairForce(configurator, graph));
//        springfd.forces.add(new CrossingForce(configurator, graph, cMinimumAngle));
//        springfd.forces.add(new IncidentEdgesForce(configurator, graph));
//
//        IGraphLayoutExecutor springEmbedderExecutor = new IGraphLayoutExecutor(fd, mainFrame.graph, mainFrame.progressBar, iterations, 20);
//        mainFrame.view.updateUI();
//
//        addAlgorithm("Spring Embedder", springConfigurator, springEmbedderExecutor);
//----------------------------------------

        addMiscAlgorithms();

        mainPanel.add(tabbedSidePane, cc);

        return tabbedSidePane;
    }

    /**
     * Adds an algorithm in a new tab, top panel is defined by the configurator, bottom is the default panel
     * @param algorithmName - name of tab
     * @param configurator - which and what parameters
     * @param executor - interface to algorithm for start/pause/stop buttons and controls for above parameters
     */
    private void addAlgorithm(String algorithmName, ILayoutConfigurator configurator, IGraphLayoutExecutor executor) {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new GridBagLayout());
        GridBagConstraints cSidePanel = new GridBagConstraints();

        //algorithm configurator specific controls
        JPanel custom = new JPanel();
        custom.setLayout(new GridBagLayout());
        GridBagConstraints cCustomPanel = new GridBagConstraints();
        cCustomPanel.gridx = 0;
        cCustomPanel.gridy = 1;
//        cCustomPanel.anchor = GridBagConstraints.PAGE_START;

        int cCustomPanelY = 1;
        if (configurator != null && configurator.getAbstractLayoutInterfaceItems() != null) {
            ArrayList<AbstractLayoutInterfaceItem> parameters = configurator.getAbstractLayoutInterfaceItems();
            for (int i = 0; i < parameters.size(); i++) {
                cCustomPanel.fill = GridBagConstraints.HORIZONTAL;
                cCustomPanel.gridx = 0;
                cCustomPanel.gridy = cCustomPanelY;
                if (parameters.get(i).getValue().getClass().equals(Double.class)) {
                    //add slider
                    GridBagConstraints cLabel = new GridBagConstraints();
                    GridBagConstraints cSlider = new GridBagConstraints();
                    GridBagConstraints cSliderMax = new GridBagConstraints();
                    cSlider.fill = GridBagConstraints.HORIZONTAL;

                    DoubleSidePanelItem item = (DoubleSidePanelItem) parameters.get(i);
                    double[] threshold = {item.getThreshold()};
                    JTextField out = new JTextField(Double.toString(item.getValue()));
                    out.setColumns(5);
                    GridBagConstraints cout = new GridBagConstraints();

                    double max = item.getMaxValue();
                    JSlider slider = new JSlider(0, (int) (max * 1000 * threshold[0]));
//                    slider.setValue((int) (1000 * threshold[0]));
                    slider.setValue((int) (1000 * item.getValue()));
                    slider.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            JSlider source = (JSlider) e.getSource();
                            int val = source.getValue();
                            threshold[0] = val / 1000.0;    //TODO less messy
                            item.setValue(threshold[0]);
                            out.setText(Double.toString(threshold[0]));
//                            System.out.println(threshold[0]);
                        }
                    });

                    SpinnerModel model = new SpinnerNumberModel(max * threshold[0], threshold[0] / 10.0, 1000 * max * threshold[0], threshold[0] / 10.0);
                    JSpinner spinner = new JSpinner(model);
                    JComponent editor = new JSpinner.NumberEditor(spinner, "#,##0.###");
                    spinner.setEditor(editor);
                    ((JSpinner.DefaultEditor) (spinner.getEditor())).getTextField().setColumns(5);
                    spinner.addChangeListener(new ChangeListener() {
                        final JSlider s = slider;

                        public void stateChanged(ChangeEvent e) {
                            JSpinner source = (JSpinner) e.getSource();
                            Double val = (Double) source.getValue();
                            int maxValSlider = (int) (val * 1000);
                            s.setMaximum(maxValSlider);
                        }
                    });


                    slider.setSize(400, 30);
                    cLabel.gridx = 1;
                    cLabel.gridy = ++cCustomPanelY;

                    cout.gridx = 0;
                    cout.gridy = ++cCustomPanelY;
                    cout.fill = GridBagConstraints.HORIZONTAL;
                    cSlider.gridx = 1;
                    cSlider.gridy = cCustomPanelY;
                    cSlider.fill = GridBagConstraints.HORIZONTAL;
                    cSliderMax.gridy = cCustomPanelY;
                    cSliderMax.gridx = 2;
                    cSliderMax.fill = GridBagConstraints.HORIZONTAL;
                    custom.add(new JLabel(parameters.get(i).getName()), cLabel);
                    custom.add(out, cout);
                    custom.add(slider, cSlider);
                    custom.add(spinner, cSliderMax);

                } else if (parameters.get(i).getValue().getClass().equals(Boolean.class)) {
                    //add checkbox
                    cCustomPanel.gridy = ++cCustomPanelY;
                    cCustomPanel.gridx = 0;
                    JCheckBox checkBox = new JCheckBox(parameters.get(i).getName());
                    custom.add(checkBox, cCustomPanel);
                    BoolSidePanelItem item = (BoolSidePanelItem) parameters.get(i);
                    checkBox.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent itemEvent) {
                            item.setValue((itemEvent.getStateChange() == ItemEvent.SELECTED));
                        }
                    });
//                    checkBox.addItemListener(this::minimumAngleDisplayEnabled);
                    checkBox.setSelected((boolean) parameters.get(i).getValue());

                } else if (parameters.get(i).getValue().getClass().equals(Integer.class)) {
                    //maybe textfield vs slider for ints?
                    //add slider
                    GridBagConstraints cLabel = new GridBagConstraints();
                    GridBagConstraints cSlider = new GridBagConstraints();
                    GridBagConstraints cSliderMax = new GridBagConstraints();
                    cSlider.fill = GridBagConstraints.HORIZONTAL;

                    IntegerSidePanelItem item = (IntegerSidePanelItem) parameters.get(i);
                    double[] threshold = {item.getThreshold()};
                    JTextField out = new JTextField(Double.toString(item.getValue()));
                    out.setColumns(5);
                    GridBagConstraints cout = new GridBagConstraints();

                    double max = item.getMaxValue();
                    JSlider slider = new JSlider(item.getMinValue(), item.getMaxValue());
                    slider.setValue(item.getValue());
                    slider.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            JSlider source = (JSlider) e.getSource();
                            int val = source.getValue();
                            item.setValue(val);
                            executor.setMaxIterations(val);
                            out.setText(Integer.toString(val));
                        }
                    });

                    SpinnerModel model = new SpinnerNumberModel(max * threshold[0], threshold[0] / 10.0, 1000 * max * threshold[0], threshold[0] / 10.0);
                    JSpinner spinner = new JSpinner(model);
                    JComponent editor = new JSpinner.NumberEditor(spinner, "#,##0.###");
                    spinner.setEditor(editor);
                    ((JSpinner.DefaultEditor) (spinner.getEditor())).getTextField().setColumns(5);
                    spinner.addChangeListener(new ChangeListener() {
                        final JSlider s = slider;

                        public void stateChanged(ChangeEvent e) {
                            JSpinner source = (JSpinner) e.getSource();
                            s.setMaximum((int) source.getValue());
                        }
                    });

                    slider.setSize(400, 30);
                    cLabel.gridx = 1;
                    cLabel.gridy = ++cCustomPanelY;

                    cout.gridx = 0;
                    cout.gridy = ++cCustomPanelY;
                    cout.fill = GridBagConstraints.HORIZONTAL;
                    cSlider.gridx = 1;
                    cSlider.gridy = cCustomPanelY;
//                    cSlider.gridwidth = 7;
                    cSlider.fill = GridBagConstraints.HORIZONTAL;
                    cSliderMax.gridy = cCustomPanelY;
                    cSliderMax.gridx = 2;
//                    cSlider.gridwidth =  1;
//                    cSliderMax.weightx = 0.2;
                    cSliderMax.fill = GridBagConstraints.HORIZONTAL;
                    custom.add(new JLabel(parameters.get(i).getName()), cLabel);
                    custom.add(out, cout);
                    custom.add(slider, cSlider);
                    custom.add(spinner, cSliderMax);

                }
            }


            cSidePanel.fill = GridBagConstraints.BOTH;
            cSidePanel.gridx = 0;
            cSidePanel.gridy = 0;
//        cSidePanel.weightx = 1;
            cSidePanel.weighty = 1;
            cSidePanel.anchor = GridBagConstraints.FIRST_LINE_START;
            sidePanel.add(custom, cSidePanel);


            //default controls (start, pause, manual mode, min angle, output, etc)
            cSidePanel.fill = GridBagConstraints.HORIZONTAL;
            cSidePanel.gridx = 0;
            cSidePanel.gridy = 1;
            cSidePanel.weightx = 0;
            cSidePanel.weighty = 0;
            sidePanel.add(getDefaultPanel(executor), cSidePanel);

            tabbedSidePane.addTab(algorithmName, sidePanel);
        } else {
            //empty custom with only lean default controls
            cSidePanel.fill = GridBagConstraints.BOTH;
            cSidePanel.gridx = 0;
            cSidePanel.gridy = 0;
//        cSidePanel.weightx = 1;
            cSidePanel.weighty = 1;
            cSidePanel.anchor = GridBagConstraints.FIRST_LINE_START;
            sidePanel.add(custom, cSidePanel);

            //default controls (manual mode, min angle, output, etc)
            cSidePanel.fill = GridBagConstraints.HORIZONTAL;
            cSidePanel.gridx = 0;
            cSidePanel.gridy = 1;
            cSidePanel.weightx = 0;
            cSidePanel.weighty = 0;
            sidePanel.add(getLeanDefaultPanel(), cSidePanel);
            tabbedSidePane.addTab(algorithmName, sidePanel);
        }
    }

    /**
     * Adds all algorithms that do not need a separate tab
     */
    private void addMiscAlgorithms() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new GridBagLayout());
        GridBagConstraints cSidePanel = new GridBagConstraints();

        //misc algorithms
        JPanel custom = new JPanel();
        custom.setLayout(new GridBagLayout());
        GridBagConstraints cCustomPanel = new GridBagConstraints();
        cCustomPanel.gridx = 0;
//        cCustomPanel.gridy = 1;
//        cCustomPanel.anchor = GridBagConstraints.PAGE_START;
//        cCustomPanel.insets= new Insets(5,5,5,5);

        int cCustomPanelY = 1;


        JButton orthogonalItem = new JButton("Orthogonal Layout");
//        JButton orthogonalItem = new JButton("<html>Orthogonal <br />Layout</html>");
        cCustomPanel.fill = GridBagConstraints.HORIZONTAL;
        cCustomPanel.gridx = 0;
        cCustomPanel.gridy = cCustomPanelY;
        orthogonalItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                mainFrame.menuBar.orthogonalItemActionPerformed(actionEvent);
            }
        });
        custom.add(orthogonalItem, cCustomPanel);

        JButton circularItem = new JButton("Circular Layout");
//        JButton circularItem = new JButton("<html>Circular <br />Layout</html>");
        cCustomPanel.fill = GridBagConstraints.HORIZONTAL;
        cCustomPanel.gridx = 1;
        cCustomPanel.gridy = cCustomPanelY;
        circularItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                mainFrame.menuBar.circularItemActionPerformed(actionEvent);
            }
        });
        custom.add(circularItem, cCustomPanel);

        JButton treeItem = new JButton("Tree Layout");
//        JButton treeItem = new JButton("<html>Tree <br />Layout</html>");
        cCustomPanel.fill = GridBagConstraints.HORIZONTAL;
        cCustomPanel.gridx = 0;
        cCustomPanel.gridy = ++cCustomPanelY;
        treeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                mainFrame.menuBar.treeItemActionPerformed(actionEvent);
            }
        });
        custom.add(treeItem, cCustomPanel);

        JButton organicItem = new JButton("Organic Layout");
//        JButton organicItem = new JButton("<html>Organic <br />Layout</html>");
        cCustomPanel.fill = GridBagConstraints.HORIZONTAL;
        cCustomPanel.gridx = 1;
        cCustomPanel.gridy = cCustomPanelY;
        organicItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                mainFrame.menuBar.organicItemActionPerformed(actionEvent);
            }
        });
        custom.add(organicItem, cCustomPanel);


        JButton yFilesSpringEmbedderItem = new JButton("yFiles Spring Embedder");
//        JButton yFilesSpringEmbedderItem = new JButton("<html>yFiles Spring <br />Embedder</html>");
        cCustomPanel.fill = GridBagConstraints.HORIZONTAL;
        cCustomPanel.gridx = 0;
        cCustomPanel.gridy = ++cCustomPanelY;
        yFilesSpringEmbedderItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                mainFrame.menuBar.yFilesSpringEmbedderItemActionPerformed(actionEvent);
            }
        });
        custom.add(yFilesSpringEmbedderItem, cCustomPanel);



        cSidePanel.fill = GridBagConstraints.HORIZONTAL;
        cSidePanel.gridx = 0;
        cSidePanel.gridy = 0;
//        cSidePanel.weightx = 1;
        cSidePanel.weighty = 1;
//        cSidePanel.anchor = GridBagConstraints.FIRST_LINE_START;
        sidePanel.add(custom, cSidePanel);

        //default controls (manual mode, min angle, output, etc)
        cSidePanel.fill = GridBagConstraints.HORIZONTAL;
        cSidePanel.gridx = 0;
        cSidePanel.gridy = 1;
        cSidePanel.weightx = 0;
        cSidePanel.weighty = 0;
        sidePanel.add(getLeanDefaultPanel(), cSidePanel);

        tabbedSidePane.addTab("Misc.", sidePanel);
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
        int cDefaultPanelY = 0; //current y pos, preincrement: myPanel.gridy = ++cDefaultPanelY;
//        defaultPanel.add(new JLabel("my second pane "));


        JButton showBestSolution = new JButton("Show best");    //TODO: fix best solution
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        showBestSolution.addActionListener(e -> {
            if (TrashCan.bestSolution == null) {
                return;
            }

            Mapper<INode, PointD> nodePositions = TrashCan.bestSolution.a;
            Optional<Double> minCrossingAngle = TrashCan.bestSolution.b;
            Double[] mods = TrashCan.bestSolution.c;
            Boolean[] switchs = TrashCan.bestSolution.d;
            PositionMap.applyToGraph(mainFrame.graph, nodePositions);
            String msg = minCrossingAngle.map(d -> "Minimum crossing angle: " + d.toString()).orElse("No crossings!");
            msg += "\n";
            msg += "Modifiers:\n";
            for(int i = 0; i < mods.length; i++){
                Double d = mods[i];
                mainFrame.sliders[i].setValue((int) (1000 * d));
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
        defaultPanel.add(showBestSolution, cDefaultPanel);

        JButton scaleToBox = new JButton("Scale me to the box");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 1;
        cDefaultPanel.gridy = cDefaultPanelY;
        defaultPanel.add(scaleToBox, cDefaultPanel);
        scaleToBox.addActionListener(e -> scalingToBox());
        scaleToBox.setSelected(false);

        JCheckBox enableMinimumAngleDisplay = new JCheckBox("Show minimum angle");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        cDefaultPanel.weightx = 0.5;
        cDefaultPanel.weighty = 0;
        defaultPanel.add(enableMinimumAngleDisplay, cDefaultPanel);
        enableMinimumAngleDisplay.addItemListener(this::minimumAngleDisplayEnabled);
        enableMinimumAngleDisplay.setSelected(true);

        JCheckBox allowClickCreateNodeEdge = new JCheckBox("Manual Mode");  //No new nodes or edges on click, can't select ports and edges, for manual tuning
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 1;
        cDefaultPanel.gridy = cDefaultPanelY;
        cDefaultPanel.weightx = 0.5;
        cDefaultPanel.weighty = 0;
        defaultPanel.add(allowClickCreateNodeEdge, cDefaultPanel);
        allowClickCreateNodeEdge.addItemListener(this::allowClickCreateNodeEdgeActionPerformed);
        allowClickCreateNodeEdge.setSelected(true);

        JTextArea output = new JTextArea("Output");
        output.setLineWrap(true);
        output.setRows(5);
        JScrollPane test = new JScrollPane(output);
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        cDefaultPanel.weightx = 1;
        cDefaultPanel.weighty = 0;
        cDefaultPanel.insets = new Insets(10,10,10,10);
        cDefaultPanel.gridwidth = 3;
        defaultPanel.add(test, cDefaultPanel);
        return defaultPanel;
    }

    /**
     * returns the default panel for all algorithms (start/pause, stop, manual mode, min angle, output)
     * @param executor - interface start/stop etc to algorithm
     * @return default panel
     */
    private JPanel getDefaultPanel(IGraphLayoutExecutor executor) {
        JPanel defaultPanel = new JPanel();
        defaultPanel.setLayout(new GridBagLayout());
        GridBagConstraints cDefaultPanel = new GridBagConstraints();
        int cDefaultPanelY = 0; //current y pos, preincrement: myPanel.gridy = ++cDefaultPanelY;
//        defaultPanel.add(new JLabel("my second pane "));

        JButton button1 = new JButton("Start");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = cDefaultPanelY;
//        cDefaultPanel.weightx = 0.5;
        cDefaultPanel.weighty = 0;
//        cDefaultPanel.insets = new Insets(0,10,0,0);
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (button1.getText().equals("Start") || button1.getText().equals("Continue")) {
                    if (executor.isRunning()) {
                        button1.setText("Pause");
                        executor.unpause();
                    } else {
                        if (executor.isFinished()) {
                            button1.setText("Start");
                        } else {
                            button1.setText("Pause");
//                        button1.setBackground(Color.green);
                            executor.start();
                        }
                    }
                } else if (button1.getText().equals("Pause")) {
                    button1.setText("Continue");
                    executor.pause();
                }
            }
        });
        defaultPanel.add(button1, cDefaultPanel);

        JButton button2 = new JButton("Stop");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 1;
        cDefaultPanel.gridy = cDefaultPanelY;
//        cDefaultPanel.weightx = 0.;
        cDefaultPanel.weighty = 0;
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                button1.setText("Start");
                executor.stop();
            }
        });
        defaultPanel.add(button2, cDefaultPanel);



        JButton showBestSolution = new JButton("Show best");    //TODO: fix best solution
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        showBestSolution.addActionListener(e -> {
            if (TrashCan.bestSolution == null) {
                return;
            }

            Mapper<INode, PointD> nodePositions = TrashCan.bestSolution.a;
            Optional<Double> minCrossingAngle = TrashCan.bestSolution.b;
            Double[] mods = TrashCan.bestSolution.c;
            Boolean[] switchs = TrashCan.bestSolution.d;
            PositionMap.applyToGraph(mainFrame.graph, nodePositions);
            String msg = minCrossingAngle.map(d -> "Minimum crossing angle: " + d.toString()).orElse("No crossings!");
            msg += "\n";
            msg += "Modifiers:\n";
            for(int i = 0; i < mods.length; i++){
                Double d = mods[i];
                mainFrame.sliders[i].setValue((int) (1000 * d));
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
        defaultPanel.add(showBestSolution, cDefaultPanel);

        JButton scaleToBox = new JButton("Scale me to the box");
        cDefaultPanel.gridx = 1;
        cDefaultPanel.gridy = cDefaultPanelY;
        defaultPanel.add(scaleToBox, cDefaultPanel);
        scaleToBox.addActionListener(e -> scalingToBox());
        scaleToBox.setSelected(false);

        JCheckBox enableMinimumAngleDisplay = new JCheckBox("Show minimum angle");
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        cDefaultPanel.weightx = 0.5;
        cDefaultPanel.weighty = 0;
        defaultPanel.add(enableMinimumAngleDisplay, cDefaultPanel);
        enableMinimumAngleDisplay.addItemListener(this::minimumAngleDisplayEnabled);
        enableMinimumAngleDisplay.setSelected(true);

        JCheckBox allowClickCreateNodeEdge = new JCheckBox("Manual Mode");  //No new nodes or edges on click, can't select ports and edges, for manual tuning
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 1;
        cDefaultPanel.gridy = cDefaultPanelY;
        cDefaultPanel.weightx = 0.5;
        cDefaultPanel.weighty = 0;
        defaultPanel.add(allowClickCreateNodeEdge, cDefaultPanel);
        allowClickCreateNodeEdge.addItemListener(this::allowClickCreateNodeEdgeActionPerformed);
        allowClickCreateNodeEdge.setSelected(true);

        JTextArea output = new JTextArea("Output");
        output.setLineWrap(true);
        output.setRows(5);
        JScrollPane test = new JScrollPane(output);
        cDefaultPanel.fill = GridBagConstraints.HORIZONTAL;
        cDefaultPanel.gridx = 0;
        cDefaultPanel.gridy = ++cDefaultPanelY;
        cDefaultPanel.weightx = 1;
        cDefaultPanel.weighty = 0;
        cDefaultPanel.insets = new Insets(10,10,10,10);
        cDefaultPanel.gridwidth = 3;
        defaultPanel.add(test, cDefaultPanel);

        return defaultPanel;
    }

    /*********************************************************************
     * Implementation of actions
     ********************************************************************/

    private void startGeneticClicked(@SuppressWarnings("unused") ActionEvent evt){
//        if(geneticAlgorithm == null || !geneticAlgorithm.running){
//            TrashCan.init();
//            initializeGeneticAlgorithm();
//            mainFrame.graphEditorInputMode.setCreateNodeAllowed(false);
//            geneticAlgorithmThread.start();
//        }
    }

    private void stopGeneticClicked(@SuppressWarnings("unused") ActionEvent evt){
//        geneticAlgorithm.running = false;
//        mainFrame.graphEditorInputMode.setCreateNodeAllowed(true);
    }

    private void startForceClicked(@SuppressWarnings("unused") ActionEvent evt){
        if(mainFrame.forceAlgorithm == null){
            TrashCan.init();
            ForceAlgorithm fd = mainFrame.defaultForceAlgorithm();
            mainFrame.forceAlgorithm = fd;
            mainFrame.graphEditorInputMode.setCreateNodeAllowed(false);
            IGraphLayoutExecutor executor =
                new IGraphLayoutExecutor(fd, mainFrame.view.getGraph(), mainFrame.progressBar, -1, 20);
            executor.start();
            mainFrame.view.updateUI();
        }
    }

    private void stopForceClicked(@SuppressWarnings("unused") ActionEvent evt){
        if (mainFrame.forceAlgorithm != null) {
            mainFrame.graphEditorInputMode.setCreateNodeAllowed(true);
        }
    }




    private void minimumAngleDisplayEnabled(ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            mainFrame.minimumAngleMonitor.registerGraphChangedListeners();
        } else if (evt.getStateChange() == ItemEvent.DESELECTED) {
            mainFrame.minimumAngleMonitor.removeGraphChangedListeners();
        }
    }

    private void scalingToBox(){
        Mapper<INode, PointD> nodePositions = PositionMap.FromIGraph(mainFrame.graph);
        double maxX=0, maxY=0;
        for(INode u : mainFrame.graph.getNodes()){
            if(u.getLayout().getCenter().getX()>maxX){
                maxX=u.getLayout().getCenter().getX();
            }
            if(u.getLayout().getCenter().getY()>maxY){
                maxY=u.getLayout().getCenter().getY();
            }
        }
        nodePositions = GraphOperations.scaleUpProcess(mainFrame.graph,nodePositions, Math.min((int)(MainFrame.BOX_SIZE/maxX), (int)(MainFrame.BOX_SIZE/maxY)));
        mainFrame.graph =  PositionMap.applyToGraph(mainFrame.graph, nodePositions);
        mainFrame.view.fitGraphBounds();
    }

    private void allowClickCreateNodeEdgeActionPerformed(ItemEvent evt) {
        mainFrame.graphEditorInputMode.setCreateNodeAllowed((evt.getStateChange() == ItemEvent.DESELECTED));     //no new nodes
        mainFrame.graphEditorInputMode.setCreateEdgeAllowed((evt.getStateChange() == ItemEvent.DESELECTED));     //no new edges
        mainFrame.graphEditorInputMode.setEditLabelAllowed((evt.getStateChange() == ItemEvent.DESELECTED));      //no editing of labels
        mainFrame.graphEditorInputMode.setShowHandleItems((evt.getStateChange() == ItemEvent.DESELECTED) ? GraphItemTypes.ALL : GraphItemTypes.NONE); //no resizing of nodes nor selection of ports
        mainFrame.graphEditorInputMode.setDeletableItems((evt.getStateChange() == ItemEvent.DESELECTED) ? GraphItemTypes.ALL : GraphItemTypes.NONE);  //no deleting of nodes
        mainFrame.graphEditorInputMode.setSelectableItems((evt.getStateChange() == ItemEvent.DESELECTED) ? GraphItemTypes.ALL : GraphItemTypes.NODE); //no selecting of edges (only nodes)
    }

    private void forceDirectionPerpendicularActionPerformed(@SuppressWarnings("unused") ActionEvent evt){    /*mainFrame.algoModifiers[0] = true;*/ }
    private void forceDirectionNonPerpendicularActionPerformed(@SuppressWarnings("unused") ActionEvent evt){ /*mainFrame.algoModifiers[0] = false;*/ }

    private void optimizingAngleNintyActionPerformed(@SuppressWarnings("unused") ActionEvent evt) { /*mainFrame.algoModifiers[1] = true;*/ }
    private void optimizingAngleSixtyActionPerformed(@SuppressWarnings("unused") ActionEvent evt) { /*mainFrame.algoModifiers[1] = false;*/ }


    private GeneticAlgorithm<ForceAlgorithm> geneticAlgorithm;
    private Thread geneticAlgorithmThread;
}
