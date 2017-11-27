package learner.mealy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import drivers.Driver;
import tools.loggers.LogManager;


public class LmConjecture extends automata.mealy.Mealy {
	private static final long serialVersionUID = -6920082057724492261L;
	private List<String> inputSymbols;

	public LmConjecture(Driver d) {
		super(d.getSystemName());
		this.inputSymbols = d.getInputSymbols();
	}

	public List<String> getInputSymbols() {
		return inputSymbols;
	}
	
	/**
	 * check if a conjecture has all transitions
	 * @return true if there is a transition from any state with any input symbol
	 */
	public boolean isFullyKnown(){
		for (State s : getStates())
			for (String i : inputSymbols)
				if (getTransitionFromWithInput(s, i) == null)
					return false;
		return true;
	}

	/**
	 * get counter examples to distinguish conjecture from another automata.
	 * 
	 * suppose that input symbols of conjecture are the same as automata
	 * (otherwise it will check for counter example matching only symbols from
	 * conjecture)
	 * 
	 * This method is implemented by walking in real automata and checking if we
	 * can associate a state of conjecture to each real state. This mean that
	 * the conjecture can have extra states which will not be checked.
	 * 
	 * @param conjectureStartingState
	 *            state to start algorithm in conjecture. Must be equivalent to
	 *            realStartingState
	 * @param realAutomata
	 *            the reference automata. Must be strongly connected (or you
	 *            expose yourself to infinite loop)
	 * @param realStartingState
	 *            state to start algorithm in reference automata. Must be
	 *            equivalent to conjectureStartingState
	 * @return a list of {@link InputSequence} which will provide different
	 *         output if applied in conjecture and real automata from the
	 *         provided starting states. This list include all counter examples
	 *         which are not containing loops.
	 */
	public List<InputSequence> getAllCounterExamples(
			State conjectureStartingState, Mealy realAutomata,
			State realStartingState) {
		assert realAutomata.isConnex();

		LmConjecture conjecture = this;
		// to each real state, we associate a state in conjecture
		Map<State, State> realToConjecture = new HashMap<>();
		// to each real state, we associate a path to reach them
		Map<State, InputSequence> accesPath = new HashMap<>();
		List<InputSequence> counterExamples = new ArrayList<>();

		realToConjecture.put(realStartingState, conjectureStartingState);
		accesPath.put(realStartingState, new InputSequence());
		LinkedList<State> uncheckedStates = new LinkedList<>();

		for (State realState : realAutomata.getStates()) {
			uncheckedStates.add(realState);
			// another way to do this would be to add states in
			// 'uncheckedStates' each time we add them in 'realToConjecture' but
			// adding all states also included unreachable states if the real
			// automata is not strongly connected
		}

		while (!uncheckedStates.isEmpty()) {
			State realState = uncheckedStates.pollFirst();
			State conjectureState = realToConjecture.get(realState);
			if (conjectureState == null) {
				uncheckedStates.add(realState);
				// note that if real automata is not strongly connected, the
				// unreachable states will make an infinite loop here
				continue;
			}
			for (String i : inputSymbols) {
				MealyTransition realTransition = realAutomata
						.getTransitionFromWithInput(realState, i);
				MealyTransition conjectureTransition = conjecture
						.getTransitionFromWithInput(conjectureState, i);
				State realTarget = realTransition.getTo();
				State conjectureTarget = conjectureTransition.getTo();
				String realOutput = realTransition.getOutput();
				String conjectureOutput = conjectureTransition.getOutput();

				// first check if transition in both automata provide same
				// output
				if (!realOutput.equals(conjectureOutput)) {
					InputSequence counterExample = accesPath.get(realState)
							.clone();
					counterExample.addInput(i);
					counterExamples.add(counterExample);
				}
				// now map the realTarget to a state in conjecture (if not
				// already done)
				State mappedTarget = realToConjecture.get(realTarget);
				if (mappedTarget == null) {
					realToConjecture.put(realTarget, conjectureTarget);
					mappedTarget = conjectureTarget;
					InputSequence path = accesPath.get(realState).clone();
					path.addInput(i);
					accesPath.put(realTarget, path);
				}
				// then check if transition in both automata lead in same state
				if (mappedTarget != conjectureTarget) {
					// if that states in conjecture are not equivalent, we
					// should find a distinction sequence.
					InputSequence distinctionSequence = conjecture
							.getDistinctionSequence(conjectureTarget,
									mappedTarget);
					if (distinctionSequence == null) {
						// the two states are equivalents
						LogManager
								.logWarning("We found two equivalent states in conjecture while looking for counter example (states "
										+ conjectureTarget
										+ " and "
										+ mappedTarget + ").");
						continue;
					}
					// now, we have realTarget which can be reach by to
					// sequences and this two sequences leads in two different
					// states in conjecture.
					OutputSequence realDistinctionOutput = realAutomata
							.simulateOutput(realTarget, distinctionSequence);
					InputSequence counterExample = new InputSequence();
					if (!conjecture.simulateOutput(mappedTarget,
							distinctionSequence).equals(realDistinctionOutput)) {
						// We use the path which lead to realTarget and
						// mappedTarget (the first path discovered)
						counterExample.addInputSequence(accesPath
								.get(realTarget));
					} else {
						assert !conjecture.simulateOutput(conjectureTarget,
								distinctionSequence).equals(realDistinctionOutput);
						// We have to use the other path which lead to realTarget
						// conjectureTarget
						counterExample.addInputSequence(accesPath
								.get(realState));
						counterExample.addInput(i);
					}
					counterExample.addInputSequence(distinctionSequence);
					counterExamples.add(counterExample);
				}
			}
		}
		return counterExamples;
	}
}
