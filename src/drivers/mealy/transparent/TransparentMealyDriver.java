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
package drivers.mealy.transparent;

import java.util.List;

import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.multiTrace.MultiTrace;
import drivers.mealy.AutomatonMealyDriver;
import learner.mealy.LmConjecture;

public class TransparentMealyDriver extends AutomatonMealyDriver {
	public TransparentMealyDriver(Mealy automata){
		super(automata);
	}
	
	public Mealy getAutomata(){
		return automata;
	}
	
	public State getCurrentState(){
		return currentState;
	}
	
	public InputSequence getShortestCE(LmConjecture conjecture,
			State conjectureState, MultiTrace appliedSequences) {
		assert this.automata != null;
		assert this.currentState != null;
		if (!this.automata.isConnex())
			throw new RuntimeException("automata must be strongly connected");
		List<InputSequence> counterExamples = conjecture.getCounterExamples(
				conjectureState, this.automata, currentState, false);
		if (counterExamples.isEmpty()) {
			return null;
		} else {
			InputSequence shortest = counterExamples.get(0);
			for (InputSequence seq : counterExamples) {
				if (seq.getLength() < shortest.getLength())
					shortest = seq;
			}
			return shortest;
		}
	}
}
