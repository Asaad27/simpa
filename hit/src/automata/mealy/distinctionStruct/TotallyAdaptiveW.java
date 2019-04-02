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
package automata.mealy.distinctionStruct;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;
import tools.loggers.LogManager;

import automata.mealy.AdaptiveStructure;
import automata.mealy.AdaptiveSymbolSequence;
import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import learner.mealy.LmTrace;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.GraphViz;

/**
 * class AdaptiveSymbolSequenceForW extends the class AdaptiveSymbolSequence and
 * add fields which can be used by the class TotallyAdaptiveW to store data in
 * this level of adaptive structure.
 * 
 * @author Nicolas BREMOND
 */
class AdaptiveSymbolSequenceForW extends AdaptiveSymbolSequence {
	private TotallyAdaptiveW treeToClone = null;

	/**
	 * get a sub-part of a W-Tree which should be copied as a child when a new
	 * input is discovered.
	 * 
	 * @return the first tree found in this node its ancestors or null if no
	 *         tree can be found.
	 */
	public TotallyAdaptiveW getTreeToClone() {
		AdaptiveSymbolSequenceForW current = this;
		while (current != null) {
			if (current.treeToClone != null)
				return current.treeToClone;
			current = (AdaptiveSymbolSequenceForW) current.getFather();
		}
		return null;
	}

	/**
	 * set the tree to clone when a new input if discovered after this node.
	 * 
	 * @param tree
	 */
	void setTreeToClone(TotallyAdaptiveW tree) {
		assert treeToClone == null;
		treeToClone = tree;
	}

	@Override
	protected AdaptiveSymbolSequenceForW createEmptyNode() {
		return new AdaptiveSymbolSequenceForW();
	}

	@Override
	protected AdaptiveSymbolSequenceForW createNewChild(String o) {
		return new AdaptiveSymbolSequenceForW();
	}

	@Override
	public AdaptiveSymbolSequenceForW extend(LmTrace trace) {
		return (AdaptiveSymbolSequenceForW) super.extend(trace);
	}

	@Override
	public AdaptiveSymbolSequenceForW getAnswer(LmTrace possibleAnswer) {
		return (AdaptiveSymbolSequenceForW) super.getAnswer(possibleAnswer);
	}

	@Override
	protected AdaptiveSymbolSequence clone_local(
			Map<String, String> clonedOutputs) {
		AdaptiveSymbolSequenceForW result = (AdaptiveSymbolSequenceForW) super.clone_local(
				clonedOutputs);
		result.treeToClone = treeToClone;
		return result;
	}

	@Override
	protected void dot_appendNode(Writer writer) throws IOException {
		super.dot_appendNode(writer);
		if (treeToClone != null) {
			treeToClone.dot_appendAll(writer);
			writer.write(getDotName() + " -> " + treeToClone.getDotName()
					+ "[color=red,style=dashed,fontcolor=grey,label="
					+ GraphViz.id2DotAuto("other output (clone tree)")
					+ "];\n");
		}

	}
}

public class TotallyAdaptiveW extends
		AdaptiveDistinctionStruct<AdaptiveSymbolSequenceForW, AdaptiveSymbolSequenceForW>
		implements
		DistinctionStruct<AdaptiveSymbolSequence, AdaptiveSymbolSequence> {

	public class AdaptiveCharacterization implements
			Characterization<AdaptiveSymbolSequence, AdaptiveSymbolSequence> {
		TotallyAdaptiveW characterizationPos;

		private AdaptiveCharacterization(TotallyAdaptiveW root) {
			characterizationPos = root;
		}

		@Override
		public boolean acceptNextPrint(LmTrace print) {
			return !characterizationPos.isFinal()
					&& characterizationPos.input.hasAnswer(print);
		}

		@Override
		public void addPrint(LmTrace print) {
			assert acceptNextPrint(print);
			addPrint(characterizationPos.input,
					characterizationPos.input.getAnswer(print));
		}

		@Override
		public void addPrint(GenericInputSequence w,
				GenericOutputSequence wResponse) {
			addPrint((AdaptiveSymbolSequenceForW) w,
					(AdaptiveSymbolSequenceForW) wResponse);
		}

		public void addPrint(AdaptiveSymbolSequenceForW w,
				AdaptiveSymbolSequenceForW wResponse) {
			assert !characterizationPos.isFinal();
			assert w == characterizationPos.input;
			assert wResponse.isAnswerTo(w);
			characterizationPos = (TotallyAdaptiveW) characterizationPos
					.getChild(wResponse);
		}

		@Override
		public Iterable<LmTrace> knownResponses() {
			class KnownResponsesIterator
					implements java.util.Iterator<LmTrace> {
				TotallyAdaptiveW iteratorPos;

				public KnownResponsesIterator() {
					iteratorPos = (TotallyAdaptiveW) TotallyAdaptiveW.this
							.getFullSequence();

				}

				@Override
				public boolean hasNext() {
					assert characterizationPos.isAnswerTo(iteratorPos);
					return !iteratorPos.isFinal()
							&& iteratorPos != characterizationPos;
				}

				@Override
				public LmTrace next() {
					TotallyAdaptiveW previousSeq = iteratorPos;
					// iterate over ancestors of characterization to find which
					// is the child of iteratorPos
					TotallyAdaptiveW father = (TotallyAdaptiveW) characterizationPos.father;
					TotallyAdaptiveW child = characterizationPos;
					while (father != iteratorPos) {
						child = father;
						father = (TotallyAdaptiveW) father.father;
						if (father == null)
							throw new NoSuchElementException();
					}
					iteratorPos = child;
					return previousSeq.input.buildTrace(iteratorPos.output);
				}
			}

			return new Iterable<LmTrace>() {
				@Override
				public KnownResponsesIterator iterator() {
					return new KnownResponsesIterator();
				}
			};
		}

		@Override
		public Iterable<AdaptiveSymbolSequence> unknownPrints() {
			class UnknownResponsesIterator
					implements java.util.Iterator<AdaptiveSymbolSequence> {
				TotallyAdaptiveW lastPos;

				public UnknownResponsesIterator() {
					lastPos = null;
				}

				@Override
				public boolean hasNext() {
					return !characterizationPos.isFinal()
							&& lastPos != characterizationPos;
				}

				@Override
				public AdaptiveSymbolSequence next() {
					if (!hasNext())
						throw new NoSuchElementException();
					lastPos = characterizationPos;
					return characterizationPos.getInput();
				}
			}

			return new Iterable<AdaptiveSymbolSequence>() {
				@Override
				public UnknownResponsesIterator iterator() {
					return new UnknownResponsesIterator();
				}
			};
		}

		@Override
		public List<AdaptiveSymbolSequence> getUnknownPrints() {
			List<AdaptiveSymbolSequence> res = new ArrayList<>();
			if (!isComplete())
				res.add(characterizationPos.getInput());
			return res;
		}

		@Override
		public boolean isComplete() {
			assert TotallyAdaptiveW.this.getAllNodes().contains(
					characterizationPos) : "characterization is not in tree anymore";
			return characterizationPos.isFinal();
		}

		@Override
		public boolean contains(LmTrace trace) {
			assert isComplete();
			TotallyAdaptiveW current = (TotallyAdaptiveW) characterizationPos.father;
			while (current != null) {
				if (current.input.hasPrefix(trace))
					return true;
				current = (TotallyAdaptiveW) current.father;
			}
			return false;
		}

		/**
		 * indicate that this characterization is not valid anymore and
		 * shouldn't be used. This can break iterators given by
		 * {@link #knownResponses()} and {@link #unknownPrints()}
		 */
		public void invalidate() {
			characterizationPos = null;
		}

		@Override
		public int hashCode() {
			return characterizationPos.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof AdaptiveCharacterization) {
				return equals((AdaptiveCharacterization) o);
			} else {
				throw new RuntimeException("not implemented yet");
			}

		}

		public boolean equals(AdaptiveCharacterization o) {
			assert characterizationPos.root == o.characterizationPos.root;
			return characterizationPos == o.characterizationPos;
		}

		@Override
		public String toString() {
			Stack<TotallyAdaptiveW> stack = characterizationPos
					.getStackFromRoot();
			TotallyAdaptiveW current = stack.pop();
			List<Object> traces = new ArrayList<>();
			while (!current.isFinal()) {
				if (!stack.isEmpty()) {
					TotallyAdaptiveW next = stack.pop();
					traces.add(current.input.buildTrace(next.getFromOutput()));
					current = next;
				} else {
					// partial characterization
					traces.add(current.input);
					break;
				}
			}
			return traces.toString();
		}
	}

	public TotallyAdaptiveW() {
		super();
	}

	@Override
	protected TotallyAdaptiveW createNewChild(
			AdaptiveSymbolSequenceForW output) {
		TotallyAdaptiveW toClone = output.getTreeToClone();
		if (toClone == null)
			return new TotallyAdaptiveW();
		assert !output.isRoot();
		if (Options.getLogLevel() == LogLevel.ALL)
			LogManager.logInfo(
					"In adaptive W : cloning a previously used tree to create the child for output ",
					output.getFromOutput(), " in characterization ",
					new AdaptiveCharacterization(this));
		return toClone.clone(null);
	}

	@Override
	protected TotallyAdaptiveW clone_local(
			Map<AdaptiveSymbolSequenceForW, AdaptiveSymbolSequenceForW> clonedOutputs) {
		TotallyAdaptiveW result = new TotallyAdaptiveW();
		if (input != null)
			result.input = input.clone(clonedOutputs);
		return result;
	}

	@Override
	public TotallyAdaptiveW clone() {
		return (TotallyAdaptiveW) clone(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Stack<TotallyAdaptiveW> getStackFromRoot() {
		return (Stack<TotallyAdaptiveW>) super.getStackFromRoot();
	}

	@Override
	protected boolean checkCompatibility(AdaptiveSymbolSequenceForW input,
			AdaptiveSymbolSequenceForW output) {
		if (!output.isAnswerTo(input))
			return false;
		return true;
	}

	@Override
	public String getDotName() {
		if (input != null)
			return input.getDotName();
		return super.getDotName();
	}

	@Override
	protected void dot_appendNode(Writer writer) throws IOException {
		if (input != null) {
			input.dot_appendAll(writer);
		} else {
			writer.write(getDotName() + "[label="
					+ GraphViz.id2DotAuto("characterization\ncomplete")
					+ "];\n");
		}
	}

	@Override
	protected void dot_appendChild(Writer writer,
			AdaptiveStructure<AdaptiveSymbolSequenceForW, AdaptiveSymbolSequenceForW> child)
			throws IOException {
		assert this.getClass().isInstance(child);
		if (child.isFinal()) {
			writer.write(child.getFromOutput().getDotName() + " -> "
					+ child.getDotName() + "[label=" + GraphViz.id2DotAuto("")
					+ ",color=red];\n");
		} else {
			writer.write(child.getFromOutput().getDotName() + " -> "
					+ child.getDotName() + "[label="
					+ GraphViz.id2DotAuto("new sequence") + ",color=red];\n");
		}
	}

	@Override
	public AdaptiveCharacterization getEmptyCharacterization() {
		assert isRoot();
		return new AdaptiveCharacterization(
				(TotallyAdaptiveW) getFullSequence());
	}

	@Override
	public void refine(
			Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> genericCharacterization,
			LmTrace newSeq) {
		assert isRoot();
		assert !genericCharacterization.contains(newSeq);
		AdaptiveCharacterization characterization = (AdaptiveCharacterization) genericCharacterization;
		TotallyAdaptiveW characterizationPos = characterization.characterizationPos;
		TotallyAdaptiveW afterPrefix = null;
		while (!characterizationPos.isRoot()) {
			AdaptiveSymbolSequence inputSeq = characterizationPos.father
					.getInput();
			if (newSeq.startsWith(inputSeq)) {
				assert afterPrefix == null : "as for non-adaptive W, only one prefix should be found.";
				afterPrefix = characterizationPos;
			}
			characterizationPos = (TotallyAdaptiveW) characterizationPos.father;
		}
		if (afterPrefix != null) {
			if (!afterPrefix.isFinal())
				afterPrefix.output.setTreeToClone(afterPrefix);
			TotallyAdaptiveW askingPrefix = (TotallyAdaptiveW) afterPrefix
					.getFather();
			AdaptiveSymbolSequenceForW inputSeq = askingPrefix.getInput();
			InputSequence newIn = newSeq.getInputsProjection();
			OutputSequence newOut = newSeq.getOutputsProjection();
			inputSeq.extend(newIn.sequence,
					newOut.sequence.subList(0, newSeq.size() - 1));
			askingPrefix.invalidateChild(afterPrefix);
			characterization.invalidate();
		} else {
			// otherwise add new input at the end of the tree
			characterizationPos = characterization.characterizationPos;
			AdaptiveSymbolSequenceForW adaptiveSeq = new AdaptiveSymbolSequenceForW();
			AdaptiveSymbolSequenceForW output = adaptiveSeq.extend(newSeq);
			characterizationPos.extend_local(adaptiveSeq);
			characterizationPos.getChild(output);
		}
	}

	/**
	 * Add a new sequence after the current position in tree.
	 * 
	 * @return the freshly created sequence.
	 */
	public AdaptiveSymbolSequenceForW createNewSymbolSequence() {
		AdaptiveSymbolSequenceForW r = new AdaptiveSymbolSequenceForW();
		extend_local(r);
		return r;
	}

	/**
	 * Create and get the node reached after a response to the input of this
	 * node. This is similar to {@link #getChild(AdaptiveSymbolSequenceForW)}
	 * but with an automatic cast.
	 * 
	 * @param end_
	 *            the response to the input of this node.
	 * @return the node of W reached after the given answer.
	 */
	public TotallyAdaptiveW recordEndOfSequence(AdaptiveSymbolSequence end_) {
		AdaptiveSymbolSequenceForW end = (AdaptiveSymbolSequenceForW) end_;
		assert end.checkCompatibilityWith(getInput());
		return (TotallyAdaptiveW) getChild(end);
	}
}
