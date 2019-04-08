/********************************************************************************
 * Copyright (c) 2018,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 ********************************************************************************/
package automata.mealy;

/**
 * A class to represent either an adaptive or a fixed sequence of input/output.
 * 
 * This interface extends {@link java.utils.Iterable} but should be used with
 * caution. Actually, this interface is not iterable because the next input to
 * apply may depend on the last observed output and thus, we need to provide
 * information during iteration.
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
public interface GenericSequence<InputT, OutputT>
		extends java.lang.Iterable<InputT> {
	/**
	 * @author Nicolas BREMOND
	 * @see java.util.Iterator
	 * @see GenericSequence
	 */
	public static interface Iterator<InputT, OutputT>
			extends java.util.Iterator<InputT> {
		/**
		 * An exception to indicate a missing call to
		 * {@link Iterator#setPreviousOutput(Object)}
		 */
		public static class InvalidCallException extends RuntimeException {
			private static final long serialVersionUID = 6101583213907652025L;

			public InvalidCallException() {
				this("last output was not provided");
			}

			public InvalidCallException(String what) {
				super("Invalid usage of " + Iterator.class + " : " + what);
			}
		}

		/**
		 * Set the response observed after applying the input given by
		 * {@link #next()} This call is mandatory for some implementations (for
		 * instance the adaptive sequence needs to know last output to decide
		 * what is the next input
		 * 
		 * This method must be called only one time after each call to
		 * {@link #next()}
		 * 
		 * @param previousOutput
		 *            the last output observed.
		 */
		public void setPreviousOutput(OutputT previousOutput);

		/**
		 * Indicate whether there are other inputs to apply or not. For some
		 * implementations, {@link #setPreviousOutput(Object)} needs to be
		 * called after {@link #next()} to compute this method.
		 * 
		 * @return true if there are other inputs to apply.
		 * @see java.util.Iterator#hasNext()
		 * @throws InvalidCallException
		 */
		public boolean hasNext();

		/**
		 * Get the next input to apply. For some implementations, the input
		 * depends on on the previous output observed and thus,
		 * {@link #setPreviousOutput(Object)} must have been called before.
		 * 
		 * @return the next input to apply.
		 * @throws java.util.NoSuchElementException
		 * @throws InvalidCallException
		 * @see java.util.Iterator#next()
		 */
		public InputT next();

		/**
		 * Get the response observed at the end of iteration. The object
		 * returned is built with output provided through
		 * {@link #setPreviousOutput(Object)} and thus, those calls should have
		 * been made each time.
		 * 
		 * @return the response observed during iteration.
		 * @throws InvalidCallException
		 */
		public GenericResponse<InputT,OutputT> getResponse();
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
	public static interface GenericResponse<InputT, OutputT> {
		public boolean checkCompatibilityWith(
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
