package learner.mealy.hW.dataManager;

import java.util.ArrayList;
import java.util.List;

import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import learner.mealy.LmTrace;

/**
 * this class store data about W-ND which occurs while merging expected traces
 * from two states.
 * 
 * @author Nicolas BREMOND
 *
 */
public class InconsistancyWhileMergingExpectedTracesException extends
		RuntimeException {

	private static final long serialVersionUID = 5548780977037123061L;
	private String otherOutput;
	private List<String> reversedInputs = new ArrayList<>();
	private List<String> reversedOutputs = new ArrayList<>();
	private List<FullyQualifiedState> reversedStates = new ArrayList<>();

	private LmTrace trace = null;
	private List<FullyQualifiedState> orderedStates = null;

	InconsistancyWhileMergingExpectedTracesException(String input,
			String output1, String output2) {
		otherOutput = output2;
		addPreviousState(null, input, output1);
	}

	InconsistancyWhileMergingExpectedTracesException(FullyQualifiedState state,
			InputSequence inSeq, OutputSequence outseq,
			OutputSequence traceOut) {
		int diffPos = 0;
		while (outseq.sequence.get(diffPos)
				.equals(traceOut.sequence.get(diffPos)))
			diffPos++;

		otherOutput = traceOut.sequence.get(diffPos);
		int cur = diffPos;
		while (cur >= 0) {
			addPreviousState(null, inSeq.sequence.get(cur),
					outseq.sequence.get(cur));
			cur--;
		}
		setLastState(state);
	}

	void addPreviousState(FullyQualifiedState state, String input,
			String output) {
		reversedInputs.add(input);
		reversedOutputs.add(output);
		reversedStates.add(state);
		trace = null;
		orderedStates = null;
	}

	void setLastState(FullyQualifiedState state) {
		reversedStates.set(reversedStates.size() - 1, state);
		orderedStates = null;
	}

	void addPreviousState(FullyQualifiedState state, InputSequence inSeq,
			OutputSequence outSeq) {
		assert inSeq.getLength() == outSeq.getLength();
		assert inSeq.getLength() > 0;
		for (int i = inSeq.getLength() - 1; i > 0; i--) {
			addPreviousState(null, inSeq.sequence.get(i),
					outSeq.sequence.get(i));
		}
		addPreviousState(state, inSeq.sequence.get(0), outSeq.sequence.get(0));
	}

	public LmTrace getTrace() {
		if (trace == null) {
			trace = new LmTrace();
			for (int i = reversedInputs.size() - 1; i >= 0; i--) {
				trace.append(reversedInputs.get(i), reversedOutputs.get(i));
			}
		}
		return trace;
	}

	public List<FullyQualifiedState> getStates() {
		if (orderedStates == null) {
			orderedStates = new ArrayList<>(reversedInputs.size());
			for (int i = reversedInputs.size() - 1; i >= 0; i--) {
				orderedStates.add(reversedStates.get(i));
			}
		}
		return orderedStates;
	}

	public String getOtherOutput() {
		return otherOutput;
	}

	public String toString() {
		return "inconsistency while merging expected traces : from state "
				+ getStates().get(0) + ", the trace «" + getTrace()
				+ "» can also end with output «" + getOtherOutput() + "».";
	}

}
