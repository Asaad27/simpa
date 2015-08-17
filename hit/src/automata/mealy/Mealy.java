package automata.mealy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import main.simpa.Options;
import tools.GraphViz;
import tools.loggers.LogManager;
import automata.Automata;
import automata.State;

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
		if (!transitions.containsKey(t.hashCode())){
			super.transitions.add(t);
			transitions.put(t.hashCode(), t);
			return true;
		}
		assert transitions.get(t.hashCode()).equals(t) : "stored " + transitions.get(t.hashCode()) + " and new " + t + " have same hash " + t.hashCode();
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

	public MealyTransition getTransitionFromWithInput(State s, String input,
			boolean loop) {
		MealyTransition t = getTransitionFromWithInput(s, input);
		if ((t.isLoop() && loop) || !t.isLoop()) return t;
		return null;
	}

	public void exportToDot() {
		Writer writer = null;
		File file = null;
		File dir = new File(Options.OUTDIR + Options.DIRGRAPH);
		try {
			if (!dir.isDirectory() && !dir.mkdirs())
				throw new IOException("unable to create " + dir.getName()
						+ " directory");

			file = new File(dir.getPath() + File.separatorChar + name
					+ "_inf.dot");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("digraph G {\n");
			for (MealyTransition t : getTransitions()) {
				writer.write("\t" + t.toDot() + "\n");
			}
			for (State s : states){
				if (s.isInitial()){
					writer.write("\t" + s.getName() + " [shape=doubleoctagon]\n");
				}
			}
			writer.write("}\n");
			writer.close();
			LogManager.logInfo("Conjecture has been exported to "
					+ file.getName());
			File imagePath = GraphViz.dotToFile(file.getPath());
			if (imagePath != null)
				LogManager.logImage(imagePath.getPath());
		} catch (IOException e) {
			LogManager.logException("Error writing dot file", e);
		}
	}

	public OutputSequence apply(InputSequence I, State s){
		OutputSequence O = new OutputSequence();
		for (String i : I.sequence){
			MealyTransition t = getTransitionFromWithInput(s, i);
			assert t != null;
			s = t.getTo();
			O.addOutput(t.getOutput());
		}
		return O;
	}
	
	public State applyGetState(InputSequence I, State s){
		for (String i : I.sequence){
			MealyTransition t = getTransitionFromWithInput(s, i);
			assert t != null;
			s = t.getTo();
		}
		return s;
	}
	
	/**
	 * compute a distinction sequence for the two states
	 * it may be a new distinction sequence or an append of an existing distinction sequence 
	 * @param automata
	 * @param inputSymbols
	 * @param s1
	 * @param s2
	 * @param W
	 */
	public void addDistinctionSequence(
			List<String> inputSymbols, State s1, State s2,
			List<InputSequence> W) {
		//first we try to add an input symbol to the existing W
		for (InputSequence w : W){
			for (String i : inputSymbols){
				InputSequence testw = new InputSequence();
				testw.addInputSequence(w);
				testw.addInput(i);
				if (!apply(testw, s1).equals(apply(testw, s2))){
					w.addInput(i);
					return;
				}
			}
		}
		//then we try to compute a w from scratch
		LinkedList<InputSequence> testW = new LinkedList<InputSequence>();
		for (String i : inputSymbols)
			testW.add(new InputSequence(i));
		while (true){
			InputSequence testw = testW.pollFirst();
			if (apply(testw, s1).equals(apply(testw, s2))){
				if (testw.getLength() > getStateCount()){
					//TODO find a better way to log and save the automata
					String dir = Options.OUTDIR;
					Options.OUTDIR = "/tmp/";
					exportToDot();
					Options.OUTDIR = dir;
					throw new RuntimeException("it looks like if we will not find a w to distinguish "+s1 +" and " +s2+".Those state may are equivalents, please look in /tmp");
				}
				for (String i : inputSymbols){
					InputSequence newTestw = new InputSequence();
					newTestw.addInputSequence(testw);
					newTestw.addInput(i);
					testW.add(newTestw);
				}
			}else{
				for (int i = 0; i < W.size(); i++){
					InputSequence w = W.get(i);
					if (testw.startsWith(w)){
						W.remove(w);
					}
				}
				W.add(testw);
				return;
			}
			
		}
	}
}
