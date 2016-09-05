package WSetFinder.TransparentFinder.WSetStrategies.Crawler;

import WSetFinder.TransparentFinder.Node;
import WSetFinder.TransparentFinder.SplittingTree;
import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by Jean Bouvattier on 07/07/16.
 */
public class States {
    private List<List<StatePair>> statePairs;
    private int nbSingleton;
    private int wordLength;
    private List<List<StatePair>> stuckPairs;


    public States(List<List<StatePair>> statePairs, int nbSingleton, int wordLength,List<List<StatePair>> stuckPairs) {
        this.statePairs = statePairs;
        this.nbSingleton = nbSingleton;
        this.wordLength = wordLength;
        this.stuckPairs = stuckPairs;
    }

    public States(List<List<StatePair>> statePairs, int nbSingleton, int wordLength) {
        this.statePairs = statePairs;
        this.nbSingleton = nbSingleton;
        this.wordLength = wordLength;
        this.stuckPairs = new ArrayList<>();
    }

    public States apply(InputSequence sequence) {
        List<List<StatePair>> newStates = new ArrayList<>();
        List<List<StatePair>> newStuckPairs = new ArrayList<>();
        for(List<StatePair> foo : stuckPairs){
            List<StatePair> bar = foo.stream().map(StatePair::copy).collect(Collectors.toList());
            newStuckPairs.add(bar);
        }
        int i = 0;
        for (List<StatePair> foo : statePairs) {
            HashMap<OutputSequence, List<StatePair>> map = new HashMap<>();
            for (StatePair sp : foo) {
                StatePair newSp = sp.copy();
                OutputSequence output = newSp.apply(sequence);
                if (!map.containsKey(output)) {
                    map.put(output, new LinkedList<>());
                }
                map.get(output).add(newSp);
            }
            for (List<StatePair> statePairs : map.values()) {
                if (statePairs.size() == 1) {
                    i++;
                } else if(StatePair.stuck(statePairs)){
                    newStuckPairs.add(statePairs);
                }else {
                    newStates.add(statePairs);
                }
            }
        }
        return new States(newStates, nbSingleton + i, wordLength + sequence.getLength(),newStuckPairs);
    }

    public boolean completed(SplittingTree tree) {
        if(stuckPairs.size() != 0)
            return false;
        for(List<StatePair> set :statePairs) {
            if(set.size() == 1)
                continue;
            List<State> initStates = new LinkedList<>();
            set.stream().forEach(st -> initStates.add(st.getInitial()));
            Node lca = tree.leastCommonAncestor(initStates);
            if (!lca.isLeaf()){
                return false;
            }
        }
        return true;
    }

    public void reset() {
        statePairs.addAll(stuckPairs);
        stuckPairs.clear();
        statePairs.stream().forEach(set -> set.stream().forEach(StatePair::reset));
    }

    public List<List<StatePair>> getStatePairs() {
        return statePairs;
    }

    public int getNbSingleton() {
        return nbSingleton;
    }

    public int getWordLength() {
        return wordLength;
    }

    public List<List<StatePair>> getStuckPairs() {
        return stuckPairs;
    }

    public boolean stuck(){
        return !stuckPairs.isEmpty() && statePairs.isEmpty();
    }
}
