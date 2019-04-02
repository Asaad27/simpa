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
package learner.mealy.combinatorial;

import java.util.HashMap;

import automata.Transition;
import automata.mealy.MealyTransition;
import drivers.mealy.MealyDriver;
import learner.mealy.LmConjecture;

public class Conjecture extends LmConjecture{
	private static final long serialVersionUID = 4982526952134622520L;

	public Conjecture(MealyDriver d) {
		super(d);
	}

	/**
	 * build a «copy» of another conjecture.
	 * The two Conjectures will have the same states but adding a transition to one will not impact the other.
	 * @param c
	 */
	public Conjecture(Conjecture c){
		super(c.driver);
		driver = c.driver;
		states = c.states;
		transitions = new HashMap<>(c.transitions);
		for (MealyTransition t : c.getTransitions())
			addTransition((Transition) t);//TODO this is not pretty, How to access to Automata.transition ?
	}
}
