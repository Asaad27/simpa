package drivers.mealy;

import java.util.List;

import tools.Utils;
import examples.mealy.RandomMealy;

public class RandomMealyDriver extends MealyDriver {
	private int nbStates = 0;
	
	public RandomMealyDriver(RandomMealy a) {
		super(a);
		nbStates = a.getStateCount();
	}
	
	public static List<String> getStatHeaders() {
		return Utils.createArrayList("States", "Inputs", "Outputs", "ARL", "Requests", "Duration");
	}
	
	public List<String> getStats(){
		return Utils.createArrayList(
				String.valueOf(nbStates),
				String.valueOf(getInputSymbols().size()),
				String.valueOf(getOutputSymbols().size()),
				String.valueOf(((float)numberOfAtomicRequest/numberOfRequest)),
				String.valueOf(numberOfRequest),
				String.valueOf(((float)duration/1000000000)));
	}		
}
