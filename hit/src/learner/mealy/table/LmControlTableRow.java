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
package learner.mealy.table;

import java.util.ArrayList;
import java.util.List;

import automata.mealy.InputSequence;

public class LmControlTableRow {
	private InputSequence is;
	private List<LmControlTableItem> cols;
	private List<InputSequence> E;
	private LmControlTableRow equivRow=null;//a row in S with same columns
	private LmControlTableRow candidateEquiv=null;//a row which might be equivalent

	public LmControlTableRow(InputSequence is, List<InputSequence> E) {
		this.is = is;
		cols = new ArrayList<LmControlTableItem>();
		for (int i = 0; i < E.size(); i++) {
			cols.add(new LmControlTableItem(null));
		}
		this.E = E;
	}

	public InputSequence getColSuffix(int i) {
		return E.get(i);
	}

	public int getColSuffixSize(int i) {
		return E.get(i).sequence.size();
	}

	public void setAtColumn(int iColumn, LmControlTableItem cti) {
		cols.set(iColumn, cti);
	}

	public LmControlTableItem getColumn(int index) {
		return cols.get(index);
	}

	public LmControlTableItem getColumnForThisE(InputSequence e) {
		for (int i = 0; i < E.size(); i++) {
			if (E.get(i).equals(e))
				return cols.get(i);
		}
		return null;
	}

	public int getColumnCount() {
		return cols.size();
	}

	public InputSequence getInputSequence(int iSeq) {
		return E.get(iSeq);
	}

	public String getLastPI() {
		return is.sequence.get(is.sequence.size() - 1);
	}

	public InputSequence getIS() {
		return is.clone();
	}

	public boolean isEpsilon() {
		return (is.sequence.size() == 0);
	}

	public boolean isEquivalentTo(LmControlTableRow aRow) {
		for (int i = 0; i < getColumnCount(); i++) {
			if (!getColumn(i).getOutputSymbol().equals(
					aRow.getColumn(i).getOutputSymbol()))
				return false;
		}
		return true;
	}

	public void addColumn(InputSequence col) {
		cols.add(new LmControlTableItem(null));
		candidateEquiv=equivRow;
		equivRow=null;
	}
	
	public boolean isClosed(LmControlTable t){
		if (equivRow!=null)return true;
		boolean rowIsClosed = false;
		if (candidateEquiv!=null){
			if (isEquivalentTo(candidateEquiv)){
				equivRow=candidateEquiv;
				return true;
			}else{
				candidateEquiv=null;
			}
		}
		for (int j = 0; j < t.S.size(); j++) {
			if (isEquivalentTo(t.S.get(j))) {
				rowIsClosed = true;
				equivRow=t.S.get(j);
				break;
			}
		}
		return rowIsClosed;
	}
}
