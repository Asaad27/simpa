package learner.mealy.table;

import java.util.ArrayList;
import java.util.List;

import automata.mealy.InputSequence;

public class LmControlTable {
	public List<String> inputSymbols;
	public List<LmControlTableRow> S;
	public List<LmControlTableRow> R;
	public List<InputSequence> E;

	public LmControlTable(List<String> inputSymbols) {
		this.inputSymbols = inputSymbols;
		initialize();
	}

	public int getColsCount() {
		return E.size();
	}

	public InputSequence getColSuffix(int i) {
		return E.get(i);
	}

	public void addColumnInE(InputSequence col) {
		boolean exists = false;
		for (InputSequence is : E) {
			if (is.equals(col))
				exists = true;
		}
		if (!exists) {
			E.add(col);
			for (LmControlTableRow row : S)
				row.addColumn(col);
			for (LmControlTableRow row : R)
				row.addColumn(col);
		}
	}

	public void addRowInR(LmControlTableRow row) {
		R.add(row);
	}

	public void addRowInS(LmControlTableRow row) {
		S.add(row);
	}

	public List<LmControlTableRow> getAllRows() {
		List<LmControlTableRow> allRows = new ArrayList<LmControlTableRow>();
		allRows.addAll(S);
		allRows.addAll(R);
		return allRows;
	}

	public int getCountOfRowsInR() {
		return R.size();
	}

	public int getCountOfRowsInS() {
		return S.size();
	}

	public List<Integer> getNonClosedRows() {
		List<Integer> ncr = new ArrayList<Integer>();
		for (int i = 0; i < R.size(); i++) {
			boolean rowIsClosed = false;
			for (int j = 0; j < S.size(); j++) {
				if (R.get(i).isEquivalentTo(S.get(j))) {
					rowIsClosed = true;
					break;
				}
			}
			for (int j : ncr) {
				if (R.get(i).isEquivalentTo(R.get(j))) {
					rowIsClosed = true;
					break;
				}
			}
			if (!rowIsClosed)
				ncr.add(i);
		}
		return ncr;
	}

	public String getInputSymbol(int iSymbol) {
		return inputSymbols.get(iSymbol);
	}

	public List<String> getInputSymbols() {
		return inputSymbols;
	}

	public int getInputSymbolsCount() {
		return inputSymbols.size();
	}

	public LmControlTableRow getRowInR(int iRow) {
		return R.get(iRow);
	}

	public LmControlTableRow getRowInS(int iRow) {
		return S.get(iRow);
	}

	public List<LmControlTableRow> getRowStartsWith(InputSequence is) {
		final List<LmControlTableRow> allRows = getAllRows();
		List<LmControlTableRow> ctrs = new ArrayList<LmControlTableRow>();
		for (LmControlTableRow ctr : allRows) {
			if (ctr.getIS().startsWith(is))
				ctrs.add(ctr);
		}
		return ctrs;
	}

	public int getFromState(LmControlTableRow ctr) {
		final List<LmControlTableRow> allRows = getAllRows();
		InputSequence is = ctr.getIS();
		is.removeLastInput();
		for (LmControlTableRow c : allRows) {
			if (is.isSame(c.getIS()))
				return getToState(c);
		}
		return 0;
	}

	public int getToState(LmControlTableRow ctr) {
		for (int i = 0; i < S.size(); i++) {
			if (ctr.isEquivalentTo(S.get(i)))
				return i;
		}
		return 0;
	}

	private void initialize() {
		R = new ArrayList<LmControlTableRow>();
		E = new ArrayList<InputSequence>();
		for (int i = 0; i < inputSymbols.size(); i++) {
			InputSequence seq = new InputSequence();
			seq.addInput(inputSymbols.get(i));
			E.add(seq);
		}
		for (int i = 0; i < inputSymbols.size(); i++) {
			R.add(new LmControlTableRow(E.get(i), E));
		}
		S = new ArrayList<LmControlTableRow>();
		S.add(new LmControlTableRow(new InputSequence(), E));
	}

	public LmControlTableRow removeRowInR(int iRow) {
		return R.remove(iRow);
	}
}
