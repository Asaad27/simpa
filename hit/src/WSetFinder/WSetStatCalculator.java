package WSetFinder;

import automata.Automata;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.OutputSequence;
import drivers.mealy.MealyDriver;
import drivers.mealy.transparent.RandomMealyDriver;
import examples.mealy.RandomMealy;
import learner.mealy.noReset.NoResetLearner;
import main.simpa.Options;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by jean Bouvattier on 20/03/16.
 */
public class WSetStatCalculator {

    private static PrintWriter writer;

    public static void main(String[] args) {
        generation();
        if(true) return;
        if (args.length != 1) {
            System.out.println(" usage : bin arg\n arg : dest cvs file");
            return;
        }
        try {
            writer = new PrintWriter(args[0], "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        //Uncomment to have wSet size statistics

         //List<ResultSet> resultSets = computeWSetSizeStat(2, 20, 2, 2, 2, 2, 10);
         //exportWSetSizeStat(resultSets);

        // Uncomment to have full array of state

        //for (ResultSet result: resultSets) {
        //    result.printResult(writer);
        //}

        // Uncomment to try the wSetGuesser
        Options.MININPUTSYM = 2;
        Options.MAXINPUTSYM = 2;
        Options.MINOUTPUTSYM = 2;
        Options.MAXOUTPUTSYM = 2;
        IOStats ioStats = computeIOStat(new RandomMealyDriver(),Options.MAXSTATES);
        ioStats.getResultMap();
        exportIOStats(computeIOStats(2,20,2,2,2,2,200));
        writer.close();

    }

    /**
     * From a set of calculation result, print with the writer a
     * VCS file containing statistics linking number of state and sizes of
     * w-set.
     * warning : does not take account of number of possible inputs and outputs
     * @param resultSets List of results from w-set computation
     */
    private static void exportWSetSizeStat(List<ResultSet> resultSets) {
        HashMap<Integer, Integer> nbTest = new HashMap<>();
        HashMap<Integer, HashMap<Integer, Integer>> wSetLengths = new HashMap<>();
        for (ResultSet result : resultSets) {
            Integer nbState = result.getNbStates();
            if (!nbTest.containsKey(result.getNbStates())) {
                nbTest.put(nbState, 0);
                wSetLengths.put(nbState, new HashMap<>());
                wSetLengths.get(nbState);
            }
            nbTest.replace(nbState, nbTest.get(nbState) + 1);
            HashMap<Integer, Integer> nbStateTable = wSetLengths.get(nbState);
            if (!nbStateTable.containsKey(result.getwSetSize())) {
                nbStateTable.put(result.getwSetSize(), 0);
            }
            nbStateTable.replace(result.getwSetSize(), nbStateTable.get(result.getwSetSize()) + 1);
        }
        HashMap<Integer, ArrayList> stats = new HashMap<>();
        writer.println("nbState,number of test,1,2,3,4");
        for (Integer nbState : nbTest.keySet()) {
            HashMap<Integer, Integer> results = wSetLengths.get(nbState);
            writer.print(nbState + "," + nbTest.get(nbState));
            for (int i = 1; i < 6; i++) {
                writer.print(",");
                if (results.containsKey(i)) {
                    String result = Float.toString((float) results.get(i) / (float) nbTest.get(nbState));
                    result = result.replace('.', ',');
                    writer.print("\"" + result + "\"");
                } else {
                    writer.print("0");
                }
            }
            writer.print("\n");
        }
    }

    /**
     *
     * compute large number of test on calculation of characterisation set
     * @param stateMin : minimum number of state
     * @param stateMax : maximum number of state
     * @param inputMin : minimum number of possible input
     * @param inputMax : maximum number of possible input
     * @param outputMin : minimum number of possible output
     * @param outputMax :  maximum number of possible output
     * @param nbTry : for each scenario having a fixed number of state, input, output, how many time
     *              the computing of characterisation set is made
     * @return a list of ResultSet each containing a number of input, output, state
     * and the size of computed w-set
     */
    private static List<ResultSet> computeWSetSizeStat(
            int stateMin, int stateMax, int inputMin, int inputMax, int outputMin, int outputMax, int nbTry) {
        List<ResultSet> resultSets = new ArrayList<>();
        for (int i = stateMin; i <= stateMax; i++) {
            System.out.println("nb states = " + i);
            Options.MINSTATES = i;
            Options.MAXSTATES = i;
            for (int j = inputMin; j <= inputMax; j++) {
                Options.MININPUTSYM = j;
                Options.MAXINPUTSYM = j;
                for (int k = outputMin; k <= outputMax; k++) {
                    for (int l = 0; l < nbTry; l++) {
                        Options.MINOUTPUTSYM = k;
                        Options.MAXOUTPUTSYM = k;
                        try {
                            RandomMealyDriver driver = new RandomMealyDriver();
                            List<InputSequence> wSet = NoResetLearner.computeCharacterizationSet(driver);
                            resultSets.add(new ResultSet(i, j, k, wSet.size()));
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                    }
                }
            }
        }
        return resultSets;
    }


    /**
     * send random inputs sequences and link them to differents ouputs
     * @param driver : driver to a blackbox mealy automata
     * @return
     */
    public static IOStats computeIOStat(MealyDriver driver, int nbState) {
        List<String> inputSymbols = driver.getInputSymbols();
        //store intput/output statistics
        HashMap<InputSequence,HashMap<OutputSequence,Integer>> resultMap = new HashMap<>();
        HashMap<InputSequence,Integer> nbTest = new HashMap<>();
        Random random = new Random();
        int size = inputSymbols.size();
        InputSequence inputLog = new InputSequence();
        OutputSequence outputLog = new OutputSequence();
        for(int i = 0; i < 3000; i++){
            //pick a random input
            String input = inputSymbols.get(random.nextInt(inputSymbols.size()));
            String output = driver.execute(input);
            inputLog.addInput(input);
            outputLog.addOutput(output);
            //find every input sequence statistic to update.
            for(int j = Math.max(0,i - 6 + 1); j <= i; j++){
                InputSequence inputSequence = inputLog.getIthSuffix(j);
                OutputSequence outputSequence = outputLog.getIthSuffix(j);
                if(!nbTest.containsKey(inputSequence)){
                    nbTest.put(inputSequence,0);
                    resultMap.put(inputSequence, new HashMap<>());
                }
                HashMap<OutputSequence,Integer> inputStats = resultMap.get(inputSequence);
                if(!inputStats.containsKey(outputSequence)){
                    inputStats.put(outputSequence,0);
                    // a single sequence destinguish all states, this is a success
                    if(inputStats.size() == nbState){
                        IOStats result = new IOStats(nbTest,resultMap,nbState);
                        result.setHasDistinguishingSequence(true);
                        nbTest.put(inputSequence,nbTest.get(inputSequence) + 1);
                        inputStats.put(outputSequence,inputStats.get(outputSequence)+1);
                        return result;
                    }
                }
                nbTest.put(inputSequence,nbTest.get(inputSequence) + 1);
                inputStats.put(outputSequence,inputStats.get(outputSequence)+1);
            }
        }
        return new IOStats(nbTest,resultMap,nbState);
    }

    /**
     * compute IOStat on a large number of case
     * @params min/max: range of tests
     * @param nbTry : for each triplet (nbStates,NbInput,nbOutput) the algorithme performs nbTry computation
     * @return List of Input/output statistics for each driver tested.
     */
    private static List<IOStats> computeIOStats(
            int stateMin, int stateMax, int inputMin, int inputMax, int outputMin, int outputMax, int nbTry){
        List<IOStats> resultSets = new ArrayList<>();
        for (int i = stateMin; i <= stateMax; i++) {
            System.out.println("nb states = " + i);
            for (int j = inputMin; j <= inputMax; j++) {
                for (int k = outputMin; k <= outputMax; k++) {
                    for (int l = 0; l < nbTry; l++) {
                        try {
                            Options.MINSTATES = i;
                            Options.MAXSTATES = i;
                            Options.MININPUTSYM = j;
                            Options.MAXINPUTSYM = j;
                            Options.MINOUTPUTSYM = k;
                            Options.MAXOUTPUTSYM = k;
                            RandomMealyDriver driver = new RandomMealyDriver();
                            resultSets.add(computeIOStat(driver,i));
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                    }
                }
            }
        }
        return resultSets;
    }

    /**
     * export to a VCS file the results from testing of blackbox
     * link to a number of state.
     * @param resultSets result from blackBoxs I/O tests
     */
    private static void exportIOStats(List<IOStats> resultSets){
        //does link the nb of state to an hashmap
        // this hashMap link the size of the most distinguishing sequence to its frequency
        // eg {(2,4)(3,2)} means 4 experiences have a w distinguishing 2 states and 3 ...  2 ...
        HashMap<Integer,HashMap<Integer,Integer>> maxSizes= new HashMap<>();
        // number of test for each number of state
        HashMap<Integer,Integer> nbTest = new HashMap<>();
        for(IOStats ioStats : resultSets){
            int nbState = ioStats.getNbState();
            if(!maxSizes.containsKey(nbState)){
                maxSizes.put(nbState,new HashMap<>());
                nbTest.put(nbState,0);
            }
            nbTest.put(nbState,nbTest.get(nbState) + 1);
            Integer max = 0;
            for(HashMap<OutputSequence,Integer> outputsResult : ioStats.getResultMap().values()){
                if(outputsResult.size() > max){
                    max = outputsResult.size();
                }
            }
            HashMap<Integer,Integer> maxSize = maxSizes.get(nbState);
            if(!maxSize.containsKey(max)){
                maxSize.put(max,0);
            }
            maxSize.put(max,maxSize.get(max) + 1);
        }
        //print nbState,max blablabla...,1,2,3,4,5,6,7
        writer.print("\"number of state\",\"number of test | maximum number of distinguisable state by a single sequence\"");
        for(int i = 1; i< 20; i++){
            writer.print("," + i);
        }
        float optimalCase = 0;
        writer.print("\n");
        for(int i = 2; i < 20; i ++){
            writer.print(i+",");
            if(nbTest.containsKey(i)){
                writer.print(nbTest.get(i));
                HashMap<Integer,Integer> maxSize = maxSizes.get(i);
                for (int j = 1; j < 20; j++) {
                    if(maxSize.containsKey(j)){
                        writer.print(","+maxSize.get(j));
                        if(i==j){
                            optimalCase = ((float) maxSize.get(j)) / (float) nbTest.get(i);
                        }
                    }else{
                        writer.print(",0");
                    }
                }
                String result = Float.toString(optimalCase);
                result = result.replace('.', ',');
                writer.print(","+result+"\n");
            }else {
                writer.print("0\n");
            }
        }
    }

    public static void dataBaseCreate(int nbState, int nbAutomata){
        Options.MININPUTSYM = 2;
        Options.MAXINPUTSYM = 2;
        Options.MINOUTPUTSYM = 2;
        Options.MAXOUTPUTSYM = 2;
        Options.MAXSTATES = nbState;
        Options.MINSTATES = nbState;
        try {
            SerializableAutomataList list = new SerializableAutomataList();
            int i = 0;
            while (i < nbAutomata){
                try{
                    list.mealys.add(RandomMealy.getConnexRandomMealy());
                    i++;
                }catch (Exception e) {
                    //doNotihng
                }
            }
            File file = new File(Options.OUTDIR +"/database/size"+nbState+".auto");
            file.createNewFile();
            FileOutputStream fileOut =
                    new FileOutputStream(Options.OUTDIR +"/database/size"+nbState+".auto");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(list);
            out.close();
            fileOut.close();
            System.out.printf(Options.OUTDIR +"/database/size"+nbState+".auto");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Mealy> databaseImport(int nbState){
        try
        {
            FileInputStream fileIn = new FileInputStream(Options.OUTDIR +"/database/size"+nbState+".auto");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            SerializableAutomataList list = (SerializableAutomataList) in.readObject();
            in.close();
            fileIn.close();
            return list.mealys;
        }catch(IOException i)
        {
            i.printStackTrace();
            return null;
        }catch(ClassNotFoundException c)
        {
            System.out.println("list of automata not found");
            c.printStackTrace();
            return null;
        }
    }

    public static void testImport(){
        dataBaseCreate(10,10);
        List<Mealy> list = databaseImport(10);
        System.out.println(list);
    }

    public static void generation(){
        for(int i = 5; i <= 35; i++){
            dataBaseCreate(i,1000);
            System.out.println("1000 automatas created for nb State = " + i);
        }
    }
}

class SerializableAutomataList implements Serializable {
    List<Mealy> mealys = new ArrayList<>();
}
