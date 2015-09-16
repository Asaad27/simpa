package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

public class AdenilsoSimaoTool {

	public static int minLengthForExhaustivAutomata(Mealy automata, InputSequence trace) {
		try {
			int min = 1;
			int max = trace.getLength();
			System.out.println("computing min trace lehgth");
			while (min + 1 < max) {
				int middle = (min + max) / 2;
				if (isLengthSufficient(automata, trace, middle))
					max = middle;
				else
					min = middle;
			}
			if (max == trace.getLength() && !isLengthSufficient(automata, trace, max))
				max = trace.getLength() + 1;
			return max;
		} catch (InterruptedException e) {
			return -1;
		}
	}

	private static boolean isLengthSufficient(Mealy automata, InputSequence trace, int length)
			throws InterruptedException {
		assert length <= trace.getLength();
		System.out.println("checking if " + length + " symbols are sufficient to found the automata");
		try {
			File tempSeq = File.createTempFile("simpa_" + length + "_", ".seq");
			// tempSeq.deleteOnExit();
			Writer writer = new BufferedWriter(new FileWriter(tempSeq));
			for (int i = 0; i < length; i++)
				writer.write(trace.sequence.get(i) + " ");
			writer.close();

			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec("../../n-complete-exhaustive " + tempSeq.getAbsolutePath());
			State init = automata.getInitialState();
			StringBuilder fsmAutomata = new StringBuilder();
			int nodeId = 0;
			Map<State, Integer> nodeIds = new HashMap<>();
			for (State s : automata.getStates())
				nodeIds.put(s, new Integer(nodeId++));
			for (MealyTransition t : automata.getTransitionFrom(init))
				fsmAutomata.append(nodeIds.get(t.getFrom()) + " -- " + t.getInput() + " / " + t.getOutput() + " -> "
						+ nodeIds.get(t.getTo()) + "\n");

			for (MealyTransition t : automata.getTransitions())
				if (t.getFrom() != init)
					fsmAutomata.append(nodeIds.get(t.getFrom()) + " -- " + t.getInput() + " / " + t.getOutput() + " -> "
							+ nodeIds.get(t.getTo()) + "\n");

			// System.out.println(fsmAutomata);
			p.getOutputStream().write(fsmAutomata.toString().getBytes());

			p.getOutputStream().close();

			// try {
			// final BufferedReader reader = new BufferedReader(new
			// InputStreamReader(p.getInputStream()));
			// String line = null;
			// while ((line = reader.readLine()) != null) {
			// System.out.println(line);
			// }
			// reader.close();
			// } catch (final Exception e) {
			// e.printStackTrace();
			// }
			if (!p.waitFor(60, TimeUnit.SECONDS)){
				p.destroy();
				System.out.println("interrupted sub-process");
				throw new InterruptedException("time out");
			}
			return p.exitValue() == 0;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
