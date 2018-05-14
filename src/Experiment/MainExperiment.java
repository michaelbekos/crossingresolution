package Experiment;

/**
 * Created by Ama on 27.04.2018.
 */
public class MainExperiment {
    public static void main(String[] args) {
        //Experiments e = new Experiments();

        // Experiments e = new Experiments(("E:\\graph\\graphml\\"), "E:\\graph\\afterRandom\\graphml2\\");

        Experiments e = new Experiments(
                "E:\\graph\\rome\\smallSet\\", "E:\\graph\\experiment\\rome\\smallSet\\random\\",
                10000, 100, 1000, 10000,
                false, false);
        e.runOnlyRandom();

        e = new Experiments(
                "E:\\graph\\rome\\smallSet\\", "E:\\graph\\experiment\\rome\\smallSet\\force\\",
                10000, 100, 1000, 10000,
                false, false);
        e.runOnlyForce();

    }

/*        for(int i = 0; i < 2; i++){
            Experiments e = new Experiments(
                    "E:\\graph\\rome\\rome\\", "E:\\graph\\afterRandom\\rome\\",
                    10000, 100, 1000, (10000 * (10 * i)) ,
                    false, false);

            e.run();
        }
*/


}
