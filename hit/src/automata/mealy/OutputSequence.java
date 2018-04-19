package automata.mealy;

import java.util.ArrayList;
import java.util.List;

import automata.mealy.GenericInputSequence.GenericOutputSequence;
import main.simpa.Options;

public class OutputSequence implements Cloneable, GenericOutputSequence {
	public List<String> sequence;

	public OutputSequence() {
		sequence = new ArrayList<String>();
	}

	public void addOmegaInput() {
		sequence.add(new String());
	}
	
	public OutputSequence(String output) {
		this();
		sequence.add(output);
	}
	

	public void addOutput(String output) {
		sequence.add(output);
	}	
	
	public void addOutputSequence(OutputSequence outputSeq) {
		sequence.addAll(outputSeq.sequence);
	}

	public int getLength() {
		return sequence.size();
	}

	@Override
	public OutputSequence clone() {
		OutputSequence newos = new OutputSequence();
		newos.addOutputSequence(this);
		return newos;
	}

	public OutputSequence getIthSuffix(int start) {
		OutputSequence newis = new OutputSequence();
		for (int i = sequence.size() - start; i < sequence.size(); i++) {
			newis.addOutput(new String(sequence.get(i)));
		}
		return newis;
	}

	public boolean equals(OutputSequence o){
		if (!sequence.equals(o.sequence))
			return false;
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return equals((OutputSequence) obj);
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
		if (Options.REDUCE_DISPLAYED_TRACES > 0
				&& sequence.size() > Options.REDUCE_DISPLAYED_TRACES) {
			int i = 0;
			while (i < Options.REDUCE_DISPLAYED_TRACES / 2) {
				String output = sequence.get(i);
				if (output.length() > 0)
					s.append(output.toString());
				else
					s.append(Options.SYMBOL_OMEGA_LOW);
				s.append('.');
				i++;
			}
			s.append(" … ");
			s.append('.');
			while (i < Options.REDUCE_DISPLAYED_TRACES) {
				String output = sequence.get(
						sequence.size() - Options.REDUCE_DISPLAYED_TRACES + i);
				if (output.length() > 0)
					s.append(output.toString());
				else
					s.append(Options.SYMBOL_OMEGA_LOW);
				s.append('.');
				i++;
			}
			s.deleteCharAt(s.length() - 1);
		} else {
			for (String output : sequence) {
				if (output.length() > 0)
					s.append(output.toString());
				else
					s.append(Options.SYMBOL_OMEGA_LOW);
				s.append('.');
			}
		}
		if (s.length() > 0)
			s.deleteCharAt(s.length() - 1);
		return s.toString();
	}
	
	@Override
	public int hashCode(){
		return 23 + sequence.hashCode();
	}

	@Override
	public boolean checkCompatibilityWith(GenericSequence<String, String> in) {
		assert in instanceof InputSequence;
		InputSequence inSeq = (InputSequence) in;
		if (inSeq.getLength() != getLength())
			return false;
		return true;
	}

	@Override
	public OutputSequence toFixedOutput() {
		return this;
	}
}