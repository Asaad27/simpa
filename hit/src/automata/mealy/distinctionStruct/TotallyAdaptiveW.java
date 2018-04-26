package automata.mealy.distinctionStruct;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import automata.mealy.AdaptiveStructure;
import automata.mealy.AdaptiveSymbolSequence;
import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import learner.mealy.LmTrace;
import tools.GraphViz;

public class TotallyAdaptiveW extends
		AdaptiveDistinctionStruct<AdaptiveSymbolSequence, AdaptiveSymbolSequence>
		implements
		DistinctionStruct<AdaptiveSymbolSequence, AdaptiveSymbolSequence> {

	private class AdaptiveCharacterization implements
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
			addPrint((AdaptiveSymbolSequence) w,
					(AdaptiveSymbolSequence) wResponse);
		}

		public void addPrint(AdaptiveSymbolSequence w,
				AdaptiveSymbolSequence wResponse) {
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
		 * forgot the last input applied. This can break iterators given by
		 * {@link #knownResponses()} and {@link #unknownPrints()}
		 */
		public void invalidateLastResponse() {
			characterizationPos = (TotallyAdaptiveW) characterizationPos.father;
			assert characterizationPos != null;
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
	}

	public TotallyAdaptiveW() {
		super();
	}

	@Override
	protected AdaptiveStructure<AdaptiveSymbolSequence, AdaptiveSymbolSequence> createNewNode() {
		return new TotallyAdaptiveW();
	}

	@Override
	protected boolean checkCompatibility(AdaptiveSymbolSequence input,
			AdaptiveSymbolSequence output) {
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
			AdaptiveStructure<AdaptiveSymbolSequence, AdaptiveSymbolSequence> child)
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
		// if tree is empty, insert new sequence at root.
		if (characterizationPos.isRoot()) {
			assert isEmpty() && characterizationPos == this;
			AdaptiveSymbolSequence adaptiveSeq = new AdaptiveSymbolSequence();
			AdaptiveSymbolSequence output = adaptiveSeq.extend(newSeq);
			characterizationPos.extend_local(adaptiveSeq);
			characterizationPos.getChild(output);
			return;
		}
		// if last input in tree is a prefix of newSeq, let's extends this input
		TotallyAdaptiveW characterizationFather = (TotallyAdaptiveW) characterizationPos.father;
		AdaptiveSymbolSequence lastInput = characterizationFather.getInput();
		if (newSeq.startsWith(lastInput)) {
			lastInput.extend(newSeq);
			characterizationFather.invalidateChild(characterizationPos);
			characterization.invalidateLastResponse();
			return;
		}
		// otherwise add new input at the end of the tree
		AdaptiveSymbolSequence adaptiveSeq = new AdaptiveSymbolSequence();
		AdaptiveSymbolSequence output = adaptiveSeq.extend(newSeq);
		characterizationPos.extend_local(adaptiveSeq);
		characterizationPos.getChild(output);
	}
}
