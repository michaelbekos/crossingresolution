package Experiment;

import algorithms.graphs.GridGraph;
import com.yworks.yfiles.algorithms.GraphChecker;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.*;

/**
 * Created by Ama on 24.04.2018.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.yworks.yfiles.layout.YGraphAdapter;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.view.GraphComponent;
import io.ContestIOHandler;
import layout.algo.randommovement.RandomMovementLayout;
import layout.algo.utils.PositionMap;
import main.MainFrame;
import sidepanel.SidePanelTab;
import util.BoundingBox;

import javax.swing.*;

public class Experiments {

    //private y.view.Graph2DView view;
     private GraphComponent comp;
    //private static String inputDirectory  = "E:\\graph\\origin\\";

//    private static String inputDirectory  = "E:\\graph\\north\\north_graphml\\north\\";
  //  private static String outputDirectory = "E:\\graph\\afterRandom\\north\\";

   private static String inputDirectory  = "E:\\graph\\graphml\\";
    private static String outputDirectory = "E:\\graph\\afterRandom\\graphml\\";




    /* Config. for the type of experiment */
    private final boolean time = false;
    private final boolean iterations = false;

    private PrintWriter pw;
    private StringBuilder sb;
    private int numThread = 6;
    private int numOfIteration = 100;
    private int numOfIterationFactor = 1000;
    private long maxCalcTime = 6000; //in mili. sec
    private boolean withPlanarGraphs = true;
    private boolean withConnectedGraphs = true;
    private String[] childernP;


    public Experiments()
    {
        this.comp = new GraphComponent();
    }

    public synchronized void run()
    {
            java.io.File dir = new java.io.File(inputDirectory);
         /*   System.out.println(dir.getName());


            sb = new StringBuilder();
            sb.append("Graph Name");
            sb.append(',');
            sb.append("Time");
            sb.append(',');
            sb.append("Iterations");
            sb.append(',');
            sb.append("Angle");
            sb.append('\n');
*/

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

                    Experiment experiment = new Experiment(mainFrame, fileName, inputDirectory, outputDirectory);
                    experiment.loadGraph();
                    System.out.println("Was da los    1");
                    boolean isNotFinished = true;
                    int iterationFactor = 1;
                    boolean reached90Deg = false;
                    experiment.setStartTime(System.currentTimeMillis());
                    experiment.setEndTime(System.currentTimeMillis());
                    experiment.setMaxIterations(this.numOfIteration);

                    YGraphAdapter graphAdapter = new YGraphAdapter(mainFrame.graph);

                    System.out.println("Was da los    2");

                    if(GraphChecker.isPlanar(graphAdapter.getYGraph()) && !withPlanarGraphs){
                        experiment.writeGraphInformationsWarning("is planar");
                        System.out.println("Graph " + fileName + " is planar.");
                    }else if (!GraphChecker.isConnected(graphAdapter.getYGraph()) || !withConnectedGraphs ){
                        experiment.writeGraphInformationsWarning("is not Connected");
                        System.out.println("Graph " + fileName + " is not connected.");
                    }else{
                        while(experiment.getCalcTime() < maxCalcTime  && iterationFactor <= numOfIterationFactor){
                            System.out.println("Was da los    3");
                            experiment.runAlgorithms();
                            //reached90Deg = saveGraphInformations(mainFrame, pattern, suffix, endTime-startTime);
                            System.out.println("Was da los    4");
                            experiment.writeGraphInformations();
                            reached90Deg = experiment.calcGraphInformations();
                            //experiment.writeGraph(suffix);
                            System.out.println("Iteration " + (numOfIteration * iterationFactor)+ "    Time: " + experiment.getCalcTime());
                            iterationFactor++;
                    }

                    System.out.println("Graph " + fileName + " finished.");
                    experiment.writeGraph();
                    mainFrame.dispose();


                }}));
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



