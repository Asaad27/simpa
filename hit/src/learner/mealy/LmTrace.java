package learner.mealy;

import java.util.ArrayList;

public class LmTrace {
	//at any time, inputs and outputs must have the same length
	private ArrayList<String> inputs;
	private ArrayList<String> outputs;

	public LmTrace(String x, String o) {
		this();
		append(x,o);
	}

	public LmTrace() {
		inputs = new ArrayList<String>();
		outputs = new ArrayList<String>();
	}

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
			if (! inputs.get(pos+i).equals(other.inputs.get(i)) || 
			!outputs.get(pos+i).equals(other.outputs.get(i))){
//				System.out.println("input : " + inputs.get(pos+i) + " other :" +other.inputs.get(i));
//				System.out.println("output : " + outputs.get(pos+i) + " other :" +other.outputs.get(i));
				return false;
			}
		}
		return true;
	}
	
	public LmTrace subtrace(int start, int end){
		LmTrace newTrace = new LmTrace();
		newTrace.inputs = new ArrayList<String>(inputs.subList(start, end));
		newTrace.outputs = new ArrayList<String>(outputs.subList(start, end));
		return newTrace;
	}
	
	public String toString(){
		StringBuilder s = new StringBuilder();
		for (int i=0; i<size(); i++){
			s.append(inputs.get(i) + "/" +outputs.get(i) + " ");
		}
		return s.toString();
	}
	
	public int hashCode(){
		int h = 0;
		for (int i=0; i<size(); i++){
			h += i*getInput(i).hashCode();
		}
		return h;
	}
	
	public boolean equals(LmTrace o){
		boolean r = outputs.equals(o.outputs) && inputs.equals(o.inputs);
		return  r;
	}
	
	public boolean equals(Object o){
		if (o instanceof LmTrace)
			return equals((LmTrace) o);
		return false;
	}
}
