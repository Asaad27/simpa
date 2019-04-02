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
package learner.efsm.table;

import java.io.Serializable;
import java.util.List;

import main.simpa.Options;
import automata.efsm.EFSM;
import automata.efsm.Parameter;

public class LiControlTableItem implements Serializable {

	private static final long serialVersionUID = -8484677909227738772L;
	private List<Parameter> inputParameters;
	private String outputSymbol;

	public LiControlTableItem(List<Parameter> list, String outputSymbol) {
		this.outputSymbol = outputSymbol;
		this.inputParameters = list;
	}

	public String getOutputSymbol() {
		return outputSymbol;
	}

	public Parameter getParameter(int index) {
		return inputParameters.get(index);
	}

	public Integer getParameterNDVIndex(int iParameter) {
		return inputParameters.get(iParameter).getNdv();
	}

	public List<Parameter> getParameters() {
		return inputParameters;
	}

	public boolean isOmegaSymbol() {
		return outputSymbol.equals(EFSM.OMEGA);
	}

	public void setNdv(int indexParam, int indexNdv) {
		inputParameters.get(indexParam).setNdv(indexNdv);
	}

	@Override
	public String toString() {
		StringBuffer res = new StringBuffer("((");
		for (int i = 0; i < inputParameters.size(); i++) {
			if (i > 0)
				res.append(", ");
			if (!inputParameters.get(i).isNDV())
				res.append(inputParameters.get(i).value);
			else
				res.append("Ndv" + inputParameters.get(i).getNdv());
		}
		return res
				.append("), "
						+ (outputSymbol.equals(EFSM.OMEGA) ? Options.SYMBOL_OMEGA_UP
								: outputSymbol) + ")").toString();
	}
}
