package drivers.mealy;

import java.util.ArrayList;
import java.util.List;

import automata.mealy.InputSequence;
import examples.mealy.ZQPaper0730;

public class ZQPaper0730Driver extends AutomatonMealyDriver {

	public ZQPaper0730Driver() {
		super(ZQPaper0730.getAutomata());
	}

	/**
	 * This is a legacy method, it is unused I left it here because those CE
	 * might be important.
	 */
	protected List<InputSequence> getForcedCE() {
		List<InputSequence> seq = new ArrayList<InputSequence>();

		InputSequence ce = new InputSequence();
		ce.addInput("a");
		ce.addInput("a");
		ce.addInput("b");
		ce.addInput("b");
		ce.addInput("b");
		ce.addInput("a");
		ce.addInput("b");
		ce.addInput("b");
		ce.addInput("a");
		seq.add(ce);

		return seq;
	}
}
