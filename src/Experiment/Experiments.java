package Experiment;

import com.yworks.yfiles.algorithms.GraphChecker;
import com.yworks.yfiles.graph.*;

/**
 * Created by Ama on 24.04.2018.
 */

import java.io.PrintWriter;
import java.util.Optional;
import java.util.stream.IntStream;

import com.yworks.yfiles.layout.YGraphAdapter;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.view.GraphComponent;
import layout.algo.randommovement.RandomMovementLayout;
import main.MainFrame;
import sidepanel.SidePanelTab;

import javax.swing.*;

public class Experiments {

    //private y.view.Graph2DView view;
     private GraphComponent comp;
    //private static String inputDirectory  = "E:\\graph\\origin\\";

//    private static String inputDirectory  = "E:\\graph\\north\\north_graphml\\north\\";
  //  private static String outputDirectory = "E:\\graph\\afterRandom\\north\\";

    private String inputDirectory  = "E:\\graph\\graphml\\";
    private String outputDirectory = "E:\\graph\\afterRandom\\graphml2\\";


    /* Config. for the type of experiment */
    private int numThread = 6;
    private int numOfIteration = 100;
    private int numOfIterationFactor = 1000;
    private long maxCalcTime = 6000; //in mili. sec
    private boolean planarGraphsAllowed = false;
    private boolean unconnectedGraphsAllowed = true;
    private String[] childernP;


    public Experiments()
    {
        this.comp = new GraphComponent();
    }

    public Experiments(String inputDirectory, String outputDirectory) {

        this.comp = new GraphComponent();
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
    }

    public Experiments(String inputDirectory, String outputDirectory, long maxCalcTime, int numOfIterationPerStep, int numOfSteps){
        this.comp = new GraphComponent();
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
        this.maxCalcTime = maxCalcTime;
        this.numOfIteration = numOfIterationPerStep;
        this.numOfIterationFactor = numOfSteps;
    }

    public Experiments(String inputDirectory, String outputDirectory, long maxCalcTime, int numOfIterationPerStep, int numOfSteps, boolean planarGraphsAllowed, boolean unconnectedGraphsAllowed){
        this.comp = new GraphComponent();
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
        this.maxCalcTime = maxCalcTime;
        this.numOfIteration = numOfIterationPerStep;
        this.numOfIterationFactor = numOfSteps;
        this.planarGraphsAllowed = planarGraphsAllowed;
        this.unconnectedGraphsAllowed = unconnectedGraphsAllowed;
    }

    public synchronized void run()
    {
            java.io.File dir = new java.io.File(inputDirectory);
            // It is also possible to filter the list of returned files.
            java.io.FilenameFilter filter = new java.io.FilenameFilter() {
                public boolean accept(java.io.File file, String name)
                {
                    return true;
                }
            };
            String[] children = dir.list(filter);
            System.out.println(children.length);

            if (children == null) {
                System.out.println("Children==null");
                // Either dir does not exist or is not a directory
            }
            else {
                    childernP = children;
                    //for(int i = 0; i<1; i++){
                    for(int i = 0; i<children.length; i++){
                    //    openFrames(i, children.length, inputDirectory, "pattern");
                        openFrames(i, i, inputDirectory, children[i]);
                        //System.out.println("Iteration:   "+ i);
                    }
            }
                System.out.println("ENDE");
    }


    private static void runAlgorithms(MainFrame mainFrame) {
        mainFrame.initSidePanel.removeDefaultListeners();
        LayoutUtilities.applyLayout(mainFrame.graph, new OrganicLayout());
        mainFrame.initSidePanel.addDefaultListeners();
        Optional<SidePanelTab> tab = mainFrame.getTabForAlgorithm(RandomMovementLayout.class);

        if (!tab.isPresent()) {
            return;
        }
        tab.get().startPauseExecution();
    }

    private void openFrames(int startIndex, int endIndex, String folderPath, String pattern) {
        IntStream.range(startIndex, endIndex + 1)
                .parallel()
                .forEach(index -> MainFrame.start(WindowConstants.DISPOSE_ON_CLOSE, false, mainFrame -> {

                    //String fileName = index +"";
                    String fileName = pattern;
                    System.out.println("Graph " + fileName + " started.");
                    //  System.out.println("1");
                    Experiment experiment = new Experiment(mainFrame, fileName, inputDirectory, outputDirectory);
                    experiment.loadGraph();
                    boolean isNotFinished = true;
                    int iterationFactor = 1;
                    boolean reached90Deg = false;
                    experiment.setStartTime(System.currentTimeMillis());
                    experiment.setEndTime(System.currentTimeMillis());
                    experiment.setMaxIterations(this.numOfIteration);

                    YGraphAdapter graphAdapter = new YGraphAdapter(mainFrame.graph);


                    if(GraphChecker.isPlanar(graphAdapter.getYGraph()) && !planarGraphsAllowed){
                        experiment.writeGraphInformationsWarning("is planar");
                        System.out.println("Graph " + fileName + " is planar.");
                    }else if (!GraphChecker.isConnected(graphAdapter.getYGraph()) && !unconnectedGraphsAllowed){
                        experiment.writeGraphInformationsWarning("is not Connected");
                        System.out.println("Graph " + fileName + " is not connected.");
                    }else{
                        while(experiment.getCalcTime() < maxCalcTime  && iterationFactor <= numOfIterationFactor && !reached90Deg){
                            // System.out.println("2");
                            experiment.runAlgorithms();
                            // System.out.println("3");
                            //reached90Deg = saveGraphInformations(mainFrame, pattern, suffix, endTime-startTime);
                            experiment.writeGraphInformations();
                            // System.out.println("4");
                            reached90Deg = experiment.calcGraphInformations();
                            //experiment.writeGraph(suffix);
                            // System.out.println("5");
                            System.out.println("Iteration " + (numOfIteration * iterationFactor)+ "    Time: " + experiment.getCalcTime());
                            //     System.out.println("6");
                            iterationFactor++;
                    }
                        //  System.out.println("7");
                    System.out.println("Graph " + fileName + " finished.");
                    experiment.writeGraph();
                    mainFrame.dispose();
                }
    }));
    }

    private String getPrefixString(String str){
        int position = str.indexOf(".");
        //System.out.println(str.substring(0, position));
        return str.substring(0, position);
    }

    private static void pause(int waitTime){
        long Time0 = System.currentTimeMillis();
        long Time1;
        long runTime = 0;
        while(runTime<waitTime){
            Time1 = System.currentTimeMillis();
            runTime = Time1 - Time0;
        }
    }
}



