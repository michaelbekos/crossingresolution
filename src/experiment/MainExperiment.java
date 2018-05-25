package experiment;

/**
 * Created by Ama on 27.04.2018.
 */

public class MainExperiment {

    public static void main(String[] args) {
        //Experiments e = new Experiments();

        // Experiments e = new Experiments(("E:\\graph\\graphml\\"), "E:\\graph\\afterRandom\\graphml2\\");

      Experiments e = new Experiments(
                "E:\\graph\\rome\\rome\\", "E:\\graph\\results\\romeTest20\\",
              Long.MAX_VALUE, 100, 1000, 10000,
                false, false);
      // e.runOnlyRandom();

     e = new Experiments(
                "E:\\graph\\rome\\rome\\", "E:\\graph\\results\\romeTest20\\",
                Long.MAX_VALUE, 100, 1000, 10000,
                false, false);
       // e.runOnlyForce();
        e = new Experiments(
                "E:\\graph\\rome\\rome\\", "E:\\graph\\results\\romeTestTotal\\",
                Long.MAX_VALUE, 100, 1000, 10000,
                false, false);
        e.runOnlyTotalResolutionForce();



     /*       Experiments e = new Experiments(
                    "E:\\graph\\rome\\smallSet\\", "E:\\graph\\results\\rome\\",
                    10000, 100, 1000, 10000,
                    false, false);

            e.run();

    */}
}
