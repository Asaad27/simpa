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
		 * {@inheritDoc}
		 * 
		 * @return a {@link GenericInputSequence}
		 */
		GenericOutputSequence getResponse();
	}

	public interface GenericOutputSequence
			extends GenericResponse<String, String> {

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

	/**
	 * indicate if a trace is a prefix of this sequence.
	 * 
	 * @param possiblePrefix
	 *            the trace to test
	 * @return true if the trace match the start of a possible execution of this
	 *         sequence
	 */
	boolean hasPrefix(LmTrace possiblePrefix);

	/**
	 * Indicate whether a trace is a possible answer to this sequence. This is
	 * semantically equivalent to {@code this.hasPrefix(possibleAnswer)} and
	 * iterating over this sequence with {@code possibleAnswer} leads to end of
	 * this sequence.
	 * 
	 * @param possibleAnswer
	 *            the trace to test
	 * @return true if iterating over this sequence using outputs from
	 *         {@code posibleAnswer} give the same inputs than
	 *         {@code possibleAnswer} and the end of this sequence and trace are
	 *         reached simultaneously.
	 */
	boolean hasAnswer(LmTrace possibleAnswer);

	/**
	 * Extends this sequence to let {@code newSeq} be a possible execution of
	 * this sequence.
	 * 
	 * @param newSeq
	 *            the trace use to extends this sequence.
	 */
	void extendsWith(LmTrace newSeq);
}
