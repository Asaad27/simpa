package drivers.mealy;

import java.util.List;

import tools.StandaloneRandom;
import tools.Utils;
import automata.mealy.Mealy;
import examples.mealy.RandomMealy;

public class RandomMealyDriver extends MealyDriver {

	public RandomMealyDriver() {
		super(new RandomMealy(new StandaloneRandom()));// TODO option for seed
	}

	public RandomMealyDriver(Mealy a) {
		super(a);
	}

	public static List<String> getStatHeaders() {
		return Utils.createArrayList("States", "Inputs", "Outputs", "ARL",
				"Requests", "Duration", "Transitions");
	}

}
