package learner.mealy;

import java.util.ArrayList;

public class LmTrace {
	//at any time, inputs and outputs must have the same length
	private ArrayList<String> inputs;
	private ArrayList<String> outputs;

	public String getInput(int pos){
		return inputs.get(pos);
	}

	public ArrayList<String> getOutputsProjection() {
		return outputs;
	}
	
	public String getOutput(int pos){
		return outputs.get(pos);
	}
	
	public ArrayList<String> getInputsProjection(){
		return inputs;
	}
	
	public void append(String input, String output){
		inputs.add(input);
		outputs.add(output);
	}
	
	public void append(LmTrace other){
		inputs.addAll(other.inputs);
		outputs.addAll(other.outputs);
	}
	
	public int size(){
		return inputs.size();//=outputs.size()
	}
	
	public Boolean hasSuffix(int pos, LmTrace other){
		if (pos + other.size() > size())
			return false;
		for (int i = 0; i < other.size(); i++){
			if (inputs.get(pos+i) != other.inputs.get(i) || 
			outputs.get(pos+i) != other.outputs.get(i))
				return false;
		}
		return true;
	}
}
