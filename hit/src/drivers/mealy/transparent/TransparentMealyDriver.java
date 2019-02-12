package drivers.mealy.transparent;

import java.util.List;

import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.multiTrace.MultiTrace;
import drivers.mealy.AutomatonMealyDriver;
import learner.mealy.LmConjecture;

public class TransparentMealyDriver extends AutomatonMealyDriver {
	public TransparentMealyDriver(Mealy automata){
		super(automata);
	}
	
	public Mealy getAutomata(){
		return automata;
	}
	
	public State getCurrentState(){
		return currentState;
	}
	
	public InputSequence getShortestCE(LmConjecture conjecture,
			State conjectureState, MultiTrace appliedSequences) {
		assert this.automata != null;
		assert this.currentState != null;
		if (!this.automata.isConnex())
			throw new RuntimeException("automata must be strongly connected");
		List<InputSequence> counterExamples = conjecture.getCounterExamples(
				conjectureState, this.automata, currentState, true);
		if (counterExamples.isEmpty()) {
			return null;
		} else {
			assert counterExamples
					.size() == 1 : "only the shortest CE should be in the list";
			return counterExamples.get(0);
		}
	}
}
