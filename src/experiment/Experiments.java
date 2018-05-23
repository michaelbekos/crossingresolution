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
import layout.algo.randommovement.RandomMovementLayout;
import main.MainFrame;

import javax.swing.*;

public class Experiments {

    //private y.view.Graph2DView view;
    private GraphComponent comp;
    private String inputDirectory  = "E:\\graph\\graphml\\";
    private String outputDirectory = "E:\\graph\\afterRandom\\graphml2\\";


    /* Config. for the type of experiment */
    private int numOfIteration = 100;
    int iterationFactor = 1;
    private int numOfIterationFactor = 1000;
    private long maxCalcTime = 1500; //in mili. sec
    private int boxSize = 10000;
    private boolean planarGraphsAllowed = false;
    private boolean unconnectedGraphsAllowed = true;
    boolean reached90Deg = false;
    private String randomMovement, forceAlgo;
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
        this.randomMovement = "randommovement_n_";
        this.forceAlgo = "force_algo_n_";
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

                    for(int  i = 20; i<=100; i+=20){
                        createFiles(i-19,i);
                    }
                    childernP = children;
                    //for(int i = 0; i<1; i++){
                    for(int i = 0; i<children.length; i++){
                        openFrame(children[i]);
                        System.out.println("Iteration:   "+ i);
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
            for(int  i = 20; i<=100; i+=20){
                createFiles(i-19,i);
            }
            childernP = children;
            for(int i = 0; i<children.length; i++){
                openFrame(children[i], ForceAlgorithm.class);
                System.out.println("Iteration:   "+ i);
            }
        }
        System.out.println("ENDE");
    }

    /**
     * Run method with the random movement
     */
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
//TODO:beides verwenden
        if (children == null) {
            System.out.println("Children==null");
            // Either dir does not exist or is not a directory
        }
        else {
            for(int  i = 20; i<=100; i+=20){
                createFiles(i-19,i);
            }
            childernP = children;
            for(int i = 0; i<children.length; i++){
                openFrame(children[i], RandomMovementLayout.class);
                System.out.println("Iteration:   "+ i);
            }
        }

/*        for (int i=0; i<children.length; i++)
        {
            System.out.println("Iteration: " + i);

            Path path = Paths.get(this.inputDirectory + children[i]);
            Charset charset = StandardCharsets.UTF_8;
try {


    String content = new String(Files.readAllBytes(path), charset);
    content = content.replaceAll("<!DOCTYPE graphml SYSTEM \"http://www.graphdrawing.org/dtds/graphml.dtd\">", "");
    Files.write(path, content.getBytes(charset));
}
    catch(IOException ioex){}
        }
*/
     System.out.println("ENDE");
    }

    /**
     * Starts a frame and create an experiment object, load the graph and start the given algorithm
     * @param pattern
     * @param algorithmLayout
     */
    private void openFrame(String pattern, Class<? extends ILayout> algorithmLayout) {
        MainFrame frame = new MainFrame();
        frame.init();
        frame.setVisible(false); //TODO
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);


        frame.BOX_SIZE = this.boxSize;
        //String fileName = index +"";
        String fileName = pattern;
        System.out.println("Graph " + fileName + " started.");
        Experiment experiment = new Experiment(frame, fileName, inputDirectory, outputDirectory, this.boxSize);

        setStartConfigurations(experiment);

        experiment.setAlgorithm(algorithmLayout);

        experiment.setMaxIterations(this.numOfIteration);
        experiment.setMaxTime(this.maxCalcTime);

        YGraphAdapter graphAdapter = new YGraphAdapter(frame.graph);


        if(GraphChecker.isPlanar(graphAdapter.getYGraph()) && !planarGraphsAllowed){
            System.out.println("Graph " + fileName + " is planar.");
        }else if (!GraphChecker.isConnected(graphAdapter.getYGraph()) && !unconnectedGraphsAllowed){
            System.out.println("Graph " + fileName + " is not connected.");
        }else{

            int maxNodeNum = calcMaxNodeNum(frame.graph);
            experiment.setAlgorithm(algorithmLayout);
            if(algorithmLayout.equals(RandomMovementLayout.class)){
                                    /* Set config. parameters */
                experiment.getTab().get().configurator.getItems().get(0).setValue(10.0); // Minimum step size
                experiment.getTab().get().configurator.getItems().get(1).setValue(150.0); // Maximum step size
                experiment.getTab().get().configurator.getItems().get(2).setValue(50); // Faild iteration ... detect loacal maximum
                experiment.getTab().get().configurator.getItems().get(3).setValue(50); // numbers .. maximum resolving
                experiment.getTab().get().configurator.getItems().get(4).setValue(false); // Allow decreasing minimum angle at local maximum
                experiment.getTab().get().configurator.getItems().get(5).setValue(false);  // Only use grid coord.
                experiment.getTab().get().configurator.getItems().get(6).setValue(true); // focus on critical nodes
                experiment.getTab().get().configurator.getItems().get(7).setValue(true); // Automatically toggle focusing in critical nodes


                startExperiment(experiment, this.randomMovement + (maxNodeNum-19) + "_to_" + maxNodeNum + ".csv");
                experiment.writeGraphBestResults(this.randomMovement+ "best_" + (maxNodeNum-19) + "_to_" + maxNodeNum + ".csv");
                //experiment.writeGraphEndResults();
                System.out.println("Graph " + fileName + " finished Random Movement.");
            //    experiment.writeGraphEndResults();
                experiment.writeGraph();

            }else if(algorithmLayout.equals(ForceAlgorithm.class)){

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

                startExperiment(experiment, this.forceAlgo + (maxNodeNum-19) + "_to_" + maxNodeNum + ".csv");
                experiment.writeGraphBestResults(this.forceAlgo + "best_" + (maxNodeNum-19) + "_to_" + maxNodeNum + ".csv");
                // experiment.writeGraphEndResults();
                System.out.println("Graph " + fileName + " finished Force.");
            //    experiment.writeGraphEndResults();
            }


        }
        frame.dispose();
        //  this.isFrameFinished = true;


    }

    /**
     * Starts a frame and generate an experiment object. The experiment loads the graph, then run the Random Movement,
     * then loads the same graph again and start the force-algorithm
     * @param pattern
     */

    private void openFrame(String pattern) {



        MainFrame frame = new MainFrame();
        frame.init();
        frame.setVisible(false);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        frame.BOX_SIZE = this.boxSize;
        //String fileName = index +"";
        String fileName = pattern;
        System.out.println("Graph " + fileName + " started.");
        //  System.out.println("1");
        Experiment experiment = new Experiment(frame, fileName, inputDirectory, outputDirectory, this.boxSize);
        setStartConfigurations(experiment);
        experiment.setMaxIterations(this.numOfIteration);
        experiment.setMaxTime(this.maxCalcTime);

        YGraphAdapter graphAdapter = new YGraphAdapter(frame.graph);


        if(GraphChecker.isPlanar(graphAdapter.getYGraph()) && !planarGraphsAllowed){
            System.out.println("Graph " + fileName + " is planar.");
        }else if (!GraphChecker.isConnected(graphAdapter.getYGraph()) && !unconnectedGraphsAllowed){
            System.out.println("Graph " + fileName + " is not connected.");
        }else{
            /* Run with Random Movement  */
            int maxNodeNum = calcMaxNodeNum(frame.graph);
            experiment.setAlgorithm(RandomMovementLayout.class);
            startExperiment(experiment, this.randomMovement + (maxNodeNum-19) + "_to_" + maxNodeNum + ".csv");
            experiment.writeGraphBestResults(this.randomMovement+ "best_" + (maxNodeNum-19) + "_to_" + maxNodeNum + ".csv");
            //experiment.writeGraphEndResults();
            System.out.println("Graph " + fileName + " finished Random Movement.");
            experiment.writeGraph();

            /* Run with Force Movement  */
            setStartConfigurations(experiment);
            experiment.setAlgorithm(ForceAlgorithm.class);
            startExperiment(experiment, this.forceAlgo + (maxNodeNum-19) + "_to_" + maxNodeNum + ".csv");
           // experiment.writeGraphEndResults();
            experiment.writeGraphBestResults(this.forceAlgo + "best_" + (maxNodeNum-19) + "_to_" + maxNodeNum + ".csv");
            System.out.println("Graph " + fileName + " finished Force.");

        }
        frame.dispose();
      //  this.isFrameFinished = true;


    }


    /**
     *
     * @param exp
     * @param fileName
     */
    private void startExperiment(Experiment exp, String fileName){
        startExperiment(exp, true, fileName);
    }

        private void startExperiment(Experiment exp, boolean writeInormations, String fileName){
            while(exp.getCalcTime() < this.maxCalcTime  && this.iterationFactor <= this.numOfIterationFactor && !this.reached90Deg && (exp.getNumOfUnchangedAngle() <= 100 ) && exp.getCalcTime() <= this.maxCalcTime ){

                exp.runAlgorithms();



                if(writeInormations){
                    //exp.writeGraphInformations();
                    exp.writeGraphResults(fileName);
                }

                this.reached90Deg = exp.calcGraphInformations();
                this.iterationFactor++;
            }

            exp.writeGraphResults(fileName);

            if(this.iterationFactor <= this.numOfIterationFactor || exp.getCalcTime() > this.maxCalcTime ){
                System.out.println("Was da lso");
                exp.writeGraphResultsMaxIterations(fileName, this.numOfIteration * this.numOfIterationFactor);
            }
        }

        private void setStartConfigurations(Experiment exp){
            System.out.println("Load Prob1");
            exp.loadGraph();
            System.out.println("Load Prob2");
            this.iterationFactor = 1;
            this.reached90Deg = false;
            exp.setStartTime(System.currentTimeMillis());
            exp.setEndTime(System.currentTimeMillis());
        }

    private void createFiles(int minNodeNum, int maxNodeNum){
        String filenameRandom = this.randomMovement + minNodeNum + "_to_" + maxNodeNum + ".csv";
        String filenameRandomBest = this.randomMovement + "best_" + minNodeNum + "_to_" + maxNodeNum + ".csv";
        String filenameForce = this.forceAlgo + minNodeNum + "_to_" + maxNodeNum + ".csv";
        String filenameForceBest = this.forceAlgo + "best_" + minNodeNum + "_to_" + maxNodeNum + ".csv";

        createFile(filenameForce);
        createFile(filenameForceBest);
        createFile(filenameRandom);
        createFile(filenameRandomBest);
    }

    private void createFile(String filename){

        try {

            File dir = new File(outputDirectory);
            if(!dir.exists()){
                dir.mkdirs();
            }

            File file = new File(outputDirectory + "\\" + filename );
            if(!file.exists()){
                file.createNewFile();
                PrintWriter pw = new PrintWriter(file);

                StringBuilder sb = new StringBuilder();

                sb.append("graph name");
                sb.append(';');
                sb.append("iterations");
                sb.append(';');
                sb.append("time");
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
            if(maxNodeNum > nodeNum){
                break;
            }
        }
        return maxNodeNum;

    }

}



