package automata.efsm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import main.simpa.Options;

public class ParameterizedInput implements Cloneable, Serializable {

	private static final long serialVersionUID = 3729415826562015733L;
	private String inputSymbol;
	private List<Parameter> parameters;

	public ParameterizedInput() {
		this.inputSymbol = EFSM.EPSILON;
		this.parameters = new ArrayList<>();
	}

	public ParameterizedInput(String input) {
		this.inputSymbol = input;
		this.parameters = new ArrayList<>();
	}

	public ParameterizedInput(String input, List<Parameter> parameters) {
		this.inputSymbol = input;
		this.parameters = parameters;
	}

	@Override
	public ParameterizedInput clone() {
		ArrayList<Parameter> params = new ArrayList<Parameter>();
		for (Parameter p : parameters)
			params.add(p.clone());
		return new ParameterizedInput(inputSymbol, params);
	}

	public String getInputSymbol() {
		return inputSymbol;
	}

	public int getNdvIndexForVar(int iVar) {
		return parameters.get(iVar).getNdv();
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public String getParameterValue(int paramIndex) {
		return parameters.get(paramIndex).value;
	}

	public String getParamHash() {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < parameters.size(); i++) {
			if (i > 0)
				s.append('|');
			if (isNdv(i))
				s.append("Ndv" + parameters.get(i).getNdv());
			else
				s.append(parameters.get(i).value);
		}
		return s.toString();
	}

	public boolean isEpsilonSymbol() {
		return inputSymbol.equals(EFSM.EPSILON);
	}

	public boolean isNdv(int iVar) {
		return parameters.get(iVar).isNDV();
	}

	public void setNdvIndexForVar(int iVar, int iNdv) {
		parameters.get(iVar).setNdv(iNdv);
	}

	public void setParameterValue(int paramIndex, Parameter p) {
		parameters.get(paramIndex).value = p.value;
		parameters.get(paramIndex).type = p.type;
		parameters.get(paramIndex).setNdv(p.getNdv());
	}

	@Override
	public String toString() {
		if (isEpsilonSymbol())
			return Options.SYMBOL_EPSILON;
		else {
			StringBuffer s = new StringBuffer(inputSymbol + "(");
			for (int i = 0; i < parameters.size(); i++) {
				if (i > 0)
					s.append(", ");
				if (isNdv(i))
					s.append("Ndv" + parameters.get(i).getNdv());
				else
					s.append(parameters.get(i).value);
			}
			s.append(')');
			return s.toString();
		}
	}
}
