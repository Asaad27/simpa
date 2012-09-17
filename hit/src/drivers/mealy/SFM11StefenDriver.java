package drivers.mealy;

import java.util.ArrayList;
import java.util.List;

import automata.mealy.InputSequence;
import examples.mealy.SFM11Stefen;

public class SFM11StefenDriver extends MealyDriver{

	public SFM11StefenDriver() {
		super(SFM11Stefen.getAutomata());
	}
	
	protected List<InputSequence> getForcedCE() {
		List<InputSequence> forcedCE = new ArrayList<InputSequence>();
		
		InputSequence seq = new InputSequence();
		seq.addInput("pod");
		seq.addInput("water");
		seq.addInput("pod");
		seq.addInput("water");
		seq.addInput("button");
		forcedCE.add(seq);
		
		seq = new InputSequence();
		seq.addInput("clean");
		seq.addInput("clean");
		seq.addInput("pod");
		seq.addInput("water");
		seq.addInput("water");
		seq.addInput("button");
		seq.addInput("clean");
		forcedCE.add(seq);
		
		return forcedCE;
	}
}
