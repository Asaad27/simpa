package WSetFinder;

import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import drivers.mealy.transparent.TransparentMealyDriver;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by Jean Bouvattier on 30/06/16 for LIG
 * Implement Splitting trees as defined in refered paper
 * such a tree split set of state according to output answers to input sequences
 * and final states obtained by applying input sequences to states
 */
public class SplittingTree {
    private Node root;
    private List<Node> nodes;
    private Random rand ;
    private TransparentMealyDriver driver;
    private List<Node> newNodes;
    private boolean doRefine;

    public SplittingTree(TransparentMealyDriver driver,boolean doRefine) {
        this.driver = driver;
        this.root = new Node(driver.getAutomata().getStates(), this);
        this.rand = new Random();
        this.nodes = new LinkedList<>();
        this.newNodes = new LinkedList<>();
        this.doRefine = doRefine;
        nodes.add(root);
        algo3();
        if(!isMinimal()){
            throw new RuntimeException("Not a minimal automata!");
        }
    }

    public static void main(String[] args){
        PrintWriter writer = null;
        try {
             writer = new PrintWriter("/home/jean/sandbox/result.cvs","UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        assert writer != null;
        writer.close();
    }

    /**
     * Methods described in paper constructing a stable splitting tree
     */
    public void algo1() {
        String input;
        Node node;
        while (!isAcceptable()){
            input = chooseInput();
            node = chooseNode();
            node.splitOutPut(input);
        }
        while (!isStable()){
            input = chooseInput();
            node = chooseNode();
            if(node.splitState(input)) {
                nodes.addAll(newNodes);
                newNodes.clear();
                if(doRefine)
                    refine(node.getSequence());
                nodes.addAll(newNodes);
                newNodes.clear();
            }
        }
    }

    /**
     * requires k, where the tree is k stable
     * in this version, nodes are refined
     */
    private void algo2(int k){
        boolean splited = false;
        for(Node node : nodes){
            if(node.isLeaf())
                for(String input : driver.getInputSymbols()){
                    Node v = leastCommonAncestor(node.destStates(input));
                    if(!v.isLeaf() && v.getSequence().getLength() == k){
                        node.splitState(input);
                        if(doRefine) {
                            refine(node.getSequence());
                        }
                        splited = true;
                        break;
                    }
                }
            if(splited){
                break;
            }
        }
        nodes.addAll(newNodes);
        newNodes.clear();
        if(splited)
            algo2(k);
    }
    public void algo3(){
        String input;
        Node node;
        while(!isAcceptable()){
            input = chooseInput();
            node = chooseNode();
            node.splitOutPut(input);
        }
        for (int k = 1; k < driver.getAutomata().getStateCount(); k++){
            algo2(k);
        }
    }

    public boolean isMinimal() {
        for(Node node : nodes){
            if(node.isLeaf() && node.getSubSet().size() >= 2)
                return false;
        }
        return true;
    }

    private void refine(InputSequence input) {
        nodes.stream().filter(Node::isLeaf).forEach(node -> node.splitOutPut(input));
    }

    public void printDot(String name) {
        PrintWriter writerMealy = null;
        PrintWriter writerTree = null;
        try {
            writerMealy = new PrintWriter("/home/jean/sandbox/" + name + "_mealy.dot","UTF-8");
            writerTree = new PrintWriter("/home/jean/sandbox/" + name + "_tree.dot","UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        assert writerTree != null;
        writerMealy.write("digraph mealy{\n");
        for(MealyTransition transition : driver.getAutomata().getTransitions()){
            writerMealy.write(transition.getFrom().getName() + " -> " +
                    transition.getTo().getName() + "[label=\"" +
                    transition.getInput() +"/" + transition.getOutput() + "\"]\n");
        }
        writerMealy.write("}\n");
        writerTree.write("digraph stable_splitting_tree {\n");
        for (Node node : nodes){
            node.printDot(writerTree);
        }
        writerTree.write("}\n");
        writerMealy.close();
        writerTree.close();
    }

    /**
     * randomly choose what input will be selected for next splitting
     */
    private String chooseInput() {
        return driver.getInputSymbols().get(rand.nextInt(driver.getInputSymbols().size()));
    }

    /**
     * randomly choose a leaf
     * @return a leaf
     */
    private Node chooseNode(){
        Node node;
        do{
             node = nodes.get(rand.nextInt(nodes.size()));
        }while(!node.isLeaf());
        return node;
    }
    private boolean isAcceptable(){
        for(Node node : nodes){
            if (!node.isAcceptable()){
                return false;
            }
        }
        return true;
    }
    private boolean isStable(){
        for(Node node : nodes){
            if(!node.isStable()){
                return false;
            }
        }
        return true;
    }

    public TransparentMealyDriver getDriver() {
        return driver;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Node leastCommonAncestor(List<State> states){
        Node lca = root;
        for (Node node : nodes){
            if (node.getSubSet().containsAll(states) && node.getSubSet().size() < lca.getSubSet().size()){
                lca = node;
            }
        }
        return lca;
    }

    /**
     * find leaf linked to this state
     * @param state
     */
    Node findBlock(State state){
        List<State> list = new ArrayList<>();
        list.add(state);
        return leastCommonAncestor(list);
    }

    public List<Node> getNewNodes() {
        return newNodes;
    }

    public Node getRoot() {
        return root;
    }

    public void setRefine(boolean refine) {
        this.doRefine = refine;
    }

    public OutputSequence apply(State st, InputSequence sequence) {
        return driver.getAutomata().apply(sequence,st);
    }

    /**
     * very costly and inefficient, just used for small case to
     * @param Wset
     * @return
     */
    public boolean checkWSet(List<InputSequence> Wset){
        for (int i = 0; i < driver.getAutomata().getStates().size(); i++){
            State s1 = driver.getAutomata().getStates().get(i);
            for (int j = i + 1; j < driver.getAutomata().getStates().size(); j++){
                State s2 = driver.getAutomata().getStates().get(j);
                boolean different = false;
                for(InputSequence input : Wset){
                    OutputSequence out1 = apply(s1,input);
                    OutputSequence out2 = apply(s2,input);
                    if (!out1.isSame(out2)){
                        different = true;
                        break;
                    }
                }
                if (!different){
                    return false;
                }
            }
        }
        return true;
    }
}
