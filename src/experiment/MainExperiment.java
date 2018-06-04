package experiment;

/**
 * Created by Ama on 27.04.2018.
 */

public class MainExperiment {
    static String inputDir = "E:\\graph\\rome\\rome\\";
    static String outputDir = "E:\\graph\\results\\rome_Angular_01062018\\";

    public static void main(String[] args) {
        //Experiments e = new Experiments();

        // Experiments e = new Experiments(("E:\\graph\\graphml\\"), "E:\\graph\\afterRandom\\graphml2\\");



        //rome

      Experiments e = new Experiments(inputDir, outputDir,
              Long.MAX_VALUE, 100, 1000, 10000,
              false, false, false, true);
    //   e.runOnlyRandom();

       e = new Experiments(inputDir, outputDir,
               Long.MAX_VALUE, 100, 1000, 10000,
               false, false, false, true);
      //  e.runOnlyForce();

        e = new Experiments(inputDir, outputDir,
                Long.MAX_VALUE, 100, 1000, 10000,
                false, false, false, true);
        //e.runOnlyTotalResolutionForce();



        //north


         inputDir = "E:\\graph\\north\\north\\north\\";
         outputDir = "E:\\graph\\results\\north_Crossing_01062018\\";

        e = new Experiments(inputDir, outputDir,
                Long.MAX_VALUE, 100, 1000, 10000,
                false, false, true, false);
        e.runOnlyRandom();

        e = new Experiments(inputDir, outputDir,
                Long.MAX_VALUE, 100, 1000, 10000,
                false, false, true, false);
        e.runOnlyForce();

        e = new Experiments(inputDir, outputDir,
                Long.MAX_VALUE, 100, 1000, 10000,
                false, false, true, false);
        e.runOnlyTotalResolutionForce();

        inputDir = "E:\\graph\\north\\north\\north\\";
        outputDir = "E:\\graph\\results\\north_Angular_01062018\\";

        e = new Experiments(inputDir, outputDir,
                Long.MAX_VALUE, 100, 1000, 10000,
                false, false, false, true);
        e.runOnlyRandom();

        e = new Experiments(inputDir, outputDir,
                Long.MAX_VALUE, 100, 1000, 10000,
                false, false, false, true);
        e.runOnlyForce();

        e = new Experiments(inputDir, outputDir,
                Long.MAX_VALUE, 100, 1000, 10000,
                false, false, false, true);
        e.runOnlyTotalResolutionForce();



        inputDir = "E:\\graph\\north\\north\\north\\";
        outputDir = "E:\\graph\\results\\north_Total_01062018\\";

        e = new Experiments(inputDir, outputDir,
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
