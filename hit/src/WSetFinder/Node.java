package WSetFinder;

import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import drivers.mealy.transparent.TransparentMealyDriver;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Jean Bouvattier on 30/06/16.
 * represents a node in a Splitting Tree
 */
public class Node {
    private TransparentMealyDriver driver;
    /**
     * Corresponding states associated to this node.
     */
    private List<State> subSet;
    /**
     * This sequence distinguish every Children of this Node
     */
    private InputSequence sequence;

    private SplittingTree tree;

    private LinkedList<Node> children;

    public LinkedList<Node> getChildren() {
        return children;
    }

    public Node(List<State> subSet, SplittingTree tree) {
        this.tree = tree;
        this.driver = tree.getDriver();
        this.subSet = subSet;
        children = new LinkedList<>();
        sequence = new InputSequence();
    }
    public boolean isLeaf(){
        return children.size() == 0;
    }

    public void addToSubSet(State s){
        subSet.add(s);
    }
    public boolean splitOutPut(String input){
        if (sequence.getLength() > 0 || !isLeaf()){
            return false;
        }
        HashMap<String,Node> childrenMap = new HashMap<>();
        for (State s : subSet){
            String output = driver.getAutomata().getTransitionFromWithInput(s,input).getOutput();
            if (!childrenMap.containsKey(output)){
                childrenMap.put(output,new Node(new ArrayList<>(),tree));
            }
            childrenMap.get(output).addToSubSet(s);
        }
        if(childrenMap.size() < 2) {
            //Rejection case! Cancelling
            return false;
        }
        sequence.addInput(input);
        children.addAll(childrenMap.values().stream().collect(Collectors.toList()));
        tree.getNodes().addAll(children);
        return true;
    }

    public boolean isAcceptable() {
        if(!isLeaf()) return true;
        for (State s : subSet){
            for (State t : subSet){
                if (s != t){
                    for(String input : driver.getInputSymbols()){
                        String outputS = driver.getAutomata().getTransitionFromWithInput(s,input).getOutput();
                        String outputT = driver.getAutomata().getTransitionFromWithInput(t,input).getOutput();
                        if (!outputS.equals(outputT)){
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public void printDot(PrintWriter writer) {
        for (Node child: children) {
            writer.println("\"" + subSet + "/" + sequence + "\"" + " -> " + "\"" + child.subSet + "/" +child.sequence + "\"");
        }
    }

    public boolean isStable() {
        if(!isLeaf()) return true;
        for (State s : subSet){
            for (State t : subSet){
                if (s != t){
                    for(String input : driver.getInputSymbols()){
                        State ToS = driver.getAutomata().getTransitionFromWithInput(s,input).getTo();
                        State Tot = driver.getAutomata().getTransitionFromWithInput(t,input).getTo();
                        for(Node node : tree.getNodes()){
                            if (node.isLeaf() && node.subSet.contains(ToS)){
                                if (!node.subSet.contains(Tot)){
                                    return false;
                                }
                            }
                        }

                    }
                }
            }
        }
        return true;
    }

    public List<State> getSubSet() {
        return subSet;
    }

    public boolean splitState(String input) {
        if(!isLeaf() || sequence.getLength() > 0){
            return false;
        }
        if(subSet.size() <= 1){
            return false;
        }
        //link all dest state to its initial states when applying input
        HashMap<State,List<State>> destStatesMap = new HashMap<>();
        List<State> destList = new LinkedList<>();
        for (State s : subSet){
            State destState = driver.getAutomata().getTransitionFromWithInput(s,input).getTo();
            if (!destStatesMap.containsKey(destState)) {
                destStatesMap.put(destState, new LinkedList<>());
            }
            destStatesMap.get(destState).add(s);
            if (!destList.contains(destState)){
                destList.add(destState);
            }
        }
        Node lca = tree.leastCommonAncestor(destList);
        if(lca.isLeaf()){
            return false;
        }

        for (Node child : lca.children){
            List<State> initialStates = new LinkedList<>();
            for(State state : child.subSet) {
                if(destStatesMap.containsKey(state))
                    initialStates.addAll(destStatesMap.get(state));
            }
            if (initialStates.size() != 0) {
                children.add(new Node(initialStates, tree));
            }
        }
        if (children.size() < 2){
            children.clear();
            sequence = new InputSequence();
            return false;
        }
        sequence.addInput(input);
        sequence.addInputSequence(lca.sequence);
        tree.getNewNodes().addAll(children);
        return true;
    }

    public List<State> destStates(String input){
        List<State> dest = new LinkedList<>();
        for(State state : subSet){
            State destState = tree.getDriver().getAutomata().getTransitionFromWithInput(state,input).getTo();
            if (!dest.contains(destState)){
                dest.add(destState);
            }
        }
        return dest;
    }

    public InputSequence getSequence() {
        return sequence;
    }

    public void splitOutPut(InputSequence input) {
        if (!isLeaf()){
            //inner node should not be modified
            return;
        }
        HashMap<OutputSequence,List<State>> map = new HashMap<>();
        for (State state: subSet){
            OutputSequence output = tree.getDriver().getAutomata().apply(input,state);
            if (!map.containsKey(output)){

                map.put(output,new LinkedList<>());
            }
            map.get(output).add(state);
        }
        if(map.size() < 2) {
            //Not concluding, do nothing
            return;
        }
        for (List<State> states : map.values()){
            Node child = new Node(states,tree);
            children.add(child);
        }
        sequence = input;
        tree.getNewNodes().addAll(children);
    }
}
