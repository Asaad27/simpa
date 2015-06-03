package automata.efsm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import main.simpa.Options;

public class ParameterizedInputSequence implements Cloneable, Serializable {

	private static final long serialVersionUID = 1075926694885495311L;
	public List<ParameterizedInput> sequence;

	public ParameterizedInputSequence() {
		sequence = new ArrayList<>();
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

	public boolean hasSameSymbolSequence(ParameterizedInputSequence i){
		return this.getSymbolSequence().equals(i.getSymbolSequence());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ParameterizedInputSequence)) {
			return false;
		}
		ParameterizedInputSequence other = (ParameterizedInputSequence) o;
		
		return this.sequence.equals(other.sequence);
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 29 * hash + Objects.hashCode(this.sequence);
		return hash;
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
		if (pis.sequence.isEmpty() || sequence.isEmpty()) {
			return false;
		}
		if (sequence.size() < pis.sequence.size()) {
			return false;
		}
		for (int i = 0; i < pis.sequence.size(); i++) {
			if (!pis.sequence.get(i).equals(sequence.get(i))) {
				return false;
			}
		}
		return true;
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

	/**
	 * @deprecated please use equals instead, String operations are really slow
	 * @return 
	 */
	public String getHash() {
		String hash = "";
		for (ParameterizedInput pi : sequence) {
			hash += pi.getInputSymbol() + pi.getParamHash();
		}
		return hash;
	}

	public String getSymbol(int k) {
		return sequence.get(k).getInputSymbol();
	}
	
	/**
	 * Return the list of symbols contained by the ParameterizedInputSequence.
	 * Warning, does not make copies, for read purpose only.
	 * @return 
	 */
	public List<String> getSymbolSequence(){
		List list = new ArrayList();
		for (ParameterizedInput pi : sequence) {
			list.add(pi.getInputSymbol());
		}
		return list;
	}

	public List<Parameter> getParameter(int k) {
		return sequence.get(k).getParameters();
	}
}
