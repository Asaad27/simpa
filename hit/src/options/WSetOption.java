package options;

import java.util.List;

import automata.mealy.InputSequence;
import drivers.mealy.MealyDriver;
import options.valueHolders.InputSequenceHolder;

public class WSetOption extends ListOption<InputSequence, InputSequenceHolder> {

	class InputExistanceValidator extends OptionValidator {
		MealyDriver lastDriver = null;

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

		public void setLastDriver(MealyDriver d) {
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

	public void updateWithDriver(MealyDriver d) {
		inputValidator.setLastDriver(d);
		validateSelectedTree();
	}

}
