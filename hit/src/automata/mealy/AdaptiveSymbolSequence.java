/**
 * 
 */
package automata.mealy;

import learner.mealy.LmTrace;

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

	public void extend(LmTrace trace) {
		extend(trace.getInputsProjection().sequence,
				trace.getOutputsProjection().sequence);
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
		OutputSequence outputSeq = new OutputSequence();
		outputSeq.sequence.addAll(getOutputsList());
		InputSequence inputSeq = new InputSequence();
		inputSeq.sequence.addAll(getInputsList());
		return new LmTrace(inputSeq, outputSeq);
	}
}
