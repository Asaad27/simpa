package WSetFinder.WSetStrategies;

import WSetFinder.WSetStrategies.Crawler.decisionFunction.NbCluster;
import WSetFinder.WeightFunction.WSetLength;
import WSetFinder.WeightFunction.WeightFunction;
import automata.mealy.InputSequence;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import drivers.mealy.MealyDriver;
import drivers.mealy.transparent.FromDotMealyDriver;
import drivers.mealy.transparent.RandomMealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import examples.mealy.RandomMealy;
import main.simpa.Options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * WARNING: may fail and return incorrect set with less than 1% failure
 * Created by Jean Bouvattier on 16/09/16.
 *
 *
 *
 * WARNIIIIIIIIIIIIIIIING DO NOT WORK, Jvais essayer de corriger ça.
 *
 */
public class BlackBoxCrawler {
    //Can be black Box driver
    private MealyDriver driver;
    int nbInput;
    int limitLength;
    private List<String> inputs = new ArrayList<>();
    private List<String> outputs = new ArrayList<>();
    private LinkedInputTree statRoot = new LinkedInputTree(new InputSequence());


    public BlackBoxCrawler(MealyDriver driver, int nbInput, int limitLength) {
        this.driver = driver;
        this.nbInput = nbInput;
        this.limitLength = limitLength;
    }

    public List<InputSequence> calculateWSet() {
        List<LinkedInputTree> stats = generateStats(nbInput);
        Collections.sort(stats);
        List<InputSequence> wSet = new ArrayList<>();
        wSet.add(stats.get(0).input);
        int prefixLimit = 1;
        boolean goodCandidate = true;
        while(wSet.size() < 4){
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
        return wSet;
    }
    public List<LinkedInputTree> generateStats(int nbInput){
        List<String> inputSymbols = driver.getInputSymbols();
        Random random = new Random();
        for(int i = 0; i < nbInput; i++){
            String input = inputSymbols.get(random.nextInt(inputSymbols.size()));
            execute(input);
        }
        return statRoot.toList();
    }

    public String execute(String input){
        String output = driver.execute(input);
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


    public static void main(String[] args){
        Options.MAXSTATES = 10;
        Options.MINSTATES = 10;
        Options.MAXINPUTSYM = 2;
        Options.MININPUTSYM = 2;
        Options.MAXOUTPUTSYM = 2;
        Options.MINOUTPUTSYM = 2;
        int nbVictory = 0;
        for(int i = 0;i < 100; i++) {
            try {
                MealyDriver driver = new RandomMealyDriver();
                driver.reset();
                BlackBoxCrawler crawler = new BlackBoxCrawler(driver, 200, 7);
                FromStats crawlerBis = new FromStats((RandomMealyDriver) driver,false,new WSetLength(),200,7);
                List<InputSequence> wset = crawler.calculateWSet();
                System.out.println(wset);
                if (driver.isCorrectWSet(wset)) {
                    nbVictory++;
                }
                else {
                    System.out.println("failed");
                }
            }catch (Exception e){
                e.printStackTrace();
                //automata generation was failed, retrying..
                i--;
            }
        }
        System.out.println(nbVictory);
    }
}
