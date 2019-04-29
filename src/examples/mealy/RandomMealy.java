/********************************************************************************
 * Copyright (c) 2012,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 *     Nicolas BREMOND
 ********************************************************************************/
package examples.mealy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.RandomGenerator;
import tools.Utils;
import tools.loggers.LogManager;

public class RandomMealy extends Mealy implements Serializable {
	private static final long serialVersionUID = -4610287835922377376L;

	public enum OUTPUT_STYLE {
		RANDOM, ONE_DIFF_PER_STATE,
	}

	private List<String> inputSymbols = null;
	private List<String> outputSymbols = null;
	private final RandomGenerator rand;
	private OUTPUT_STYLE outputStyle;

	public static String replaceCharAt(String s, int pos, char c) {
		StringBuffer buf = new StringBuffer(s);
		buf.setCharAt(pos, c);
		return buf.toString();
	}

	private void generateSymbols() {
		int nbSym = 0;
		String s = "a";
		inputSymbols = new ArrayList<String>();
		nbSym = rand.randIntBetween(Options.MININPUTSYM, Options.MAXINPUTSYM);
		for (int i = 0; i < nbSym; i++) {
			inputSymbols.add(s);
			s = Utils.nextSymbols(s);
		}
		int o = 0;
		outputSymbols = new ArrayList<String>();
		nbSym = rand.randIntBetween(Options.MINOUTPUTSYM, Options.MAXOUTPUTSYM);
		for (int i = 0; i < nbSym; i++) {
			outputSymbols.add(String.valueOf(o++));
		}
	}

	private Map<State, Map<String, String>> chooseOutputs() {
		Map<State, Map<String, String>> outputs = new HashMap<>();
		for (State s : states) {
			Map<String, String> localOutputs = new HashMap<>();
			switch (outputStyle) {
			case RANDOM:
				for (String i : inputSymbols) {
					localOutputs.put(i, rand.randIn(outputSymbols));
				}
				break;
			case ONE_DIFF_PER_STATE:
				String diff = rand.randIn(inputSymbols);
				if (Options.getLogLevel() != LogLevel.LOW)
					LogManager.logInfo("Changing output for state " + s
							+ " is " + diff);
				for (String i : inputSymbols) {
					localOutputs.put(i, (i.equals(diff)) ? "special" : "same");
				}
				break;
			}
			outputs.put(s, localOutputs);
		}
		return outputs;
	}

	public RandomMealy(RandomGenerator rand) {
		this(rand, false, OUTPUT_STYLE.RANDOM);
	}

	public RandomMealy(RandomGenerator rand, boolean forceConnex) {
		this(rand, forceConnex, OUTPUT_STYLE.RANDOM);
	}

	private static String getOutputStyleName(OUTPUT_STYLE outputStyle) {
		switch (outputStyle) {
		case RANDOM:
			return "randomOutputs";
		case ONE_DIFF_PER_STATE:
			return "oneOutputDiff";
		default:
			return "unknown output style";
		}
	}

	public RandomMealy(RandomGenerator rand, boolean forceConnex,
			OUTPUT_STYLE outputStyle) {
		super((forceConnex ? "ConnexRandom(" : ("Random("
				+ Options.TRANSITIONPERCENT + ";"))
				+ getOutputStyleName(outputStyle) + ")");
		this.rand = rand;
		assert rand.getRand() != null;
		LogManager.logStep(LogManager.STEPOTHER, "Generating random Mealy");
		this.outputStyle = outputStyle;
		generateSymbols();
		createStates();
		if (forceConnex)
			createConnexTransitions(chooseOutputs());
		else
			createTransitions(chooseOutputs());
		if (Options.getLogLevel() != Options.LogLevel.LOW)
			exportToDot();
	}
	
	public static void serialize(RandomMealy o) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(
					Options.getSerializedObjectsDir().getAbsolutePath()
							+ File.separator + o.getName() + ".serialized");
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

	private void createTransitions(Map<State, Map<String, String>> outputs) {
		for (State s1 : states) {
			for (String is : inputSymbols) {
				String output = outputs.get(s1).get(is);
				if (rand.randBoolWithPercent(Options.TRANSITIONPERCENT)) {
					addTransition(new MealyTransition(this, s1,
							rand.randIn(states), is, output));
				} else {
					addTransition(new MealyTransition(this, s1, s1, is, output));
				}
			}
		}
	}

	private void createConnexTransitions(Map<State, Map<String, String>> outputs) {
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
				String input = rand.randIn(inputSymbols);
				String output = outputs.get(s).get(input);
				State target = rand.randIn(reachingStart);
				addTransition(new MealyTransition(this, s, target, input,
						output));
				reachingStart.add(s);
				remainingInputs.get(s).remove(input);
			}
		}
		assert (reachingStart.containsAll(states));

		while (!notReachedFromStart.isEmpty()) {
			State s = rand.randIn(reachedFromStartAndNotComplete);
			String input = rand.randIn(remainingInputs.get(s));
			String output = outputs.get(s).get(input);
			State target = rand.randIn(notReachedFromStart);
			addTransition(new MealyTransition(this, s, target, input, output));

			Set<String> remaining = remainingInputs.get(s);
			remaining.remove(input);
			if (remaining.isEmpty())
				reachedFromStartAndNotComplete.remove(s);
			// we suppose that at this time, there is only one transition
			// starting from states in notReachedFromStart
			State reached = target;
			while (reached != null && notReachedFromStart.contains(reached)) {
				Collection<MealyTransition> transitions = getTransitionFrom(
						reached);
				assert (transitions.size() == 1);
				reachedFromStartAndNotComplete.add(reached);
				notReachedFromStart.remove(reached);
				reached = transitions.iterator().next().getTo();
			}

		}

		for (State s1 : states) {
			for (String is : remainingInputs.get(s1)) {
				addTransition(new MealyTransition(this, s1,
						rand.randIn(states), is, outputs.get(s1).get(is)));
			}
		}
	}

	private void createStates() {
		int nbStates = rand.randIntBetween(Options.MINSTATES,
				Options.MAXSTATES);
		for (int i = 0; i < nbStates; i++)
			addState(i == 0);
		LogManager.logInfo("Number of states : " + nbStates);
	}

	public static RandomMealy getConnexRandomMealy(RandomGenerator rand) {
		return getConnexRandomMealy(rand, OUTPUT_STYLE.RANDOM);
	}

	public static RandomMealy getConnexRandomMealy(RandomGenerator rand,
			OUTPUT_STYLE outputStyle) {

		RandomMealy automata;
		int nbTry = 0;
		do {
			automata = new RandomMealy(rand, true, outputStyle);
			nbTry++;
			if (nbTry > 100)
				throw new RuntimeException(
						"tried several time to generate a random automaton but each time, it wasn't minimal.");
		} while (!automata.isMinimal());
		return automata;
	}
}
