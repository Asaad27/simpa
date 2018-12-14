package drivers.mealy.transparent;

import java.util.ArrayList;
import java.util.Arrays;

import automata.mealy.InputSequence;
import examples.mealy.LockerMealy;
import examples.mealy.LockerMealy.OnError;
import examples.mealy.LockerMealy.OutputPolicy;

public class LockerMealyDriver extends TransparentMealyDriver {
	public LockerMealyDriver(){
		super(new LockerMealy(
				new InputSequence("a").addInput("b").addInput("a"),
				OnError.RANDOM_BACK,
				OutputPolicy.UNLOCK_ONLY,
				new ArrayList<String>(Arrays.asList(new String[] { "other" })),
				null
				));
		automata.exportToDot();
	}
}
