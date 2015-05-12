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
		this.parameters = new ArrayList<>();
	}

	public ParameterizedOutput(String output) {
		this();
		this.outputSymbol = output;
	}

	@SuppressWarnings("unchecked")
	public ParameterizedOutput(String output, List<Parameter> parameters) {
		this(output);
		this.parameters = (List<Parameter>) new ArrayList<>(parameters).clone();
	}

	public ParameterizedOutput(String output, Parameter parameter) {
		this(output);
		this.parameters.add(parameter.clone());
	}

	@Override
	@SuppressWarnings("unchecked")
	public ParameterizedOutput clone() {
		ArrayList<Parameter> parametersClone = (ArrayList<Parameter>) 
				new ArrayList<>(parameters).clone();
		return new ParameterizedOutput(outputSymbol, parametersClone);
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
			s.append(')');
			return s.toString();
		}
	}
}
