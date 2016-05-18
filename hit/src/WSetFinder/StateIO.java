package WSetFinder;

import automata.State;

import java.util.ArrayList;

/**
 * regroup a list of Input/Output defining a state.
 * it can regroup multiple states if it is not defined enough
 * Created by jean on 24/04/16.
 */
public class StateIO {
    private ArrayList<IO> ios;

    public void add(String input, String output){
        ios.add(new IO(input,output));
    }
    public void add(IO io){
        ios.add(io);
    }

    public StateIO() {
        ios = new ArrayList<>();
    }
    public String get(String input){
        for(IO io : ios){
            if(io.getInput().equals(input)){
                return io.getOutput();
            }
        }
        return null;
    }
}
