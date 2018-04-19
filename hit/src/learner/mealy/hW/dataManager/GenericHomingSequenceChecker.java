package learner.mealy.hW.dataManager;

import automata.mealy.AdaptiveSymbolSequence;
import automata.mealy.GenericInputSequence;
import automata.mealy.InputSequence;

public abstract class GenericHomingSequenceChecker {
	/**
	 * Create an object of type {@link GenericHomingSequenceChecker}.
	 * 
	 * @param h
	 *            the homing sequence to check.
	 * @return a {@link FixedHomingSequenceChecker} or an
	 *         {@link AdaptiveHomingSequenceChecker} depending on the type of h
	 */
	static public GenericHomingSequenceChecker getChecker(
			GenericInputSequence h) {
		if (h instanceof InputSequence)
			return new FixedHomingSequenceChecker((InputSequence) h);
		throw new RuntimeException("others types of sequence not implemented");
	}

	public abstract GenericInputSequence getH();

	/**
	 * check if the execution of {@code input} producing the given
	 * {@code output} indicate an inconsistency on homing sequence.
	 * 
	 * @param input
	 *            the input applied on SUI
	 * @param output
	 *            the output observed
	 * @throws GenericHNDException
	 *             if h is not homing (i.e. we observed two times the same
	 *             answer to h followed by same sequence of input and producing
	 *             different outputs).
	 */
	public abstract void apply(String input, String output);
}
