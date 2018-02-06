package cli;

import com.yworks.yfiles.graph.DefaultGraph;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.layout.circular.CircularLayout;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.layout.orthogonal.OrthogonalLayout;
import com.yworks.yfiles.layout.tree.TreeLayout;
import io.ContestIOHandler;
import layout.algo.*;
import layout.algo.genetic.GeneticForceAlgorithmConfigurator;
import layout.algo.genetic.GeneticForceAlgorithmLayout;
import layout.algo.layoutinterface.VoidItemFactory;
import org.apache.commons.cli.*;
import yfilesadapter.YFilesLayoutAdapter;

import java.io.IOException;
import java.util.Arrays;

public class CommandLineInterface {
  private static final String ALGORITHMS_PARAMETER = "algorithms";
  private static final String GRAPHS_PARAMETER = "graphs";
  private static final String ITERATIONS_PARAMETER = "iterations";
  /*
  TODO:
   - stats
   */

  private static Options createCLIOptions() {
    Options options = new Options();
    options.addOption(Option.builder()
        .hasArgs()
        .required()
        .longOpt(GRAPHS_PARAMETER)
        .desc("A list of graph files [*.txt]")
        .build());
    options.addOption(Option.builder()
        .hasArgs()
        .longOpt(ALGORITHMS_PARAMETER)
        .desc("A list of algorithms that should be applied to each graph in order. Available algorithms: TODO")
        .build());
    options.addOption(Option.builder()
        .hasArg()
        .numberOfArgs(1)
        .optionalArg(true)
        .longOpt(ITERATIONS_PARAMETER)
        .desc("Maximum number of iterations for each algorithm")
        .build());
    return options;
  }

  public static void main(String[] args) {
    Options options = createCLIOptions();
    CommandLine cmd;
    try {
      cmd = parseCLIOptions(args, options);
    } catch (ParseException e) {
      System.out.println(e.getLocalizedMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("CommandLineInterface --graphs [GRAPH_FILE]... --algorithms [ALGORITHM]...", options);
      return;
    }

    String[] graphFiles = cmd.getOptionValues(GRAPHS_PARAMETER);
    String[] algorithms;
    if (cmd.hasOption(ALGORITHMS_PARAMETER)) {
      algorithms = cmd.getOptionValues(ALGORITHMS_PARAMETER);
    } else {
      algorithms = new String[]{"organic", "random_movement"};
    }

    String iterationsString = cmd.getOptionValue(ITERATIONS_PARAMETER, "1000");
    int iterations = Integer.parseInt(iterationsString);

    run(graphFiles, algorithms, iterations);
  }

  private static void run(String[] graphFiles, String[] algorithms, int iterations) {
    Arrays.stream(graphFiles)
        .parallel()
        .map(CommandLineInterface::loadGraph)
        .peek(fileData -> runAlgorithms(algorithms, fileData, iterations))
        .forEach(CommandLineInterface::writeGraph);
  }

  private static FileData loadGraph(String file) {
    IGraph graph = new DefaultGraph();
    try {
      ContestIOHandler.read(graph, file);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new FileData(file, graph);
  }

  private static void runAlgorithms(String[] algorithms, FileData fileData, int iterations) {
    Arrays.stream(algorithms)
        .forEach(algorithmName -> {
          ILayout algorithm = getAlgorithm(algorithmName, fileData.graph);
          BasicIGraphLayoutExecutor executor =
              new BasicIGraphLayoutExecutor(algorithm, fileData.graph, iterations, iterations);
          executor.start();
          executor.waitUntilFinished();
        });
  }

  private static void writeGraph(FileData fileData) {
    try {
      ContestIOHandler.write(fileData.graph, fileData.fileName.substring(0, fileData.fileName.length() - ".txt".length()) + "-cli.txt");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static ILayout getAlgorithm(String name, IGraph graph) {
    VoidItemFactory itemFactory = new VoidItemFactory();
    switch (name) {
      case "organic":
        return new YFilesLayoutAdapter(graph, new OrganicLayout());
      case "orthogonal":
        return new YFilesLayoutAdapter(graph, new OrthogonalLayout());
      case "tree":
        return new YFilesLayoutAdapter(graph, new TreeLayout());
      case "circular":
        return new YFilesLayoutAdapter(graph, new CircularLayout());

      case "random_movement":
        RandomMovementConfigurator configurator = new RandomMovementConfigurator();
        configurator.init(itemFactory);
        return new RandomMovementLayout(graph, configurator);
      case "force":
        return InitForceAlgorithm.defaultForceAlgorithm(graph, itemFactory);
      case "genetic":
        GeneticForceAlgorithmConfigurator geneticConfigurator = new GeneticForceAlgorithmConfigurator();
        geneticConfigurator.init(itemFactory);
        return new GeneticForceAlgorithmLayout(geneticConfigurator, graph);
      case "clinch":
        ClinchLayoutConfigurator clinchLayoutConfigurator = new ClinchLayoutConfigurator();
        clinchLayoutConfigurator.init(itemFactory);
        return new ClinchLayout(clinchLayoutConfigurator, graph);
    }
    return null;
  }

  private static CommandLine parseCLIOptions(String[] args, Options options) throws ParseException {
    CommandLineParser parser = new DefaultParser();
    return parser.parse(options, args);
  }

  private static class FileData {
    String fileName;
    IGraph graph;

    FileData(String fileName, IGraph graph) {
      this.fileName = fileName;
      this.graph = graph;
    }
  }
}
