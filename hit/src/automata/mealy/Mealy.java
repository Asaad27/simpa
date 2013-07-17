package automata.mealy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import main.Options;
import tools.GraphViz;
import tools.loggers.LogManager;
import automata.Automata;
import automata.State;

public class Mealy extends Automata implements Serializable {
	private static final long serialVersionUID = 3590635279837551088L;

	protected List<MealyTransition> transitions;

	public final static String OMEGA = "omega|symbol";
	public final static String EPSILON = "espilon|symbol";

	public Mealy(String name) {
		super(name);
		transitions = new ArrayList<MealyTransition>();
	}

	public Boolean addTransition(MealyTransition t) {
		return transitions.add(t);
	}

	public List<MealyTransition> getTransitions() {
		return transitions;
	}

	public MealyTransition getTransition(int index) {
		return transitions.get(index);
	}

	public MealyTransition removeTransition(int index) {
		return transitions.remove(index);
	}

	public int getTransitionCount() {
		return transitions.size();
	}

	public List<MealyTransition> getTransitionFrom(State s) {
		return getTransitionFrom(s, true);
	}

	public List<MealyTransition> getTransitionTo(State s) {
		return getTransitionFrom(s, true);
	}

	public List<MealyTransition> getTransitionFrom(State s, boolean loop) {
		List<MealyTransition> res = new ArrayList<MealyTransition>();
		for (MealyTransition t : transitions) {
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

	public List<MealyTransition> getTransitionTo(State s, boolean loop) {
		List<MealyTransition> res = new ArrayList<MealyTransition>();
		for (MealyTransition t : transitions) {
			if (t.getTo().equals(s)) {
				if (t.getFrom().equals(s)) {
					if (loop)
						res.add(t);
				} else
					res.add(t);
			}
		}
		return res;
	}

	public MealyTransition getTransitionFromWithInput(State s, String input) {
		for (MealyTransition t : getTransitionFrom(s)) {
			if (t.getInput().equals(input))
				return t;
		}
		return null;
	}

	public MealyTransition getTransitionFromWithInput(State s, String input,
			boolean loop) {
		for (MealyTransition t : getTransitionFrom(s, loop)) {
			if (t.getInput().equals(input))
				return t;
		}
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
