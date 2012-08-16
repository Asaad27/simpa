package drivers.mealy;

import java.util.ArrayList;
import java.util.List;

import automata.mealy.InputSequence;
import examples.mealy.ZQPaper;

public class ZQDriver extends MealyDriver{

	public ZQDriver() {
		super(ZQPaper.getAutomata());
	}
	
	protected List<InputSequence> getForcedCE() {
		List<InputSequence> seq = new ArrayList<InputSequence>();
		
		InputSequence ce = new InputSequence();
		ce.addInput("a");
		ce.addInput("b");
		ce.addInput("a");
		ce.addInput("a");
		seq.add(ce);
		
		ce = new InputSequence();
		ce.addInput("a");
		ce.addInput("a");
		ce.addInput("c");
		ce.addInput("c");	
		seq.add(ce);

		return seq;
	}
}
