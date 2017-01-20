
import com.yworks.yfiles.layout.orthogonal.OrthogonalLayout;
import util.GridPositioning;
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
  static BufferedWriter out;
  public static Double[] springThreshholds = new Double[]{0.1, 0.01, 0.4, 0.1};
  static boolean forceAlgoOnly = false;
  public static Boolean[] algoModifiers = new Boolean[]{false, false};
  static boolean usagePrinted = false;
  /**
   * tryParse tries to parse content as Int, on success passes the result to field, on failure prints an error message
   */
  public static void tryParse(String content, Consumer<Integer> field, String fieldName){
    try{
      field.accept(Integer.parseInt(content));
    }
    catch(Exception e){
      if(!usagePrinted){
        printUsage();
        usagePrinted = true;
      }
      System.out.println("Error parsing " + content + " as " + fieldName + ".");            
    }
  }

  /**
   * parseArgs gets a list of parsers as it's second argument. A parser is a tuple of:
   * - a predicate deciding whether an arg matches
   * - a callback on match
   * - an error message, should further parsing fail
   * The callback is one of:
   * - a Runnable, should the predicate succeed (for example for setting flags)
   * - an action accepting an integer, which is the string after the matching arg
   */
  public static void parseArgs(String[] args, List<Tuple3<Predicate<String>, Either<Runnable, Consumer<Integer>>, String>> matchArgs){
    // parse all args
    for(int i = 0; i < args.length; i++){
      String testString = args[i];
      // try all parsers, until one succeeds
      for(Tuple3<Predicate<String>, Either<Runnable, Consumer<Integer>>, String> matchArg: matchArgs){
        // test the predicate
        if(matchArg.a.test(testString)){
          // for lambdas, everything must be final -.-
          final int nextArgIndex = i + 1;
          // match on the sum type
          matchArg.b.match(
            // runnable get's run
            left -> left.run(), 
            // consumers consume, if there's something to consume
            (Consumer<Integer> right) -> {
              if(nextArgIndex < args.length) tryParse(args[nextArgIndex], right, matchArg.c);});
          // we successfully parsed, stop here
          break;
        }
      }
    }
  }

  public static void printUsage(){
    String msg = "Usage:\n"
    + "\tjava BatchOptimizer [-i init-time] [-r rounds] [-f] path-to-rome-graphs\n"
    + "\t\t-i init-time: how long one round of ForceAlgorithmApplier should run\n"
    + "\t\t-r rounds:    how many rounds of genetic generations should be run\n"
    + "\t\t-f:           only apply ForceAlgorithmApplier, don't do genetic part\n";
    System.out.println(msg);
  }
  // easier instantiation of FAA
  public static ForceAlgorithmApplier defaultForceAlgorithmApplier(int iterations){
    // we don't care about drawing/callbacks, so Maybe.nothing().
    ForceAlgorithmApplier fd = InitForceAlgorithm.defaultForceAlgorithmApplier(iterations, view, Maybe.nothing(), Maybe.nothing());
    springThreshholds[1] = 50 * Math.log(graph.getNodes().size());
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
    // see parseArgs
    List<Tuple3<Predicate<String>, Either<Runnable, Consumer<Integer>>, String>> parsers = new LinkedList<>();
    parsers.add(new Tuple3<>(s -> s.equals("-r"), Either.right(v -> rounds = v), "rounds count"));
    parsers.add(new Tuple3<>(s -> s.equals("-i"), Either.right(v -> initTime = v), "time"));
    parsers.add(new Tuple3<>(s -> s.equals("-f"), Either.left(() -> forceAlgoOnly = true), "force algorithm only"));
    parseArgs(args, parsers);
    
    view = new GraphComponent();
    graph = view.getGraph();
    
    Path romeFolder = Paths.get(path);
    String outFolder = path + File.separator + "out";
    File file = new File(outFolder);
    if(!file.isDirectory()){
      file.mkdir();
    }
    out = Files.newBufferedWriter(Paths.get(outFolder + File.separator + "romeStats.txt"));
    DirectoryStream<Path> romeFolderStream = Files.newDirectoryStream(romeFolder, "*.graphml");
    for(Path romeGraph: romeFolderStream){
      System.out.println(romeGraph);
      String fileName = romeGraph.getFileName().toString();
      Path outFile = Paths.get(outFolder + File.separator + fileName);
      runAlgo(romeGraph, outFile);
    }
    out.close();

  }
  public static void runAlgo(Path romeGraph, Path outFile) throws IOException {
    view.importFromGraphML(romeGraph.toFile());
    // start with some default layouting, to work around all nodes on one point
    //LayoutUtilities.applyLayout(graph, new OrganicLayout());
    LayoutUtilities.applyLayout(graph, new OrthogonalLayout());

    // do some default metrics
    Maybe<Tuple3<LineSegment, LineSegment, Intersection>>
            minAngleCr = MinimumAngle.getMinimumAngleCrossing(graph, Maybe.nothing());
    // if there is a crossing, map to get the angle, then get it, otherwise "no crossings".
    String initialAngle = minAngleCr.fmap(abc->abc.c.angle.toString()).getDefault("no crossings");
  
    ForceAlgorithmApplier.init();
    ForceAlgorithmApplier firstFAA = defaultForceAlgorithmApplier(initTime);
    
    if(forceAlgoOnly){
      System.out.println("running FAA only");
      firstFAA.runNoDraw();
    }
    else {
      System.out.println("running genetic");
      GeneticAlgorithm ga = InitGeneticAlgorithm.defaultGeneticAlgorithm(firstFAA, graph, view, Maybe.nothing());
      ga.runRounds(rounds);
    }

    // afterwards: apply new positions to graph...
    ForceAlgorithmApplier.bestSolution.andThen(nm_mca_da_ba -> {
      Mapper<INode, PointD> nodePositions = nm_mca_da_ba.a;
      ForceAlgorithmApplier.applyNodePositionsToGraph(graph, nodePositions);
    });
    // ... grid it...
    GridPositioning.simpleGridGraph(graph);
    // ... get metrics...
    // (Maybe (LS, LS, I) --fmap--> Maybe String --getDefault--> String)
    Maybe<Tuple3<LineSegment, LineSegment, Intersection>>
            minAngleOpt = MinimumAngle.getMinimumAngleCrossing(graph, Maybe.nothing());
    double area = computeArea(graph);
    String optimizedAngle = minAngleOpt.fmap(m -> m.c.angle.toString()).getDefault("no crossings");
    // ... export the computed layout...
    view.exportToGraphML(Files.newOutputStream(outFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE));
    // ... show metrics...
    String metrics = outFile 
      + "\t#v\t" + graph.getNodes().size()
      + "\t#e\t" + graph.getEdges().size() 
      + "\tminAngleInitial\t" + initialAngle 
      + "\tminAngleOptimized\t" + optimizedAngle
      + "\tarea\t" + computeArea(graph)
      + "\n";
    System.out.print(metrics);
    // ... and write metrics to file.
    out.write(metrics);
    // flush, so we can safely interrupt
    out.flush();
                
  }

  private static double computeArea(IGraph g){
    double xmin, ymin = xmin = Double.POSITIVE_INFINITY, xmax, ymax = xmax = Double.NEGATIVE_INFINITY;
    for(INode n: g.getNodes()){
      PointD p = n.getLayout().getCenter();
      double x = p.getX();
      double y = p.getX();
      xmin = Math.min(xmin, x);
      ymin = Math.min(ymin, y);
      xmax = Math.max(xmax, x);
      ymax = Math.max(ymax, y);
    }
    return (xmax - xmin) * (ymax - ymin);
  }



}