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
package learner.mealy.hW.dataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import learner.mealy.LmTrace;

public class TraceTree {
	private HashMap<String, TraceTree> children;
	private HashMap<String, String> outputs;

	public TraceTree() {
		children = new HashMap<>();
		outputs = new HashMap<>();
	}

	public boolean contains(InputSequence inSeq) {
		return getOutput(inSeq) != null;
	}

	public OutputSequence getOutput(InputSequence inSeq) {
		TraceTree currentTree = this;
		OutputSequence outSeq = new OutputSequence();
		int seqPos = 0;
		while (currentTree != null) {
			if (inSeq.getLength() == seqPos)
				return outSeq;
			String input = inSeq.sequence.get(seqPos);
			String output = outputs.get(input);
			if (output == null)
				return null;
			outSeq.addOutput(output);
			currentTree = currentTree.children.get(input);
			seqPos++;
		}
		if (seqPos == inSeq.getLength())
			return outSeq;
		else
			return null;
	}

	public String getOutput(String input) {
		return outputs.get(input);
	}

	/**
	 * add other tree to this tree (other tree is not modified)
	 * 
	 * @param other
	 * @return null if everything was OK, or ONE inconsistency (even if several
	 *         exists for the given operation)
	 */
	public InconsistancyWhileMergingExpectedTracesException add(
			TraceTree other) {
		if (other == null)
			return null;
		for (String input : other.outputs.keySet()) {
			String otherOutput = other.outputs.get(input);
			String thisOutput = outputs.get(input);
			TraceTree otherChild = other.children.get(input);
			if (thisOutput == null) {
				this.outputs.put(input, otherOutput);
				assert !this.children.containsKey(input);
				this.children.put(input, otherChild);
			} else {
				if (!thisOutput.equals(otherOutput))
					return new InconsistancyWhileMergingExpectedTracesException(
							input, thisOutput, otherOutput);
				TraceTree thisChild = children.get(input);
				if (thisChild == null) {
					children.put(input, otherChild);
				} else {
					InconsistancyWhileMergingExpectedTracesException inconsistency = thisChild
							.add(otherChild);
					if (inconsistency != null) {
						inconsistency.addPreviousState(null, input, thisOutput);
						return inconsistency;
					}
				}
			}
		}
		return null;
	}

	/**
	 * add a trace to the tree
	 * 
	 * @param trace
	 * @return true if everything is OK, false if the trace is inconsistent with
	 *         the tree
	 */
	public boolean addTrace(LmTrace trace) {
		if (trace.size() == 0)
			return true;

		TraceTree currentTree = this;
		int seqPos = 0;
		while (trace.size() != seqPos) {
			String input = trace.getInput(seqPos);
			String traceOutput = trace.getOutput(seqPos);
			String treeOutput = currentTree.outputs.get(input);
			TraceTree child = currentTree.children.get(input);
			if (treeOutput == null) {
				currentTree.outputs.put(input, traceOutput);
				treeOutput = traceOutput;
				assert child == null;
			} else {
				if (!traceOutput.equals(treeOutput))
					return false;
			}
			if (child == null) {
				child = new TraceTree();
				currentTree.children.put(input, child);
			}

			currentTree = child;
			seqPos++;
		}
		return true;
	}

	public static boolean isLeaf(TraceTree tt) {
		return tt == null || tt.outputs.size() == 0;
	}

	/**
	 * remove prefixes of inSeq in tree and detach trace beginning with inSeq.
	 * 
	 * @param inSeq
	 * @param toClean
	 *            the list of nodes crossed while reading inSeq
	 */
	private void remove(InputSequence inSeq, List<TraceTree> toClean) {
		assert toClean.size() <= inSeq.getLength();
		// detach subtree after inSeq
		if (toClean.size() == inSeq.getLength()) {
			TraceTree last = toClean.get(toClean.size() - 1);
			last.children.remove(inSeq.sequence.get(toClean.size() - 1));
			last.outputs.remove(inSeq.sequence.get(toClean.size() - 1));
		}
		// remove prefixes
		for (int i = toClean.size() - 2; i >= 0; i++) {
			assert toClean.get(i).children.get(inSeq.sequence.get(i)) == toClean
					.get(i + 1);
			if (isLeaf(toClean.get(i + 1))) {
				toClean.get(i).children.remove(inSeq.sequence.get(i));
				toClean.get(i).outputs.remove(inSeq.sequence.get(i));
			}
		}
	}

	public TraceTree pollTree(InputSequence inSeq) {
		TraceTree currentTree = this;
		int seqPos = 0;
		ArrayList<TraceTree> toClean = new ArrayList<>();
		while (inSeq.getLength() != seqPos) {
			toClean.add(currentTree);
			if (currentTree == null) {
				remove(inSeq, toClean);
				return null;
			}
			String input = inSeq.sequence.get(seqPos);
			currentTree = currentTree.children.get(input);
			seqPos++;
		}
		remove(inSeq, toClean);
		return currentTree;
	}

	/**
	 * The returned tree must only be read-only. a change in resulting tree will
	 * not always be reflected in the parent tree (because it do not instantiate
	 * missing subtrees)
	 * 
	 * @param inSeq
	 * @return
	 */
	public TraceTree getSubTreeRO(InputSequence inSeq) {
		TraceTree currentTree = this;
		int seqPos = 0;
		while (inSeq.getLength() != seqPos) {
			if (currentTree == null)
				return null;
			String input = inSeq.sequence.get(seqPos);
			currentTree = currentTree.children.get(input);
			seqPos++;
		}
		return currentTree;

	}

	/**
	 * The returned tree is read-only. A change in resulting tree will not
	 * always be reflected in the parent tree (because it do not instantiate
	 * missing subtrees)
	 */
	public TraceTree getSubTreeRO(String input) {
		return children.get(input);
	}

	public void addSequencesToList(List<LmTrace> results, LmTrace prefix) {
		for (Map.Entry<String, String> e : outputs.entrySet()) {
			LmTrace newTrace = prefix.clone();
			newTrace.append(e.getKey(), e.getValue());
			results.add(newTrace);
			TraceTree child = children.get(e.getKey());
			if (child != null) {
				child.addSequencesToList(results, newTrace);
			}
		}
	}

	public String toString() {
		ArrayList<LmTrace> traces = new ArrayList<>();
		addSequencesToList(traces, new LmTrace());
		return traces.toString();
	}

	public void exportToDot(StringBuilder builder, String previousName) {
		for (Map.Entry<String, String> e : outputs.entrySet()) {
			String childName = previousName + "_"
					+ e.getKey().replaceAll("[^0-9a-zA-Z_]*", "_");
			builder.append("\t" + childName + " [label=\"\",color=red];\n");
			builder.append("\t" + previousName + " -> " + childName
					+ " [style=dashed,color=red,fontcolor=red,label=\""
					+ new LmTrace(e.getKey(), e.getValue()) + "\"]" + "\n");

			TraceTree child = children.get(e.getKey());
			if (child != null) {
				child.exportToDot(builder, childName);
			}
		}
	}
}
