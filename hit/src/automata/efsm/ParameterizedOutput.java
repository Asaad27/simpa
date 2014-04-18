package automata.efsm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import main.simpa.Options;

public class ParameterizedOutput implements Cloneable, Serializable {
	private static final long serialVersionUID = -8078721161724041483L;
	private String outputSymbol;
	private List<Parameter> parameters;

	public ParameterizedOutput() {
		this.outputSymbol = EFSM.OMEGA;
		this.parameters = new ArrayList<Parameter>();
	}

	public ParameterizedOutput(String output) {
		this.outputSymbol = output;
		this.parameters = new ArrayList<Parameter>();
	}

	@SuppressWarnings("unchecked")
	public ParameterizedOutput(String output, List<Parameter> paramaters) {
		this.outputSymbol = output;
		this.parameters = (ArrayList<Parameter>) ((ArrayList<Parameter>) paramaters)
				.clone();
	}

	public ParameterizedOutput(String output, Parameter parameter) {
		this.outputSymbol = output;
		this.parameters = new ArrayList<Parameter>();
		this.parameters.add((Parameter) parameter.clone());
	}

	@Override
	@SuppressWarnings("unchecked")
	public ParameterizedOutput clone() {
		return new ParameterizedOutput(outputSymbol,
				(ArrayList<Parameter>) ((ArrayList<Parameter>) parameters)
						.clone());
	}

	public String getOutputSymbol() {
		return outputSymbol;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public String getParameterValue(int paramIndex) {
		return parameters.get(paramIndex).value;
	}

	public boolean isOmegaSymbol() {
		return outputSymbol.equals(EFSM.OMEGA);
	}

	@Override
	public String toString() {
		if (isOmegaSymbol())
			return Options.SYMBOL_OMEGA_UP;
		else {
			StringBuffer s = new StringBuffer(outputSymbol + "(");
			if (parameters.size() > 0)
				s.append(parameters.get(0).value);
			for (int i = 1; i < parameters.size(); i++)
				s.append(", " + parameters.get(i).value);
			s.append(")");
			return s.toString();
		}
	}
}
