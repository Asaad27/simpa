package WSetFinder.TransparentFinder.WSetStrategies;

import WSetFinder.TransparentFinder.WeightFunction.LocaliseWeightFunction;
import WSetFinder.TransparentFinder.WeightFunction.WeightFunction;
import WSetFinder.WSetStatCalculator;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import drivers.Driver;
import drivers.mealy.MealyDriver;
import drivers.mealy.transparent.RandomMealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import examples.mealy.RandomMealy;
import main.simpa.Options;

import java.util.*;

/**
 * Created by Jean Bouvattier on 30/08/16.
 * Apply large number of input to try to determine the most distinguishing sequences.
 * These sequences are then unified in a wset.
 */
public class FromStats extends WSetStrategy {
    // wanted results for computing correct parameters.
    // because copy past is fast.
    final static float[] targetValues = {1.374f,1.463f,1.587f,1.672f,1.721f,1.781f,1.81f,1.874f
            ,1.911f,1.915f,1.951f,1.956f,1.989f,2.007f,1.999f,2.001f,2.024f,2.006f,2.038f,2.029f
            ,2.022f,2.024f,2.027f,2.04f,2.038f,2.042f,2.042f,2.046f,2.035f,2.035f,2.039f};

    private int limitLength = 10;
    private int nbInput;
    private List<String> inputs = new ArrayList<>();
    private List<String> outputs = new ArrayList<>();
    private LinkedInputTree statRoot = new LinkedInputTree(new InputSequence());
    List<LinkedInputTree> stats;
    public FromStats(TransparentMealyDriver driver, boolean doRefine, WeightFunction weightFunction,
                     int nbInput, int limitLength) {
        super(driver, doRefine, weightFunction);
        s = driver.getAutomata().getInitialState();
        this.limitLength = limitLength;
        this.nbInput = nbInput;
    }

    /**
     * performs statistics to determine best distinguishing sequences.
     * also privilege sequences if their sufixes are not present in the
     * on construction w-set
     * @return
     */
    @Override
    public List<InputSequence> calculateWSet() {
        TransparentMealyDriver driver = tree.getDriver();
        int nbState = driver.getAutomata().getStates().size();
        stats = generateStats(nbInput);
        Collections.sort(stats);
        List<InputSequence> wSet = new ArrayList<>();
        wSet.add(stats.get(0).input);
        int prefixLimit = 1;
        boolean goodCandidate = true;
        while(!driver.isCorrectWSet(wSet) && wSet.size() <= 10){
            InputSequence candidate = null;
            for (LinkedInputTree node: stats) {
                goodCandidate = true;
                candidate = node.input;
                for(InputSequence w : wSet){
                    if(w.startsWith(candidate)){
                        goodCandidate = false;
                        break;
                    }
                    InputSequence prefix;
                    if (w.getLength() < prefixLimit) {
                        prefix = w;
                    } else {
                        prefix = w.getIthPreffix(prefixLimit);
                    }
                    if(candidate.startsWith(prefix)){
                        goodCandidate = false;
                        break;
                    }
                }
                if(goodCandidate){
                    break;
                }
            }
            //seems like we are stuck..
            if(!goodCandidate){
                prefixLimit++;
                continue;
            }
            //Here we have the best candidate.
            wSet.add(candidate);
            if(Math.pow(2,prefixLimit) <= wSet.size()){
                prefixLimit++;
            }
        }
        Iterator<LinkedInputTree> itr = stats.iterator();
        while(!driver.isCorrectWSet(wSet) && itr.hasNext()){
            wSet.add(itr.next().input);
        }
        return wSet;
    }

    private automata.State s;

    public String execute(String input){
        TransparentMealyDriver driver = tree.getDriver();
        MealyTransition transition = driver.getAutomata().getTransitionFromWithInput(s,input);
        String output = transition.getOutput();
        s = transition.getTo();
        inputs.add(input);
        outputs.add(output);
        for(int i = Math.max(0, inputs.size() - limitLength); i < inputs.size(); i++){
            InputSequence checkedInput = new InputSequence();
            OutputSequence checkedOutput = new OutputSequence();
            for (int j = i; j < inputs.size(); j++){
                checkedInput.addInput(inputs.get(j));
                checkedOutput.addOutput(outputs.get(j));
            }
            statRoot.put(checkedInput,checkedOutput);
        }
        return output;
    }

    /**
     * performs multiple input/output to create a trace
     * This trace is put as an array of input sequence.
     * Each of these input sequence is linked to every observed outputs.
     * @param nbInput to perform
     * @return the list
     */
    public List<LinkedInputTree> generateStats(int nbInput){
        List<String> inputSymbols = tree.getDriver().getInputSymbols();
        Random random = new Random();
        for(int i = 0; i < nbInput; i++){
            String input = inputSymbols.get(random.nextInt(inputSymbols.size()));
            execute(input);
        }
        return statRoot.toList();
    }

    public static void main(String args[]){
        int nbState = 4;
        Options.MAXOUTPUTSYM = 2;
        Options.MINOUTPUTSYM = 2;
        Options.MAXINPUTSYM = 2;
        Options.MININPUTSYM = 2;
        ArrayList<Integer> nbTry = new ArrayList<>();
        for(float expectedValue : FromStats.targetValues){
            nbState++;
            float targetValue = expectedValue*1.1f;
            int result = getParameters(targetValue,nbState);
            nbTry.add(result);
            System.out.println("nbTry = " + nbTry + " for nb State =" + nbState);
        }
    }

    /**
     * for a wanted size, execute many test
     * @param wantedSize margin
     * @param nbState automata size to be tested
     * @return number of input to be computed on a classic wset search
     */
    public static int getParameters(float wantedSize, int nbState){
        float averageSize;
        Options.MAXSTATES = nbState;
        Options.MINSTATES = nbState;
        List<Mealy> database = WSetStatCalculator.databaseImport(nbState);
        int nbSample = database.size();
        int nbInput = 200;
        int wordLength;
        if(nbState <= 10)
            wordLength = nbState;
        else{
            wordLength = 5 + nbState/2;
        }
        List<TransparentMealyDriver> drivers = new ArrayList<>();
        for (Mealy mealy : database) {
            TransparentMealyDriver driver = new TransparentMealyDriver(mealy);
            drivers.add(driver);
        }
        System.out.println("target average size: " + wantedSize);
        do {
            nbInput += 40;
            //wordLength++;
            averageSize = 0;
            int nbTest = 0;
            long timeSpent = 0;
            for (int i = 0; i < nbSample; i++) {
                try {
                    TransparentMealyDriver driver = drivers.get(i);
                    long begin = System.currentTimeMillis();
                    FromStats strat = new FromStats(driver, true, new LocaliseWeightFunction(), nbInput, wordLength);//wordLength);
                    List<InputSequence> wSet = strat.calculateWSet();
                    int size = wSet.size();
                    averageSize += size;
                    nbTest++;
                    long end = System.currentTimeMillis();
                    timeSpent += (end - begin);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
            averageSize /= nbTest;
            timeSpent /= (nbTest);
            System.out.println("avg size = " + averageSize + " with nb Input = " +
                    nbInput + " nb try = " + nbTest + " Time spent = " + timeSpent + " wordLength = " + wordLength);
        } while (averageSize > wantedSize && nbInput < 1000);
            return nbInput;
    }
}

/**
 * Link every input sequence to all its extensions.
 * also contains outpurs
 */
class LinkedInputTree implements Comparable<LinkedInputTree>{
    final static boolean reccursiveAdding = true;
    InputSequence input;
    Set<OutputSequence> answers;
    HashMap<String,LinkedInputTree> children;

    public LinkedInputTree(InputSequence input) {
        this.input = input;
        this.children = new HashMap<>();
        this.answers = new HashSet<>();
    }

    /**
     * return statABR with input identifiant
     * @param input
     * @return
     */
    public LinkedInputTree get(InputSequence input){
        if(input.equals(this.input)){
            return this;
        }
        if(!input.startsWith(this.input)){
            System.err.println("Erreur, arbre mal formé");
            return null;
        }
        String charInput = input.sequence.get(this.input.getLength());
        if(!children.containsKey(charInput)){
            System.err.println("Erreur, clé non présente: absence de l'input dans l'arbre");
            return null;
        }
        return children.get(charInput).get(input);

    }

    /**
     * make so input is linked to output in every child.
     * @param input
     * @param output
     */
    public void put(InputSequence input,OutputSequence output) {
        if (input.equals(this.input)) {
            add(output);
            return;
        }
        if (!input.startsWith(this.input) && this.input.getLength() != 0) {
            System.err.println("Erreur, noeud actuel pas un préfixe du noeuf à ajouter");
            return;
        }
        String charInput = input.sequence.get(this.input.getLength());
        if (!children.containsKey(charInput)) {
            children.put(charInput,new LinkedInputTree(input.getIthPreffix(this.input.getLength() + 1)));
            for(OutputSequence possibleOutput : answers){
                children.get(charInput).add(possibleOutput);
            }
        }
        children.get(charInput).put(input,output);
    }

    /**
     * add output to answers and so for children
     * @param output
     */
    public void add(OutputSequence output) {
        boolean doAdd = true;
        for (OutputSequence o : answers) {
            // the output to be added already have a prefix in it, we remove it.
            if (output.startsWith(o)) {
                answers.remove(o);
                break;
            }
            // we try to add a suffix..
            if(o.startsWith(output)){
                doAdd = false;
                break;
            }
        }
        if(doAdd) {
            answers.add(output);
        }
        if(LinkedInputTree.reccursiveAdding)
        for(LinkedInputTree stat: children.values()){
            stat.add(output);
        }
    }

    public List<LinkedInputTree> toList() {
        List<LinkedInputTree> stats = new LinkedList<>();
        for(LinkedInputTree child: children.values()){
            stats.addAll(child.toList());
        }
        stats.add(this);
        return stats;
    }

    @Override
    public int compareTo(LinkedInputTree linkedInputTree) {
        return linkedInputTree.answers.size() - this.answers.size();
    }
}

