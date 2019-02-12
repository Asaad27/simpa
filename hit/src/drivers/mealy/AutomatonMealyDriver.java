package drivers.mealy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import drivers.mealy.transparent.TransparentMealyDriver;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.loggers.LogManager;

public class AutomatonMealyDriver extends MealyDriver {

	protected Mealy automata;
	protected State currentState;
	/**
	 * record the state before last execution for logging purpose.
	 */
	private State previousState = null;
	protected Set<InputSequence> triedCE;
	private int nbStates = 0;
	private String name = null;

	public AutomatonMealyDriver(Mealy automata) {
		super(automata.getName());
		this.automata = automata;
		triedCE = new HashSet<>();
		this.nbStates = automata.getStateCount();
		this.currentState = automata.getInitialState();
		if (this.currentState == null)
			throw new RuntimeException("the driver has no initial state");
	}

	@Override
	protected void logRequest(String input, String output) {
		LogManager.logRequest(input, output, getNumberOfAtomicRequest(),
				previousState, currentState);
	}

	@Override
	public final String execute_implem(String input) {
		assert currentState != null : "is the initial state of driver specified ?";
		previousState = currentState;
		String output = null;
		if (input.length() > 0) {
			MealyTransition currentTrans = automata
					.getTransitionFromWithInput(currentState, input);
			if (currentTrans != null) {
				output = new String(currentTrans.getOutput());
				currentState = currentTrans.getTo();
			} else {
				output = new String();
			}
		}
		return output;
	}

	@Override
	public List<String> getInputSymbols() {
		List<String> is = new ArrayList<String>();
		for (MealyTransition t : automata.getTransitions()) {
			if (!is.contains(t.getInput()))
				is.add(t.getInput());
		}
		Collections.sort(is);
		return is;
	}

	/**
	 * get the number of states in driver if the driver is transparent.
	 * 
	 * @return the number of states in driver or {@code null} if the driver is
	 *         not transparent.
	 */
	public Integer getStateCount() {
		if (this instanceof TransparentMealyDriver) {
			return ((TransparentMealyDriver) this).getAutomata()
					.getStateCount();
		}
		return null;
	}

	@Override
	public String getSystemName() {
		return name;
	}

	@Override
	public void reset_implem() {
		assert (automata != null);
		currentState = automata.getInitialState();
	}

	/**
	 * compute an input sequence s.t. the output sequence entirely define the
	 * final state
	 * 
	 * @return null if a such sequence cannot be computed
	 * @throws UnableToComputeException
	 */
	@Override
	public InputSequence getHomingSequence() throws UnableToComputeException {
		LogManager.logInfo("Computing homing sequence");
		InputSequence r = new InputSequence();
		boolean found = false;
		while (!found) {
			found = true;
			for (int i = 0; i < automata.getStateCount(); i++) {
				State s1 = automata.getState(i);
				for (int j = i + 1; j < automata.getStateCount(); j++) {
					State s2 = automata.getState(j);
					OutputSequence o1 = automata.apply(r, s1);
					State os1 = automata.applyGetState(r, s1);
					OutputSequence o2 = automata.apply(r, s2);
					State os2 = automata.applyGetState(r, s2);
					if (o1.equals(o2) && os1 != os2) {
						found = false;
						LinkedList<InputSequence> l = new LinkedList<>();
						l.add(new InputSequence());
						boolean foundLocalSeq = false;
						while (!foundLocalSeq) {
							InputSequence current = l.poll();
							if (current.getLength() >= nbStates) {
								LogManager.logInfo(
										"Unable to compute homming sequence because "
												+ os1 + " and " + os2
												+ " have same outputs which leads in differents states");
								LogManager.logInfo(
										"Maybe thoose states are equivalent and you can use "
												+ r
												+ " as homming sequence (be careful, some states have not been tested). But in strict definition of homing sequence, if you got the same output, you must be in the same state");
								automata.exportToDot();
								throw new UnableToComputeException(os1 + " and "
										+ os2 + " seems to be equivalents");
							}
							OutputSequence currentO1 = automata.apply(current,
									os1);
							State currentOs1 = automata.applyGetState(current,
									os1);
							OutputSequence currentO2 = automata.apply(current,
									os2);
							State currentOs2 = automata.applyGetState(current,
									os2);
							if (currentOs1 == currentOs2
									|| !currentO1.equals(currentO2)) {
								foundLocalSeq = true;
								r.addInputSequence(current);
								if (Options.getLogLevel() != LogLevel.LOW) {
									LogManager.logInfo("appending " + current
											+ " to homing sequence in order to distinguish "
											+ os1 + " and " + os2
											+ " respectively reached from " + s1
											+ " and " + s2 + " with output "
											+ o1);
									if (currentOs1 == currentOs2)
										LogManager.logInfo(
												"Now, applying homing sequence from "
														+ s1 + " and " + s2
														+ " lead in same state "
														+ currentOs1);
									else {
										o1.addOutputSequence(currentO1);
										o2.addOutputSequence(currentO2);
										LogManager.logInfo(
												"Now, applying homing sequence from "
														+ s1 + " and " + s2
														+ " give outputs " + o1
														+ " and " + o2);
									}
								}

							} else {
								for (String in : getInputSymbols()) {
									InputSequence toTry = new InputSequence();
									toTry.addInputSequence(current);
									toTry.addInput(in);
									l.add(toTry);
								}
							}
						}
					}
				}
			}
		}
		LogManager.logInfo("Found homing sequence " + r);
		return r;
	}

	/** Get init state **/
	public State getInitState() {
		return automata.getInitialState();
	}

}
