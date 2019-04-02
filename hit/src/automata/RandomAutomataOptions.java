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
package automata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import options.IntegerOption;
import options.OptionCategory;
import options.OptionTree;
import options.RandomOption;
import tools.RandomGenerator;

public class RandomAutomataOptions {
	List<OptionTree> options = new ArrayList<>();

	protected void addSubOption(OptionTree option) {
		options.add(option);
		option.setCategoryIfUndef(OptionCategory.STATS);
	}

	public List<OptionTree> getOptions() {
		return Collections.unmodifiableList(options);
	}

	private final RandomOption rand;
	private final IntegerOption statesNumber;
	private final IntegerOption inputsNumber;
	private final IntegerOption outputsNumber;

	public int getStatesNumber() {
		return statesNumber.getValue();
	}

	public int getInputsNumber() {
		return inputsNumber.getValue();
	}

	public int getOutputsNumber() {
		return outputsNumber.getValue();
	}

	public RandomGenerator getRand() {
		return rand.getRand();
	}

	public RandomAutomataOptions() {
		rand = new RandomOption("--Sgeneration_seed",
				"seed used to initialize random to generate automata");
		statesNumber = new IntegerOption("--Snb_states", "number of states",
				"Number of states for the generated automaton.", 10);
		inputsNumber = new IntegerOption("--Snb_inputs",
				"number of input symbols",
				"Number of inputs for the generated automaton.", 2);
		outputsNumber = new IntegerOption("--Snb_outputs",
				"number of output symbols",
				"Number of outputs for the generated automaton.", 2);
		addSubOption(statesNumber);
		addSubOption(inputsNumber);
		addSubOption(outputsNumber);
		addSubOption(rand);
	}
}
