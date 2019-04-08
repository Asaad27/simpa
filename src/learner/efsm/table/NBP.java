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

import java.util.List;

import automata.efsm.Parameter;

public class NBP {
	public List<Parameter> params;
	public int iInputSymbol;
	public String inputSymbol;

	public NBP(List<Parameter> params, int IInputSymbol, String inputSymbol) {
		this.inputSymbol = inputSymbol;
		this.iInputSymbol = IInputSymbol;
		this.params = params;
	}

	public boolean hasSameParameters(LiControlTableItem cti){
		return cti.getParameters().equals(this.params);
	}
	
	public void setNdvIndex(int iVar, int iNdv) {
		this.params.get(iVar).setNdv(iNdv);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("(");
		for (int i = 0; i < params.size(); i++) {
			if (i > 0)
				s.append(", ");
			s.append(params.get(i).value);
			if (params.get(i).isNDV())
				s.append("(Ndv").append(params.get(i).getNdv()).append(")");
		}
		return s.append(") for input symbol ").append(inputSymbol).toString();
	}
}
