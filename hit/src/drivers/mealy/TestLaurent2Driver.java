package drivers.mealy;

import java.util.ArrayList;
import java.util.List;

import automata.mealy.InputSequence;
import examples.mealy.TestLaurent2;

public class TestLaurent2Driver extends MealyDriver {

	public TestLaurent2Driver() {
		super(TestLaurent2.getAutomata());
	}
	
	protected List<InputSequence> getForcedCE() {
		List<InputSequence> ces = new ArrayList<InputSequence>();
		InputSequence seq1 = new InputSequence();
		seq1.addInput("c");
		seq1.addInput("c");
		seq1.addInput("c");
		seq1.addInput("b");
		seq1.addInput("b");
		ces.add(seq1);
		return ces;
	}
}
