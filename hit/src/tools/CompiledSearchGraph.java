/********************************************************************************
 * Copyright (c) 2017,2019 Institut Polytechnique de Grenoble 
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
package tools;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import automata.mealy.InputSequence;

/**
 * graph for optimized sequence search in a (long) trace.
 * 
 */
public class CompiledSearchGraph {
	public static class Node {
		protected Map<String, Node> children;// the children of the node mapped
												// with input symbols. The
												// absence of child means that
												// we should go to starting
												// node.
		protected boolean acceptWord = false;
		protected int neededTraceLength;// the number of symbols seen since
										// start of researched word

		protected Node(int neededTraceLength) {
			children = new HashMap<>();
			this.neededTraceLength = neededTraceLength;
		}

		/**
		 * return a set of inputs which do not lead to the starting node.
		 * Actually, one input might lead to starting node in some case (e.g. when searching the empty word).
		 * So, more precisely, the 
		 */
		public Set<String> getNonResetingInput() {
			return children.keySet();
		}

		public int getNeededTraceLength() {
			return neededTraceLength;
		}

		public boolean acceptWord() {
			return acceptWord;
		}
	}

	private Node startNode;
	private Node currentNode;

	public CompiledSearchGraph(InputSequence seq) {
		startNode = new Node(0);
		List<Node> nodes = new ArrayList<>(seq.getLength() + 1);
		Node current = startNode;
		for (int i = 0; i < seq.getLength(); i++) {
			nodes.add(current);
			Node child = new Node(i + 1);
			current.children.put(seq.sequence.get(i), child);
			current = child;
		}
		nodes.add(current);
		current.acceptWord = true;

		for (int i = 1; i <= seq.getLength(); i++) {
			int j = 0;
			current = nodes.get(i);
			while (current.children.containsKey(seq.sequence.get(j))) {
				current = current.children.get(seq.sequence.get(j));
				j++;
				if (j == seq.getLength())
					break;
			}
			if (j < seq.getLength())
				current.children.put(seq.sequence.get(j), nodes.get(j + 1));

		}

		currentNode = startNode;
	}

	public Node getStatus() {
		assert currentNode != null;
		return currentNode;
	}

	public void setStatus(Node status) {
		currentNode = status;
		assert currentNode != null;
	}

	public Node getStart() {
		return startNode;
	}

	public void resetStatus() {
		currentNode = startNode;
	}

	public int neededTraceLength() {
		if (currentNode == startNode)
			return 0;
		return currentNode.neededTraceLength;
	}

	public boolean apply(Node status, String input) {
		currentNode = status;
		return apply(input);
	}

	public boolean apply(String input) {
		Node next = currentNode.children.get(input);
		if (next != null) {
			currentNode = next;
		} else {
			currentNode = startNode;
		}
		return currentNode.acceptWord;
	}

	private int nodeDotNb = 0;

	public void addToDot(StringBuilder s, Map<Node, String> exported) {
		Set<Node> seen = new HashSet<>();
		Queue<Node> toWrite = new ArrayDeque<>();
		toWrite.add(startNode);
		nodeDotNb = 1;
		exported.put(startNode, "CSG_0");
		seen.add(startNode);

		while (!toWrite.isEmpty()) {
			Node current = toWrite.poll();
			String name = exported.get(current);
			assert (name != null);
			s.append("\t" + name + " [label=\""
					+ (current.acceptWord ? "OK" : "") + "\",color=green"
					+ (current.acceptWord ? ",shape=doublecircle" : "")
					+ "];\n");

			for (Entry<String, Node> entry : current.children.entrySet()) {
				String input = entry.getKey();
				Node child = entry.getValue();
				String childName = exported.get(child);
				if (childName == null) {
					childName = "CSG_" + nodeDotNb++;
					exported.put(child, childName);
				}
				if (!seen.contains(child)) {
					toWrite.add(child);
					seen.add(child);
				}

				s.append("\t" + name + " -> " + childName
						+ " [color=green,fontcolor=green,label=\"" + input
						+ "\"]" + "\n");
			}
		}
	}

	public boolean isAcceptingWord() {
		return currentNode.acceptWord;
	}

}
