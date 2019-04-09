/********************************************************************************
 * Copyright (c) 2015,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 *     Roland GROZ
 *     Yves GUERTE
 ********************************************************************************/
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
	
	private static String nCompleteExhaustiveToolPath = "n-complete-exhaustive";
	
	public static boolean isToolPresent() {
		boolean isPresent = false;
		
		File f = new File(nCompleteExhaustiveToolPath);
		if (f.exists() && !f.isDirectory() && f.canExecute()) { 
			isPresent = true;
		}
		return isPresent;
	}

	public static int minLengthForExhaustivAutomata(Mealy automata, InputSequence trace) {
		if (!isToolPresent()) {
			System.out.println("tool to compute minimum trace ('"
					+ nCompleteExhaustiveToolPath + "') is not found.");
			return -2;
		}
		try {
			int min = 1;
			int max = trace.getLength();
			System.out.println("computing min trace length");
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
		System.out.println("checking if " + length + " symbols are sufficient to find the automaton");
		File tempSeq = null;
		try {
			tempSeq = File.createTempFile("simpa_" + length + "_", ".seq");
			Writer writer = new BufferedWriter(new FileWriter(tempSeq));
			for (int i = 0; i < length; i++)
				writer.write(trace.sequence.get(i) + " ");
			writer.close();

			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec(nCompleteExhaustiveToolPath + " " + tempSeq.getAbsolutePath());
			State init = automata.getInitialState();
			StringBuilder fsmAutomata = new StringBuilder();
			int nodeId = 0;
			Map<State, Integer> nodeIds = new HashMap<>();
			for (State s : automata.getStates())
				nodeIds.put(s, Integer.valueOf(nodeId++));
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
			if (!p.waitFor(15, TimeUnit.SECONDS)){
				p.destroy();
				System.out.println("interrupted sub-process (you can try to change time in java source"
						+ "");
				throw new InterruptedException("time out");
			}
			return p.exitValue() == 0;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (tempSeq != null && tempSeq.exists())
				tempSeq.delete();
		}
	}
}
