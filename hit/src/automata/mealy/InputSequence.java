package automata.mealy;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import learner.mealy.LmTrace;
import main.simpa.Options;
import tools.Utils;

public class InputSequence implements Cloneable, GenericInputSequence {
	public class SequenceIterator implements Iterator {
		ListIterator<String> parent;
		OutputSequence response = new OutputSequence();

		protected SequenceIterator() {
			this.parent = sequence.listIterator();
		}

		@Override
		public void setPreviousOutput(String previousOutput) {
			if (parent.previousIndex() != response.getLength())
				throw new InvalidCallException(
						"some previous outputs were not providen");
			response.addOutput(previousOutput);

		}

		@Override
		public boolean hasNext() {
			return parent.hasNext();
		}

		@Override
		public String next() {
			return parent.next();
		}

		@Override
		public OutputSequence getResponse() {
			if (response.getLength() != InputSequence.this.getLength())
				throw new InvalidCallException(
						"some outputs were not providen");
			assert response.checkCompatibilityWith(InputSequence.this);
			return response;
		}
	}

	public List<String> sequence;

	public InputSequence() {
		sequence = new ArrayList<>();
	}

	public InputSequence(String input) {
		this();
		sequence.add(input);
	}

	public InputSequence addInput(String input) {
		sequence.add(input);
		return this;
	}

	public InputSequence addInputSequence(InputSequence inputSeq) {
		sequence.addAll(inputSeq.sequence);
		return this;
	}

	public int getLength() {
		return sequence.size();
	}

	public InputSequence getIthSuffix(int start) {
		InputSequence newis = new InputSequence();
		for (int i = sequence.size() - start; i < sequence.size(); i++) {
			newis.addInput(sequence.get(i));
		}
		return newis;
	}

	public InputSequence getIthPreffix(int end) {
		InputSequence newis = new InputSequence();
		for (int i = 0; i < end; i++) {
			newis.addInput(sequence.get(i));
		}
		return newis;
	}

	@Override
	public InputSequence clone() {
		InputSequence newis = new InputSequence();
		newis.addInputSequence(this);
		return newis;
	}

	public boolean equals(InputSequence o) {
		if (sequence.size() != o.sequence.size())
			return false;
		else {
			for (int i = 0; i < sequence.size(); i++) {
				if (!sequence.get(i).equals(o.sequence.get(i)))
					return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + Objects.hashCode(this.sequence);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final InputSequence other = (InputSequence) obj;
		if (!Objects.equals(this.sequence, other.sequence)) {
			return false;
		}
		return true;
	}

	public String getFirstSymbol() {
		return sequence.get(0);
	}

	public String getLastSymbol() {
		return sequence.get(sequence.size() - 1);
	}

	public boolean isSame(InputSequence pis) {
		return pis.toString().equals(toString());
	}

	public boolean startsWith(InputSequence pis) {
		if (pis.sequence.isEmpty() || sequence.isEmpty())
			return false;
		if (pis.sequence.size() <= sequence.size()) {
			for (int i = 0; i < pis.sequence.size(); i++) {
				if ((!pis.sequence.get(i).equals(sequence.get(i))))
					return false;
			}
			return true;
		} else
			return false;
	}

	@Override
	public boolean hasPrefix(LmTrace possiblePrefix) {
		if (possiblePrefix.size() == 0)
			return true;
		return startsWith(possiblePrefix.getInputsProjection());
	}

	@Override
	public void extendsWith(LmTrace newSeq) {
		assert newSeq.startsWith(this);
		InputSequence other = newSeq.getInputsProjection();
		while (getLength() < other.getLength())
			addInput(other.sequence.get(getLength()));
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		if (sequence.isEmpty())
			s.append(Options.SYMBOL_EPSILON);
		else if (Options.REDUCE_DISPLAYED_TRACES > 0
				&& sequence.size() > Options.REDUCE_DISPLAYED_TRACES) {
			int i = 0;
			while (i < Options.REDUCE_DISPLAYED_TRACES / 2) {
				s.append(sequence.get(i));
				s.append('.');
				i++;
			}
			s.append(" … ");
			s.append('.');
			while (i < Options.REDUCE_DISPLAYED_TRACES) {
				s.append(sequence.get(
						sequence.size() - Options.REDUCE_DISPLAYED_TRACES + i));
				s.append('.');
				i++;
			}
			s.deleteCharAt(s.length() - 1);
		} else {
			for (String input : sequence) {
				s.append(input);
				s.append('.');
			}
			s.deleteCharAt(s.length() - 1);
		}
		return s.toString();
	}

	public InputSequence removeLastInput() {
		if (!sequence.isEmpty())
			sequence.remove(sequence.size() - 1);
		return this;
	}

	public InputSequence removeFirstInput() {
		if (!sequence.isEmpty())
			sequence.remove(0);
		return this;
	}

	/**
	 * generate a sequence of random inputs
	 * 
	 * @param is
	 *            the set of usable inputs
	 * @param length
	 *            the wanted length of sequence.
	 * @return a sequence made of inputs randomly chosen.
	 */
	public static InputSequence generate(List<String> is, int length) {
		InputSequence seq = new InputSequence();
		for (int i = 0; i < length; i++) {
			seq.addInput(Utils.randIn(is));
		}
		return seq;
	}

	public void prependInput(String input) {
		sequence.add(0, input);
	}

	@Override
	public Iterator inputIterator() {
		return new SequenceIterator();
	}

	@Override
	public automata.mealy.GenericSequence.Iterator<String, String> iterator() {
		return inputIterator();
	}

	@Override
	public boolean isEmpty() {
		return sequence.isEmpty();
	}

	@Override
	public LmTrace buildTrace(GenericOutputSequence outSeq) {
		assert outSeq.checkCompatibilityWith(this);
		return new LmTrace(this, outSeq.toFixedOutput());
	}

	@Override
	public int getMaxLength() {
		return getLength();
	}

}
