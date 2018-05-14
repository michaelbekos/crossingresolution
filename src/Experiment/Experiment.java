package Experiment;

import algorithms.graphs.GridGraph;
import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.graphml.GraphMLIOHandler;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import io.ContestIOHandler;
import io.GraphIOHandler;
import layout.algo.execution.ILayout;
import layout.algo.forcealgorithm.ForceAlgorithm;
import layout.algo.genetic.GeneticForceAlgorithmLayout;
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
    private long maxTimeForAlgo = 100000;
    private boolean reached90Degree = false;
    private boolean isInInvLoop = false;
    private PrintWriter pw;
    private StringBuilder sb;
    private int maxIterations = 1000;
    private int iterations = 0;
    private int numOfUnchangedAngle = 0;
    private int numOfCrossings = -1;
    private double bestAngle = 0;
    private int algorithmNum = 1; // 1: Random Algo. 2: Force Algo
    private Mapper<INode, PointD> positions;
    Optional<SidePanelTab> tab;

    private int totalVertecies, totalEdges;
    private double currentGraphSizeX, currentGraphSizeY, minimumAngle, testAngle;

    public  Experiment (MainFrame mainFrame, String fileName, String inputDir, String outputDir, int boxSize) {
        this.mainFrame = mainFrame;
        this.graph = mainFrame.graph;
        this.fileName = fileName;
        this.filePrefix = getPrefixString(fileName) + "_"+ boxSize;
        this.inputDirectory = inputDir;
        this.outputDirectory = outputDir;
        this.calcTime = 0;

        this.tab = this.mainFrame.getTabForAlgorithm(RandomMovementLayout.class); // Random Movement is the default algorithm

        //sb = new StringBuilder();

        try {

            File dir = new File(outputDirectory);
            if(!dir.exists()){
                dir.mkdirs();
            }

            File file = new File(outputDirectory + "\\" + filePrefix + ".csv");
            if(!file.exists()){
                file.createNewFile();
            }

            pw = new PrintWriter(file);

            sb = new StringBuilder();

            sb.append("graph name");
            sb.append(';');
            sb.append("n");
            sb.append(';');
            sb.append("m");
            sb.append(';');
            sb.append("crossing_res");
            sb.append(';');
            sb.append("number_crossing");
            sb.append(';');
            sb.append("angular_ratio");
            sb.append(';');
            sb.append("aspect-ratio");
            sb.append(';');
            sb.append("no. iterations");
            sb.append(';');
            sb.append("time");
            sb.append(';');
            sb.append("algorithm");
            sb.append(';');
            sb.append('\n');


            sb.append("Time");
            sb.append(';');
            sb.append("Iterations");
            sb.append(';');
            sb.append("Angle");
            sb.append(';');
            sb.append('\n');

            pw.write(sb.toString());
            pw.close();
        }
        catch(FileNotFoundException ex){
            System.out.println(ex);
        }
        catch (IOException ex){
            System.out.println(ex);

        }
    }

    public void run(){

        loadGraph();

        boolean isNotFinished = true;
        int iterationFactor = 0;
        boolean reached90Deg = false;
        setStartTime(System.currentTimeMillis());
        setEndTime(System.currentTimeMillis());


        while (!reached90Deg && iterationFactor != 100) {
            runAlgorithms();
            setEndTime(System.currentTimeMillis());
            String suffix = this.filePrefix + "_" + (iterationFactor * 1000);
            //reached90Deg = saveGraphInformations(mainFrame, pattern, suffix, endTime-startTime);
            writeGraphInformations();
            //reached90Deg = experiment.printGraphInformations();
            reached90Deg = calcGraphInformations();
            writeGraph();
            iterationFactor++;

        }

    }

    public void writeGraphEndResults(){
        File file = new File(outputDirectory + "\\" + filePrefix + ".csv");
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
        sb.append("angular_ratio");
        sb.append(';');
        sb.append("aspect-ratio");
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

    private void insertStringInFile(File inFile, int lineno, String lineToBeInserted) throws Exception {
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
            if(i == lineno) out.println(lineToBeInserted);
            out.println(thisLine);
            i++;
        }
        out.flush();
        out.close();
        in.close();

        inFile.delete();
        outFile.renameTo(inFile);
    }

    public void writeGraphInformations(){
        sb = new StringBuilder();
        sb.append(this.calcTime);
        sb.append(';');
        sb.append(this.iterations);
        sb.append(';');
        sb.append(this.minimumAngle);
        sb.append('\n');

        Path path = FileSystems.getDefault().getPath(outputDirectory , "\\" + filePrefix +".csv");
        try {
            Files.write(path, sb.toString().getBytes(), StandardOpenOption.APPEND);
        }
        catch(IOException ex){
                System.out.println(ex);
        }
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

        Path path = FileSystems.getDefault().getPath(outputDirectory , "\\" + filePrefix +".csv");
        try {
            Files.write(path, sb.toString().getBytes(), StandardOpenOption.APPEND);
        }
        catch(IOException ex){
            System.out.println(ex);
        }
    }

    public void deleteGraphInformationFile(){
        Path path = FileSystems.getDefault().getPath(outputDirectory , "\\" + filePrefix +".csv");
        try {
            Files.delete(path);
        }
        catch(IOException ex){
            System.out.println(ex);
        }
    }

    public String getGraphInformationsString(){
        return sb.toString();
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

    public void loadGraph() {
        this.reached90Degree = false;
        this.isInInvLoop = false;
        this.numOfUnchangedAngle = 0;
        this.minimumAngleIterations = 0;
        this.minimumAngle = 0;
        this.minimumAngleTime = 0;

        if (this.fileName.endsWith(".txt")){
            System.out.println("Try to open .txt");
            this.mainFrame.openContestFile(this.inputDirectory + this.fileName);
        } else if(this.fileName.endsWith(".graphml")){
            this.mainFrame.openFile(this.inputDirectory + this.fileName);

        }

        mainFrame.minimumAngleMonitor.updateMinimumAngleInfoBar();
        this.minimumAngle = Math.round(1000.0 * mainFrame.minimumAngleMonitor.getMinAngle()) / 1000.0;
        System.out.println("load Graph " + this.minimumAngle);
    }

    public void writeGraph() {
        try {
            ContestIOHandler.write(graph, outputDirectory + "out_" + filePrefix + ".txt", mainFrame.initSidePanel.getOutputTextArea());
        } catch (IOException ioe) {
            System.out.println("An error occured while exporting the graph.");
        }
    }

    public  void runAlgorithms() {
        this.mainFrame.initSidePanel.removeDefaultListeners();
        LayoutUtilities.applyLayout(this.mainFrame.graph, new OrganicLayout());
        this.mainFrame.initSidePanel.addDefaultListeners();


        if (!this.tab.isPresent()) {
            return;
        }
        this.tab.get().getExecutor().setMaxIterations(maxIterations);
        long tmpTime = System.currentTimeMillis();
        this.tab.get().startPauseExecution();
        boolean value = true;

        //while(!tab.get().getExecutor().isFinished()){
        while(!this.tab.get().getExecutor().isFinished() && this.tab.get().getExecutor().isRunning() && !this.isInInvLoop){
           // System.out.println("++ while");
            if(System.currentTimeMillis() - tmpTime > maxTimeForAlgo){
                this.isInInvLoop = true;
                this.tab.get().stopExecution();
                System.out.println("Catched in a inv. Loop. The calculation will be restarted.");
            }
        }
        this.positions = this.tab.get().getExecutor().getLayout().getNodePositions();
        System.out.println(fileName + "OUT 13");
        this.endTime = endTime + (System.currentTimeMillis() - tmpTime);
    }



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
        this.iterations += this.maxIterations;

        mainFrame.minimumAngleMonitor.updateMinimumAngleInfoBar();

        double actAngle = Math.round(1000.0 * mainFrame.minimumAngleMonitor.getMinAngle()) / 1000.0;
        if(this.minimumAngle >= actAngle){
            this.numOfUnchangedAngle++;
        }else{
            this.minimumAngle = actAngle;
            this.minimumAngleTime = this.calcTime;
            this.minimumAngleIterations = this.iterations;
            this.numOfUnchangedAngle = 0;
        }

        if(this.minimumAngle >= 90.0){
            reached90Degree = true;
        }

        this.testAngle = mainFrame.minimumAngleMonitor.getMinAngle();
        //outputTextArea.setText(graphInfo.toString());


        this.numOfCrossings = MinimumAngle.getCrossings(this.graph, this.positions).size();

        return reached90Degree;

    }

    private String getPrefixString(String str){
        int position = str.lastIndexOf(".");
        //System.out.println(str.substring(0, position));
        return str.substring(0, position);
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

    public void setMaxIterations(int maxIta){
        this.maxIterations = maxIta;
    }

    public long getCalcTime(){return this.calcTime;}

    public boolean getIsInInvLoop(){return this.isInInvLoop;}

    public int getNumOfUnchangedAngle(){return this.numOfUnchangedAngle;}

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

        graphInfo.append("\nMin. Angle: ").append(mainFrame.minimumAngleMonitor.getMinAngle()).append("\n");
        mainFrame.minimumAngleMonitor.updateMinimumAngleInfoBar();
        double minAngle = mainFrame.minimumAngleMonitor.getMinAngle();
        graphInfo.append("\nMin. Angle: ").append(minAngle).append("\n");
        if(minAngle>=87.0){
            reached90Degree = true;
        }

        graphInfo.append("\nCalc Time: ").append(this.endTime - this.sartTime).append("\n");


        System.out.println(graphInfo);

        //outputTextArea.setText(graphInfo.toString());
        return reached90Degree;

    }

}
