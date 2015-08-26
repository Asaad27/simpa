package drivers.mealy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import learner.mealy.LmConjecture;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.Utils;
import tools.loggers.LogManager;
import automata.Automata;
import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import drivers.Driver;

public class MealyDriver extends Driver {
	protected Mealy automata;
	protected State currentState;
	protected List<InputSequence> forcedCE;
	protected Set<InputSequence> triedCE;
	private int nbStates = 0;
	private int transitionCount = 0;
	private String name = null;

	public MealyDriver(Mealy automata) {
		super();
		type = DriverType.MEALY;
		this.automata = automata;
		this.forcedCE = getForcedCE();
		triedCE = new HashSet<>();
		this.nbStates = automata.getStateCount();
		this.name = automata.getName();
	}

	public MealyDriver(String name) {
		this.name = name;
		this.automata = null;
	}

	public List<String> getStats() {
		return Utils.createArrayList(String.valueOf(nbStates),
				String.valueOf(getInputSymbols().size()),
				String.valueOf(getOutputSymbols().size()),
				String.valueOf(((float) numberOfAtomicRequest / numberOfRequest)),
				String.valueOf(numberOfRequest),
				String.valueOf(((float) duration / 1000000000)),
				String.valueOf(automata.getTransitionCount()));
	}

	protected List<InputSequence> getForcedCE() {
		return null;
	}

	public String execute(String input) {
		String output = null;
		if (input.length() > 0) {
			if (addtolog)
				numberOfAtomicRequest++;
			MealyTransition currentTrans = automata.getTransitionFromWithInput(currentState, input);
			if (currentTrans != null) {
				output = new String(currentTrans.getOutput());
				currentState = currentTrans.getTo();
			} else {
				output = new String();
			}
			if (addtolog)
				LogManager.logRequest(input, output, transitionCount);
			transitionCount++;
		}
		return output;
	}

	public List<String> getInputSymbols() {
		List<String> is = new ArrayList<String>();
		for (MealyTransition t : automata.getTransitions()) {
			if (!is.contains(t.getInput()))
				is.add(t.getInput());
		}
		Collections.sort(is);
		return is;
	}

	public List<String> getOutputSymbols() {
		List<String> os = new ArrayList<String>();
		for (MealyTransition t : automata.getTransitions()) {
			if (!os.contains(t.getOutput()))
				os.add(t.getOutput());
		}
		Collections.sort(os);
		return os;
	}

	@Override
	public String getSystemName() {
		return name;
	}

	public InputSequence getCounterExample(Automata c) {
		Mealy m = (Mealy) c;
		LogManager.logInfo("Searching counter example");
		InputSequence ce = null;
		if (forcedCE != null && !forcedCE.isEmpty()) {
			ce = forcedCE.remove(0);
			LogManager.logInfo("Counter example found (forced) : " + ce);
		}
		if (Options.STOP_ON_CE_SEARCH){
			LogManager.logInfo("CE search aborted (see Options.STOP_ON_CE_SEARCH");
			return null;
		}

		if (ce == null){
			LogManager.logInfo("search theorical CE");
			if (m.isConnex())
				ce = getShortestCounterExemple(m);
			if (ce != null){
				reset();
				for (String i : ce.sequence)
					execute(i);
			}
		}
		//TODO we don't know if ce is null because the two automata are identical or because we cannot compute a shortest CE (due to unknown driver for example). todo : add an exception

		if (ce == null){
			LogManager.logInfo("search random CE");
			ce = getRandomCounterExemple(m);
		}

		if (ce == null)
			LogManager.logInfo("No counter example found");
		LogManager.logInfo("found ce : "+ce);
		return ce;
	}

	public InputSequence getRandomCounterExemple(Mealy c){
		boolean found = false;
		InputSequence ce = null;

		int maxTries = Options.MAX_CE_RESETS;
		List<String> is = getInputSymbols();
		MealyDriver conjDriver = new MealyDriver(c);
		stopLog();
		conjDriver.stopLog();
		int i = 0;
		while (i < maxTries && !found) {
			ce = InputSequence.generate(is, Options.MAX_CE_LENGTH);
			while (triedCE.contains(ce))
				ce = InputSequence.generate(is, Options.MAX_CE_LENGTH);
			triedCE.add(ce);
			OutputSequence osSystem = new OutputSequence();
			OutputSequence osConj = new OutputSequence();
			reset();
			conjDriver.reset();
			if (ce.getLength() > 0) {
				for (String input : ce.sequence) {
					String _sys = execute(input);
					String _conj = conjDriver.execute(input);
					if (_sys.length() > 0) {
						osSystem.addOutput(_sys);
						osConj.addOutput(_conj);
					}
					if (!_sys.equals(_conj)
							&& (osSystem.getLength() > 0 && !osSystem
									.getLastSymbol().isEmpty())) {
						found = true;
						ce = ce.getIthPreffix(osSystem.getLength());
						LogManager.logInfo("Counter example found : " + ce);
						LogManager.logInfo("On system : " + osSystem);
						LogManager.logInfo("On conjecture : " + osConj);
						break;
					}
				}
				i++;
			}
		}
		startLog();
		conjDriver.startLog();
		return (found ? ce : null);
	}

	/**
	 * get a shortest distinguish sequence for an automata
	 * the computed sequence is not applied to the driver
	 * The two automata are supposed to be connex.
	 * @param s1 the position in the driver equivalent to s2. If null, the current position is chosen
	 * @param a2 the second automata
	 * @param s2 the current position in a2
	 * @return a distinguish sequence for the two automata starting from their current states.
	 */
	public InputSequence getShortestCounterExemple(
			State s1, Mealy a2, State s2) {
		if (s1 == null)
			s1 = currentState;
		assert automata.isConnex() && a2.isConnex();
		int maxLength = (automata.getStateCount() > a2.getStateCount() ? automata.getStateCount() : a2.getStateCount());
		class Node{public InputSequence i; public State originalEnd; public State conjectureEnd;public String toString(){return "for input '" + i + "' this driver go to '" + originalEnd + "' and the other go to '"+conjectureEnd+"'\n";}}
		LinkedList<Node> toCompute = new LinkedList<Node>();
		Node n = new Node();
		n.i = new InputSequence();
		n.originalEnd = s1;
		n.conjectureEnd = s2;
		toCompute.add(n);
		while (!toCompute.isEmpty()){
			Node current = toCompute.pollFirst();
			if (current.i.getLength() > maxLength)
				continue;
			for (String i : getInputSymbols()){
				MealyTransition originalT = automata.getTransitionFromWithInput(current.originalEnd, i);
				MealyTransition conjectureT = a2.getTransitionFromWithInput(current.conjectureEnd, i);
				if (!originalT.getOutput().equals(conjectureT.getOutput())){
					current.i.addInput(i);
					return current.i;
				}
				Node newNode = new Node();
				newNode.i = new InputSequence();
				newNode.i.addInputSequence(current.i);
				newNode.i.addInput(i);
				newNode.originalEnd = originalT.getTo();
				newNode.conjectureEnd = conjectureT.getTo();
				toCompute.add(newNode);
			}
		}
		return null;
	}

	/**
	 * get a shortest distinguish sequence for an automata
	 * the computed sequence is not applied to the driver
	 * The two automata ares supposed to be connex.
	 * @param a2 the second automata
	 * @return a distinguish sequence for the two automata starting from their initial states.
	 */
	public InputSequence getShortestCounterExemple(Mealy m) {
		return getShortestCounterExemple(automata.getInitialState(), m, m.getInitialState());
	}

	@Override
	public void reset() {
		super.reset();
		if (automata != null) {
			automata.reset();
			currentState = automata.getInitialState();
		}
	}

	public boolean isCounterExample(Object ce, Object c) {
		if (ce == null)
			return false;
		InputSequence realCe = (InputSequence) ce;
		LmConjecture conj = (LmConjecture) c;
		MealyDriver conjDriver = new MealyDriver(conj);
		stopLog();
		conjDriver.stopLog();
		reset();
		conjDriver.reset();
		boolean isCe = false;
		for (String input : realCe.sequence) {
			if (!execute(input).equals(conjDriver.execute(input))) {
				isCe = true;
				break;
			}
		}
		startLog();
		conjDriver.startLog();
		return isCe;
	}

	/**
	 * compute an input sequence s.t. the output sequence entirely define the final state
	 * @return null if a such sequence cannot be computed
	 */
	public InputSequence getHomingSequence(){
		LogManager.logInfo("Computing homing sequence");
		if (automata == null){
			LogManager.logInfo("Unable to compute homing sequence");
			return null;
		}
		InputSequence r = new InputSequence();
		boolean found = false;
		while (!found){
			found = true;
			for (int i = 0; i < automata.getStateCount(); i++){
				State s1 = automata.getState(i);
				for (int j = i+1; j < automata.getStateCount(); j++){
					State s2 = automata.getState(j);
					OutputSequence o1 = automata.apply(r, s1);
					State os1 = automata.applyGetState(r, s1);
					OutputSequence o2 = automata.apply(r, s2);
					State os2 = automata.applyGetState(r, s2);
					if (o1.equals(o2) && os1 != os2){
						found = false;
						List<InputSequence> W = new ArrayList<InputSequence>();
						automata.addDistinctionSequence(getInputSymbols(), os1, os2, W);
						r.addInputSequence(W.get(0));
						if (Options.LOG_LEVEL != LogLevel.LOW)
							LogManager.logInfo("appending " + W.get(0) + " to homing sequence in order to distinguish " + os1 + " and " + os2 
									+ " respectively reached from " + s1 + " and "+ s2 + " with output " + o1);
					}
				}
			}
		}
		LogManager.logInfo("Found homing sequence " + r);
		return r;
	}
}
