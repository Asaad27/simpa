package automata.mealy;

import learner.mealy.LmTrace;

/**
 * This interface is mostly used to specify template type of
 * {@link GenericSequence}.
 * 
 * @author Nicolas BREMOND
 *
 */
public interface GenericInputSequence extends GenericSequence<String, String> {
	public interface Iterator extends GenericSequence.Iterator<String, String> {
		/**
		 * @param lastOutput
		 *            same as {@link #getResponse(String)}
		 * @return a {@link GenericInputSequence}
		 * @see GenericSequence.Iterator#getResponse(Object)
		 */
		GenericOutputSequence getOutputResponse(String lastOutput);
	}

	public interface GenericOutputSequence extends GenericResponse<String> {

		/**
		 * get the sequence of output symbols.
		 * 
		 * @return the sequence of output symbols
		 */
		OutputSequence toFixedOutput();
	}

	/**
	 * @return an {@link GenericInputSequence.Iterator}
	 * @see GenericSequence#iterator()
	 */
	Iterator inputIterator();

	/**
	 * build the trace for this sequence with the given output.
	 * 
	 * @param outSeq
	 *            the output observed
	 * @return a trace representing the execution of this sequence leading to
	 *         the given output.
	 */
	LmTrace buildTrace(GenericOutputSequence outSeq);
}
