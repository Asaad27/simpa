package learner.mealy.noReset;

import drivers.mealy.MealyDriver;

import java.util.ArrayList;

import learner.mealy.LmTrace;
import learner.mealy.noReset.dataManager.FullyQualifiedState;

public class GlobalTrace extends LmTrace {
	private MealyDriver driver;
	private ArrayList<FullyQualifiedState> C;
	
	public GlobalTrace(MealyDriver d){
		driver = d;
		driver.reset();//should be removed ?
		C = new ArrayList<FullyQualifiedState>();
	}
	
	/**
	 * apply an input to the automata, get the output and add it to the globalTrace
	 * @param d the driver of automata
	 * @param input the input to apply
	 * @return the output of the automata
	 */
	public String apply(String input){
		String output = driver.execute(input);
		append(input,output);
		C.add(null);
		return output;
	}
	
	public ArrayList<String> apply(ArrayList<String> inputs){
		ArrayList<String> outputs = new ArrayList<String>();
		for (String input : inputs)
			outputs.add(apply(input));
		return outputs;
	}
	
	public FullyQualifiedState getC(int pos){
		return C.get(pos);
	}
	
	public void setC(int pos,FullyQualifiedState s){
		C.set(pos, s);
	}
}
