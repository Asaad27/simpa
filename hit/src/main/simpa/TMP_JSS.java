package main.simpa;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import stats.GlobalGraphGenerator;
import tools.DotAntlrListener.ParseException;

public class TMP_JSS {

	public static void main(String[] args) throws IOException {
		new TMP_JSS();
	}

	private TMP_JSS() throws IOException {
		File dir = new File(
				"/home/nbremond/.cache/SIMPA/Download/automata.cs.ru.nl/automata_pmwiki/uploads");
		for (File cat : dir.listFiles()) {
			if (cat.getName().contains("Circuit"))
				continue;
			if (cat.getName().contains("tmp.sh"))
				continue;

			if (cat.getName().contains("X-ray"))
				continue;
			processCat(cat);
		}

		System.out.println(latexConex);
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println(latexNotConex);

		writeTofile(latexNotConex, "benchmarkDescriptionReset.tex");
		writeTofile(latexConex, "benchmarkDescriptionStrong.tex");

	}

	void writeTofile(StringBuilder s, String fileName) {
		File f = new File(
				"/home/nbremond/shared_articles/JSS2018/figures/" + fileName);
		PrintStream out = null;
		try {
			out = new PrintStream(f);
			out.println("\\begin{tabular}{|l|l|l|l|l|l|}\n\\hline");
			out.println(
					"Category & model & states & inputs & outputs & transitions\\\\");
			out.println(s);
			out.println("\\hline\n\\end{tabular}");
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			out = System.out;
		}
	}


	void extend(StringBuilder builder, File cat, List<Mealy> automata) {
		if (automata.size() == 0)
			return;
		String catName = cat.getName().replaceFirst("Benchmark", "");
		for (int i = 0; i < automata.size(); i++) {
			if (i == 0)
				builder.append("\\hline\n\\multirow{" + automata.size() + "}{*}{"
								+ catName
				+ "}");
			Mealy m = automata.get(i);
			Set<String> input = new HashSet<>();
			Set<String> output = new HashSet<>();

			for (MealyTransition t : m.getTransitions()) {
				input.add(t.getInput());
				output.add(t.getOutput());
			}
			builder.append(
					"&" + GlobalGraphGenerator.dot_to_short_name(m.getName())
							.replaceAll(catName + "[ ]*", ""));
			builder.append("&" + m.getStateCount());
			builder.append("&" + input.size());
			builder.append("&" + output.size());
			builder.append("&" + m.getTransitions().size());
			builder.append("\\\\\n");
		}
	}

	class MinMax {
		Integer min = null;
		Integer max = null;

		void add(Integer v) {
			if (min == null) {
				min = v;
				max = v;
			} else {
				if (min > v)
					min = v;
				if (max < v)
					max = v;
			}
		}

		@Override
		public String toString() {
			if (min == null)
				return "-";
			if (min.equals(max)) {
				return min.toString();
			}
			return min.toString() + "-" + max.toString();
		}
	}

	void processCat(File cat) throws IOException {
		List<Mealy> conex = new ArrayList<>();
		List<Mealy> notConex = new ArrayList<>();
		MinMax states = new MinMax();
		MinMax outputs = new MinMax();
		MinMax inputs = new MinMax();
		MinMax transitions = new MinMax();
		System.out.println(cat);
		for (File dot : cat.listFiles()) {
			System.out.println(dot);
			Mealy m;
			try {
				m = Mealy.importFromDot(dot);
			} catch (ParseException e) {
				continue;
			}
			if (m.isConnex(false))
				conex.add(m);
			else
				notConex.add(m);
			states.add(m.getStateCount());

			Set<String> input = new HashSet<>();
			Set<String> output = new HashSet<>();

			for (MealyTransition t : m.getTransitions()) {
				input.add(t.getInput());
				output.add(t.getOutput());
			}
			outputs.add(output.size());
			inputs.add(input.size());
			transitions.add(m.getTransitions().size());
		}
		// if (connex == nb)
		// latex.append("all");
		// else if (connex == 0)
		// latex.append("none");
		// else
		// latex.append("some");

		extend(latexConex, cat, conex);
		extend(latexNotConex, cat, notConex);

	}

	StringBuilder latexConex = new StringBuilder();
	StringBuilder latexNotConex = new StringBuilder();
}
