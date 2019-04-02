/********************************************************************************
 * Copyright (c) 2018,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 ********************************************************************************/
package learner.mealy.rivestSchapire;

import java.util.HashMap;
import java.util.Map;

import learner.mealy.LmTrace;

public class KnownTracesTree {
	class Node {
		protected String output;
		protected KnownTracesTree childTree;
	}

	public class InconsistencyException extends RuntimeException {
		private static final long serialVersionUID = 6384352447697004647L;
		public final LmTrace seen;
		public final String expectedLastOutput;

		public InconsistencyException(LmTrace t, String expected) {
			this.seen = t;
			this.expectedLastOutput = expected;
		}
	}

	Map<String, Node> children = new HashMap<String, KnownTracesTree.Node>();

	public void tryAndInsert(LmTrace trace) {
		KnownTracesTree current = this;
		for (int i = 0; i < trace.size(); i++) {
			Node currentNode = current.children.get(trace.getInput(i));
			if (currentNode == null) {
				currentNode = new Node();
				currentNode.output = trace.getOutput(i);
				currentNode.childTree = new KnownTracesTree();
				current.children.put(trace.getInput(i), currentNode);
			} else {
				if (!currentNode.output.equals(trace.getOutput(i))) {
					throw new InconsistencyException(trace.subtrace(0, i + 1),
							currentNode.output);
				}
			}
			current = currentNode.childTree;
			assert (current != null);
		}
	}
}
