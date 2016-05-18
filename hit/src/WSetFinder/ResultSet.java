package WSetFinder;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by Jean Bouvattier for LIG on 24/03/16.
 * link the W-Set Size to a number of states, inputs and outputs,
 * Used to see if ComputeCharacterizationSet is useful and efficient
 */
public class ResultSet {
    private int nbStates, nbInputs, nbOutputs, wSetSize;

    public ResultSet(int nbStates, int nbInputs, int nbOutputs, int wSetSize) {
        this.nbStates = nbStates;
        this.nbInputs = nbInputs;
        this.nbOutputs = nbOutputs;
        this.wSetSize = wSetSize;
    }

    public void printResult(PrintWriter writer){
        //writer.println("nbStates,nbInputs,+nbOutputs+,+wSetSize");
        writer.println(nbStates+","+nbInputs+","+nbOutputs+","+wSetSize);
    }

    public int getNbStates() {
        return nbStates;
    }

    public int getNbInputs() {
        return nbInputs;
    }

    public int getNbOutputs() {
        return nbOutputs;
    }

    public int getwSetSize() {
        return wSetSize;
    }
}
