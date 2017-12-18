package drivers.mealy.transparent;

import java.util.List;

import tools.Utils;
import automata.mealy.Mealy;
import examples.mealy.RandomMealy;
import examples.mealy.RandomMealy.OUTPUT_STYLE;

public class RandomOneOutputDiffMealyDriver extends TransparentMealyDriver {

	public RandomOneOutputDiffMealyDriver() {
		super(RandomMealy.getConnexRandomMealy(OUTPUT_STYLE.ONE_DIFF_PER_STATE));
		Utils.setSeed(((RandomMealy) automata).getSeed());
	}

	public RandomOneOutputDiffMealyDriver(Mealy a) {
		super(a);
		Utils.setSeed(((RandomMealy) automata).getSeed());
	}

	public static List<String> getStatHeaders() {
		return Utils.createArrayList("States", "Inputs", "Outputs", "ARL",
				"Requests", "Duration", "Transitions");
	}

}
