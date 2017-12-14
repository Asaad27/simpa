package examples.mealy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.simpa.Options;
import tools.Utils;
import tools.loggers.LogManager;
import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

public class RandomMealy extends Mealy implements Serializable {
	private static final long serialVersionUID = -4610287835922377376L;

	private List<String> inputSymbols = null;
	private List<String> outputSymbols = null;
	private long seed = 0;

	public static String replaceCharAt(String s, int pos, char c) {
		StringBuffer buf = new StringBuffer(s);
		buf.setCharAt(pos, c);
		return buf.toString();
	}

	private void generateSymbols() {
		int nbSym = 0;
		String s = "a";
		inputSymbols = new ArrayList<String>();
		nbSym = Utils.randIntBetween(Options.MININPUTSYM, Options.MAXINPUTSYM);
		for (int i = 0; i < nbSym; i++) {
			inputSymbols.add(s);
			s = Utils.nextSymbols(s);
		}
		int o = 0;
		outputSymbols = new ArrayList<String>();
		nbSym = Utils.randIntBetween(Options.MINOUTPUTSYM, Options.MAXOUTPUTSYM);
		for (int i = 0; i < nbSym; i++) {
			outputSymbols.add(String.valueOf(o++));
		}
	}

	public RandomMealy() {
		this(false);
	}

	public RandomMealy(boolean forceConnex) {
		super(forceConnex ? "ConnexRandom()" : ("Random("
				+ Options.TRANSITIONPERCENT + ")"));
		LogManager.logStep(LogManager.STEPOTHER, "Generating random Mealy");
		seed = Utils.randLong();
		generateSymbols();
		createStates();
		if (forceConnex)
			createConnexTransitions();
		else
			createTransitions();
		exportToDot();
	}
	
	public long getSeed(){
		return seed;
	}

	public static void serialize(RandomMealy o) {
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

	public static RandomMealy deserialize(String filename) {
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
		return (RandomMealy) o;
	}

	private void createTransitions() {
		for (State s1 : states) {
			for (String is : inputSymbols) {
				if (Utils.randBoolWithPercent(Options.TRANSITIONPERCENT)) {
					addTransition(new MealyTransition(this, s1,
							Utils.randIn(states), is,
							Utils.randIn(outputSymbols)));
				} else {
					addTransition(new MealyTransition(this, s1, s1, is,
							Utils.randIn(outputSymbols)));
				}
			}
		}
	}

	private void createConnexTransitions() {
		Set<State> reachedFromStartAndNotComplete = new HashSet<>();
		Set<State> notReachedFromStart = new HashSet<>();
		List<State> reachingStart = new ArrayList<>();
		State initial = states.get(0);
		assert (initial.isInitial());
		reachedFromStartAndNotComplete.add(initial);
		reachingStart.add(initial);
		notReachedFromStart.addAll(states);
		notReachedFromStart.remove(initial);

		Map<State, Set<String>> remainingInputs = new HashMap<>();
		for (State s : states) {
			remainingInputs.put(s, new HashSet<>(inputSymbols));
		}

		for (State s : states) {
			if (s != initial) {
				String input = Utils.randIn(inputSymbols);
				String output = Utils.randIn(outputSymbols);
				State target = Utils.randIn(reachingStart);
				addTransition(new MealyTransition(this, s, target, input,
						output));
				reachingStart.add(s);
				remainingInputs.get(s).remove(input);
			}
		}
		assert (reachingStart.containsAll(states));

		while (!notReachedFromStart.isEmpty()) {
			State s = Utils.randIn(reachedFromStartAndNotComplete);
			String input = Utils.randIn(remainingInputs.get(s));
			String output = Utils.randIn(outputSymbols);
			State target = Utils.randIn(notReachedFromStart);
			addTransition(new MealyTransition(this, s, target, input, output));

			Set<String> remaining = remainingInputs.get(s);
			remaining.remove(input);
			if (remaining.isEmpty())
				reachedFromStartAndNotComplete.remove(s);
			// we suppose that at this time, there is only one transition
			// starting from states in notReachedFromStart
			State reached = target;
			while (reached != null && notReachedFromStart.contains(reached)) {
				List<MealyTransition> transitions = getTransitionFrom(reached);
				assert (transitions.size() == 1);
				reachedFromStartAndNotComplete.add(reached);
				notReachedFromStart.remove(reached);
				reached = transitions.get(0).getTo();
			}

		}

		for (State s1 : states) {
			for (String is : remainingInputs.get(s1)) {
				addTransition(new MealyTransition(this, s1,
						Utils.randIn(states), is, Utils.randIn(outputSymbols)));
			}
		}
	}

	private void createStates() {
		int nbStates = Utils.randIntBetween(Options.MINSTATES,
				Options.MAXSTATES);
		for (int i = 0; i < nbStates; i++)
			addState(i == 0);
		LogManager.logInfo("Number of states : " + nbStates);
	}

	public static RandomMealy getConnexRandomMealy() {

		RandomMealy automata = new RandomMealy(true);

		return automata;
	}
}
