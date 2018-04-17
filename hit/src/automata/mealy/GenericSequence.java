package automata.mealy;

/**
 * A class to represent either an adaptive or a fixed sequence of input/output.
 * 
 * This interface do not extends {@link java.utils.Iterable} although there are
 * almost the same methods. Actually, this interface is not iterable because the
 * next input to apply may depend on the last observed output and thus, we need
 * to provide information during iteration.
 * 
 * @author Nicolas BREMOND
 *
 * @param <InputT>
 *            the type of input. Can be the type of one symbol for adaptive
 *            sequences or the type of one sequence for adaptive sets.
 * @param <OutputT>
 *            the type of output. Can be the type of one symbol for adaptive
 *            sequences or the type of one sequence for adaptive sets.
 */
public interface GenericSequence<InputT, OutputT> {
	/**
	 * @author Nicolas BREMOND
	 * @see java.util.Iterator
	 * @see GenericSequence
	 */
	public static interface Iterator<InputT, OutputT> {
		/**
		 * indicate if there are other inputs to apply.
		 * 
		 * @return true if there are other inputs to apply.
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext();

		/**
		 * Get the next input to apply depending on the previous output
		 * observed.
		 * 
		 * @param lastOutput
		 *            the last output observed. Should be {@code null} for the
		 *            first call and not null for the followings.
		 * @return the next input to apply.
		 * @throws java.util.NoSuchElementException
		 * @see java.util.Iterator#next()
		 */
		public InputT next(OutputT lastOutput);

		/**
		 * Get the response observed at the end of iteration.
		 * 
		 * @param lastOutput
		 *            the last output observed (the one which has not been
		 *            passed through {@link #next()} call.
		 * @return the response observed during iteration.
		 */
		public GenericResponse<OutputT> getResponse(OutputT lastOutput);
	}

	/**
	 * A generic interface for an output observed after a
	 * {@link GenericSequence}. Although all output sequences can be represented
	 * by a list of {@code OutputT} elements, some sequences might have an
	 * internal representation more efficient (for instance a node of a tree is
	 * a better key for a HashMap than a list of {@code OutputT}).
	 * 
	 * @author Nicolas BREMOND
	 */
	public static interface GenericResponse<OutputT> {
		public <InputT> boolean checkCompatibilityWith(
				GenericSequence<InputT, OutputT> in);
	}

	/**
	 * Get an iterator from the start of this sequence.
	 * 
	 * @return an {@link Iterator}
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<InputT, OutputT> iterator();

	/**
	 * indicate that this sequence is empty.
	 * 
	 * @return true if there is no input to apply.
	 */
	public boolean isEmpty();

	/**
	 * get the maximum depth of the sequence (ie depth of tree for adaptive
	 * sequence and length for fixed sequence).
	 * 
	 * @return the length of longest input sequence applied.
	 */
	int getMaxLength();
}
