package automata.mealy;

import java.util.ArrayList;
import java.util.List;

import main.simpa.Options;

public class OutputSequence implements Cloneable {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((sequence == null) ? 0 : sequence.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OutputSequence other = (OutputSequence) obj;
		if (sequence == null) {
			if (other.sequence != null)
				return false;
		} else if (!sequence.equals(other.sequence))
			return false;
		return true;
	}

	public List<String> sequence;

	public OutputSequence() {
		sequence = new ArrayList<String>();
	}

	public void addOmegaInput() {
		sequence.add(new String());
	}

	public void addOutput(String output) {
		sequence.add(output);
	}

	public int getLength() {
		return sequence.size();
	}

	@Override
	public OutputSequence clone() {
		OutputSequence newos = new OutputSequence();
		for (String output : sequence) {
			newos.addOutput(new String(output));
		}
		return newos;
	}

	public OutputSequence getIthSuffix(int start) {
		OutputSequence newis = new OutputSequence();
		for (int i = sequence.size() - start; i < sequence.size(); i++) {
			newis.addOutput(new String(sequence.get(i)));
		}
		return newis;
	}
	
	public OutputSequence getIthPreffix(int end) {
		OutputSequence newis = new OutputSequence();
		for (int i = 0; i < end; i++) {
			newis.addOutput(new String(sequence.get(i)));
		}
		return newis;
	}

	public OutputSequence subSequence(int start, int end){
		OutputSequence newis = new OutputSequence();
		for (int i = start; i < end; i++) {
			newis.addOutput(new String(sequence.get(i)));
		}
		return newis;
	}
	
	public boolean equals(OutputSequence o) {
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

	public String getLastSymbol() {
		return sequence.get(sequence.size() - 1);
	}

	public boolean isSame(OutputSequence os) {
		return os.toString().equals(toString());
	}

	public OutputSequence removeOmegaOutput() {
		if (sequence.get(0).length() == 0)
			sequence.remove(0);
		return this;
	}

	public boolean startsWith(OutputSequence os) {
		if (os.sequence.isEmpty() || sequence.isEmpty())
			return false;
		if (os.sequence.size() <= sequence.size()) {
			for (int i = 0; i < os.sequence.size(); i++) {
				if ((!os.sequence.get(i).equals(sequence.get(i))))
					return false;
			}
			return true;
		} else
			return false;
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		for (String input : sequence) {
			if (input.length() > 0)
				s.append(input.toString());
			else
				s.append(Options.SYMBOL_OMEGA_LOW);
		}
		return s.toString();
	}
}