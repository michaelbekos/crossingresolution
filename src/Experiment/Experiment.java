package Experiment;

import algorithms.graphs.GridGraph;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.LayoutUtilities;
import com.yworks.yfiles.graphml.GraphMLIOHandler;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import io.ContestIOHandler;
import io.GraphIOHandler;
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
    private long sartTime, endTime, calcTime;
    private boolean reached90Degree = false;
    private PrintWriter pw;
    private StringBuilder sb;
    private int maxIterations = 1000;
    private int iterations = 0;
    private double bestAngle = 0;

    private int totalVertecies, totalEdges;
    private double currentGraphSizeX, currentGraphSizeY, minimumAngle, testAngle;

    public  Experiment (MainFrame mainFrame, String fileName, String inputDir, String outputDir) {
        this.mainFrame = mainFrame;
        this.graph = mainFrame.graph;
        this.fileName = fileName;
        this.filePrefix = getPrefixString(fileName);
        this.inputDirectory = inputDir;
        this.outputDirectory = outputDir;
        this.calcTime = 0;

        //sb = new StringBuilder();

        try {
            pw = new PrintWriter(new File(outputDirectory + "\\" + filePrefix + ".csv"));

            sb = new StringBuilder();
            sb.append("Graph Name");
            sb.append(';');
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

    public void writeGraphInformations(){
        sb = new StringBuilder();
        sb.append(this.filePrefix);
        sb.append(';');
        sb.append(this.calcTime);
        sb.append(';');
        sb.append(this.iterations);
        sb.append(';');
        sb.append(this.minimumAngle);
        sb.append('\n');
        //System.out.println("write Graph " + this.minimumAngle);

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
        //System.out.println("write Graph " + this.minimumAngle);

        Path path = FileSystems.getDefault().getPath(outputDirectory , "\\" + filePrefix +".csv");
        try {
            Files.write(path, sb.toString().getBytes(), StandardOpenOption.APPEND);
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

        if (this.fileName.endsWith(".txt")){
            this.mainFrame.openContestFile(this.inputDirectory + this.fileName);
            System.out.println("Try to open .txt");
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
        //System.out.println("++ 1");
        this.mainFrame.initSidePanel.removeDefaultListeners();
        // System.out.println("++ 2");
        LayoutUtilities.applyLayout(this.mainFrame.graph, new OrganicLayout());
        //System.out.println("++ 3");
        this.mainFrame.initSidePanel.addDefaultListeners();
        //System.out.println("++ 4");
        Optional<SidePanelTab> tab = this.mainFrame.getTabForAlgorithm(RandomMovementLayout.class);
        // System.out.println("++ 5");
        if (!tab.isPresent()) {
            return;
        }
        //System.out.println("++ 6");
        tab.get().getExecutor().setMaxIterations(maxIterations);
        //System.out.println("++ 7");
        long tmpTime = System.currentTimeMillis();
        // System.out.println("++ 8");
        tab.get().startPauseExecution();
        boolean value = true;

       // System.out.println("++ 8.5");
        //while(!tab.get().getExecutor().isFinished()){
        while(!tab.get().getExecutor().isFinished() && tab.get().getExecutor().isRunning()){
           // System.out.println("++ while");
        }
        //System.out.println("++ 9");
        this.endTime = endTime + (System.currentTimeMillis() - tmpTime);
        // System.out.println("++ 10");
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


        if(this.minimumAngle>=90.0){
            reached90Degree = true;
        }

        this.calcTime = this.endTime - this.sartTime;
        this.iterations += this.maxIterations;

        mainFrame.minimumAngleMonitor.updateMinimumAngleInfoBar();
        this.minimumAngle = Math.round(1000.0 * mainFrame.minimumAngleMonitor.getMinAngle()) / 1000.0;
        this.testAngle = mainFrame.minimumAngleMonitor.getMinAngle();
        //outputTextArea.setText(graphInfo.toString());
        return reached90Degree;

    }

    private String getPrefixString(String str){
        int position = str.lastIndexOf(".");
        //System.out.println(str.substring(0, position));
        return str.substring(0, position);
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

    public long getCalcTime(){return this.calcTime;};

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
