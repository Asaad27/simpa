package WSetFinder;

import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import drivers.mealy.MealyDriver;
import drivers.mealy.transparent.RandomMealyDriver;
import main.simpa.Options;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * DEPRECATED
 * Created by jean on 24/04/16.
 */
public class WSetCalculator {
    private static PrintWriter writer;

    private HashMap<InputSequence,HashMap<OutputSequence, Integer>> statistics;
    private ArrayList<InputSequence> WSet = new ArrayList<>();
    int distinguishedStates = 0;
    private List<String> inputs;
    private String currentWord = "";
    private ArrayList<StateIO> stateIOs = new ArrayList<>();
    private ArrayList<IO> trace = new ArrayList<>();
    private MealyDriver driver;
    private int nbState;

    public WSetCalculator(MealyDriver driver, int nbState) {
        this.driver = driver;
        this.nbState = nbState;
    }

    /**
     * try to compute a WSet with a 10 states random automata
     * @param args: should be of length 1
     */
    public static void main(String[] args) {
        System.out.println(Options.OUTDIR);
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
        Options.MININPUTSYM = 2;
        Options.MAXINPUTSYM = 2;
        Options.MINOUTPUTSYM = 2;
        Options.MAXOUTPUTSYM = 2;
        Options.MAXSTATES = 10;
        Options.MINSTATES = 10;
        WSetCalculator calculator = new WSetCalculator(new RandomMealyDriver(),Options.MAXSTATES);
        calculator.calculateWSet();
    }

    public void calculateWSet(){
        /*
        driver.reset();
        statistics = WSetStatCalculator.computeIOStat(driver,nbState).getResultMap();
        //inputs = driver.getInputSymbols();
        InputSequence w = chooseWord();
        WSet.add(w);
        for(OutputSequence output : statistics.get(w).keySet()){
            StateIO newState = new StateIO();
            newState.add(w,output);
            stateIOs.add(newState);
        }
        while(stateIOs.size() < nbState){
            strategy();
            printWSet();
            printStates();
        }
        */
    }
    private void printWSet(){
        System.out.print("Wset : {");
        for(InputSequence w : WSet){
            System.out.print(w+",");
        }
        System.out.println("}");
    }
    private void printStates(){
        for(int i =0; i < stateIOs.size(); i++){
            StateIO stateIO = stateIOs.get(i);
            System.out.print("state "+ i +" : ");
            for(InputSequence input : WSet) {
                OutputSequence output = stateIO.get(input);
                System.out.print("{" + input + "->" + output + "}, ");
            }
            System.out.println();
        }
        System.out.println();
    }
    /* A DEBUGUER
    private void strategy() {
        InputSequence w = chooseWord();
        System.out.println("choosen word: " + w);
        WSet.add(w);
        int i = 0;
        boolean continuer = true;
        Random random = new Random();
        while(continuer){
            int n = random.nextInt(nbState*10);
            for(int j =0; j < n; j++){
                execute(driver.getInputSymbols().get(random.nextInt(driver.getInputSymbols().size())));
            }
            StateIO detectedState = localizer(WSet);
            update(detectedState);
            if(stateIOs.size() == nbState){
                return;
            }
            //assert that every state has a w part
            boolean wCharacterized = true;
            for(StateIO io : stateIOs){
                if(io.get(w) == null){
                    wCharacterized = false;
                }
            }
            if(i >= 100 || wCharacterized){
                continuer = false;
            }
            i++;
        }
        int nbChar = 0;
        for(StateIO io : stateIOs){
            if(io.get(w) != null){
                nbChar++;
            }
        }
        if( i >= 100){
            System.out.println("Characterization of " + w + " not completed.");
            System.out.println("Only " + nbChar + "/"+stateIOs.size()+ " characterized");
        }
    }
    */

    private void update(StateIO detectedState) {
        InputSequence w = WSet.get(WSet.size() - 1);
        ArrayList<StateIO> candidates = new ArrayList<>(stateIOs);
        ArrayList<InputSequence> set = new ArrayList<>(WSet);
        set.remove(w);
        for(StateIO  candidate : candidates){
            boolean sameQuotientState = true;
            for(InputSequence word : set) {
                if (candidate.get(word) != null) {
                    if (!candidate.get(word).equals(detectedState.get(word))) {
                        sameQuotientState = false;
                    }
                }
            }
            if(sameQuotientState){
                if(candidate.get(w) == null){
                    System.out.println("updating state ");
                    candidate.add(w,detectedState.get(w));
                    return;
                }else{
                    if(!candidate.get(w).equals(detectedState.get(w))){
                        System.out.println("adding new state : case 2");
                        stateIOs.add(detectedState);
                        return;
                    }
                    System.out.println("state already exist");
                    return;
                }
            }
        }
        //No candidate :
        System.out.println("new answers to older word");
        stateIOs.add(detectedState);
    }

    //A DEBUGUER
    /*
    private StateIO localizer(List<InputSequence> set){
        if(set.size() == 1){
            execute(set.get(0).toString());
            StateIO state = new StateIO();
            //recreating trace
            InputSequence input = new InputSequence();
            OutputSequence output = new OutputSequence();
            int wordSize = set.get(0).getLength();
            for(int i = 0; i < wordSize; i++){
                IO io = trace.get(trace.size() - wordSize + i);
                input.addInputSequence(io.getInput());
                output.addOutputSequence(io.getOutput());
            }
            state.add(new IO(input,output));
            return state;
        }else{
            //what are answers to set(k -1);
            ArrayList<IO> traces = new ArrayList<>();
            ArrayList<InputSequence> subSet1 = new ArrayList<>(set);
            subSet1.remove(set.get(set.size() -1));
            ArrayList<String> subSet2 = new ArrayList<>(set);
            subSet2.remove(set.get(set.size() - 2));
            for (int i =0; i <= 2*nbState - 2; i++){
                localizer(subSet1);
                String input = "";
                String output = "";
                for(int k =0; k< set.get(0).length() ; k++){
                    input = trace.get(trace.size() - 1 - k).getInput() + input;
                    output = trace.get(trace.size() - 1 - k).getOutput() + output;
                }
                traces.add(new IO(input,output));
            }
            int greatestJ = -1;
            for(int j = 0; j< nbState -1; j++){
                int m = 0;
                boolean candidate = true;
                while (candidate){
                    if (!Objects.equals(traces.get(j + m).getInput(), traces.get(nbState + m).getInput())
                            || !Objects.equals(traces.get(j + m).getOutput(), traces.get(nbState + m).getOutput())){
                        //traces are different, not a candidate
                        candidate = false;
                    }else{
                        m++;
                    }if(m == nbState - 1){
                        candidate = false;
                    }
                }
                if (m == nbState - 1){
                    greatestJ = j;
                }
            }
            if (greatestJ == -1) throw new AssertionError();
            StateIO trace2 = localizer(subSet2);
            trace2.add(traces.get(greatestJ));
            return trace2;
        }
    }
    */

    private void execute(String s) {
        System.out.print("Executing {" + s + "->");
        for(int i =0; i < s.length(); i++){
            String input = "" + s.charAt(i);
            String output = driver.execute(input);
            System.out.print(output);
            trace.add(new IO(new InputSequence(input),new OutputSequence(output)));
        }
        System.out.println();
    }

    private InputSequence chooseWord(){
        int quality = 0;
        InputSequence word = new InputSequence();
        for(InputSequence key : statistics.keySet()){
            if(statistics.get(key).size() > quality){
                if(!WSet.contains(key)){
                    word = key;
                    quality = statistics.get(key).size();
                }
            }
            else if(statistics.get(key).size() == quality && key.getLength() < word.getLength()) {
                if (!WSet.contains(key)) {
                    word = key;
                    quality = statistics.get(key).size();
                }
            }
        }
        if(word.getLength() == 0){
            System.err.println("Erreur : pas de nouveau mot trouvé");
        }
        return word;
    }

}
