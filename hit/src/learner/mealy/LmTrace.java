package learner.mealy;

import java.util.ArrayList;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import learner.mealy.noReset.NodeSplittingTree;

public class LmTrace {
	// at any time, inputs and outputs must have the same length
	private InputSequence inputs;
	private OutputSequence outputs;

	public LmTrace(String x, String o) {
		this();
		append(x, o);
	}

	public LmTrace(InputSequence x, OutputSequence o) {
		this();
		append(x, o);
	}

	public LmTrace() {
		inputs = new InputSequence();
		outputs = new OutputSequence();
	}

	public String getInput(int pos) {
		return inputs.sequence.get(pos);
	}

	public OutputSequence getOutputsProjection() {
		return outputs;
	}

	public String getOutput(int pos) {
		return outputs.sequence.get(pos);
	}

	public InputSequence getInputsProjection() {
		return inputs;
	}

	public LmTrace getIO(int pos) {
		LmTrace newTrace = new LmTrace();
		newTrace.append(inputs.sequence.get(pos), outputs.sequence.get(pos));
		return newTrace;
	}

	public void append(InputSequence inputs, OutputSequence outputs) {
		assert inputs.getLength() == outputs.getLength();
		this.inputs.addInputSequence(inputs);
		this.outputs.addOutputSequence(outputs);
	}

	public void append(String input, String output) {
		inputs.addInput(input);
		outputs.addOutput(output);
	}

	public void append(LmTrace other) {
		append(other.inputs, other.outputs);
	}

	public int size() {

		return inputs.getLength();// =outputs.size()

	}

	public Boolean hasSuffix(int pos, LmTrace other) {
		// TODO check if there is a better way to do that using input sequences
		if (pos + other.size() > size())
			return false;
		for (int i = 0; i < other.size(); i++) {
			if (!inputs.sequence.get(pos + i).equals(other.inputs.sequence.get(i))
					|| !outputs.sequence.get(pos + i).equals(other.outputs.sequence.get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * get a subtrace of current trace
	 * 
	 * @param start
	 *            the position of the first element to keep
	 * @param end
	 *            the position of the first element to not take
	 * @return an empty Trace if the asked subtrace is not in the current trace
	 */
	public LmTrace subtrace(int start, int end) {
		LmTrace newTrace = new LmTrace();
		if (end > size())
			return newTrace;
		if (start < 0)
			return newTrace;
		newTrace.inputs = new InputSequence();
		newTrace.inputs.sequence = new ArrayList<String>(inputs.sequence.subList(start, end));
		newTrace.outputs = new OutputSequence();
		newTrace.outputs.sequence = new ArrayList<String>(outputs.sequence.subList(start, end));
		return newTrace;
	}




	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < size(); i++) {
			s.append(inputs.sequence.get(i) + "/" + outputs.sequence.get(i) + " ");
		}
		return s.toString();
	}

	public int hashCode() {
		int h = 0;
		for (int i = 0; i < size(); i++) {
			h += i * getInput(i).hashCode();
		}
		return h;
	}

	public boolean equals(LmTrace o) {
		boolean r = outputs.equals(o.outputs) && inputs.equals(o.inputs);
		return r;
	}

	public boolean equals(Object o) {
		if (o instanceof LmTrace)
			return equals((LmTrace) o);
		return false;
	}
}
