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
package examples.mealy;

import java.util.ArrayList;
import java.util.List;

import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import main.simpa.Options;
import tools.RandomGenerator;
import tools.Utils;
import tools.loggers.LogManager;

public class LockerMealy extends Mealy {
	private static final long serialVersionUID = 6120248765563814109L;

	public enum OnError{
		RESET,
		STAY_IN_PLACE,
		GO_BACK, // go one state back or stay in reset state
		RANDOM_BACK,//go to a random previous state
	}
	public enum OutputPolicy{
		UNLOCK_ONLY,
		UNLOCK_GOOD_BAD,
	}

	public LockerMealy(InputSequence unlock, OnError onError,
			OutputPolicy outputPolicy, List<String> inputSym,
			RandomGenerator rand// when
																				// switching
																				// to
																				// options,
																				// rand
																				// should
																				// be
																				// a
																				// sub-option
																				// of
																				// RANDOM_BACK
	) {
		super("Locker("+onError+";"+outputPolicy+")");
		LogManager.logInfo("Generate LockerMealy");
		for (String s : unlock.sequence)
			if (!inputSym.contains(s))
				inputSym.add(s);
				for (int i = 0 ; i < unlock.getLength(); i++)
					addState(i == 0);

		for (int i = 0 ; i < unlock.getLength() ; i++){
			String s = unlock.sequence.get(i);
			for (String input : inputSym){
				State s1 = states.get(i);
				State s2 = new State("",false);
				if (input.equals(s)){
					if (i+1 == unlock.getLength())
						s2 = states.get(0);
					else
						s2 = states.get(i+1);
				}else{
					switch (onError) {
					case RESET:
						s2 = states.get(0);
						break;
					case STAY_IN_PLACE:
						s2 = s1;
						break;
					case GO_BACK:
						s2 = states.get((i == 0 ) ? 0 : i-1);
						break;
					case RANDOM_BACK:
						s2 = states.get(rand.randInt(i + 1));// rand include
																// adding loop
																// transition
					}
				}

				String output="";

				switch (outputPolicy) {
				case UNLOCK_ONLY:
					if (input.equals(s) && i+1 == unlock.getLength())
						output = "unlocked";
					else
						output = "locked";
					break;
				case UNLOCK_GOOD_BAD:
					if (input.equals(s)){
						if(i+1 == unlock.getLength())
							output = "unlocked";
						else
							output = "progress";
					}else
						output = "fail";
					break;
				}

				addTransition(new MealyTransition(this, s1, s2, input, output));
			}
		}
	}
	
	public static LockerMealy getRandomLockerMealy(OnError onError,
			OutputPolicy outputPolicy, RandomGenerator rand) {
		List<String> inputs = new ArrayList<>();
		String s = "a";
		int inputs_nb = rand.randIntBetween(Options.MININPUTSYM,
				Options.MAXINPUTSYM);
		for (int i = 0; i< inputs_nb; i++){
			inputs.add(s);
			s = Utils.nextSymbols(s);
		}
		int lockerLength = rand.randIntBetween(Options.MINSTATES,
				Options.MAXSTATES);
		InputSequence unlock = new InputSequence();
		for (int i = 0; i < lockerLength; i++)
			unlock.addInput(rand.randIn(inputs));
		return new LockerMealy(unlock, onError, outputPolicy, inputs, rand);
	}
}
