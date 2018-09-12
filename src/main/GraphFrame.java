package main;

import algorithms.graphs.GridGraph;
import com.yworks.yfiles.graph.LayoutUtilities;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import layout.algo.randommovement.RandomMovementLayout;
import sidepanel.SidePanelTab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

public class GraphFrame {
    public int id;
    public JPanel panel;
    public MainFrame mainFrame;
    public double minAngle;
    public String fileName;
    public String folderPath;
    public String savePath;
    private boolean automatic;
    private JButton startPauseButton;
    private JButton stopButton;
    private JLabel minAngleLabel;
    private JLabel timeLabel;
    private JCheckBox allowDecreasing;
    private double runningTime;
    private long prevTime;
    private long currTime;


    public GraphFrame(int index, String pattern, String folderPath, boolean automatic) {
        this.id = index;
        this.automatic = automatic;
        this.folderPath = folderPath;
        this.savePath = folderPath + "/saved/";
        File directory = new File(savePath);
        if (! directory.exists()){
            directory.mkdir();
        }
        this.fileName = pattern.replace("$$$", Integer.toString(id));
        this.runningTime = 0;
        this.prevTime = System.nanoTime();
        initMainFrame();
        initFrame();
        updateButtons(getSidePanel());

        mainFrame.minimumAngleMonitor.addPropertyChangeListener(this::angleChangedPropertyChanged);
    }

    private void initMainFrame() {
        mainFrame = new MainFrame();
        mainFrame.init();
        mainFrame.setVisible(false);
        mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        loadGraph(mainFrame, folderPath, fileName);
        runAlgorithms(mainFrame, automatic);
    }

    private void initFrame() {
        panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints cFramePanel = new GridBagConstraints();

        JLabel frameName = new JLabel(fileName);
        cFramePanel.gridx = 0;
        cFramePanel.gridy = 0;
        cFramePanel.insets = new Insets(5,5,5,5);
        cFramePanel.fill = GridBagConstraints.VERTICAL; //for vert separators
        panel.add(frameName, cFramePanel);
        cFramePanel.gridx += 1;
        panel.add(createSeparator(), cFramePanel);



        minAngleLabel = new JLabel(String.format("%.5f",mainFrame.minimumAngleMonitor.getCurrentCrossingResolution()));
//        minAngleLabel = new JLabel("-1");
        cFramePanel.gridx += 1;
        panel.add(minAngleLabel, cFramePanel);
        cFramePanel.gridx += 1;
        panel.add(createSeparator(), cFramePanel);


        timeLabel = new JLabel("-1");
        cFramePanel.gridx += 1;
        panel.add(timeLabel, cFramePanel);
        cFramePanel.gridx += 1;
        panel.add(createSeparator(), cFramePanel);

        allowDecreasing = new JCheckBox("Allow Dec.");
        cFramePanel.gridx += 1;
        panel.add(allowDecreasing, cFramePanel);
        allowDecreasing.addItemListener(this::allowDecreasingEnabled);
        allowDecreasing.setSelected(false);
        cFramePanel.gridx += 1;
        panel.add(createSeparator(), cFramePanel);

        JButton showBest = new JButton("Best");
        cFramePanel.gridx += 1;
        panel.add(showBest, cFramePanel);
        showBest.addActionListener(this::showBestActionPerformed);
        cFramePanel.gridx += 1;
        panel.add(createSeparator(), cFramePanel);

        startPauseButton = new JButton("Start");
        cFramePanel.gridx += 1;
        startPauseButton.addActionListener(this::startPauseActionPerformed);
        panel.add(startPauseButton, cFramePanel);

        stopButton = new JButton("Stop");
        cFramePanel.gridx += 1;
        stopButton.addActionListener(this::stopActionPerformed);
        panel.add(stopButton, cFramePanel);

        JButton saveButton = new JButton("Save");
        cFramePanel.gridx += 1;
        saveButton.addActionListener(this::saveGraphActionPerformed);
        panel.add(saveButton, cFramePanel);
    }

    public  static JComponent createSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(5,1));
        return sep;
    }


    private static void loadGraph(MainFrame mainFrame, String folderPath, String fileName) {
//        String fileName = pattern.replace("$$$", Integer.toString(index));
//        mainFrame.openContestFile(Paths.get(folderPath, fileName).toString());
        mainFrame.openContest2018File(Paths.get(folderPath, fileName).toString());
        System.out.println("Loaded " + folderPath +  "  " +fileName);
    }

    private void saveGraphActionPerformed(ActionEvent evt) {
//    private void saveGraph(String fileNamePath) {
        saveGraph();
    }
    public void saveGraph() {
        showBest();
        String fileNamePath = savePath+Integer.toString(id)+".json";

        if (fileNamePath != null) {
            try {
                mainFrame.contest2018IOHandler.write(mainFrame.graph, fileNamePath, mainFrame.initSidePanel.getOutputTextArea());
                System.out.println("Saved "+fileNamePath);
            } catch (IOException ioe) {
                System.err.println("Error writing to file " + fileNamePath + " Exception: " + ioe);
            }
        } else {
            System.err.println("Error writing to null file.");
        }
    }

    private static void runAlgorithms(MainFrame mainFrame, boolean automatic) {
        mainFrame.initSidePanel.removeDefaultListeners();
        LayoutUtilities.applyLayout(mainFrame.graph, new OrganicLayout());
        mainFrame.initSidePanel.addDefaultListeners();

        System.out.println("Gridded " + GridGraph.isGridGraph(mainFrame.graph)); //TODO: automatically grid?

        Optional<SidePanelTab> tab = mainFrame.getTabForAlgorithm(RandomMovementLayout.class);
        if (!tab.isPresent()) {
            return;
        }

        tab.get().scaleToBox();
        if (automatic) {
            tab.get().startPauseExecution();
        }
    }

    private void angleChangedPropertyChanged(PropertyChangeEvent evt) {
        if ("updateAngle".equals(evt.getPropertyName())) {
            updateAngle();
        }
    }


    private void startPauseActionPerformed(ActionEvent evt) {
        Optional<SidePanelTab> tab = mainFrame.getTabForAlgorithm(RandomMovementLayout.class);
        if (!tab.isPresent()) {
            return;
        }
        tab.get().startPauseExecution();
        updateButtons(tab.get());
        prevTime = System.nanoTime();
    }

    private void stopActionPerformed(ActionEvent evt) {
        stopAlgo();
    }

    public void stopAlgo() {
        SidePanelTab tab = getSidePanel();
        tab.stopExecution();
        updateButtons(tab);
        automatic = false;
    }

    private void updateButtons(SidePanelTab tab) {
        startPauseButton.setText(tab.startPauseButton.getText());
        startPauseButton.setBackground(tab.startPauseButton.getBackground());
        stopButton.setText(tab.stopButton.getText());
        stopButton.setBackground(tab.stopButton.getBackground());
    }

    private void showBestActionPerformed(ActionEvent evt) {
        showBest();
    }

    public void showBest() {
        getSidePanel().showBest();
    }

    public SidePanelTab getSidePanel() {
        Optional<SidePanelTab> tab = mainFrame.getTabForAlgorithm(RandomMovementLayout.class);
        return tab.orElse(null);
    }

    public double getRunningTimeS() {//in seconds
        if (getSidePanel().getExecutor().isRunning()) {
            currTime = System.nanoTime();
            runningTime += (currTime - prevTime) / 1000000000.0;
            prevTime = currTime;
        }
        return runningTime;
    }

    public void updateTime() {
        timeLabel.setText(String.format("%.2f",(getRunningTimeS())));
    }

    public void updateAngle() {
        //            this.minAngle = mainFrame.minimumAngleMonitor.getBestCrossingResolution();
        this.minAngle = mainFrame.minimumAngleMonitor.getCurrentCrossingResolution();
        minAngleLabel.setText(String.format("%.5f",this.minAngle));

        //termination conditions
        if (!automatic) {
            return;
        }
        if (this.minAngle >= 89.9) {
            stopAlgo();
            saveGraph();
            automatic = false;
        } else if (this.minAngle >= 89 && getRunningTimeS() > 15) {
            stopAlgo();
            saveGraph();
            automatic = false;
        } else if (this.minAngle >= 85 && getRunningTimeS() > 30) {
            stopAlgo();
            saveGraph();
            automatic = false;
        } else if (this.minAngle >= 80 && getRunningTimeS() > 60) {
            stopAlgo();
            saveGraph();
            automatic = false;
        } else if (this.minAngle < 60 && getRunningTimeS() > 15) { //experimental
            setAllowDecreasing(true);
        } else if (this.minAngle > 60 && getRunningTimeS() > 15) {
            setAllowDecreasing(false);
        }
        //other scenarios?
    }

    private void allowDecreasingEnabled(ItemEvent evt) {
        setAllowDecreasing(evt.getStateChange() == ItemEvent.SELECTED);
    }

    private void setAllowDecreasing(boolean value) {
        getSidePanel().configurator.getItems().get(5).setValue(value); //better with map
        allowDecreasing.setSelected(value);
    }



}
