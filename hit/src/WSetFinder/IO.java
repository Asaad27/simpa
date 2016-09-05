package WSetFinder;

import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;

/**
 * Created by jean on 27/04/16.
 * Store an Input sequence and its answer
 */
public class IO {
    private InputSequence input;
    private OutputSequence output;

    public IO(InputSequence input, OutputSequence output) {
        this.input = input;
        this.output = output;
    }

    public InputSequence getInput() {
        return input;
    }

    public OutputSequence getOutput() {
        return output;
    }
}
