package experiment;

import algorithms.graphs.GridGraph;
import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import graphoperations.GraphOperations;
import graphoperations.Scaling;
import io.ContestIOHandler;
import layout.algo.execution.ILayout;
import layout.algo.randommovement.RandomMovementLayout;
import layout.algo.utils.PositionMap;
import main.MainFrame;
import sidepanel.SidePanelTab;
import util.BoundingBox;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by Ama on 27.04.2018.
 */
public class Experiment {

    private static String outputDirectory, inputDirectory, fileName, filePrefix;

    private MainFrame mainFrame;
    private IGraph graph;
    private long sartTime, endTime, calcTime, minimumAngleTime, minimumAngleIterations;
    private long maxTimeForAlgo = 2000; //default 100 sec
    private boolean reached90Degree = false;
    private boolean isInInvLoop = false;
    private PrintWriter pw;
    private StringBuilder sb;
    private int maxIterations = 1000;
    private int iterations = 0;
    private int numOfUnchangedAngle = 0;
    private int numOfCrossings = -1;
    private int  minimumAngleNumOfCrossings;
    private double aspect_ratio, minimumAngleAspectratio;
    private double angular_resolution, minimumAngleAngularResolution, minimumAngleTotalResolution;
    private final double approx_epsilon = 0.001; // If the angle plus epsilon doesn't change  after number of iterations, then it is converged
    private double actAngle = 0;
    private double actAngularAngle = 0;
    private double actTotalAngle = 0;
    private Mapper<INode, PointD> positions;
    Optional<SidePanelTab> tab;

    private int totalVertecies, totalEdges;
    private double currentGraphSizeX, currentGraphSizeY, minimumAngle, testAngle, totalAngle;

    public  Experiment (MainFrame mainFrame, String fileName, String inputDir, String outputDir, int boxSize) {
        this.mainFrame = mainFrame;
        this.graph = mainFrame.graph;
        this.fileName = fileName;
        this.filePrefix = getPrefixString(fileName) + "_"+ boxSize;
        this.inputDirectory = inputDir;
        this.outputDirectory = outputDir;
        this.calcTime = 0;

        this.tab = this.mainFrame.getTabForAlgorithm(RandomMovementLayout.class); // Random Movement is the default algorithm
    }

    /**
     * This method is for the last line of the results, if the  angel converge
     */
    public void writeGraphEndResults(){
        File file = new File(outputDirectory + "/" + filePrefix + ".csv");
        sb = new StringBuilder();


        sb.append(this.filePrefix);
        sb.append(';');
        sb.append(this.graph.getNodes().size());
        sb.append(';');
        sb.append(this.graph.getEdges().size());
        sb.append(';');
        sb.append(this.minimumAngle);
        sb.append(';');
        sb.append(this.numOfCrossings);
        sb.append(';');
        sb.append(this.actAngularAngle);
        sb.append(';');
        sb.append(this.aspect_ratio);
        sb.append(';');
        sb.append(this.minimumAngleIterations);
        sb.append(';');
        sb.append(this.minimumAngleTime);
        sb.append(';');
        sb.append(this.tab.get().algorithmName);
        sb.append(';');
        sb.append('\n');



        int index = sb.indexOf(";" + "\n");

        try{
            insertStringInFile(file, 2, sb.toString());
        } catch (Exception ex){
            System.out.println(ex);
        }


    }

    /**
     * write down the best results
     * @param filename
     */

    public void writeGraphBestResults(String filename){
        sb = new StringBuilder();


        sb.append(this.fileName);
        sb.append(';');
        sb.append(this.minimumAngleIterations);
        sb.append(';');
        sb.append(this.minimumAngleTime);
        sb.append(';');
        sb.append(this.minimumAngle);
        sb.append(';');
        sb.append(this.graph.getNodes().size());
        sb.append(';');
        sb.append(this.graph.getEdges().size());
        sb.append(';');
        sb.append(this.minimumAngleAngularResolution);
        sb.append(';');
        sb.append(this.minimumAngleAspectratio);
        sb.append(';');
        sb.append(this.minimumAngleNumOfCrossings);
        sb.append(';');
        sb.append(this.minimumAngleTotalResolution);
        sb.append(';');
        sb.append('\n');

        Path path = FileSystems.getDefault().getPath(outputDirectory , "/" + filename);
        try {
            Files.write(path, sb.toString().getBytes(), StandardOpenOption.APPEND);
        }
        catch(IOException ex){
            System.out.println(ex);
        }

    }

    /**
     *  Standard method to write down the results each step
     * @param filename
     */

    public void writeGraphResults(String filename){
        sb = new StringBuilder();


        sb.append(this.fileName);
        sb.append(';');
        sb.append(this.iterations);
        sb.append(';');
        sb.append(this.calcTime);
        sb.append(';');
        sb.append(this.actAngle);
        sb.append(';');
        sb.append(this.graph.getNodes().size());
        sb.append(';');
        sb.append(this.graph.getEdges().size());
        sb.append(';');
        sb.append(this.actAngularAngle);
        sb.append(';');
        sb.append(this.aspect_ratio);
        sb.append(';');
        sb.append(this.numOfCrossings);
        sb.append(';');
        sb.append(this.actTotalAngle);
        sb.append(';');
        sb.append('\n');

        Path path = FileSystems.getDefault().getPath(outputDirectory , "/" + filename);
        try {
            Files.write(path, sb.toString().getBytes(), StandardOpenOption.APPEND);
        }
        catch(IOException ex){
            System.out.println(ex);
        }

    }

    /**
     *  This method is for the last line of the results, if the  angel converge
     * @param filename
     * @param iterations
     */

    public void writeGraphResultsMaxIterations(String filename, int iterations){
        sb = new StringBuilder();


        sb.append(this.fileName);
        sb.append(';');
        sb.append(iterations);
        sb.append(';');
        sb.append(this.maxTimeForAlgo);
        sb.append(';');
        sb.append(this.minimumAngle);
        sb.append(';');
        sb.append(this.graph.getNodes().size());
        sb.append(';');
        sb.append(this.graph.getEdges().size());
        sb.append(';');
        sb.append(this.minimumAngleAngularResolution);
        sb.append(';');
        sb.append(this.aspect_ratio);
        sb.append(';');
        sb.append(this.numOfCrossings);
        sb.append(';');
        sb.append(this.minimumAngleTotalResolution);
        sb.append(';');
        sb.append('\n');

        Path path = FileSystems.getDefault().getPath(outputDirectory , "/" + filename);
        try {
            Files.write(path, sb.toString().getBytes(), StandardOpenOption.APPEND);
        }
        catch(IOException ex){
            System.out.println(ex);
        }

    }



    public void writeGraphInformations(){
        sb = new StringBuilder();
        sb.append(this.calcTime);
        sb.append(';');
        sb.append(this.iterations);
        sb.append(';');
        sb.append(this.minimumAngle);
        sb.append('\n');

        Path path = FileSystems.getDefault().getPath(outputDirectory , "/" + filePrefix +".csv");
        try {
            Files.write(path, sb.toString().getBytes(), StandardOpenOption.APPEND);
        }
        catch(IOException ex){
                System.out.println(ex);
        }
    }


    public void deleteGraphInformationFile(){
        System.out.println(outputDirectory + "/" + filePrefix +".csv");
        Path path = FileSystems.getDefault().getPath(outputDirectory , "/" + filePrefix +".csv");
        try {
            Files.delete(path);
        }
        catch(IOException ex){
            System.out.println(outputDirectory + "/" + filePrefix +".csv");
            System.out.println(ex);
        }
    }

    /**
     * Runs the graph algorithm until it reached the max number of iterations or max time
     */
    public  void runAlgorithms() {
//        this.mainFrame.initSidePanel.removeDefaultListeners();
//
//        this.mainFrame.initSidePanel.addDefaultListeners();


        if (!this.tab.isPresent()) {
            System.out.println("TAb not present!!!");
            return;
        }
        this.tab.get().getExecutor().setMaxIterations(maxIterations);
        long tmpTime = System.currentTimeMillis();
        this.tab.get().startPauseExecution();
        boolean value = true;

        while(!this.tab.get().getExecutor().isFinished() && this.tab.get().getExecutor().isRunning() && !this.isInInvLoop){

            if(System.currentTimeMillis() - tmpTime + this.calcTime >  this.maxTimeForAlgo){ // + this.calcTime
                this.isInInvLoop = true;
                this.tab.get().startPauseExecution();
                System.out.println("Catched in a inv. Loop. The calculation will be restarted.");
            }
        }
        this.positions = this.tab.get().getExecutor().getLayout().getNodePositions();
        this.endTime = endTime + (System.currentTimeMillis() - tmpTime);
        this.iterations += maxIterations;
        calcGraphInformations();
    }

    /**
     * Loads a new graph and reset the parameters
     */
    public void loadGraph() {
        this.reached90Degree = false;
        this.isInInvLoop = false;
        this.numOfUnchangedAngle = 0;
        this.minimumAngleIterations = 0;
        this.minimumAngle = 0;
        this.totalAngle = 0;
        this.minimumAngleTime = 0;
        this.minimumAngleAspectratio = -1;
        this.minimumAngleAngularResolution = 0;
        this.minimumAngleTotalResolution = 0;
        this.iterations = 0;
        this.calcTime = 0;
        this.numOfCrossings = -1;
        this.actAngle = 0;
        this.actAngularAngle = 0;
        this.actTotalAngle = 0;


        if (this.fileName.endsWith(".txt")){
            System.out.println("Try to open .txt");
            this.mainFrame.openContestFile(this.inputDirectory + this.fileName);
        } else if(this.fileName.endsWith(".graphml")){
           // this.mainFrame.openFile(this.inputDirectory + this.fileName);
            this.mainFrame.openSimpleFile(this.inputDirectory + this.fileName);
        }

        mainFrame.minimumAngleMonitor.updateCrossingResolutionInfoBar();
        this.minimumAngle = Math.round(1000.0 * mainFrame.minimumAngleMonitor.getBestCrossingResolution()) / 1000.0;
        System.out.println("load Graph " + this.minimumAngle);
        this.aspect_ratio = GraphOperations.getAspectRatio(this.graph).getValue();
    }

    void runOrganic(){
        this.mainFrame.initSidePanel.removeDefaultListeners();
        LayoutUtilities.applyLayout(this.mainFrame.graph, new OrganicLayout());
        this.mainFrame.initSidePanel.addDefaultListeners();
    }

    void scaleToBox(){
        this.mainFrame.initSidePanel.removeDefaultListeners();

        IGraph graph = this.mainFrame.graph;
        Mapper<INode, PointD> nodePositions = PositionMap.FromIGraph(graph);
        RectD bounds = BoundingBox.from(nodePositions);
        Scaling.scaleBy(Math.min((MainFrame.BOX_SIZE[0] / bounds.getWidth()), (MainFrame.BOX_SIZE[1] / bounds.getHeight())), nodePositions);
        PositionMap.applyToGraph(graph, nodePositions);
        this.mainFrame.view.fitGraphBounds();

        bounds = BoundingBox.from(nodePositions);
//        s.setText("Scaled to Box with Size: " + bounds.getWidth() + "x" + bounds.getHeight());
        System.out.println("SCALED TO BOX(MAX: "+ MainFrame.BOX_SIZE[0] +" and " + MainFrame.BOX_SIZE[1] +" to " +bounds.width + " " +bounds.height);
        this.mainFrame.initSidePanel.addDefaultListeners();
    }

    void isGridded() {
        System.out.println("GRIDDED: "+GridGraph.isGridGraph(graph));

    }

    /**
     * Saves the resulting graph in contest format
     */
    public void writeGraph() {
        try {
            ContestIOHandler.write(graph, outputDirectory + "resultGraph/" + "out_" + filePrefix + ".txt", mainFrame.initSidePanel.getOutputTextArea());
        } catch (IOException ioe) {
            System.out.println("An error occured while exporting the graph.");
        }
    }

    private static void pause(int waitTime){
        long Time0 = System.currentTimeMillis();
        long Time1;
        long runTime = 0;
        while(runTime < waitTime){
            Time1 = System.currentTimeMillis();
            runTime = Time1 - Time0;
        }
    }

    /**
     * Calculates and updates the graph parameters
     * @return
     */
    public  boolean calcGraphInformations(){

        ArrayList<Integer> verticesDegree = new ArrayList<>();

        graph = mainFrame.graph;

        for (INode u : graph.getNodes()) {
            int deg = graph.degree(u);
            while(deg >= verticesDegree.size()) {
                verticesDegree.add(0);
            }
            verticesDegree.set(deg, verticesDegree.get(deg) + 1);
        }
        this.totalVertecies = graph.getNodes().size();
        this.totalEdges = graph.getEdges().size();

        RectD bounds = BoundingBox.from(PositionMap.FromIGraph(graph));
        this.currentGraphSizeX = bounds.getWidth() < 1 ? 0 : bounds.getWidth();   //smaller than 1 is not a graph
        this.currentGraphSizeY = bounds.getHeight() < 1 ? 0 : bounds.getHeight();


        this.calcTime = this.endTime - this.sartTime;
       // this.iterations += this.maxIterations;
        this.numOfCrossings = MinimumAngle.getCrossings(this.graph, this.positions).size();

        this.aspect_ratio = GraphOperations.getAspectRatio(this.graph).getValue();

        mainFrame.minimumAngleMonitor.updateCrossingResolutionInfoBar();

        mainFrame.minimumAngleMonitor.computeTotalResolution();

        this.actAngle = Math.round(10000.0 * mainFrame.minimumAngleMonitor.getBestCrossingResolution()) / 10000.0; // with the best crossing angle
        this.actAngularAngle = Math.round(10000.0 * mainFrame.minimumAngleMonitor.getBestAngularResolution()) / 10000.0; // with the best angular
        this.actTotalAngle = Math.round(10000.0 * mainFrame.minimumAngleMonitor.getBestTotalResolution()) / 10000.0; // with the best total angle

        if(((this.totalAngle + this.approx_epsilon ) >= this.actTotalAngle)){
            this.numOfUnchangedAngle++;
//            System.out.println("Num. of unchanged angele: \t\t" + this.numOfUnchangedAngle);
        }else{
//            System.out.println("Num. of unchanged angele: \t\t" + this.numOfUnchangedAngle);
            this.minimumAngle = this.actAngle;
            this.minimumAngleTime = this.calcTime;
            this.minimumAngleIterations = this.iterations;
            this.numOfUnchangedAngle = 0;
            this.minimumAngleNumOfCrossings = this.numOfCrossings;
            this.minimumAngleAspectratio = this.aspect_ratio;
            this.minimumAngleAngularResolution = this.actAngularAngle;
            this.minimumAngleTotalResolution = this.actTotalAngle;
            this.totalAngle = this.actTotalAngle;
        }

        if(this.minimumAngle >= 90.0){
            reached90Degree = true;
        }

        this.testAngle = mainFrame.minimumAngleMonitor.getBestCrossingResolution();

        this.numOfCrossings = MinimumAngle.getCrossings(this.graph, this.positions).size();

        this.aspect_ratio = GraphOperations.getAspectRatio(this.graph).getValue();

        return reached90Degree;

    }

    private void insertStringInFile(File inFile, int lineNo, String lineToBeInserted) throws Exception {
        // temp file
        File outFile = new File("$$$$$$$$.tmp");

        // input
        FileInputStream fis  = new FileInputStream(inFile);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));

        // output
        FileOutputStream fos = new FileOutputStream(outFile);
        PrintWriter out = new PrintWriter(fos);

        String thisLine = "";
        int i =1;
        while ((thisLine = in.readLine()) != null) {
            if(i == lineNo) out.println(lineToBeInserted);
            out.println(thisLine);
            i++;
        }
        out.flush();
        out.close();
        in.close();

        inFile.delete();
        outFile.renameTo(inFile);
    }

    public void setAlgorithm(Class<? extends ILayout> algorithmLayout){
        this.tab = this.mainFrame.getTabForAlgorithm(algorithmLayout);
    }

    public void setStartTime(long startTime){
        this.sartTime = startTime;
    }

    public void setEndTime(long endTime){
        this.endTime = endTime;
    }

    public void setMaxTime(long maxTime){this.maxTimeForAlgo = maxTime;}

    public void setMaxIterations(int maxIta){
        this.maxIterations = maxIta;
    }

    public Optional<SidePanelTab> getTab(){return this.tab;}

    public long getCalcTime(){return this.calcTime;}

    public boolean getIsInInvLoop(){return this.isInInvLoop;}

    public int getNumOfUnchangedAngle(){return this.numOfUnchangedAngle;}


    private String getPrefixString(String str){
        int position = str.lastIndexOf(".");
        //System.out.println(str.substring(0, position));
        return str.substring(0, position);
    }

    public String getGraphInformationsString(){
        return sb.toString();
    }

    public  boolean printGraphInformations(){
        ArrayList<Integer> verticesDegree = new ArrayList<>();

        graph = mainFrame.graph;
        for (INode u : graph.getNodes()) {
            int deg = graph.degree(u);
            while(deg >= verticesDegree.size()) {
                verticesDegree.add(0);
            }
            verticesDegree.set(deg, verticesDegree.get(deg) + 1);
        }
        StringBuilder graphInfo = new StringBuilder();
        graphInfo.append("------------------------------------" + fileName + "------------------------------------" + "\n");
        graphInfo.append("Deg. Num.\n");
        for (int i = 0 ; i < verticesDegree.size(); i++) {
            if (verticesDegree.get(i) > 0) {
                graphInfo.append("  ").append(i).append("   :   ").append(verticesDegree.get(i).toString()).append("\n");
            }
        }
        graphInfo.append("\nTotal Vertices: ").append(graph.getNodes().size())
                .append("\nTotal Edges:    ").append(graph.getEdges().size()).append("\n");

        RectD bounds = BoundingBox.from(PositionMap.FromIGraph(graph));
        double width = bounds.getWidth() < 1 ? 0 : bounds.getWidth();   //smaller than 1 is not a graph
        double height = bounds.getHeight() < 1 ? 0 : bounds.getHeight();
        graphInfo.append("\nCurrent Graph Size: \nX: ").append(width).append("\nY: ").append(height).append("\n\n");

        double threshold = 0.999;
        double edgeThreshold = 0.999;
        boolean nodeOverlap = false;
        boolean nodeEdgeOverlap = false;
        for (INode u : mainFrame.graph.getNodes()) {

            if (u.getPorts().size() == 1) {
                for (IEdge e : graph.edgesAt(u.getPorts().first())) {
                    INode src = e.getSourceNode();
                    INode dst = e.getTargetNode();
                    for (INode v : mainFrame.graph.getNodes()) {
                        if ((u.hashCode() != v.hashCode()) &&
                                v.hashCode() != src.hashCode() && v.hashCode() != dst.hashCode()) {
                            double x0 = v.getLayout().getCenter().getX();
                            double y0 = v.getLayout().getCenter().getY();
                            double x1 = src.getLayout().getCenter().getX();
                            double y1 = src.getLayout().getCenter().getY();
                            double x2 = dst.getLayout().getCenter().getX();
                            double y2 = dst.getLayout().getCenter().getY();
                            double dist;
                            if (y1 == y2) { //vertical
                                dist = Math.abs(x0 - x1);
                            } else if (x1 == x2) {  //horizontal
                                dist = Math.abs(y0 -y1);
                            } else {
                                dist = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1) / Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
                            }
                            if (dist < edgeThreshold) {
                                nodeEdgeOverlap = true;
                            }
                        }
                    }
                }
            }
            for (INode v : mainFrame.graph.getNodes()) {
                if ((u.hashCode() != v.hashCode()) &&
                        Math.abs(u.getLayout().getCenter().getX() - v.getLayout().getCenter().getX()) < threshold &&
                        Math.abs(u.getLayout().getCenter().getY() - v.getLayout().getCenter().getY()) < threshold ) {
                    nodeOverlap = true;
                }

            }
        }
        graphInfo.append("Node Node Overlap: ").append(nodeOverlap).append("\n");
        graphInfo.append("Node Edge Overlap: ").append(nodeEdgeOverlap).append("\n");

        graphInfo.append("\nGridded: ").append(GridGraph.isGridGraph(graph)).append("\n");

        graphInfo.append("\nMin. Angle: ").append(mainFrame.minimumAngleMonitor.getBestCrossingResolution()).append("\n");
        mainFrame.minimumAngleMonitor.updateCrossingResolutionInfoBar();
        double minAngle = mainFrame.minimumAngleMonitor.getBestCrossingResolution();
        graphInfo.append("\nMin. Angle: ").append(minAngle).append("\n");
        if(minAngle>=87.0){
            reached90Degree = true;
        }

        graphInfo.append("\nCalc Time: ").append(this.endTime - this.sartTime).append("\n");


        System.out.println(graphInfo);

        //outputTextArea.setText(graphInfo.toString());
        return reached90Degree;

    }

    public void writeGraphInformationsWarning(String str){
        sb = new StringBuilder();
        sb.append(str);
        sb.append(';');
        sb.append(str);
        sb.append(';');
        sb.append(str);
        sb.append(';');
        sb.append(str);
        sb.append('\n');

        Path path = FileSystems.getDefault().getPath(outputDirectory , "/" + filePrefix +".csv");
        try {
            Files.write(path, sb.toString().getBytes(), StandardOpenOption.APPEND);
        }
        catch(IOException ex){
            System.out.println(ex);
        }
    }

}
