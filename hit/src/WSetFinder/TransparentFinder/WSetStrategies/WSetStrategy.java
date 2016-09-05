package WSetFinder.TransparentFinder.WSetStrategies;

import WSetFinder.TransparentFinder.WSetStrategies.Crawler.CrawlerWset;
import WSetFinder.TransparentFinder.WSetStrategies.Crawler.decisionFunction.NbCluster;
import WSetFinder.TransparentFinder.WSetStrategies.Crawler.decisionFunction.NbTwin;
import WSetFinder.TransparentFinder.WSetStrategies.Crawler.inputStrategy.AlphabetStrategy;
import WSetFinder.TransparentFinder.SplittingTree;
import WSetFinder.TransparentFinder.WSetStrategies.Crawler.inputStrategy.LcaStrategy;
import WSetFinder.TransparentFinder.WeightFunction.LocaliseWeightFunction;
import WSetFinder.TransparentFinder.WeightFunction.WeightFunction;
import automata.mealy.InputSequence;
import drivers.mealy.transparent.RandomMealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import main.simpa.Options;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jean Bouvattier on 01/07/16.
 * from a splitting tree, compute a wSet
 * main compute statistics in first arg file
 */
public abstract class WSetStrategy {
    protected SplittingTree tree;
    private List<InputSequence> wSet = null;
    protected WeightFunction weightFunction;
    private int weight;



    public WSetStrategy(TransparentMealyDriver driver,boolean doRefine, WeightFunction weightFunction) {
        this.weightFunction = weightFunction;
        this.tree = new SplittingTree(driver,doRefine);
    }

    public WSetStrategy(SplittingTree tree, WeightFunction weightFunction) {
        this.weightFunction = weightFunction;
        this.tree = tree;
    }

    public abstract List<InputSequence> calculateWSet();

    public static void main(String[] args){
        computeWSetWeightStat(5,35,2,2,2,2,1000);
    }
    private static void computeWSetWeightStat(
            int stateMin, int stateMax, int inputMin, int inputMax, int outputMin, int outputMax, int nbTry) {
        PrintWriter tryWriter = null;
        try {
            tryWriter = new PrintWriter("/home/jean/sandbox/result.csv","UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        assert tryWriter != null;
        final PrintWriter writer = tryWriter;
        LocaliseWeightFunction weightFunction = new LocaliseWeightFunction();
        int nbFunction = 1;
        writer.println("state min, state max, input min, input max, output min, output max, nb Try,");
        writer.println(stateMin +"," + stateMax + "," + inputMin + "," + inputMax + "," + outputMin + "," + outputMax + "," + nbTry);
        final String tab = ",,,,";
        writer.println(",greedy,,,,,union,,,,,lcaInput depth 1 twin,,,,,lcaInput depth 1 cluster" +
                ",,,,,AlphabetInput depth 10 twin,,,,,AlphabetInput depth 10 cluster");
        writer.print("automata size,");
        for(int j = 0; j < nbFunction; j++){
            writer.print(RunResult.title());
        }
        writer.println();
        for (int i = stateMin; i <= stateMax; i++) {
            System.out.println("nb states = " + i);
            Options.MINSTATES = i;
            Options.MAXSTATES = i;
            weightFunction.setNbState(i);
            List<RunResult> resultSet = new ArrayList<>();
            for (int j = 0; j< nbFunction; j++){
                resultSet.add(new RunResult());
            }
            for (int j = inputMin; j <= inputMax; j++) {
                Options.MININPUTSYM = j;
                Options.MAXINPUTSYM = j;
                for (int k = outputMin; k <= outputMax; k++) {
                    for (int l = 0; l < nbTry; l++){
                        Options.MINOUTPUTSYM = k;
                        Options.MAXOUTPUTSYM = k;
                        try {
                            RandomMealyDriver driver = new RandomMealyDriver();
                            driver.getAutomata().exportToDot();
                            List<WSetStrategy> strategies = new ArrayList<>();
                            SplittingTree tree = new SplittingTree(driver,true);
                            /*strategies.add(new FromStats(driver,true,new LocaliseWeightFunction(),1000,20));
                            strategies.add(new FromStats(driver,true,new LocaliseWeightFunction(),1000,10));
                            strategies.add(new FromStats(driver,true,new LocaliseWeightFunction(),1000,5));
                            strategies.add(new FromStats(driver,true,new LocaliseWeightFunction(),300,20));
                            strategies.add(new FromStats(driver,true,new LocaliseWeightFunction(),300,10));
                            strategies.add(new OldWSet(driver,true,new LocaliseWeightFunction()));
                            strategies.add(new UnionWSet(driver,true,new LocaliseWeightFunction()));
                            strategies.add(new CrawlerWset(tree,weightFunction,new NbTwin(),new LcaStrategy(),1));
                            strategies.add(new CrawlerWset(tree,weightFunction,new NbCluster(),new LcaStrategy(),1));
                            strategies.add(new CrawlerWset(tree,weightFunction,new NbTwin(),new AlphabetStrategy(),10));
                            strategies.add(new CrawlerWset(tree,weightFunction,new NbCluster(),new AlphabetStrategy(),10));
                            */
                            strategies.add(new FromStats(driver,true,new LocaliseWeightFunction(),300,5));

                            assert strategies.size() == nbFunction;
                            for(int m = 0; m < nbFunction; m++ ){
                                resultSet.get(m).add(strategies.get(m),i);
                            }

                        } catch (Exception e) {
                           // e.printStackTrace();
                            l--;
                        }
                    }
                }
            }
            writer.print(i + ",");
            resultSet.stream().forEach(res ->
                writer.print(res.synthesise())
            );
            writer.println();
        }
        writer.close();
    }

    public List<InputSequence> getWSet() {
        if(wSet == null) {
            wSet = calculateWSet();
            if(!tree.checkWSet(wSet)){
                return null;
            }
            weight = weightFunction.weight(wSet);
        }
        return wSet;
    }

    public int getWeight() {
        if(wSet == null) {
            wSet = calculateWSet();
            weight = weightFunction.weight(wSet);
        }
        return weight;
    }
}
//used to handle results : weight and number of test
class RunResult{
    private int nbTest = 0;
    private float expSum = 0;
    private float sumSum = 0; // No, i had no other idea to rename this field
    private float maxLengthSum = 0;
    private float sizeSum = 0;
    private float timeSpentSum = 0;
    private int nbFail = 0;
    private List<InputSequence> wSet = null;
    private HashMap<Integer,Integer> sizeRepartition = new HashMap<>();

    public void add(WSetStrategy strategy,int nbState) throws Exception {
        long start_time = System.currentTimeMillis();
        List<InputSequence> wSet = strategy.getWSet();
        long end_time = System.currentTimeMillis();
        timeSpentSum += end_time - start_time;
        if(wSet == null){
            nbFail +=1;
            throw new Exception("fail");
        }
        int size = wSet.size();
        int exp = 1;
        for (int i = 0; i < size; i++)
            exp *= nbState;
        int sum = 0;
        for(InputSequence word : wSet)
            sum += word.getLength();
        int max = 0;
        for(InputSequence word : wSet)
            if(max <= word.getLength())
                max = word.getLength();
        expSum += exp;
        sumSum += sum;
        maxLengthSum += max;
        sizeSum += size;
        nbTest += 1;
    }


    public static String title(){
        return "avg size, avg exp(size),avg(sum),avg(max length),avg time,";
    }

    public String synthesise() {
        assert nbTest != 0;
        String val1 = "\"" + (new Float(sizeSum/nbTest)).toString().replace(".",",") + "\"," ;
        String val2 = "\"" + (new Float(expSum/nbTest)).toString().replace(".",",") + "\"," ;
        String val3 = "\"" + (new Float(sumSum/nbTest)).toString().replace(".",",") + "\"," ;
        String val4 = "\"" + (new Float(maxLengthSum/nbTest)).toString().replace(".",",") + "\"," ;
        String val5 = "\"" + (new Float(timeSpentSum/nbTest)).toString().replace(".",",") + "\"," ;
        return  val1 + val2 + val3 + val4 + val5;
    }
}