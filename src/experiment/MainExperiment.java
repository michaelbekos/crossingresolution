package experiment;

/**
 * Created by Ama on 27.04.2018.
 */

public class MainExperiment {
    static String inputDir = "E:\\graph\\rome\\rome\\";
    static String outputDir = "E:\\graph\\results\\TESTrome290518\\";

    public static void main(String[] args) {
        //Experiments e = new Experiments();

        // Experiments e = new Experiments(("E:\\graph\\graphml\\"), "E:\\graph\\afterRandom\\graphml2\\");

      Experiments e = new Experiments(inputDir, outputDir,
              Long.MAX_VALUE, 100, 1000, 10000,
              false, false, true, true);
       e.runOnlyRandom();

       e = new Experiments(inputDir, outputDir,
               Long.MAX_VALUE, 100, 1000, 10000,
               false, false, true, true);
        e.runOnlyForce();

        e = new Experiments(inputDir, outputDir,
                Long.MAX_VALUE, 100, 1000, 10000,
                false, false, true, true);
        e.runOnlyTotalResolutionForce();

    }
}
