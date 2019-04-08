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
 *     Franck DE GOËR
 *     Maxime MEIGNAN
 ********************************************************************************/
package learner.efsm.table;

import java.util.ArrayList;
import java.util.List;

import automata.efsm.ParameterizedInputSequence;

public class LiDataTableRow {
	private ParameterizedInputSequence pis;
	private List<ArrayList<LiDataTableItem>> cols;

	public LiDataTableRow(ParameterizedInputSequence pis,
			List<String> inputSymbols) {
		this.pis = pis;
		cols = new ArrayList<ArrayList<LiDataTableItem>>();
		for (int i = 0; i < inputSymbols.size(); i++) {
			cols.add(new ArrayList<LiDataTableItem>());
		}
	}

	public ArrayList<LiDataTableItem> getColumn(int index) {
		return cols.get(index);
	}

	public void addColumn() {
		cols.add(new ArrayList<LiDataTableItem>());
	}
	
	public int getColumnCount() {
		return cols.size();
	}

	public List<ArrayList<LiDataTableItem>> getColumns() {
		return cols;
	}

	public ParameterizedInputSequence getPIS() {
		return pis.clone();
	}

}
