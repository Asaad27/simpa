package WSetFinder;

import automata.State;
import drivers.mealy.MealyDriver;
import drivers.mealy.transparent.RandomMealyDriver;
import main.simpa.Options;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * warning: due to lacks of time, this is not wished to be read by anyone
 * allergies and loss of will to live may occurs.
 * Created by jean on 24/04/16.
 */
public class WSetCalculator {
    private static PrintWriter writer;

    private HashMap<String,HashMap<String, Integer>> statistics;
    private ArrayList<String> WSet = new ArrayList<>();
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
        statistics = WSetStatCalculator.computeIOStat(driver,nbState).getResultMap();
        inputs = driver.getInputSymbols();
        String w = chooseWord();
        WSet.add(w);
        for(String output : statistics.get(w).keySet()){
            StateIO newState = new StateIO();
            newState.add(w,output);
            stateIOs.add(newState);
        }
        while(stateIOs.size() < nbState){
            strategy();
            printWSet();
            printStates();
        }
    }
    private void printWSet(){
        System.out.print("Wset : {");
        for(String w : WSet){
            System.out.print(w+",");
        }
        System.out.println("}");
    }
    private void printStates(){
        for(int i =0; i < stateIOs.size(); i++){
            StateIO stateIO = stateIOs.get(i);
            System.out.print("state "+ i +" : ");
            for(String input : WSet) {
                String output = stateIO.get(input);
                System.out.print("{" + input + "->" + output + "}, ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private void strategy() {
        String w = chooseWord();
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

    private void update(StateIO detectedState) {
        String w = WSet.get(WSet.size() - 1);
        ArrayList<StateIO> candidates = new ArrayList<>(stateIOs);
        ArrayList<String> set = new ArrayList<>(WSet);
        set.remove(w);
        for(StateIO  candidate : candidates){
            boolean sameQuotientState = true;
            for(String word : set) {
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

    private StateIO localizer(List<String> set){
        if(set.size() ==1){
            execute(set.get(0));
            StateIO state = new StateIO();
            //recreating trace
            String input = "";
            String output = "";
            for(int i =0; i< set.get(0).length() ; i++){
                input = trace.get(trace.size() - 1 - i).getInput() + input;
                output = trace.get(trace.size() - 1 - i).getOutput() + output;
            }
            state.add(new IO(input,output));
            return state;
        }else{
            //what are answers to set(k -1);
            ArrayList<IO> traces = new ArrayList<>();
            ArrayList<String> subSet1 = new ArrayList<>(set);
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

    private void execute(String s) {
        System.out.print("Executing {" + s + "->");
        for(int i =0; i < s.length(); i++){
            String input = "" + s.charAt(i);
            String output = driver.execute(input);
            System.out.print(output);
            trace.add(new IO(input,output));
        }
        System.out.println();
    }

    private String chooseWord(){
        int quality = 0;
        String word = "";
        for(String key : statistics.keySet()){
            if(statistics.get(key).size() > quality){
                if(!WSet.contains(key)){
                    word = key;
                    quality = statistics.get(key).size();
                }
            }
            else if(statistics.get(key).size() == quality && key.length() < word.length()) {
                if (!WSet.contains(key)) {
                    word = key;
                    quality = statistics.get(key).size();
                }
            }
        }
        if(Objects.equals(word, "")){
            System.err.println("Erreur : pas de nouveau mot trouvé");
        }
        return word;
    }
}
