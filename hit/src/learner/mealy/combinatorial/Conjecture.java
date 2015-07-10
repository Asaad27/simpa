package learner.mealy.combinatorial;

import java.util.HashMap;
import automata.Transition;
import automata.mealy.MealyTransition;
import drivers.Driver;
import learner.mealy.LmConjecture;

public class Conjecture extends LmConjecture{
	private static final long serialVersionUID = 4982526952134622520L;

	Driver driver;
	public Conjecture(Driver d) {
		super(d);
		driver = d;
	}

	/**
	 * build a «copy» of another conjecture.
	 * The two Conjectures will have the same states but adding a transition to one will not impact the other.
	 * @param c
	 */
	public Conjecture(Conjecture c){
		super(c.driver);
		driver = c.driver;
		states = c.states;
		transitions = new HashMap<Integer, MealyTransition>(c.transitions);
		for (MealyTransition t : c.getTransitions())
			addTransition((Transition) t);//TODO this is not pretty, How to access to Automata.transition ?
	}
}
