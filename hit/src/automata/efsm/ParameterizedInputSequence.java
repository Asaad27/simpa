package automata.efsm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import main.simpa.Options;

public class ParameterizedInputSequence implements Cloneable, Serializable {

	private static final long serialVersionUID = 1075926694885495311L;
	public List<ParameterizedInput> sequence;

	public ParameterizedInputSequence() {
		sequence = new ArrayList<ParameterizedInput>();
	}

	public void addEmptyParameterizedInput() {
		sequence.add(new ParameterizedInput());
	}

	public void addParameterizedInput(ParameterizedInput pi) {
		sequence.add(pi);
	}

	public void addParameterizedInput(String input, List<Parameter> parameters) {
		sequence.add(new ParameterizedInput(input, parameters));
	}

	public int getLength() {
		return sequence.size();
	}

	@Override
	public ParameterizedInputSequence clone() {
		ParameterizedInputSequence newpis = new ParameterizedInputSequence();
		for (ParameterizedInput pi : sequence) {
			newpis.addParameterizedInput(pi.clone());
		}
		return newpis;
	}

	public boolean equals(ParameterizedInputSequence o) {
		if (sequence.size() != o.sequence.size())
			return false;
		else {
			for (int i = 0; i < sequence.size(); i++) {
				if (!sequence.get(i).getInputSymbol()
						.equals(o.sequence.get(i).getInputSymbol()))
					return false;
			}
		}
		return true;
	}

	public List<Parameter> getLastParameters() {
		return sequence.get(sequence.size() - 1).getParameters();
	}

	public String getLastSymbol() {
		return sequence.get(sequence.size() - 1).getInputSymbol();
	}

	public boolean isSame(ParameterizedInputSequence pis) {
		return pis.toString().equals(toString());
	}

	public ParameterizedInputSequence removeEmptyInput() {
		if (sequence.get(0).isEpsilonSymbol())
			sequence.remove(0);
		return this;
	}

	public ParameterizedInput removeLastParameterizedInput() {
		ParameterizedInput pi = sequence.remove(sequence.size() - 1);
		return pi;
	}

	
	/**
	 * Check whether this starts with pis
	 * 
	 * @param pis 	Sequence of inputs to test
	 * @return		True if this starts with pis, false otherwise (e.g. if pis is longer than
	 * 				this, return false)
	 */
	public boolean startsWith(ParameterizedInputSequence pis) {
		if (pis.sequence.isEmpty() || sequence.isEmpty())
			return false;
		if (pis.sequence.size() <= sequence.size()) {
			for (int i = 0; i < pis.sequence.size(); i++) {
				if ((!pis.sequence.get(i).getInputSymbol()
						.equals(sequence.get(i).getInputSymbol()))
						|| (!pis.sequence.get(i).getParamHash()
								.equals(sequence.get(i).getParamHash())))
					return false;
			}
			return true;
		} else
			return false;
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		for (ParameterizedInput pi : sequence) {
			if (!pi.isEpsilonSymbol())
				s.append(pi.toString());
			else
				s.append(Options.SYMBOL_EPSILON);
		}
		return s.toString();
	}

	public ParameterizedInputSequence getIthSuffix(int start) {
		ParameterizedInputSequence newis = new ParameterizedInputSequence();
		for (int i = sequence.size() - start; i < sequence.size(); i++) {
			newis.addParameterizedInput(sequence.get(i));
		}
		return newis;
	}

	public String getHash() {
		String hash = "";
		for (int i = 0; i < sequence.size(); i++) {
			hash += sequence.get(i).getInputSymbol()
					+ sequence.get(i).getParamHash();
		}
		return hash;
	}

	public String getSymbol(int k) {
		return sequence.get(k).getInputSymbol();
	}

	public List<Parameter> getParameter(int k) {
		return sequence.get(k).getParameters();
	}
}
