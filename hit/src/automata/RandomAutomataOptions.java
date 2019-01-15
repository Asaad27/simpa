package automata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import options.IntegerOption;
import options.OptionTree;
import options.RandomOption;

public class RandomAutomataOptions {
	List<OptionTree> options = new ArrayList<>();

	protected void addSubOption(OptionTree option) {
		options.add(option);
	}

	public List<OptionTree> getOptions() {
		return Collections.unmodifiableList(options);
	}

	private final RandomOption rand = new RandomOption("--automat-seed",
			"seed used to initialize random to generate automata");
	private final IntegerOption statesNumber = new IntegerOption("--nb-states",
			"number of states for the generated automaton", 10);
	private final IntegerOption inputsNumber = new IntegerOption("--nb-inputs",
			"number of inputs for the generated automaton", 2);
	private final IntegerOption outputsNumber = new IntegerOption(
			"--nb-outputs", "number of outputs for the generated automaton", 2);

	public int getStatesNumber() {
		return statesNumber.getValue();
	}

	public int getInputsNumber() {
		return inputsNumber.getValue();
	}

	public int getOutputsNumber() {
		return outputsNumber.getValue();
	}

	public RandomOption getRand() {
		return rand;
	}

	public RandomAutomataOptions() {
		addSubOption(statesNumber);
		addSubOption(inputsNumber);
		addSubOption(outputsNumber);
		addSubOption(rand);
	}
}