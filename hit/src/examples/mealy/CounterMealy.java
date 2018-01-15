package examples.mealy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.loggers.LogManager;
import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

public class CounterMealy extends Mealy implements Serializable {
	private static final long serialVersionUID = 4685322377371L;
	
	private static final String resetSuffix = "Reset";
	private static final String noResetSuffix = "NoReset";
	private static final String notInCounterSuffix = "NotInCounter";

	private String noResetSymbol;
	private String ResetSymbol;
	
	public CounterMealy(int nbStates, String inputSymbol){
		super("Counter("+nbStates+")");
		LogManager.logStep(LogManager.STEPOTHER, "Generate Counter Mealy");
		noResetSymbol = noResetSuffix;
		ResetSymbol = resetSuffix;
		createStates(nbStates, true,inputSymbol);
		createTransitions(inputSymbol);
		if (Options.LOG_LEVEL != LogLevel.LOW)
			exportToDot();
	}
	
	private void createTransitions(String inputSymbol){
		addTransition(new MealyTransition(this, states.get(states.size()-1), states.get(0), inputSymbol, ResetSymbol));
		for (int i = 1; i < states.size(); i ++){
			State s2 = states.get(i);
			State s1 = states.get(i-1);
			addTransition(new MealyTransition(this, s1, s2, inputSymbol, noResetSymbol));
			
		}
	}

	private void createStates(int nbStates, boolean verbose, String inputSymbol) {
		for (int i = 0; i < nbStates; i++)
			addState(new State(inputSymbol + i, i==0));;
		if (verbose) LogManager.logInfo("Number of states : " + nbStates);
	}
	
	/**
	 * add a counter to one state of the automata.
	 * @param initial the automata
	 * @param initialState the state where the counter will be added
	 * @param endState the state where we go at the end of the counter
	 * @param inputSymbol the symbol used to increment the counter. This input symbol may be already used by the automata and in this case the transition going out of initialState will be overwritten
	 * @param counterSize the size of the counter
	 * @return a new automata with a counter
	 */
	public static Mealy addCounter(Mealy initial, State initialState, State endState, String inputSymbol, int counterSize){
		assert initial.getStates().contains(initialState);
		assert initial.getStates().contains(endState);
		Mealy m = new Mealy(initial.getName() + "_with_"+inputSymbol+counterSize);
		for (State s : initial.getStates())
			m.addState(s);
		for (MealyTransition t : initial.getTransitions())
			if (t.getFrom() != initialState || t.getInput() != inputSymbol)
				m.addTransition(new MealyTransition(m, t.getFrom(), t.getTo(), t.getInput(), t.getOutput()));
		
		LogManager.logStep(LogManager.STEPOTHER, "Add a counter to " + m.getName());
		State previous = initialState;
		for (int i = 0 ; i < counterSize - 1; i++){
			State to = new State(initialState+"_"+inputSymbol+(i+1),false);
			m.addState(to);
			for (MealyTransition t : m.getTransitionFrom(previous))
				m.addTransition(new MealyTransition(m, to, t.getTo(), t.getInput(), t.getOutput()));
			MealyTransition t = new MealyTransition(m, previous, to, inputSymbol, noResetSuffix);
			m.addTransition(t);
			previous = to;
		}
		m.addTransition(new MealyTransition(m, previous, endState, inputSymbol, resetSuffix));
		for (State s : m.getStates())
			if (m.getTransitionFromWithInput(s, inputSymbol) == null)
				m.addTransition(new MealyTransition(m, s, s, inputSymbol, notInCounterSuffix));
		if (!Options.TEST) m.exportToDot();
		return m;
	}

	public static void serialize(CounterMealy o) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(Options.OUTDIR + o.getName()
					+ ".serialized");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(o);
			oos.flush();
			oos.close();
			fos.close();
		} catch (Exception e) {
			LogManager.logException("Error serializing generated Mealy", e);
		}
	}

	public static CounterMealy deserialize(String filename) {
		Object o = null;
		File f = new File(filename);
		LogManager.logStep(LogManager.STEPOTHER, "Loading Randommealy from "
				+ f.getName());
		try {
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(fis);
			o = ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception e) {
			LogManager.logException("Error deserializing generated Mealy", e);
		}
		return (CounterMealy) o;
	}


}
