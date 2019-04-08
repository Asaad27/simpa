/********************************************************************************
 * Copyright (c) 2011,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 *     Maxime MEIGNAN
 *     Nicolas BREMOND
 ********************************************************************************/
package automata.efsm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
		ArrayList<Parameter> params = new ArrayList<>();
		for (Parameter p : parameters)
			params.add(p.clone());
		return new ParameterizedInput(inputSymbol, params);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 47 * hash + Objects.hashCode(this.inputSymbol);
		hash = 47 * hash + Objects.hashCode(this.parameters);
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
		final ParameterizedInput other = (ParameterizedInput) obj;
		if (!Objects.equals(this.inputSymbol, other.inputSymbol)) {
			return false;
		}
		if (!Objects.equals(this.parameters, other.parameters)) {
			return false;
		}
		return true;
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
