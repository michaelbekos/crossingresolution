package cli;

import com.yworks.yfiles.graph.DefaultGraph;
import com.yworks.yfiles.graph.IGraph;
import io.ContestIOHandler;
import layout.algo.BasicIGraphLayoutExecutor;
import layout.algo.ILayout;
import layout.algo.RandomMovementConfigurator;
import layout.algo.RandomMovementLayout;
import layout.algo.layoutinterface.VoidItemFactory;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.Arrays;

public class CommandLineInterface {
  private static final String ALGORITHMS_PARAMETER = "algorithms";
  private static final String GRAPHS_PARAMETER = "graphs";
  /*
  TODO:
   - blobs
   - iterations as CLI parameter
   - stats
   */

  private static Options createCLIOptions() {
    Options options = new Options();
    options.addOption(Option.builder()
        .hasArgs()
        .required()
        .longOpt(GRAPHS_PARAMETER)
        .desc("A list of graph files [*.txt]. Blobs are supported")
        .build());
    options.addOption(Option.builder()
        .hasArgs()
        .longOpt(ALGORITHMS_PARAMETER)
        .desc("A list of algorithms that should be applied to each graph in order. Available algorithms: TODO")
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

    run(graphFiles, algorithms);
  }

  private static void run(String[] graphFiles, String[] algorithms) {
    Arrays.stream(graphFiles)
        .parallel()
        .map(CommandLineInterface::loadGraph)
        .peek(fileData -> runAlgorithms(algorithms, fileData))
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

  private static void runAlgorithms(String[] algorithms, FileData fileData) {
    Arrays.stream(algorithms)
        .forEach(algorithmName -> {
          ILayout algorithm = getAlgorithm(algorithmName, fileData.graph);
          BasicIGraphLayoutExecutor executor =
              new BasicIGraphLayoutExecutor(algorithm, fileData.graph, 1000, 1000);
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
      // TODO:
      case "organic":
        return null;
      case "random_movement":
        RandomMovementConfigurator configurator = new RandomMovementConfigurator();
        configurator.init(itemFactory);
        return new RandomMovementLayout(graph, configurator);
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
