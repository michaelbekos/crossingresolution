package Experiment;

import com.yworks.yfiles.algorithms.GraphChecker;
import com.yworks.yfiles.graph.*;

/**
 * Created by Ama on 24.04.2018.
 */

import java.util.Optional;

import com.yworks.yfiles.layout.YGraphAdapter;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.view.GraphComponent;
import layout.algo.execution.ILayout;
import layout.algo.forcealgorithm.ForceAlgorithm;
import layout.algo.genetic.GeneticForceAlgorithmLayout;
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
    private int boxSize = 10000;
    private boolean planarGraphsAllowed = false;
    private boolean unconnectedGraphsAllowed = true;
    private static boolean isFrameFinished = false;
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

    public Experiments(String inputDirectory, String outputDirectory, long maxCalcTime, int numOfIterationPerStep, int numOfSteps, int boxSize){
        this.comp = new GraphComponent();
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
        this.maxCalcTime = maxCalcTime;
        this.numOfIteration = numOfIterationPerStep;
        this.numOfIterationFactor = numOfSteps;
        this.boxSize = boxSize;
    }

    public Experiments(String inputDirectory, String outputDirectory, long maxCalcTime, int numOfIterationPerStep, int numOfSteps, int boxSize, boolean planarGraphsAllowed, boolean unconnectedGraphsAllowed){
        this.comp = new GraphComponent();
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
        this.maxCalcTime = maxCalcTime;
        this.numOfIteration = numOfIterationPerStep;
        this.numOfIterationFactor = numOfSteps;
        this.boxSize = boxSize;
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
                       // openFrames(i, i, inputDirectory, children[i]);
                        //openFrame(children[i]);
                        openFrame(children[i]);
                        System.out.println("Iteration:   "+ i);
                     /*   while(!this.isFrameFinished){
                            System.out.println("Graph dsfadsfasdf");

                        }
                        this.isFrameFinished = false;
                   */
                    }
            }
                System.out.println("ENDE");
    }
    public synchronized void runOnlyForce()
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
            for(int i = 0; i<children.length; i++){
                openFrame(children[i], ForceAlgorithm.class);
                System.out.println("Iteration:   "+ i);
            }
        }
        System.out.println("ENDE");
    }
    public synchronized void runOnlyRandom()
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
            for(int i = 0; i<children.length; i++){
                openFrame(children[i], RandomMovementLayout.class);
                System.out.println("Iteration:   "+ i);
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

    private void openFrame(String pattern, Class<? extends ILayout> algorithmLayout) {
        MainFrame frame = new MainFrame();
        frame.init();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);


        frame.BOX_SIZE = this.boxSize;
        //String fileName = index +"";
        String fileName = pattern;
        System.out.println("Graph " + fileName + " started.");
        Experiment experiment = new Experiment(frame, fileName, inputDirectory, outputDirectory, this.boxSize);
        experiment.loadGraph();
        experiment.setAlgorithm(algorithmLayout);
        boolean isNotFinished = true;
        int iterationFactor = 1;
        boolean reached90Deg = false;
        experiment.setStartTime(System.currentTimeMillis());
        experiment.setEndTime(System.currentTimeMillis());
        experiment.setMaxIterations(this.numOfIteration);

        YGraphAdapter graphAdapter = new YGraphAdapter(frame.graph);


        if(GraphChecker.isPlanar(graphAdapter.getYGraph()) && !planarGraphsAllowed){
            //experiment.writeGraphInformationsWarning("is planar");
            experiment.deleteGraphInformationFile();
            System.out.println("Graph " + fileName + " is planar.");
        }else if (!GraphChecker.isConnected(graphAdapter.getYGraph()) && !unconnectedGraphsAllowed){
            //experiment.writeGraphInformationsWarning("is not Connected");
            experiment.deleteGraphInformationFile();
            System.out.println("Graph " + fileName + " is not connected.");
        }else{
            while(experiment.getCalcTime() < maxCalcTime  && iterationFactor <= numOfIterationFactor && !reached90Deg && (experiment.getNumOfUnchangedAngle() <= 100 )){
                experiment.runAlgorithms();

                if(experiment.getIsInInvLoop()){ // Restart the calc. for this graph.
                    experiment.loadGraph();
                    isNotFinished = true;
                    iterationFactor = 1;
                    reached90Deg = false;
                    experiment.setStartTime(System.currentTimeMillis());
                    experiment.setEndTime(System.currentTimeMillis());
                }

                //reached90Deg = saveGraphInformations(mainFrame, pattern, suffix, endTime-startTime);
                experiment.writeGraphInformations();
                reached90Deg = experiment.calcGraphInformations();
                //experiment.writeGraph(suffix);
                iterationFactor++;
            }
            experiment.writeGraphEndResults();
            System.out.println("Graph " + fileName + " finished.");
            experiment.writeGraph();

        }
        frame.dispose();
        //  this.isFrameFinished = true;


    }

    private void openFrame(String pattern) {

        MainFrame frame = new MainFrame();
        frame.init();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);


        frame.BOX_SIZE = this.boxSize;
        //String fileName = index +"";
        String fileName = pattern;
        System.out.println("Graph " + fileName + " started.");
        //  System.out.println("1");
        Experiment experiment = new Experiment(frame, fileName, inputDirectory, outputDirectory, this.boxSize);
        experiment.loadGraph();
        boolean isNotFinished = true;
        int iterationFactor = 1;
        boolean reached90Deg = false;
        experiment.setStartTime(System.currentTimeMillis());
        experiment.setEndTime(System.currentTimeMillis());
        experiment.setMaxIterations(this.numOfIteration);

        YGraphAdapter graphAdapter = new YGraphAdapter(frame.graph);


        if(GraphChecker.isPlanar(graphAdapter.getYGraph()) && !planarGraphsAllowed){
            //experiment.writeGraphInformationsWarning("is planar");
            experiment.deleteGraphInformationFile();
            System.out.println("Graph " + fileName + " is planar.");
        }else if (!GraphChecker.isConnected(graphAdapter.getYGraph()) && !unconnectedGraphsAllowed){
            //experiment.writeGraphInformationsWarning("is not Connected");
            experiment.deleteGraphInformationFile();
            System.out.println("Graph " + fileName + " is not connected.");
        }else{
            while(experiment.getCalcTime() < maxCalcTime  && iterationFactor <= numOfIterationFactor && !reached90Deg && (experiment.getNumOfUnchangedAngle() <= 100 )){
                experiment.runAlgorithms();

                if(experiment.getIsInInvLoop()){ // Restart the calc. for this graph.
                    experiment.loadGraph();
                    isNotFinished = true;
                    iterationFactor = 1;
                    reached90Deg = false;
                    experiment.setStartTime(System.currentTimeMillis());
                    experiment.setEndTime(System.currentTimeMillis());
                }

                experiment.writeGraphInformations();
                reached90Deg = experiment.calcGraphInformations();
                iterationFactor++;
            }
            experiment.writeGraphEndResults();
            System.out.println("Graph " + fileName + " finished.");
            experiment.writeGraph();

        }
        frame.dispose();
      //  this.isFrameFinished = true;


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



