package drivers.mealy;

import java.util.List;
import automata.mealy.InputSequence;
import examples.mealy.SFM11Stefen;

public class SFM11StefenDriver extends MealyDriver{

	public SFM11StefenDriver() {
		super(SFM11Stefen.getAutomata());
	}
	
	protected List<InputSequence> getForcedCE() {
		return null;
	}
}
