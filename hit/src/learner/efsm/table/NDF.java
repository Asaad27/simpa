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
 ********************************************************************************/
package learner.efsm.table;

import java.util.ArrayList;
import java.util.List;

import automata.efsm.Parameter;
import automata.efsm.ParameterizedInputSequence;

public class NDF implements Cloneable {
	private ParameterizedInputSequence pis;
	private String inputSymbol;
	public List<ArrayList<Parameter>> parameters;

	public NDF(ParameterizedInputSequence pis, String inputSymbol,
			List<ArrayList<Parameter>> parameters) {
		this.pis = pis;
		this.inputSymbol = inputSymbol;
		this.parameters = parameters;
	}

	@Override
	@SuppressWarnings("unchecked")
	public NDF clone() {
		return new NDF(
				pis.clone(),
				inputSymbol,
				(ArrayList<ArrayList<Parameter>>) ((ArrayList<ArrayList<Parameter>>) parameters)
						.clone());
	}

	public boolean equals(Object to) {
		if (this == to)
			return true;
		if (!(to instanceof NDF))
			return false;
		NDF comp = (NDF) to;
		return (pis.isSame(comp.pis) && (inputSymbol.equals(comp.inputSymbol)));
	}

	public int hashCode() {
		return 7 * pis.toString().hashCode() + 31 * inputSymbol.hashCode();
	}

	public String getInputSymbol() {
		return inputSymbol;
	}

	public ParameterizedInputSequence getPIS() {
		return pis.clone();
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer(pis.toString() + " " + inputSymbol
				+ "(");
		for (int i = 0; i < parameters.size(); i++) {
			if (i > 0)
				s.append('/');
			s.append('(');
			for (int j = 0; j < parameters.get(i).size(); j++) {
				if (j > 0)
					s.append(", ");
				if (parameters.get(i).get(j).isNDV())
					s.append("Ndv" + parameters.get(i).get(j).getNdv());
				else
					s.append(parameters.get(i).get(j).value);
			}
			s.append(')');
		}
		return s.append(')').toString();
	}
}
