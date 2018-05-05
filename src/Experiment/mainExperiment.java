package Experiment;

/**
 * Created by Ama on 27.04.2018.
 */
public class mainExperiment {
    public static void main(String[] args)
    {
        //Experiments e = new Experiments();

       // Experiments e = new Experiments(("E:\\graph\\graphml\\"), "E:\\graph\\afterRandom\\graphml2\\");
        Experiments e = new Experiments(
                "E:\\graph\\rome\\rome\\", "E:\\graph\\afterRandom\\rome\\",
                10000, 100, 1000,
                false, false);
        e.run();
    }
}
