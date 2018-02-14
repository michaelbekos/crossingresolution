package main;

import com.yworks.yfiles.graph.LayoutUtilities;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import layout.algo.BasicIGraphLayoutExecutor;
import layout.algo.RandomMovementLayout;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.IntStream;

public class Main {
  private static volatile int openFrames;

  public static void main(String[] args) {
    if (args.length == 0) {
      runSingleFrame();
      return;
    }

    if (args.length > 4
        || args.length == 1 && args[0].equals("--help") || args[0].equals("-h")) {
      printUsage();
      return;
    }

    int startIndex = 1;
    int endIndex = 1;
    String folderPath = "contest-2017";
    String pattern = "automatic-$$$.txt";

    if (args.length >= 2) {
      try {
        startIndex = Integer.parseInt(args[0]);
        endIndex = Integer.parseInt(args[1]);
      } catch (NumberFormatException e) {
        System.out.println("Could not parse indices");
        printUsage();
      }
    }

    if (args.length >= 3) {
      folderPath = args[2];
      Path path = Paths.get(folderPath);
      if (Files.notExists(path) || !Files.isDirectory(path)) {
        System.out.println("Could not open directory " + folderPath);
        printUsage();
      }
    }

    if (args.length == 4) {
      pattern = args[3];
      if (!pattern.contains("$$$")) {
        System.out.println("Pattern must contain three $$$!");
        printUsage();
      }
    }

    openFrames(startIndex, endIndex, folderPath, pattern);
  }

  private static void runSingleFrame() {
    MainFrame.start(WindowConstants.EXIT_ON_CLOSE, null);
  }

  private static void openFrames(int startIndex, int endIndex, String folderPath, String pattern) {
    IntStream.range(startIndex, endIndex + 1)
        .parallel()
        .forEach(index -> MainFrame.start(WindowConstants.DISPOSE_ON_CLOSE, mainFrame -> {
          addClosingListener(mainFrame);
          loadGraph(mainFrame, folderPath, pattern, index);
          runAlgorithms(mainFrame);
        }));
  }

  private static void addClosingListener(MainFrame mainFrame) {
    openFrames++;
    mainFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent evt) {
        openFrames--;
        if (openFrames == 0) {
          System.exit(0);
        }
      }
    });
  }

  private static void loadGraph(MainFrame mainFrame, String folderPath, String pattern, int index) {
    String fileName = pattern.replace("$$$", Integer.toString(index));
    mainFrame.openContestFile(Paths.get(folderPath, fileName).toString());
  }

  private static void runAlgorithms(MainFrame mainFrame) {
    LayoutUtilities.applyLayout(mainFrame.graph, new OrganicLayout());

    Optional<BasicIGraphLayoutExecutor> executor = mainFrame.getExecutorForAlgorithm(RandomMovementLayout.class);
    if (!executor.isPresent()) {
      return;
    }

    executor.get().start();
  }

  private static void printUsage() {
    System.out.println("Usage: run [startIndex] [endIndex] [folderPath] [pattern]\n"
        + "\tstartIndex, endIndex    Indices of the first and last file to open (inclusive)\n"
        + "\tfolderPath              Relative or absolute Path to the folder that contain the graph files\n"
        + "\tpattern                 A pattern for the graph file names. Uses this syntax: graph-$$$.txt, where\n"
        + "\t                        the $$$ will be replaced by the index of the graph\n");
  }
}