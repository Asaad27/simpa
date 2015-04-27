package automata.mealy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import main.simpa.Options;
import tools.Utils;

public class InputSequence implements Cloneable {
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
		for (String input : sequence) {
			newis.addInput(input);
		}
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
	public String toString() {
		StringBuilder s = new StringBuilder();
		if (sequence.isEmpty())
			s.append(Options.SYMBOL_EPSILON);
		else {
			for (String input : sequence)
				s.append(input);
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
}
