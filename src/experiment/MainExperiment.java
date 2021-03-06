package experiment;

/**
 * Created by Ama on 27.04.2018.
 */

public class MainExperiment {
    static String inputRomeDir = "E:\\graph\\rome\\rome\\";
    static String inputNorthDir = "E:\\graph\\north\\north\\north\\";

    //static String inputRomeDir = "E:\\graph\\rome\\smallSet\\"; // testing
   //  static String inputNorthDir = "E:\\graph\\rome\\smallSet\\"; // testing

    static String outputDir = "E:\\graph\\results\\07_06_2018\\";

    static  Experiments e;

    /** experiments configurations **/
    static long maxCalcTime = Long.MAX_VALUE;
    static int numOfIterationPerStep = 100;
    static int numOfSteps = 1000;
    static int boxSize = 10000;
    static boolean planarGraphsAllowed = false;
    static boolean unconnectedGraphsAllowed = false;
    static boolean useCrossingRes;  // if crossing and angular is enabled, the experiment focus on total resolution
    static boolean useAngularRes;
    static boolean useAspectRatio;

    /** run booleans  **/
    static boolean runRome = true;
    static boolean runNorth = false;
    static boolean runRandomMovement = false;
    static boolean runRandomMovementWithAspectRatio = false;
    static boolean runForce = true; //big angle force
    static boolean runTotalForce = false;



    public static void main(String[] args) {

        /** crossing resolution (CR)**/
        useCrossingRes = true;
        useAngularRes = false;
        run("CR\\");
        /** angular resolution (AR)**/
        useCrossingRes = false;
        useAngularRes = true;
        run("AR\\");
        /** total resolution (TR) **/
        useCrossingRes = true;
        useAngularRes = true;
        run("TR\\");


    }
    public static void run(String mode){
        if(runRome){
            useAspectRatio = false;
            e = new Experiments(inputRomeDir, outputDir + "rome\\"+ mode,
                    maxCalcTime, numOfIterationPerStep, numOfSteps, boxSize,
                    planarGraphsAllowed, unconnectedGraphsAllowed, useCrossingRes, useAngularRes, useAspectRatio);
            if(runRandomMovement)
                e.runOnlyRandom();  //run random without aspect ratio
            if(runForce)
                e.runOnlyForce();
            if(runTotalForce)
                e.runOnlyTotalResolutionForce();
            if(runRandomMovementWithAspectRatio){
                useAspectRatio = true;
                e = new Experiments(inputRomeDir, outputDir + "rome\\"+ mode,
                        maxCalcTime, numOfIterationPerStep, numOfSteps, boxSize,
                        planarGraphsAllowed, unconnectedGraphsAllowed, useCrossingRes, useAngularRes, useAspectRatio);
                e.runOnlyRandom(); //run random  movement with aspect ratio
            }

        }
        if(runNorth){
            useAspectRatio = false;
            e = new Experiments(inputNorthDir, outputDir + "north\\"+ mode,
                    maxCalcTime, numOfIterationPerStep, numOfSteps, boxSize,
                    planarGraphsAllowed, unconnectedGraphsAllowed, useCrossingRes, useAngularRes, useAspectRatio);
            if(runRandomMovement)
                e.runOnlyRandom();  //run random without aspect ratio
            if(runForce)
                e.runOnlyForce();
            if(runTotalForce)
                e.runOnlyTotalResolutionForce();
            if(runRandomMovementWithAspectRatio){
                useAspectRatio = true;
                e = new Experiments(inputNorthDir, outputDir + "north\\"+ mode,
                        maxCalcTime, numOfIterationPerStep, numOfSteps, boxSize,
                        planarGraphsAllowed, unconnectedGraphsAllowed, useCrossingRes, useAngularRes, useAspectRatio);
                e.runOnlyRandom(); //run random  movement with aspect ratio
            }
        }
    }
}
