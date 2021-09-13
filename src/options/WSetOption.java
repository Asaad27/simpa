/********************************************************************************
 * Copyright (c) 2019 Institut Polytechnique de Grenoble 
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
package options;

import java.util.List;

import automata.mealy.InputSequence;
import drivers.mealy.CompleteMealyDriver;
import options.valueHolders.InputSequenceHolder;

public class WSetOption extends ListOption<InputSequence, InputSequenceHolder> {

	class InputExistanceValidator extends OptionValidator {
		CompleteMealyDriver lastDriver = null;

		@Override
		public void check() {
			clear();
			if (lastDriver == null)
				return;
			List<String> symbols = lastDriver.getInputSymbols();
			for (InputSequence seq : getValues()) {
				for (String input : seq.sequence)
					if (!symbols.contains(input)) {
						setCriticality(CriticalityLevel.WARNING);
						setMessage("input '" + input
								+ "' is not an input for the last driver tried.");
					}
			}
		}

		public void setLastDriver(CompleteMealyDriver d) {
			lastDriver = d;
			check();
		}

	}

	InputExistanceValidator inputValidator = new InputExistanceValidator();

	public WSetOption() {
		super("--M_W-set", "Characterization set ('W-Set')",
				"A set of sequences which distinguishes pairs of states.");
		addValidator(inputValidator);
		setCategory(OptionCategory.ALGO_COMMON);
	}

	@Override
	protected InputSequenceHolder createSimpleHolder() {
		return new InputSequenceHolder("Characterization sequence",
				"A sequence of inputs which distinguishes at least two states of the automaton.");
	}

	@Override
	protected String getAddButtonText() {
		return "Add new sequence";
	}

	public void updateWithDriver(CompleteMealyDriver d) {
		inputValidator.setLastDriver(d);
		validateSelectedTree();
	}

}
