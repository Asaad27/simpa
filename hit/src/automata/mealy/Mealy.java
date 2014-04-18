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
		   transitions.put(t.hashCode(), t);
		   return true;
		}
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
			writer.write("}\n");
			writer.close();
			LogManager.logInfo("Conjecture have been exported to "
					+ file.getName());
			File imagePath = GraphViz.dotToFile(file.getPath());
			if (imagePath != null)
				LogManager.logImage(imagePath.getPath());
		} catch (IOException e) {
			LogManager.logException("Error writing dot file", e);
		}
	}

}
