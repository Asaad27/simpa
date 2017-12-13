package drivers.mealy.transparent;

import java.util.List;

import main.simpa.Options;

import drivers.ExhaustiveGenerator;

import tools.Utils;
import examples.mealy.EnumeratedMealy;

public class EnumeratedMealyDriver extends TransparentMealyDriver implements ExhaustiveGenerator{

	public EnumeratedMealyDriver() {
		super(EnumeratedMealy.getConnexMealy());
		Options.SEED=((EnumeratedMealy) automata).getSeed();
		Utils.setSeed(Options.SEED);
	}

	public static List<String> getStatHeaders() {
		return Utils.createArrayList("States", "Inputs", "Outputs", "ARL",
				"Requests", "Duration", "Transitions");
	}

}
