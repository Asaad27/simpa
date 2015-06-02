package automata.efsm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ParameterizedOutputSequence implements Serializable {

	private static final long serialVersionUID = -3673446835319484666L;
	public List<ParameterizedOutput> sequence;

	public ParameterizedOutputSequence() {
		sequence = new ArrayList<ParameterizedOutput>();
	}

	public void addOmegaOutput() {
		sequence.add(new ParameterizedOutput());
	}

	public void addParameterizedOuput(ParameterizedOutput po) {
		sequence.add(po);
	}

	public void addParameterizedOuput(String output, List<Parameter> parameters) {
		sequence.add(new ParameterizedOutput(output, parameters));
	}

	public int getLength() {
		return sequence.size();
	}

	public List<Parameter> getLastParameters() {
		List<Parameter> lp = new ArrayList<>();
		if (!sequence.get(sequence.size() - 1).getParameters().isEmpty())
			lp.addAll(sequence.get(sequence.size() - 1).getParameters());
		return lp;
	}

	public String getLastSymbol() {
		return sequence.get(sequence.size() - 1).getOutputSymbol();
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		for (ParameterizedOutput po : sequence) {
			s.append(po.toString());
		}
		return s.toString();
	}
}