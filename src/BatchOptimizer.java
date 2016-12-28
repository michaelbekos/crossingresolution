
import util.*;
import layout.algo.*;

import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.view.*;
import com.yworks.yfiles.view.input.*;
import com.yworks.yfiles.geometry.PointD;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;

public class BatchOptimizer {
  static GraphComponent view;
  static IGraph graph;
  static int rounds = 100;
  public static Double[] springThreshholds = new Double[]{0.01, 0.01, 0.01, 0.1};
  public static Boolean[] algoModifiers = new Boolean[]{false, false};

  public static void printUsage(){
    String msg = "Usage:\n"
    + "\tjava BatchOptimizer [-r rounds] path-to-rome-graphs\n";
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
    else {
      path = args[args.length - 1];
      for(int i = 0; i < args.length - 1; i++){
        if(args[i] == "-r"){
          try{
            rounds = Integer.parseInt(args[i + 1]);
          }
          catch(Exception e){
            printUsage();
            System.out.println("Error parsing " + args[i + 1] + " as rounds count.");            
          }
        }
      }
    }
    view = new GraphComponent();
    graph = view.getGraph();
    
    Path romeFolder = Paths.get(path);
    String outFolder = path + File.separator + "out";
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
    ForceAlgorithmApplier.init();
    ForceAlgorithmApplier firstFAA = defaultForceAlgorithmApplier(100);
    GeneticAlgorithm ga = InitGeneticAlgorithm.defaultGeneticAlgorithm(firstFAA, graph, view, Maybe.nothing());
    ga.runRounds(rounds);
    ForceAlgorithmApplier.bestSolution.andThen(nm_mca_da_ba -> {
      IMapper<INode, PointD> nodePositions = nm_mca_da_ba.a;
      ForceAlgorithmApplier.applyNodePositionsToGraph(graph, nodePositions);
    });
    System.out.println(outFile);
    view.exportToGraphML(Files.newOutputStream(outFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE));
                
  }

}