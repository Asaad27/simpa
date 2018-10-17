package learner.mealy;

import java.util.ArrayList;

import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import main.simpa.Options;

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
	 * Iterate over the given sequence and check whether the inputs applied are
	 * the same as the ones in this trace.
	 * 
	 * Behavior is undefined if this trace has same inputs but is shorter than
	 * (i.e. is a prefix of) the given sequence.
	 * 
	 * @param seq
	 *            the input sequence to check
	 * @return false if the inputs in {@code seq} differs from this trace.
	 */
	public boolean startsWith(GenericInputSequence seq) {
		GenericInputSequence.Iterator it = seq.inputIterator();
		int pos = 0;
		while (it.hasNext()) {
			assert (pos < size()) : "undifed behavior. can be modified to return true or false";
			String input = it.next();
			if (!input.equals(getInput(pos)))
				return false;
			it.setPreviousOutput(getOutput(pos));
			pos++;
		}
		return true;
	}

	/**
	 * Iterate over the given sequence and check whether the inputs applied are
	 * the same as the ones in this trace.
	 * 
	 * @param seq
	 *            the input sequence to check
	 * @return the corresponding output sequence or {@code null} if the input
	 *         sequence do not match (or is shorter than) inputs projections.
	 */
	public GenericOutputSequence getOutput(GenericInputSequence seq) {
		GenericInputSequence.Iterator it = seq.inputIterator();
		int pos = 0;
		while (it.hasNext()) {
			if (pos >= size())
				return null;
			String input = it.next();
			if (!input.equals(getInput(pos)))
				return null;
			it.setPreviousOutput(getOutput(pos));
			pos++;

		}
		return it.getResponse();
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
		if (Options.REDUCE_DISPLAYED_TRACES > 0
				&& size() > Options.REDUCE_DISPLAYED_TRACES) {
			int i = 0;
			while (i < Options.REDUCE_DISPLAYED_TRACES / 2) {
				s.append(inputs.sequence.get(i) + "/" + outputs.sequence.get(i)
						+ " ");
				i++;
			}
			s.append(" … ");
			s.append('.');
			while (i < Options.REDUCE_DISPLAYED_TRACES) {
				s.append(inputs.sequence.get(i) + "/" + outputs.sequence.get(i)
						+ " ");
				i++;
			}

		} else {
			for (int i = 0; i < size(); i++) {
				s.append(inputs.sequence.get(i) + "/" + outputs.sequence.get(i)
						+ " ");
			}
		}
		if (s.length() > 0)
			s.deleteCharAt(s.length() - 1);
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
	
	public LmTrace clone(){
		LmTrace c=new LmTrace();
		c.inputs=inputs.clone();
		c.outputs=outputs.clone();
		return c;
	}
}
