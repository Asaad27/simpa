package drivers.mealy;

import java.util.List;

import tools.Utils;
import automata.mealy.Mealy;
import examples.mealy.RandomMealy;

public class RandomMealyDriver extends MealyDriver {

	public RandomMealyDriver() {
		super(new RandomMealy());
		//Utils.setSeed(((RandomMealy) automata).getSeed());
	}

	public RandomMealyDriver(Mealy a) {
		super(a);
		//Utils.setSeed(((RandomMealy) automata).getSeed());
	}

	public static List<String> getStatHeaders() {
		return Utils.createArrayList("States", "Inputs", "Outputs", "ARL",
				"Requests", "Duration", "Transitions","CElength","Rounds","Seeds","Number of suffixes","number of states in conjecture");
	}

}
