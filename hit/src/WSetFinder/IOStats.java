package WSetFinder;

import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;

import java.util.HashMap;

/**
 * results from IO tests on a blackbox automata.
 * contains :
 * The number of time an input sequence has been tested
 * for each of those input, what were the different answers
 * Created by Jean Bouvattier for LIG on 07/04/16.
 */
public class IOStats {
    /**
     * number of computed tests for each observed input sequences
     */
    private HashMap<InputSequence, Integer> nbTest;
    /**
     * link each observed input sequence the the differents observed output
     * and their frequencies
     */
    private HashMap<InputSequence,HashMap<OutputSequence, Integer>> resultMap;
    /**
     * number of state in the automata
     */
    private int nbState;
    /**
     * is equals to true if a single sequence distinguish every state
     * ie : if a sequence get nbState different answers : a member of resultMap has a size of nbState
     */
    private boolean hasDistinguishingSequence = false;


    public IOStats(HashMap<InputSequence, Integer> nbTest, HashMap<InputSequence, HashMap<OutputSequence, Integer>> resultMap, int nbState) {
        this.nbTest = nbTest;
        this.resultMap = resultMap;
        this.nbState = nbState;
    }

    public HashMap<InputSequence, Integer> getNbTest() {
        return nbTest;
    }

    public HashMap<InputSequence, HashMap<OutputSequence, Integer>> getResultMap() {
        return resultMap;
    }

    public int getNbState() {
        return nbState;
    }

    public void setHasDistinguishingSequence(boolean hasDistinguishingSequence) {
        this.hasDistinguishingSequence = hasDistinguishingSequence;
    }

    public boolean hasDistinguishingSequence() {
        return hasDistinguishingSequence;
    }
}
