package WSetFinder.WSetStrategies.Crawler;

import WSetFinder.SplittingTree;
import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;

import java.util.List;

/**
 * Created by Jean Bouvattier on 05/07/16.
 * a state of pair: the initial one and the last one is the result from applying current word.
 * see article for more information
 */
public class StatePair{
    public State initial;
    private State last;
    private SplittingTree tree;

    public StatePair(State initial,SplittingTree tree) {
        this.initial = initial;
        this.last = initial;
        this.tree = tree;
    }

    public OutputSequence apply(InputSequence input){
        OutputSequence outputSequence = new OutputSequence();
        input.sequence.stream().forEach(symb -> {
                    MealyTransition transition = tree.getDriver().getAutomata().getTransitionFromWithInput(last,symb);
                    last = transition.getTo();
                    outputSequence.addOutput(transition.getOutput());
                });
        return outputSequence;
    }

    public State reset(){
        last = initial;
        return initial;
    }

    public State getLast() {
        return last;
    }

    public StatePair copy() {
        StatePair newSp = new StatePair(initial,tree);
        newSp.last = last;
        return newSp;
    }

    public State getInitial() {
        return initial;
    }

    public static boolean stuck(List<StatePair> pairs){
        for(int i = 0; i< pairs.size(); i++){
            for(int j = i + 1; j< pairs.size(); j++) {
                if(!pairs.get(i).getLast().equals(pairs.get(j).getLast())){
                    return false;
                }
            }
        }
        return true;
    }
}
