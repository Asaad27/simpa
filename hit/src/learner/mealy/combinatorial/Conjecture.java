package learner.mealy.combinatorial;

import java.util.HashMap;
import automata.Automata;
import automata.Transition;
import automata.mealy.MealyTransition;
import drivers.Driver;
import learner.mealy.LmConjecture;

class Conjecture extends LmConjecture{
	private static final long serialVersionUID = 4982526952134622520L;
	
	Driver driver;
	public Conjecture(Driver d) {
		super(d);
		driver = d;
	}

	public Conjecture(Conjecture c){
		super(c.driver);
		driver = c.driver;
		states = c.states;
		transitions = new HashMap<Integer, MealyTransition>(c.transitions);
		for (MealyTransition t : c.getTransitions())
			addTransition((Transition) t);
	}
}
