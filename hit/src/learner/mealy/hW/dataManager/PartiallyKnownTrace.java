/********************************************************************************
 * Copyright (c) 2015,2019 Institut Polytechnique de Grenoble 
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

import learner.mealy.LmTrace;
import main.simpa.Options;

import java.util.List;

import tools.loggers.LogManager;
import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.distinctionStruct.Characterization;
import automata.mealy.distinctionStruct.DistinctionStruct;

/**
 * This class aim to replace the K set.
 */
public class PartiallyKnownTrace {
	private final FullyQualifiedState start;
	private final LmTrace transition; //this is probably in I \cup W. Read algorithm carefully to be sure 
	private Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> WResponses;
	
	public PartiallyKnownTrace(FullyQualifiedState start, LmTrace transition,
			DistinctionStruct<? extends GenericInputSequence, ? extends GenericOutputSequence> W) {
		this.start = start;
		this.transition = transition;
		WResponses = W.getEmptyCharacterization();
	}
	
	protected List<? extends GenericInputSequence> getUnknownPrints() {
		return WResponses.getUnknownPrints();
	}

	protected Iterable<? extends GenericInputSequence> unknownPrints() {
		return WResponses.unknownPrints();
	}

	/**
	 * get the characterization. The characterization returned shouldn't be
	 * modified.
	 * 
	 * @return the characterization of state at end of transition.
	 */
	Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> getCharacterization() {
		return WResponses;
	}

	protected void addPrint(final LmTrace print) {
		assert WResponses.acceptNextPrint(print);

		WResponses.addPrint(print);
		if (Options.getLogLevel() != Options.LogLevel.LOW)
			LogManager.logInfo("New print(=a response to W input) found : " + start + " followed by " + transition + " → " + print);
		if (Options.getLogLevel() == Options.LogLevel.ALL)
			LogManager.logInfo("K is now : " + SimplifiedDataManager.instance.getK());
		if (WResponses.isComplete()) {
			//we have totally found a transition
			FullyQualifiedState state = SimplifiedDataManager.instance.getFullyQualifiedState(WResponses);
			FullyKnownTrace t = new FullyKnownTrace(start, transition, state);
			SimplifiedDataManager.instance.addFullyKnownTrace(t);//TODO avoid loop in this call
		}
	}
	
	public LmTrace getTransition(){
		return transition;
	}

	public FullyQualifiedState getStart() {
		return start;
	}
	
	public String toString(){
		StringBuilder s = new StringBuilder();
		for (LmTrace t : WResponses.knownResponses()) {
				s.append("(" + start + ", " + transition + ", " + t + "),");
		}
		return s.toString();
	}
}
