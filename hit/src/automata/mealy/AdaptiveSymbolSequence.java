/**
 * 
 */
package automata.mealy;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import learner.mealy.LmTrace;
import tools.GraphViz;

/**
 * @author Nicolas BREMOND
 *
 */
public class AdaptiveSymbolSequence extends AdaptiveStructure<String, String>
		implements GenericInputSequence,
		GenericInputSequence.GenericOutputSequence {

	public AdaptiveSymbolSequence() {
		super();
	}

	@Override
	protected AdaptiveStructure<String, String> createNewNode() {
		return new AdaptiveSymbolSequence();
	}

	@Override
	protected AdaptiveSymbolSequence clone_local(
			Map<String, String> clonedOutputs) {
		AdaptiveSymbolSequence result = new AdaptiveSymbolSequence();
		result.input = input;// String are immutable and can be shared
		for (String key : getKnownOutputs()) {
			clonedOutputs.put(key, key);// String are immutable and can be
										// shared between trees.
		}
		return result;
	}

	@Override
	protected boolean checkCompatibility(String inputT, String outputT) {
		return true;
	}

	@Override
	public AdaptiveSymbolSequence getFather() {
		return (AdaptiveSymbolSequence) super.getFather();
	}

	@Override
	public AdaptiveSymbolSequence getChild(String output) {
		return (AdaptiveSymbolSequence) super.getChild(output);
	}

	public AdaptiveSymbolSequence extend(LmTrace trace) {
		return (AdaptiveSymbolSequence) extend(
				trace.getInputsProjection().sequence,
				trace.getOutputsProjection().sequence);
	}

	@Override
	public boolean hasPrefix(LmTrace possiblePrefix) {
		return hasPrefix(possiblePrefix.getInputsProjection().sequence,
				possiblePrefix.getOutputsProjection().sequence);
	}

	@Override
	public boolean hasAnswer(LmTrace possibleAnswer) {
		return hasAnswer(possibleAnswer.getInputsProjection().sequence,
				possibleAnswer.getOutputsProjection().sequence);
	}

	public AdaptiveSymbolSequence getAnswer(LmTrace possibleAnswer) {
		return (AdaptiveSymbolSequence) getAnswer(
				possibleAnswer.getInputsProjection().sequence,
				possibleAnswer.getOutputsProjection().sequence);
	}

	@Override
	public void extendsWith(LmTrace newSeq) {
		extend(newSeq);
	}

	class InputIterator extends AdaptiveStructure<String, String>.Iterator
			implements GenericInputSequence.Iterator {
		@Override
		public AdaptiveSymbolSequence getResponse() {
			return (AdaptiveSymbolSequence) super.getResponse();
		}
	}

	@Override
	public automata.mealy.GenericInputSequence.Iterator inputIterator() {
		return new InputIterator();
	}

	@Override
	public OutputSequence toFixedOutput() {
		OutputSequence seq = new OutputSequence();
		seq.sequence.addAll(getOutputsList());
		;
		return seq;
	}

	@Override
	public LmTrace buildTrace(GenericOutputSequence outSeq) {
		assert outSeq.checkCompatibilityWith(this);
		AdaptiveSymbolSequence outNode = (AdaptiveSymbolSequence) outSeq;
		assert outNode.isFinal();
		assert isRoot() : "trace from sub sequence is not implemented";
		OutputSequence outputSeq = new OutputSequence();
		outputSeq.sequence.addAll(outNode.getOutputsList());
		InputSequence inputSeq = new InputSequence();
		inputSeq.sequence.addAll(outNode.getInputsList());
		return new LmTrace(inputSeq, outputSeq);
	}

	@Override
	public AdaptiveSymbolSequence getFullSequence() {
		return (AdaptiveSymbolSequence) super.getFullSequence();
	}

	@Override
	public String toString() {
		if (isFinal() && isRoot())
			return "epsilon";
		if (isFinal())
			return getFullSequence().buildTrace(this).toString();
		return super.toString();
	}

	@Override
	protected void dot_appendNode(Writer writer) throws IOException {
		if (input != null) {
			super.dot_appendNode(writer);
		} else {
			writer.write(getDotName() + "[label="
					+ GraphViz.id2DotAuto("end of sequence") + "];\n");
		}
	}
}
