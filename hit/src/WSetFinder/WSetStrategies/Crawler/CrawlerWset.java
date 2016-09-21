package WSetFinder.WSetStrategies.Crawler;

import WSetFinder.SplittingTree;
import WSetFinder.WSetStrategies.Crawler.decisionFunction.NbCluster;
import WSetFinder.WSetStrategies.Crawler.decisionFunction.NbTwin;
import WSetFinder.WSetStrategies.Crawler.decisionFunction.ValueFunction;
import WSetFinder.WSetStrategies.Crawler.inputStrategy.AlphabetStrategy;
import WSetFinder.WSetStrategies.Crawler.inputStrategy.InputStrategy;
import WSetFinder.WSetStrategies.Crawler.inputStrategy.LcaStrategy;
import WSetFinder.WSetStrategies.WSetStrategy;
import WSetFinder.WeightFunction.LocaliseWeightFunction;
import WSetFinder.WeightFunction.WeightFunction;
import automata.mealy.InputSequence;
import automata.State;
import drivers.mealy.transparent.RandomMealyDriver;
import drivers.mealy.transparent.TransparentMealyDriver;
import learner.mealy.noReset.NoResetLearner;
import main.simpa.Options;
import tools.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * main idea:
 * 1) create sets of statePairs regrouping all statePairs, initialy all state in a single set
 * 2) for a set, find its lca, apply the sequence to all statePairs in each state
 * 3) if two statePairs from a single set have different output, the set is splitted.
 * 4) repeat until end or until there is no lca possible which isn't a leaf
 * 5) then, begin another word
 * Created by Jean Bouvattier on 05/07/16.
 */
public class CrawlerWset extends WSetStrategy {
    protected ValueFunction valueFunction;
    protected InputStrategy inputStrategy;
    private int depth;
    public CrawlerWset(TransparentMealyDriver driver,
                       boolean doRefine,
                       WeightFunction function,
                       ValueFunction valueFunction,
                       InputStrategy inputStrategy,
                       int depth) {
        super(driver, doRefine, function);
        this.depth = depth;
        this.valueFunction = valueFunction;
        this.inputStrategy = inputStrategy;
    }

    public CrawlerWset(SplittingTree tree,
                       WeightFunction weightFunction,
                       ValueFunction valueFunction,
                       InputStrategy inputStrategy,
                       int depth) {
        super(tree, weightFunction);
        this.depth = depth;
        this.inputStrategy = inputStrategy;
        this.valueFunction = valueFunction;
    }

    private Scenario recursiveCrawler(States states, int n){
        if(n <=  0 || states.completed(tree) || states.stuck()){
            return new Scenario(states,new InputSequence());
        }else{
            Scenario bestCandidate = new Scenario(null,null);
            List<InputSequence> sequenceToTest = inputStrategy.choseCandidates(states,tree);
            for(InputSequence inputSequence : sequenceToTest){
                States newStates = states.apply(inputSequence);
                Scenario possibleScenario = recursiveCrawler(newStates,n - 1);
                if (valueFunction.sup(possibleScenario.getStates(),bestCandidate.getStates(),newStates)){
                    InputSequence newInput = new InputSequence();
                    newInput.addInputSequence(inputSequence);
                    newInput.addInputSequence(possibleScenario.getSequence());
                    bestCandidate = new Scenario(possibleScenario.getStates(),newInput);
                }
            }
            //in case no sequence are proposed
            if (bestCandidate.getStates() == null){
                return new Scenario(states,new InputSequence());
            }
            else
                return bestCandidate;
        }
    }

    @Override
    public List<InputSequence> calculateWSet() {
        List<List<StatePair>> statePairs = new ArrayList<>();
        statePairs.add(new ArrayList<>());
        for(State state : tree.getRoot().getSubSet()) {
            statePairs.get(0).add(new   StatePair(state,tree));
        }
        States states = new States(statePairs,0,0);
        List<InputSequence> WSet = new ArrayList<>();
        InputSequence currWord = new InputSequence();
        while(!states.completed(tree)){
            if(states.stuck() || currWord.getLength() > tree.getDriver().getAutomata().getStateCount()) {
                WSet.add(currWord);
                currWord = new InputSequence();
                states.reset();
            }
            //first we simulate a scenario by applying
            List<StatePair> choosenSet = Utils.randIn(states.getStatePairs());
            assert choosenSet != null;
            List<State> choosenStates = choosenSet.stream().map(StatePair::getLast).collect(Collectors.toList());
            InputSequence lcaSequence = tree.leastCommonAncestor(choosenStates).getSequence();
            States lcaApplied = states.apply(lcaSequence);
            Scenario lcaScenario = new Scenario(lcaApplied,lcaSequence);
            Scenario scenario = recursiveCrawler(states,depth);
            if(valueFunction.sup(lcaScenario.getStates(),scenario.getStates(),states)){
                scenario = lcaScenario;
            }
            currWord.addInputSequence(scenario.getSequence());
            states = states.apply(scenario.getSequence());
        }
        WSet.add(currWord);
        states.completed(tree);
        return WSet;
    }

    public static void main(String args[]){
        Options.MINSTATES = 3;
        Options.MAXSTATES = 3;
        Options.MININPUTSYM = 2;
        Options.MAXINPUTSYM = 2;
        Options.MINOUTPUTSYM = 2;
        Options.MAXOUTPUTSYM = 2;
        TransparentMealyDriver driver = new RandomMealyDriver();
        SplittingTree tree = new SplittingTree(driver,true);
        tree.printDot("newTree");
        System.out.println("beginning");
        long start_time = System.currentTimeMillis();
        List<InputSequence> WSet1 = new CrawlerWset(tree,new LocaliseWeightFunction(),new NbCluster(),new AlphabetStrategy(),6).calculateWSet();
        long end_time = System.currentTimeMillis();
        System.out.println("Cluster Alpha 10: " + WSet1 + "; time spent : " + (end_time - start_time) + "ms");
        System.out.println(AlphabetStrategy.spentTime + "ms");
        start_time = System.currentTimeMillis();
        List<InputSequence> WSet2 = new CrawlerWset(tree,new LocaliseWeightFunction(),new NbTwin(),new LcaStrategy(),3).calculateWSet();
        end_time = System.currentTimeMillis();
        System.out.println("Twin Lca 10: " + WSet2 + "; time spent : " + (end_time - start_time) + "ms");
        System.out.println(LcaStrategy.spentTime + "ms");
        WSet2 = new CrawlerWset(tree,new LocaliseWeightFunction(),new NbTwin(),new LcaStrategy(),10).calculateWSet();
        System.out.println("nb twin depth = 10 : " + WSet2);
        List<InputSequence> WSet3 = new CrawlerWset(driver,false,new LocaliseWeightFunction(),new NbCluster(),new AlphabetStrategy(),10).calculateWSet();
        System.out.println("nb fusion depth = 10 : " + WSet3);
        List<InputSequence> WSet4 = new CrawlerWset(driver,false,new LocaliseWeightFunction(),new NbCluster(),new LcaStrategy(),10).calculateWSet();
        System.out.println("nb twin depth = 10 : " + WSet4);
        //List<InputSequence> WSet1 = new CrawlerWset(tree,new LocaliseWeightFunction(),new NbTwin(),new AlphabetStrategy(),10).calculateWSet();
        //System.out.println(WSet1);
        //assert tree.checkWSet(WSet1);
        List<InputSequence> oldWset = NoResetLearner.computeCharacterizationSet(driver);
        System.out.println("good old WSet : " +oldWset);
        assert tree.checkWSet(WSet1);
        assert tree.checkWSet(WSet2);
        assert tree.checkWSet(WSet3);
        assert tree.checkWSet(WSet4);
        assert tree.checkWSet(oldWset);
        System.out.println("nb call CountCLuster = " + NbCluster.nbCall);
        System.out.println("nb call NbTwin = " + NbTwin.nbCall);

    }


    //Simulation of set evolution if output is choose.
    //1) for each set of look-alike statePairs, simulate output
    //1) sort them by output
    //3) different output means distinguishable statePairs, they are split apart

}

/**
 * A scenario represent a possible outcome:
 * 1) statePairs is the statePairs separation according to distinguishability
 * 2) inputSequence is chosen word to apply after statePairs, length 0 means no input is possible
 * 3) value represent interest in using it.
 */
class Scenario{
    private States states;
    private InputSequence sequence;

    public Scenario(States states, InputSequence sequence) {
        this.states = states;
        this.sequence = sequence;
    }


    public InputSequence getSequence() {
        return sequence;
    }

    public States getStates() {
        return states;
    }
}




