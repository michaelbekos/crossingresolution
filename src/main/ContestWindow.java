package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static main.GraphFrame.createSeparator;

public class ContestWindow {
    private static volatile int openFrames;
    private JFrame contestWindow;
    private GridBagConstraints cFrame;

    private ArrayList<GraphFrame> graphFrames;

    private Timer timer;
    private final static int INTERVAL = 10;


    public static void main(String[] args) {

        ContestWindow contestWindow = new ContestWindow();
        if (args.length == 0) {
            runSingleFrame(); //TODO test
            return;
        }

        if (args.length > 4
                || args.length == 1 && args[0].equals("--help") || args[0].equals("-h")) {
            printUsage();
            return;
        }

        int startIndex = 1;
        int endIndex = startIndex;
        String folderPath = "contest-2018";
        String pattern = "$$$.json";

        if (args.length == 1) {
            folderPath = args[0];
            try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
                List<Integer> list = paths
                        .filter(Files::isRegularFile)
                        .map(path -> Integer.parseInt(path.getFileName().toString().replaceFirst("[.][^.]+$", "")))
                        .sorted()
                        .collect(Collectors.toList());
                startIndex = list.get(0);
                endIndex = list.get(list.size() - 1);

                System.out.println(startIndex +  " " + endIndex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            startIndex = Integer.parseInt(args[0]);
            if (args.length >= 2) {
                endIndex = Integer.parseInt(args[1]);
            } else {
                endIndex = startIndex;
            }
        } catch (NumberFormatException e) {
            System.out.println("Could not parse indices");
            printUsage();
        }

        if (args.length >= 3) {
            folderPath = args[2];
            Path path = Paths.get(folderPath);
            if (Files.notExists(path) || !Files.isDirectory(path)) {
                System.out.println("Could not open directory " + folderPath);
                printUsage();
            }
        }

        if (args.length == 4) {
            pattern = args[3];
            if (!pattern.contains("$$$")) {
                System.out.println("Pattern must contain three $$$!");
                printUsage();
            }
        }

        openFrames(contestWindow, startIndex, endIndex, folderPath, pattern);
    }

    private static void runSingleFrame() {
        MainFrame.start(WindowConstants.EXIT_ON_CLOSE, true, null);
    }

    private static void openFrames(ContestWindow contestWindow, int startIndex, int endIndex, String folderPath, String pattern) {
        boolean automatic = true;
        for (int i = startIndex; i < endIndex + 1; i++) {
            if ((i - startIndex) >= 8) { //only first 8 start automatically
                automatic = false;
            }

            GraphFrame graphFrame = new GraphFrame(i, pattern, folderPath, automatic);
            contestWindow.addFrameToContestWindow(graphFrame);

        }
        contestWindow.addStopAllButton();
        contestWindow.addSaveAllButton();
    }

    private static void addClosingListener(MainFrame mainFrame) {
        synchronized (Main.class) {
            openFrames++;
        }
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                synchronized (Main.class) {
                    openFrames--;
                }
                if (openFrames == 0) {
                    System.exit(0);
                }
            }
        });
    }



    public ContestWindow() {
        this.contestWindow = new JFrame("Contest Window");
        this.contestWindow.setLayout(new GridBagLayout());
        this.contestWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);     //TODO: window listener to save and exit others gracefully

        this.contestWindow.setMinimumSize(new Dimension(500,700));
        this.contestWindow.setVisible(true);
//        this.contestWindow.pack();

        this.cFrame = new GridBagConstraints();
        this.cFrame.gridx = 0;
        this.cFrame.gridy = 0;
        this.cFrame.fill = GridBagConstraints.HORIZONTAL;
        this.cFrame.insets = new Insets(0,0,5,0);


        //TODO align columns
        this.contestWindow.add(createLegend(), this.cFrame);

        graphFrames = new ArrayList<>();

        timer = new Timer(INTERVAL, time -> {
            for (GraphFrame frame : graphFrames) {
                    if (frame.getSidePanel().getExecutor().isRunning()) {
//                        frame.updateAngle(); //maybe use it to update angle vs firingevent
                        frame.updateTime();
                    }
                };
//                if (graphFrames.stream().noneMatch(frame -> frame.getSidePanel().getExecutor().isRunning())) {
//                 timer.stop();
//                }
        }); //TODO: WHAT happens when no graphs are running/they start/stop?
        timer.start();
    }

    private static JComponent createLegend() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints cFramePanel = new GridBagConstraints();

        JLabel frameName = new JLabel("File");
        cFramePanel.gridx = 0;
        cFramePanel.gridy = 0;
        cFramePanel.insets = new Insets(5,5,5,5);
        cFramePanel.fill = GridBagConstraints.VERTICAL; //for vert separators
        panel.add(frameName, cFramePanel);
        cFramePanel.gridx += 1;
        panel.add(createSeparator(), cFramePanel);


        JLabel minAngleLabel = new JLabel("Min. Angle");
        cFramePanel.gridx += 1;
        panel.add(minAngleLabel, cFramePanel);
        cFramePanel.gridx += 1;
        panel.add(createSeparator(), cFramePanel);

        JLabel timeElapsed = new JLabel("Time Elapsed (s)");
        cFramePanel.gridx += 1;
        panel.add(timeElapsed, cFramePanel);
        cFramePanel.gridx += 1;
        panel.add(createSeparator(), cFramePanel);

        JLabel allowDecreasing = new JLabel("Allow Decreasing");
        cFramePanel.gridx += 1;
        panel.add(allowDecreasing, cFramePanel);
        cFramePanel.gridx += 1;
        panel.add(createSeparator(), cFramePanel);

        JLabel showBest = new JLabel("Show Best");
        cFramePanel.gridx += 1;
        panel.add(showBest, cFramePanel);
        cFramePanel.gridx += 1;
        panel.add(createSeparator(), cFramePanel);

        JLabel startPauseButton = new JLabel("Start");
        cFramePanel.gridx += 1;
        panel.add(startPauseButton, cFramePanel);

        JLabel stopButton= new JLabel("Stop");
        cFramePanel.gridx += 1;
        panel.add(stopButton, cFramePanel);

        JLabel saveButton= new JLabel("Save");
        cFramePanel.gridx += 1;
        panel.add(saveButton, cFramePanel);

        return panel;
    }

public void addFrameToContestWindow(GraphFrame graphFrame) {
        this.cFrame.gridy = graphFrame.id;

        this.graphFrames.add(graphFrame);

        this.contestWindow.add(graphFrame.panel, this.cFrame);

//        this.contestWindow.pack();
        this.contestWindow.setVisible(true);
        this.contestWindow.revalidate();
        this.contestWindow.repaint();
    }

    private void addSaveAllButton() {
        JButton saveAllButton = new JButton("Save All"); //TODO automate
        cFrame.gridx = 0;
        cFrame.gridy += 1;
        saveAllButton.addActionListener(this::saveAllActionPerformed);
        contestWindow.add(saveAllButton, cFrame);
    }

    private void saveAllActionPerformed(ActionEvent evt) {
        for (GraphFrame frame : graphFrames) {
            frame.saveGraph();
        }
    }

    private void addStopAllButton() {
        JButton stopAllButton = new JButton("Stop All");
        cFrame.gridx = 0;
        cFrame.gridy += 1;
        stopAllButton.addActionListener(this::stopAllActionPerformed);
        contestWindow.add(stopAllButton, cFrame);
    }

    private void stopAllActionPerformed(ActionEvent evt) {
        for (GraphFrame frame : graphFrames) {
            frame.stopAlgo();
        }
    }


    private static void printUsage() {
        System.out.println("Usage: run [startIndex] [endIndex] [folderPath] [pattern]\n"
                + "\tstartIndex, endIndex    Indices of the first and last file to open (inclusive)\n"
                + "\tfolderPath              Relative or absolute Path to the folder that contain the graph files\n"
                + "\tpattern                 A pattern for the graph file names. Uses this syntax: graph-$$$.txt, where\n"
                + "\t                        the $$$ will be replaced by the index of the graph\n");
    }

}
