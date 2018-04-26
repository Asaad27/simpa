package automata.mealy.distinctionStruct;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import learner.mealy.LmTrace;
import tools.loggers.LogManager;

public class TotallyFixedW extends ArrayList<InputSequence>
		implements DistinctionStruct<InputSequence, OutputSequence> {
	private static final long serialVersionUID = 38357699697464527L;

	private class FixedCharacterization
			implements Characterization<InputSequence, OutputSequence> {
		List<OutputSequence> WResponses;

		@Override
		public boolean acceptNextPrint(LmTrace print) {
			// start with heuristics checking to save time :
			if (isComplete())
				return false;
			if (TotallyFixedW.this.size() > WResponses.size()
					&& TotallyFixedW.this.get(WResponses.size())
							.equals(print.getInputsProjection()))
				return true;
			// Otherwise, perform a complete checking.
			for (int i = 0; i < TotallyFixedW.this.size(); i++) {
				if (TotallyFixedW.this.get(i)
						.equals(print.getInputsProjection())
						&& (WResponses.size() < i || WResponses.get(i) == null))
					return true;
			}

			return false;
		}

		@Override
		public void addPrint(LmTrace print) {
			addPrint(print.getInputsProjection(), print.getOutputsProjection());
		}

		@Override
		public void addPrint(GenericInputSequence wGeneric,
				GenericOutputSequence wResponseGeneric) {
			addPrint((InputSequence) wGeneric,
					(OutputSequence) wResponseGeneric);
		}

		public void addPrint(InputSequence w, OutputSequence wResponse) {
			// heuristic add
			if (TotallyFixedW.this.size() == WResponses.size() + 1
					&& TotallyFixedW.this.get(WResponses.size()).equals(w)) {
				WResponses.add(wResponse);
			}
			// complete checking for adding
			for (int i = 0; i < TotallyFixedW.this.size(); i++) {
				if (TotallyFixedW.this.get(i).equals(w)
						&& (WResponses.size() < i || WResponses.get(i) == null))
					for (int j = WResponses.size(); j < i; j++)
						WResponses.add(null);
				WResponses.add(wResponse);
			}
			throw new RuntimeException("invalid print");

		}

		@Override
		public Iterable<LmTrace> knownResponses() {
			class KnownResponsesIterator
					implements java.util.Iterator<LmTrace> {
				int pos = 0;

				@Override
				public boolean hasNext() {
					while (pos < WResponses.size()) {
						if (WResponses.get(pos) != null)
							return true;
						pos++;
					}
					return false;

				}

				@Override
				public LmTrace next() {
					if (!hasNext())
						throw new NoSuchElementException();
					LmTrace trace = new LmTrace(TotallyFixedW.this.get(pos),
							WResponses.get(pos));
					pos++;
					return trace;
				}

			}

			return new Iterable<LmTrace>() {
				@Override
				public Iterator<LmTrace> iterator() {
					return new KnownResponsesIterator();
				}

			};
		}

		@Override
		public Iterable<InputSequence> unknownPrints() {
			class UnknownResponsesIterator
					implements java.util.Iterator<InputSequence> {
				int pos = 0;

				@Override
				public boolean hasNext() {
					while (pos < TotallyFixedW.this.size()) {
						if (pos >= WResponses.size()
								|| WResponses.get(pos) == null)
							return true;
						pos++;
					}
					return false;

				}

				@Override
				public InputSequence next() {
					if (!hasNext())
						throw new NoSuchElementException();
					return TotallyFixedW.this.get(pos++);
				}

			}
			return new Iterable<InputSequence>() {

				@Override
				public Iterator<InputSequence> iterator() {
					return new UnknownResponsesIterator();
				}

			};
		}

		@Override
		public List<InputSequence> getUnknownPrints() {
			List<InputSequence> res = new ArrayList<>();
			for (int i = 0; i < TotallyFixedW.this.size(); i++) {
				if (i >= WResponses.size() || WResponses.get(i) == null)
					res.add(TotallyFixedW.this.get(i));
			}
			return res;
		}

		@Override
		public boolean isComplete() {
			return WResponses.size() == TotallyFixedW.this.size()
					&& !WResponses.contains(null);
		}

		@Override
		public boolean contains(LmTrace trace) {
			assert isComplete();
			for (InputSequence w : TotallyFixedW.this) {
				if (w.hasPrefix(trace))
					return true;
			}
			return false;
		}

	}

	@Override
	public FixedCharacterization getEmptyCharacterization() {
		return new FixedCharacterization();
	}

	@Override
	public void refine(
			Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> characterization,
			LmTrace newSeq) {
		assert characterization.isComplete()
				&& !characterization.contains(newSeq);
		for (InputSequence w : this) {
			assert !w.hasPrefix(newSeq);
			if (newSeq.startsWith(w) || w.getLength() == 0) {
				set(indexOf(w), newSeq.getInputsProjection());
				LogManager.logInfo("removing " + w
						+ " from W-set because it's a prefix of new inputSequence");
				break;// because W is only extended with this method, there is
						// at most one prefix of newSeq in W (otherwise, one
						// prefix of newSeq in W is also a prefix of the other
						// prefix of newSeq in W, which is not possible by
						// construction of W)
			}
		}
	}

	@Override
	public StringBuilder toString(StringBuilder s) {
		s.append("[");
		for (InputSequence seq : this) {
			s.append(seq);
			s.append(", ");
		}
		s.setLength(s.length() - 2);
		s.append("]");
		return s;
	}

}
