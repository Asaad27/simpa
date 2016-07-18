package automata.mealy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import automata.Automata;
import automata.State;
import main.simpa.Options;
import tools.DotParser;
import tools.GraphViz;
import tools.loggers.LogManager;

public class Mealy extends Automata implements Serializable {
	private static final long serialVersionUID = 3590635279837551088L;

	protected Map<Integer, MealyTransition> transitions;

	public final static String OMEGA = "omega|symbol";
	public final static String EPSILON = "espilon|symbol";

	public Mealy(String name) {
		super(name);
		transitions = new HashMap<Integer, MealyTransition>();
	}

	public Boolean addTransition(MealyTransition t) {
		if (!transitions.containsKey(t.hashCode())) {
			super.transitions.add(t);
			transitions.put(t.hashCode(), t);
			return true;
		}
		assert transitions.get(t.hashCode()).equals(t) : "stored " + transitions.get(t.hashCode()) + " and new " + t
				+ " have same hash " + t.hashCode();
		return false;
	}

	public Collection<MealyTransition> getTransitions() {
		return transitions.values();
	}

	public int getTransitionCount() {
		return transitions.size();
	}

	public List<MealyTransition> getTransitionFrom(State s) {
		return getTransitionFrom(s, true);
	}

	public List<MealyTransition> getTransitionFrom(State s, boolean loop) {
		List<MealyTransition> res = new ArrayList<MealyTransition>();
		for (MealyTransition t : transitions.values()) {
			if (t.getFrom().equals(s)) {
				if (t.getTo().equals(s)) {
					if (loop)
						res.add(t);
				} else
					res.add(t);
			}
		}
		return res;
	}

	public MealyTransition getTransitionFromWithInput(State s, String input) {
		assert states.contains(s);
		return transitions.get((s + input).hashCode());
	}

	public MealyTransition getTransitionFromWithInput(State s, String input, boolean loop) {
		MealyTransition t = getTransitionFromWithInput(s, input);
		if ((t.isLoop() && loop) || !t.isLoop())
			return t;
		return null;
	}

	public void exportToDot() {
		Writer writer = null;
		File file = null;
		File dir = new File(Options.OUTDIR + Options.DIRGRAPH);
		try {
			if (!dir.isDirectory() && !dir.mkdirs())
				throw new IOException("unable to create " + dir.getName() + " directory");

			file = new File(dir.getPath() + File.separatorChar + name + "_inf.dot");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("digraph G {\n");
			for (MealyTransition t : getTransitions()) {
				writer.write("\t" + t.toDot() + "\n");
			}
			for (State s : states) {
				if (s.isInitial()) {
					writer.write("\t" + s.getName() + " [shape=doubleoctagon]\n");
				}
			}
			writer.write("}\n");
			writer.close();
			LogManager.logInfo("Conjecture has been exported to " + file.getName());
			File imagePath = GraphViz.dotToFile(file.getPath());
			if (imagePath != null)
				LogManager.logImage(imagePath.getPath());
		} catch (IOException e) {
			LogManager.logException("Error writing dot file", e);
		}
	}

	public OutputSequence apply(InputSequence I, State s) {
		OutputSequence O = new OutputSequence();
		for (String i : I.sequence) {
			MealyTransition t = getTransitionFromWithInput(s, i);
			assert t != null;
			s = t.getTo();
			O.addOutput(t.getOutput());
		}
		return O;
	}

	public State applyGetState(InputSequence I, State s) {
		for (String i : I.sequence) {
			MealyTransition t = getTransitionFromWithInput(s, i);
			assert t != null;
			s = t.getTo();
		}
		return s;
	}

	/**
	 * compute a distinction sequence for the two states it may be a new
	 * distinction sequence or an append of an existing distinction sequence
	 * 
	 * @param automata
	 * @param inputSymbols
	 * @param s1
	 * @param s2
	 * @param W
	 */
	public void addDistinctionSequence(List<String> inputSymbols, State s1, State s2, List<InputSequence> W) {
		// first we try to add an input symbol to the existing W
		for (InputSequence w : W) {
			for (String i : inputSymbols) {
				InputSequence testw = new InputSequence();
				testw.addInputSequence(w);
				testw.addInput(i);
				if (!apply(testw, s1).equals(apply(testw, s2))) {
					w.addInput(i);
					return;
				}
			}
		}
		// then we try to compute a w from scratch
		LinkedList<InputSequence> testW = new LinkedList<InputSequence>();
		for (String i : inputSymbols)
			testW.add(new InputSequence(i));
		while (true) {
			InputSequence testw = testW.pollFirst();
			if (apply(testw, s1).equals(apply(testw, s2))) {
				if (testw.getLength() > getStateCount()) {
					// TODO find a better way to log and save the automata
					String dir = Options.OUTDIR;
					Options.OUTDIR = "/tmp/";
					LogManager.logInfo("unable to compute distinguish sequence for " + s1 + " and " + s2);
					exportToDot();
					Options.OUTDIR = dir;
					throw new RuntimeException("it looks like if we will not find a w to distinguish " + s1 + " and "
							+ s2 + ".Those state may are equivalents, please look in /tmp");
				}
				for (String i : inputSymbols) {
					InputSequence newTestw = new InputSequence();
					newTestw.addInputSequence(testw);
					newTestw.addInput(i);
					testW.add(newTestw);
				}
			} else {
				for (int i = 0; i < W.size(); i++) {
					InputSequence w = W.get(i);
					if (testw.startsWith(w)) {
						W.remove(w);
					}
				}
				W.add(testw);
				return;
			}

		}
	}

	private static State getStateOrCreate(HashMap<String, State> h, String name) {
		if (!h.containsKey(name))
			h.put(name, new State(name, false));
		return h.get(name);
	}

	/*
	 * public static Mealy importFromDot(File f) throws IOException { if
	 * (!f.exists()) throw new IOException("'" + f.getAbsolutePath() +
	 * "' do not exists"); if (!f.getName().endsWith(".dot"))
	 * LogManager.logError("Are you sure that '" + f + "' is a dot file ?");
	 * BufferedReader reader = new BufferedReader(new FileReader(f)); String
	 * line = reader.readLine(); String name = line.split(" ")[1]; Mealy result
	 * = new Mealy(name); Mealy tmpMealy = new Mealy("tmp");// used to store
	 * transition before // states update. HashMap<String, State> states = new
	 * HashMap<>(); while ((line = reader.readLine()) != null) { if
	 * (line.contains(" ")) { String[] splitedLine = line.split(" "); if
	 * (splitedLine.length >= 4) { assert splitedLine[1].equals("->"); State s1
	 * = getStateOrCreate(states, splitedLine[0].substring(1)); State s2 =
	 * getStateOrCreate(states, splitedLine[2]); String label =
	 * splitedLine[3].substring(splitedLine[3].indexOf("\"") + 1,
	 * splitedLine[3].lastIndexOf("\"")); String input = label.split("/")[0];
	 * String output = label.split("/")[1]; System.err.println("----result: " +
	 * result + "---- S1: " + s1 + "----S2:" + s2 + "----input: " + input +
	 * "----output: " + output); MealyTransition t = new MealyTransition(result,
	 * s1, s2, input, output); tmpMealy.addTransition(t);
	 * 
	 * } if (splitedLine.length == 2) { // System.out.println(line); // assert
	 * splitedLine[1].startsWith("["); System.err.println(splitedLine[1]);
	 * String stateName = splitedLine[0].substring(1); states.put(stateName, new
	 * State(stateName, true));
	 * 
	 * } } else {// ajouter le traitement pour déclaration d'état
	 * System.out.println(line);
	 * 
	 * }
	 * 
	 * } reader.close();
	 * 
	 * for (State s : states.values()) result.addState(s); for (MealyTransition
	 * t : tmpMealy.getTransitions()) { State s1 =
	 * states.get(t.getFrom().getName()); State s2 =
	 * states.get(t.getTo().getName()); result.addTransition(new
	 * MealyTransition(result, s1, s2, t.getInput(), t.getOutput())); } return
	 * result; }
	 */
	public static Mealy importFromDot(File file) throws IOException {

		if (!file.exists())
			throw new IOException("'" + file.getAbsolutePath() + "' do not exists");
		if (!file.getName().endsWith(".dot"))
			LogManager.logError("Are you sure that '" + file + "' is a dot file ?");

		DotParser dotParser = new DotParser();

		Map<String, ArrayList> allTrans = dotParser.getAutomate(file);

		HashMap<String, State> states = new HashMap<>();

		Mealy tmpMealy = new Mealy("tmp");

		String name = allTrans.get("GNAME").get(0).toString();

		// System.out.println(">>>>>>>>>>>> "+allTrans.size());
		Mealy result = new Mealy(name);

		Iterator it = allTrans.entrySet().iterator();
		while (it.hasNext()) {
			// System.err.println("simpa ---- test");
			Map.Entry<String, ArrayList> entry = (Entry<String, ArrayList>) it.next();
			String key = entry.getKey();
			ArrayList<String> value = entry.getValue();
			// System.out.println(">>>>>>>>>>>> "+value.size());
			// System.out.println(">>>>>>>>>>>> "+key);
			if (key.equals("TRANSALTION")) {
				ArrayList<ArrayList> set = entry.getValue();
				for (ArrayList element : set) {

					State s1 = getStateOrCreate(states, element.get(0).toString());
					State s2 = getStateOrCreate(states, element.get(1).toString());
					String input = element.get(2).toString();
					String output = element.get(3).toString();
					System.err.println(" S1 : " + s1 + " S2:" + s2 + " input " + input + " output " + output);
					MealyTransition t = new MealyTransition(result, s1, s2, input, output);
					tmpMealy.addTransition(t);
				}

			}
			if (key.equals("INIT")) {

				String stateName = value.get(0).toString();
				states.put(stateName, new State(stateName, true));
			}
			// ...
		}

		for (State s : states.values())
			result.addState(s);
		for (MealyTransition t : tmpMealy.getTransitions()) {
			State s1 = states.get(t.getFrom().getName());
			State s2 = states.get(t.getTo().getName());
			result.addTransition(new MealyTransition(result, s1, s2, t.getInput(), t.getOutput()));
		}
		return result;
	}

}
