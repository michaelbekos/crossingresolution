package experiment;

import com.yworks.yfiles.algorithms.GraphChecker;
import com.yworks.yfiles.graph.*;

/**
 * Created by Ama on 24.04.2018.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.yworks.yfiles.layout.YGraphAdapter;
import com.yworks.yfiles.view.GraphComponent;
import layout.algo.execution.ILayout;
import layout.algo.forcealgorithm.ForceAlgorithm;
import layout.algo.gridding.CombinedGridder;
import layout.algo.randommovement.RandomMovementLayout;
import main.MainFrame;

import javax.swing.*;

public class Experiments {

    //private y.view.Graph2DView view;
    private GraphComponent comp;
    private String inputDirectory  = "E:/graph/inputfolder/";
    private String outputDirectory = "E:/graph/outputfolder/graphml/";
    private final String randomMovementString = "Random Movement";
    private final String forceAlgoString = "Force Algorithm";
    private final String totalResolutionString = "Total Resolution Force";
    private final String randomMovement = "randommovement_n_";
    private final String forceAlgo = "force_algo_n_";
    private final String totalResForce = "total_resolution_force_n_";

    /* Config. for the type of experiment */
    private int numOfIteration = 100;
    int iterationFactor = 1;
    private int numOfIterationFactor = 1000;
    private long maxCalcTime = Long.MAX_VALUE; //in mili. sec
    private int boxSize = 10000;
    private int maxNumberOfUnchangedAngle = 5;
    private int aspectRatio = -1;
    private boolean planarGraphsAllowed = false;
    private boolean unconnectedGraphsAllowed = true;
    boolean reached90Deg = false;
    private String[] childernP;
    private boolean useCrossingRes = true;
    private boolean useAngularRes = false;
    private boolean useAspectRatio = false;
    private boolean useOnlyGrid = true;
    private int gridSize = 1000000;




    public Experiments(String inputDirectory, String outputDirectory, long maxCalcTime, int numOfIterationPerStep, int numOfSteps, int boxSize, boolean planarGraphsAllowed,
                       boolean unconnectedGraphsAllowed, boolean useCrossingRes, boolean useAngularRes, boolean useAspectRatio){
        this.comp = new GraphComponent();
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
        this.maxCalcTime = maxCalcTime;
        this.numOfIteration = numOfIterationPerStep;
        this.numOfIterationFactor = numOfSteps;
        this.boxSize = boxSize;
        this.planarGraphsAllowed = planarGraphsAllowed;
        this.unconnectedGraphsAllowed = unconnectedGraphsAllowed;
        this.useCrossingRes = useCrossingRes;
        this.useAngularRes = useAngularRes;
        this.useAspectRatio = useAspectRatio;

    }

    /**
     * Run method with the random movement
     */
    public synchronized void runOnlyRandom()
    {
        java.io.File dir = new java.io.File(inputDirectory);
        // It is also possible to filter the list of returned files.
        java.io.FilenameFilter filter = new java.io.FilenameFilter() {
            public boolean accept(java.io.File file, String name) {return true;}
        };
        String[] children = dir.list(filter);
        System.out.println(children.length);

        if (children == null) {
            System.out.println("Children==null");
            // Either dir does not exist or is not a directory
        }
        else {
            String folderName = "randomMovement";
            fixGraphmlFormat(children, folderName);

            for(int  i = 20; i<=100; i+=20){
                if(this.useAspectRatio){
                    createFiles(this.randomMovement+"aspect_ratio_"+this.aspectRatio+"_", i-19,i);
                }else {
                    createFiles(this.randomMovement, i-19,i);
                }

            }
            childernP = children;
            for(int i = 0; i<children.length; i++){
                openFrame(children[i], this.randomMovementString);
                System.out.println("Iteration:   "+ i);
                deleteOutPutFile(children[i], folderName);  //delete the file to see which one are left. This will delete the copied files frome the fixGraphmlFormat() method
            }
        }
        System.out.println("ENDE");
    }


    /**
     * Run method for the force algorithm
     */
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
            String folderName = "force";
            fixGraphmlFormat(children, folderName);
            for(int  i = 20; i<=100; i+=20){
                createFiles(this.forceAlgo,i-19,i);
            }
            childernP = children;
            for(int i = 0; i<children.length; i++){
                openFrame(children[i], this.forceAlgoString);
                System.out.println("Iteration:   "+ i);
                deleteOutPutFile(children[i], folderName);  //delete the file to see which one are left
            }
        }
        System.out.println("ENDE");
    }

    /**
     * Run method for the force algorithm
     */
    public synchronized void runOnlyTotalResolutionForce()
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
            String folderName = "totalResolutionForce";
            fixGraphmlFormat(children, folderName);

            for(int  i = 20; i<=100; i+=20){
                createFiles(this.totalResForce,i-19,i);
            }
            childernP = children;
            for(int i = 0; i<children.length; i++){
                openFrame(children[i], this.totalResolutionString);
                System.out.println("Iteration:   "+ i);
                deleteOutPutFile(children[i], folderName);  //delete the file to see which one are left
            }
        }
        System.out.println("ENDE");
    }



    /**
     * Starts a frame and create an experiment object, load the graph and start the given algorithm
     * @param pattern
     * @param algorithmName
     */
    private void openFrame(String pattern, String algorithmName) {
        MainFrame frame = new MainFrame();
        frame.init();
        frame.setVisible(false); //TODO
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        String fileName = pattern;
        System.out.println("Graph " + fileName + " started.");
        frame.setBoxSize(boxSize, boxSize);
        Experiment experiment = new Experiment(frame, fileName, inputDirectory, outputDirectory, this.boxSize);

        setStartConfigurations(experiment);

        experiment.setMaxIterations(this.numOfIteration);
        experiment.setMaxTime(this.maxCalcTime);
        experiment.getTab().get().setEnableAngularResolution(this.useAngularRes);
        experiment.getTab().get().setEnableCrossingResolution(this.useCrossingRes);
        experiment.getTab().get().setEnableAspectRazio(this.useAspectRatio);

        YGraphAdapter graphAdapter = new YGraphAdapter(frame.graph);


        if(GraphChecker.isPlanar(graphAdapter.getYGraph()) && !planarGraphsAllowed){
            System.out.println("Graph " + fileName + " is planar.");
        }else if (!GraphChecker.isConnected(graphAdapter.getYGraph()) && !unconnectedGraphsAllowed){
            System.out.println("Graph " + fileName + " is not connected.");
        }else{

            int maxNodeNum = calcMaxNodeNum(frame.graph);

            //if(maxNodeNum != 40){frame.dispose();return;}//TODO: Nur zum testen

//            experiment.runOrganic();
//            experiment.scaleToBox();

            //First grid the graph
            if(useOnlyGrid) {
//                experiment.setAlgorithm(CombinedGridder.class);
//                /* Set config. parameters */
//                experiment.getTab().get().configurator.getItems().get(0).setValue(1); // Number of parallel executions
//                experiment.getTab().get().configurator.getItems().get(1).setValue(true); // Respect Minimum Angle
//                experiment.getTab().get().configurator.getItems().get(2).setValue(0.1); // Allow decreasing angle by
//                experiment.getTab().get().configurator.getItems().get(3).setValue(false); // Scale up if necessary
//                experiment.getTab().get().configurator.getItems().get(4).setValue(20); // Iterations until scale-up
//                experiment.getTab().get().configurator.getItems().get(5).setValue(2.0); // Scale by
//                experiment.getTab().get().configurator.getItems().get(6).setValue("");  // Status message
//                experiment.getTab().get().configurator.getItems().get(7).setValue(true); // Force grid after stop was pressed
//
//                //TODO: run gridding
//                experiment.runAlgorithms();
                experiment.runRandomLayout();
            }
            System.out.println("grid size " + gridSize);
            experiment.isGridded();


            if(algorithmName.equals(this.randomMovementString)){

                experiment.setAlgorithm(RandomMovementLayout.class);
            /*    for(int i = 0; i < experiment.getTab().get().configurator.getItems().size(); i++){
                    System.out.println(i + "   " + experiment.getTab().get().configurator.getItems().get(i).getName());
                }
              */                      /* Set config. parameters */
                experiment.getTab().get().configurator.getItems().get(0).setValue(10.0); // Minimum step size
                experiment.getTab().get().configurator.getItems().get(1).setValue(150.0); // Maximum step size
                experiment.getTab().get().configurator.getItems().get(2).setValue(50); // Failed iteration ... detect loacal maximum
                experiment.getTab().get().configurator.getItems().get(3).setValue(50); // numbers .. maximum resolving
                experiment.getTab().get().configurator.getItems().get(4).setValue(this.aspectRatio); // Maximum legal aspect Ratio
                experiment.getTab().get().configurator.getItems().get(5).setValue(false); // Allow decreasing minimum angle at local maximum
                experiment.getTab().get().configurator.getItems().get(6).setValue(useOnlyGrid);  // Only use grid coord.
                experiment.getTab().get().configurator.getItems().get(7).setValue(true); // focus on critical nodes
                experiment.getTab().get().configurator.getItems().get(8).setValue(true); // Automatically toggle focusing in critical nodes
                experiment.getTab().get().configurator.getItems().get(9).setValue(false); // auto increase step size
                experiment.getTab().get().configurator.getItems().get(10).setValue(useCrossingRes); // use crossing Resolution
                experiment.getTab().get().configurator.getItems().get(11).setValue(useAngularRes); // use angular Resolution
                experiment.getTab().get().configurator.getItems().get(12).setValue(useAspectRatio); // use aspect ratio


                if(this.useAspectRatio){
                    startExperiment(experiment, this.randomMovement+"aspect_ratio_" + this.aspectRatio + "_"+ (maxNodeNum-19) + "_to_" + maxNodeNum + ".csv");
                    experiment.writeGraphBestResults(this.randomMovement+"aspect_ratio_"+this.aspectRatio + "_"+ "best_" + (maxNodeNum-19) + "_to_" + maxNodeNum + ".csv");
                }else{
                    startExperiment(experiment, this.randomMovement + (maxNodeNum-19) + "_to_" + maxNodeNum + ".csv");
                    experiment.writeGraphBestResults(this.randomMovement+ "best_" + (maxNodeNum-19) + "_to_" + maxNodeNum + ".csv");
                }
                experiment.isGridded();
                System.out.println("Graph " + fileName + " finished Random Movement.");

            }else if(algorithmName.equals(this.forceAlgoString)){
                experiment.setAlgorithm(ForceAlgorithm.class);
                                /* Set config. parameters */
                experiment.getTab().get().configurator.getItems().get(0).setValue(1.0); // Node Neighbor Force
                experiment.getTab().get().configurator.getItems().get(1).setValue(false);
                experiment.getTab().get().configurator.getItems().get(2).setValue(2.0); // Node Pair force
                experiment.getTab().get().configurator.getItems().get(3).setValue(false);
                experiment.getTab().get().configurator.getItems().get(4).setValue(0.1); // Incident Edges Force
                experiment.getTab().get().configurator.getItems().get(5).setValue(true);
                experiment.getTab().get().configurator.getItems().get(6).setValue(50.0); // Spring Stiffness Force
                experiment.getTab().get().configurator.getItems().get(7).setValue(true);
                experiment.getTab().get().configurator.getItems().get(8).setValue(50000.0); // Electric Repulsiion Force
                experiment.getTab().get().configurator.getItems().get(9).setValue(true);
                experiment.getTab().get().configurator.getItems().get(10).setValue(0.35); // Crossing Force
                experiment.getTab().get().configurator.getItems().get(11).setValue(true);
                experiment.getTab().get().configurator.getItems().get(12).setValue(0.07); // Slope Force
                experiment.getTab().get().configurator.getItems().get(13).setValue(false);
                experiment.getTab().get().configurator.getItems().get(15).setValue(1.0);    //Total Resolution Force
                experiment.getTab().get().configurator.getItems().get(16).setValue(false);
                experiment.getTab().get().configurator.getItems().get(17).setValue(0.13);   //Crossing1
                experiment.getTab().get().configurator.getItems().get(18).setValue(false);
                experiment.getTab().get().configurator.getItems().get(19).setValue(0.05);   //Crossing2
                experiment.getTab().get().configurator.getItems().get(20).setValue(false);
                experiment.getTab().get().configurator.getItems().get(21).setValue(0.03);   //Angular1
                experiment.getTab().get().configurator.getItems().get(22).setValue(false);
                experiment.getTab().get().configurator.getItems().get(23).setValue(0.008);  //Angular2
                experiment.getTab().get().configurator.getItems().get(24).setValue(false);
          /*      for(int i = 0; i < experiment.getTab().get().configurator.getItems().size(); i++){
                    System.out.println(i + "   " + experiment.getTab().get().configurator.getItems().get(i).getName());
                }
*/
                startExperiment(experiment, this.forceAlgo + (maxNodeNum-19) + "_to_" + maxNodeNum + ".csv");
                experiment.writeGraphBestResults(this.forceAlgo + "best_" + (maxNodeNum-19) + "_to_" + maxNodeNum + ".csv");

                System.out.println("Graph " + fileName + " finished Force.");

            }else if(algorithmName.equals(this.totalResolutionString)){
                experiment.setAlgorithm(ForceAlgorithm.class);
                                                /* Set config. parameters */
                experiment.getTab().get().configurator.getItems().get(0).setValue(1.0); // Node Neighbor Force
                experiment.getTab().get().configurator.getItems().get(1).setValue(false);
                experiment.getTab().get().configurator.getItems().get(2).setValue(2.0); // Node Pair force
                experiment.getTab().get().configurator.getItems().get(3).setValue(false);
                experiment.getTab().get().configurator.getItems().get(4).setValue(0.1); // Incident Edges Force
                experiment.getTab().get().configurator.getItems().get(5).setValue(false);
                experiment.getTab().get().configurator.getItems().get(6).setValue(50.0); // Spring Stiffness Force
                experiment.getTab().get().configurator.getItems().get(7).setValue(true);
                experiment.getTab().get().configurator.getItems().get(8).setValue(50000.0); // Electric Repulsiion Force
                experiment.getTab().get().configurator.getItems().get(9).setValue(false);
                experiment.getTab().get().configurator.getItems().get(10).setValue(0.35); // Crossing Force
                experiment.getTab().get().configurator.getItems().get(11).setValue(false);
                experiment.getTab().get().configurator.getItems().get(12).setValue(0.07); // Slope Force
                experiment.getTab().get().configurator.getItems().get(13).setValue(false);
                experiment.getTab().get().configurator.getItems().get(15).setValue(1.0);    //Total Resolution Force
                experiment.getTab().get().configurator.getItems().get(16).setValue(true);
                experiment.getTab().get().configurator.getItems().get(17).setValue(0.13);   //Crossing1
                experiment.getTab().get().configurator.getItems().get(18).setValue(true);
                experiment.getTab().get().configurator.getItems().get(19).setValue(0.05);   //Crossing2
                experiment.getTab().get().configurator.getItems().get(20).setValue(true);
                experiment.getTab().get().configurator.getItems().get(21).setValue(0.03);   //Angular1
                experiment.getTab().get().configurator.getItems().get(22).setValue(true);
                experiment.getTab().get().configurator.getItems().get(23).setValue(0.008);  //Angular2
                experiment.getTab().get().configurator.getItems().get(24).setValue(true);

        /*        for(int i = 0; i < experiment.getTab().get().configurator.getItems().size(); i++){
                    System.out.println(i + "   " + experiment.getTab().get().configurator.getItems().get(i).getName());
                }
        */
                startExperiment(experiment, this.totalResForce + (maxNodeNum-19) + "_to_" + maxNodeNum + ".csv");
                experiment.writeGraphBestResults(this.totalResForce + "best_" + (maxNodeNum-19) + "_to_" + maxNodeNum + ".csv");
                // experiment.writeGraphEndResults();
                System.out.println("Graph " + fileName + " finished Force.");


            }
        }
        experiment.writeGraph();
        frame.dispose();
    }

    /**
     *
     * @param exp
     * @param fileName
     */
    private void startExperiment(Experiment exp, String fileName){
        startExperiment(exp, true, fileName);
    }

        private void startExperiment(Experiment exp, boolean writeInormations, String fileName){ //
            this.reached90Deg = exp.calcGraphInformations();
            if(writeInormations){
                //exp.
                // Informations();
                exp.writeGraphResults(fileName);
            }
            while(exp.getCalcTime() < this.maxCalcTime
                    && this.iterationFactor <= this.numOfIterationFactor
                    && !this.reached90Deg
                    && (exp.getNumOfUnchangedAngle() < this.maxNumberOfUnchangedAngle )
                    && exp.getCalcTime() <= this.maxCalcTime ){

                exp.runAlgorithms();

                if(writeInormations){
                    exp.writeGraphResults(fileName);
                }

                this.iterationFactor++;
            }
            if(this.iterationFactor <= this.numOfIterationFactor || exp.getCalcTime() > this.maxCalcTime ){
                exp.writeGraphResultsMaxIterations(fileName, this.numOfIteration * this.numOfIterationFactor);
            }
        }

        private void setStartConfigurations(Experiment exp){
//            System.out.println("Load Prob1");
            exp.loadGraph();
//            System.out.println("Load Prob2");
            this.iterationFactor = 1;
            this.reached90Deg = false;
            exp.setStartTime(System.currentTimeMillis());
            exp.setEndTime(System.currentTimeMillis());
        }

    private void createFiles(String algoName, int minNodeNum, int maxNodeNum){
        String filename = algoName + minNodeNum + "_to_" + maxNodeNum + ".csv";
        String filenameBest = algoName + "best_" + minNodeNum + "_to_" + maxNodeNum + ".csv";

        createFile(filename);
        createFile(filenameBest);
    }

    private void createFile(String filename){

        try {
            File dir = new File(outputDirectory);
            if(!dir.exists()){
                dir.mkdirs();
            }

            dir = new File(outputDirectory + "resultGraph/");
            if(!dir.exists()){
                dir.mkdirs();
            }

            File file = new File(outputDirectory + "/" + filename );
            if(!file.exists()){
                file.createNewFile();
                PrintWriter pw = new PrintWriter(file);

                StringBuilder sb = new StringBuilder();

                sb.append("graph name");
                sb.append(';');
                sb.append("iterations");
                sb.append(';');
                sb.append("time[ms]");
                sb.append(';');
                sb.append("crossing_res");
                sb.append(';');
                sb.append("n");
                sb.append(';');
                sb.append("m");
                sb.append(';');
                sb.append("ang.res");
                sb.append(';');
                sb.append("aspect. Ratio");
                sb.append(';');
                sb.append("#crossing");
                sb.append(';');
                sb.append("total resolution");
                sb.append(';');
                sb.append('\n');

                pw.write(sb.toString());
                pw.close();
            }
        }
        catch(FileNotFoundException ex){
            System.out.println(ex);
        }
        catch (IOException ex){
            System.out.println(ex);
        }
    }
    private int calcMaxNodeNum(IGraph g){
        int nodeNum = g.getNodes().size();
        int maxNodeNum = 0;
        for(maxNodeNum = 0; maxNodeNum <= 100; maxNodeNum+=20){
            if(maxNodeNum >= nodeNum){
                break;
            }
        }
        return maxNodeNum;
    }

    private void fixGraphmlFormat(String[] children, String specialFoldername){
        /**  For the rome graphs which has a strange .graphml format  **/
        for (int i=0; i<children.length; i++)
        {
            Path pathInput = Paths.get(this.inputDirectory + children[i]);
            Path pathOutput = Paths.get(this.outputDirectory + specialFoldername +"/" + children[i]);
            Charset charset = StandardCharsets.UTF_8;
            try {

                String content = new String(Files.readAllBytes(pathInput), charset);
                content = content.replaceAll("<!DOCTYPE graphml SYSTEM \"http://www.graphdrawing.org/dtds/graphml.dtd\">", "");


                File dir = new File(this.outputDirectory + specialFoldername +"/");
                if(!dir.exists()){
                    dir.mkdirs();
                }

                Files.write(pathOutput, content.getBytes(charset));
            }
            catch(IOException ioex){
                System.out.println("Can't read the files to convert the strange .graphml format.");
                System.out.println(ioex);
            }
        }
    }

    private void deleteOutPutFile(String child, String specialFoldername){

        Path path = Paths.get(this.outputDirectory+ specialFoldername +"/" + child);
        try {
            Files.delete(path);
        } catch (IOException x) {
            // File permission problems are caught here.
            System.err.println(x);
        }
    }

}



