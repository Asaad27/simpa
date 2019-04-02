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
		if (h instanceof AdaptiveSymbolSequence)
			return new AdaptiveHomingSequenceChecker(
					(AdaptiveSymbolSequence) h);
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

	/**
	 * notify the checker that a reset was applied. The checker should go back
	 * in its initial state
	 */
	public abstract void reset();
}
