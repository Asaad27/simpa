/********************************************************************************
 * Copyright (c) 2012,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 *     Nicolas BREMOND
 ********************************************************************************/
package drivers.mealy.transparent;

import java.util.List;

import tools.Utils;
import automata.mealy.Mealy;
import examples.mealy.RandomMealy;
import examples.mealy.RandomMealy.RandomOutputOptions;
import options.BooleanOption;
import options.IntegerOption;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import options.RandomOption;
import options.automataOptions.DriverChoice;
import options.automataOptions.DriverChoiceItem;

public class RandomMealyDriver extends TransparentMealyDriver {

	public static class RandomDriverOption
			extends DriverChoiceItem<RandomMealyDriver> {
		private BooleanOption stronglyConnected = new BooleanOption(
				"generate strongly connected", "DRnd_connected",
				"Generate transition in order to build a strongly connected automaton.") {
			@Override
			protected void makeArgumentDescriptors(String argument) {
				super.makeArgumentDescriptors(argument);
				disableArgumentDescriptor = new ArgumentDescriptor(
						AcceptedValues.NONE, "--DRnd_not_connected", this);
			};
		};
		private IntegerOption stateNumber = new IntegerOption(
				"--DRnd_states", "number of states",
				"Number of states in the random automaton.", 10);
		private IntegerOption inputNumber = new IntegerOption("--DRnd_inputs",
				"Number of inputs symbols",
				"Number of inputs symbols for the generated automaton.", 2);
		private RandomOutputOptions outputsOptions = new RandomOutputOptions();
		private RandomOption random = new RandomOption("--DRnd_seed",
				"automaton generation");

		public RandomDriverOption(DriverChoice<?> parent) {
			super("Random automaton", "random", parent,
					RandomMealyDriver.class);
			stateNumber.setDefaultValue(10);
			inputNumber.setDefaultValue(2);
			subTrees.add(stronglyConnected);
			subTrees.add(stateNumber);
			subTrees.add(inputNumber);
			subTrees.add(outputsOptions);
		}

		@Override
		public RandomMealyDriver createDriver() {
			return new RandomMealyDriver(RandomMealy.getRandomMealy(
					random.getRand(), stronglyConnected.isEnabled(),
					stateNumber.getValue(), inputNumber.getValue(),
					outputsOptions));
		}

	}

	public RandomMealyDriver(Mealy a) {
		super(a);
	}

	public static List<String> getStatHeaders() {
		return Utils.createArrayList("States", "Inputs", "Outputs", "ARL",
				"Requests", "Duration", "Transitions");
	}

}
