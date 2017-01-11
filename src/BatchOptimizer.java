
import algorithms.graphs.GridPositioning;
import algorithms.graphs.MinimumAngle;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import layout.algo.event.AlgorithmEvent;
import layout.algo.event.AlgorithmListener;
import util.*;
import layout.algo.*;

import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.view.*;
import com.yworks.yfiles.view.input.*;
import com.yworks.yfiles.geometry.PointD;
import util.graph2d.Intersection;
import util.graph2d.LineSegment;

import java.time.Duration;
import java.util.*;
import java.util.function.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;

public class BatchOptimizer {
  static GraphComponent view;
  static IGraph graph;
  static int rounds = 100;
  static int initTime = 100;
  public static Double[] springThreshholds = new Double[]{0.01, 0.01, 0.01, 0.1};
  public static Boolean[] algoModifiers = new Boolean[]{false, false};
  public static void tryParse(String content, Consumer<Integer> field, String fieldName){
    try{
      field.accept(Integer.parseInt(content));
    }
    catch(Exception e){
      printUsage();
      System.out.println("Error parsing " + content + " as " + fieldName + ".");            
    }
  }
  public static void parseArgs(String[] args, List<Tuple3<Predicate<String>, Consumer<Integer>, String>> matchArgs){
    for(int i = 0; i < args.length - 1; i++){
      String testString = args[i];
      for(Tuple3<Predicate<String>, Consumer<Integer>, String> matchArg: matchArgs){
        if(matchArg.a.test(testString)){
          tryParse(args[i+1], matchArg.b, matchArg.c);
          break;
        }
      }
    }
  }

  public static void printUsage(){
    String msg = "Usage:\n"
    + "\tjava BatchOptimizer [-i init-time] [-r rounds] path-to-rome-graphs\n";
    System.out.println(msg);
  }
  public static ForceAlgorithmApplier defaultForceAlgorithmApplier(int iterations){
    ForceAlgorithmApplier fd = InitForceAlgorithm.defaultForceAlgorithmApplier(iterations, view, Maybe.nothing(), Maybe.nothing());
    fd.modifiers = springThreshholds.clone();
    fd.switches = algoModifiers.clone();
    return fd;
  }
  public static void main(String[] args) throws IOException {
    String path = "";
    if(args.length <= 0){
      printUsage();
      return;
    }
    path = args[args.length - 1];
    List<Tuple3<Predicate<String>, Consumer<Integer>, String>> parsers = new LinkedList<>();
    parsers.add(new Tuple3<>(s -> s == "-r", v -> rounds = v, "rounds count"));
    parsers.add(new Tuple3<>(s -> s == "-f", v -> initTime = v, "time"));
    parseArgs(args, parsers);
    
    view = new GraphComponent();
    graph = view.getGraph();
    
    Path romeFolder = Paths.get(path);
    String outFolder = path + File.separator + "out";
    File file = new File(outFolder);
    if(!file.isDirectory()){
      file.mkdir();
    }
    DirectoryStream<Path> romeFolderStream = Files.newDirectoryStream(romeFolder, "*.graphml");
    for(Path romeGraph: romeFolderStream){
      System.out.println(romeGraph);
      String fileName = romeGraph.getFileName().toString();
      Path outFile = Paths.get(outFolder + File.separator + fileName);
      runAlgo(romeGraph, outFile);
    }

  }
  public static void runAlgo(Path romeGraph, Path outFile) throws IOException {
    view.importFromGraphML(romeGraph.toFile());
    LayoutUtilities.applyLayout(graph, new OrganicLayout());
    /*ForceDirectedAlgorithm fd = new ForceDirectedAlgorithm(view, 1000) {
      public void calculateVectors() {
        ForceDirectedFactory.calculateSpringForcesEades(graph, 150, 100, 0.01, map);
        ForceDirectedFactory.calculateElectricForcesEades(graph, 50000, 0.01, map);
      }
    };*/


    /*ForceAlgorithmApplier.init();
    ForceAlgorithmApplier firstFAA = defaultForceAlgorithmApplier(initTime);
    GeneticAlgorithm ga = InitGeneticAlgorithm.defaultGeneticAlgorithm(firstFAA, graph, view, Maybe.nothing());
    ga.runRounds(rounds);
    ForceAlgorithmApplier.bestSolution.andThen(nm_mca_da_ba -> {
      IMapper<INode, PointD> nodePositions = nm_mca_da_ba.a;
      ForceAlgorithmApplier.applyNodePositionsToGraph(graph, nodePositions);
    });
    System.out.println(outFile);
    view.exportToGraphML(Files.newOutputStream(outFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE));*/

    Maybe<Tuple3<LineSegment, LineSegment, Intersection>>
            minAngleCr = MinimumAngle.getMinimumAngleCrossing(graph, Maybe.nothing());
    double initialAngle = 0.0,
            optimizedAngle = 0.0;
    if(minAngleCr.hasValue()){ initialAngle = minAngleCr.get().c.angle; }

    ForceAlgorithmApplier.init();
    ForceAlgorithmApplier firstFAA = defaultForceAlgorithmApplier(rounds);
    firstFAA.runBatch();
    ForceAlgorithmApplier.bestSolution.andThen(nm_mca_da_ba -> {
      IMapper<INode, PointD> nodePositions = nm_mca_da_ba.a;
      ForceAlgorithmApplier.applyNodePositionsToGraph(graph, nodePositions);
    });

    GridPositioning.gridGraph(graph);
    Maybe<Tuple3<LineSegment, LineSegment, Intersection>>
            minAngleOpt = MinimumAngle.getMinimumAngleCrossing(graph, Maybe.nothing());
    if(minAngleOpt.hasValue()){ optimizedAngle = minAngleOpt.get().c.angle; }
    view.exportToGraphML(Files.newOutputStream(outFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE));
    System.out.println(outFile + " " + "#v: " + graph.getNodes().size()+  " #e: " + graph.getEdges().size() + " minAngleInitial: " + initialAngle + " minAngleOptimized: " + optimizedAngle);
    ;
    //}


                
  }


}