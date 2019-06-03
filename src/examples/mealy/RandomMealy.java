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
import options.GenericChoiceOption;
import options.GenericOneArgChoiceOption;
import options.IntegerOption;
import options.OneArgChoiceOptionItem;
import tools.RandomGenerator;
import tools.Utils;
import tools.loggers.LogManager;

import options.valueHolders.SeedHolder; // Added by Catherine

public class RandomMealy extends Mealy implements Serializable {
	private static final long serialVersionUID = -4610287835922377376L;

	public enum OUTPUT_STYLE {
		RANDOM, ONE_DIFF_PER_STATE,
	}

	public static class OuputStyleItem extends OneArgChoiceOptionItem {
		OUTPUT_STYLE outputStyle;

		public OuputStyleItem(String name, String argValue,
				GenericChoiceOption<?> parent, OUTPUT_STYLE outputStyle) {
			super(name, argValue, parent);
			this.outputStyle = outputStyle;
		}

	}

	public static class RandomOutputOptions
			extends GenericOneArgChoiceOption<OuputStyleItem> {

		private IntegerOption outputNumber = new IntegerOption(
				"--DRnd_output_number", "number of output symbols",
				"The number of output symbols to generate.", 2);

		public OuputStyleItem random = new OuputStyleItem("Random outputs",
				"random", this, OUTPUT_STYLE.RANDOM);
		public OuputStyleItem oneDiff = new OuputStyleItem(
				"One random output different from the others", "oneDiff", this,
				OUTPUT_STYLE.ONE_DIFF_PER_STATE);

		public RandomOutputOptions(
		) {
			super("--DRnd_generator_type", "Type of outputs",
					"Select the repartition of output symbols.");
			random.subTrees.add(outputNumber);
			this.addChoice(random);
			this.addChoice(oneDiff);
			this.setDefaultItem(random);
			outputNumber.setDefaultValue(2);
		}

		public RandomOutputOptions(OUTPUT_STYLE random2) {
			this();
			switch (random2) {
			case RANDOM:
				selectChoice(random);
				break;
			case ONE_DIFF_PER_STATE:
				selectChoice(oneDiff);
				break;
			}
		}

		public int getOutputNumber() {
			assert getSelectedItem() == random;
			return outputNumber.getValue();
		}
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

	/**
	 * generate the list of input symbols and the list of output symbols.
	 */
	private void generateSymbols(int nbInput, int nbOutput) {
		String s = "a";
		inputSymbols = new ArrayList<String>();
		for (int i = 0; i < nbInput; i++) {
			inputSymbols.add(s);
			s = Utils.nextSymbols(s);
		}
		int o = 0;
		outputSymbols = new ArrayList<String>();
		for (int i = 0; i < nbOutput; i++) {
			outputSymbols.add(String.valueOf(o++));
		}
	}

	/**
	 * Select the output symbols associated to each transition.
	 * 
	 * @return a Map of State -> (Input -> Output)
	 */
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

//	public RandomMealy(RandomGenerator rand) {
//		this(rand, false, OUTPUT_STYLE.RANDOM);
//	}

//	public RandomMealy(RandomGenerator rand, boolean forceConnex) {
//		this(rand, forceConnex, OUTPUT_STYLE.RANDOM);
//	}

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


	public RandomMealy(RandomGenerator rand, boolean connex, int statesNumber,
			int inputNumber, RandomOutputOptions outputs) {
		super((connex ? "ConnexRandom("
				: ("Random("
				+ Options.TRANSITIONPERCENT + ";"))
				+ getOutputStyleName(outputs.getSelectedItem().outputStyle)
				+ ")");
		this.rand = rand;

                if (rand instanceof SeedHolder) {
                   SeedHolder seedHolder = (SeedHolder) rand; 
                   seedHolder.initRandom();
                } // Patch added by Catherine, very dirty
                
		assert rand.getRand() != null;
		LogManager.logStep(LogManager.STEPOTHER, "Generating random Mealy");
		this.outputStyle = outputs.getSelectedItem().outputStyle;
		generateSymbols(inputNumber,(outputStyle==OUTPUT_STYLE.ONE_DIFF_PER_STATE?0:outputs.getOutputNumber()));
		createStates(statesNumber);
		if (connex)
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

	private void createStates(int nbStates) {
		for (int i = 0; i < nbStates; i++)
			addState(i == 0);
		LogManager.logInfo("Number of states : " + nbStates);
	}

	@Deprecated
	/**
	 * kept for compilation issue but this method is hard-coded and should be
	 * removed
	 * 
	 * @param rand
	 * @return
	 */
	public static RandomMealy getConnexRandomMealy(RandomGenerator rand) {
		return getRandomMealy(rand, true, 10, 2, new RandomOutputOptions());
	}



	public static RandomMealy getRandomMealy(RandomGenerator rand,
			boolean connex, int statesNumber, int inputNumber,
			RandomOutputOptions outputs) {

		RandomMealy automata;
		int nbTry = 0;
		do {
			nbTry++;
			if (nbTry > 100)
				throw new RuntimeException(
						"tried several time to generate a random automaton but each time, it wasn't minimal.");
			automata = new RandomMealy(rand, connex, statesNumber, inputNumber,
					outputs);
		} while (!automata.isMinimal());
		return automata;

	}
}
