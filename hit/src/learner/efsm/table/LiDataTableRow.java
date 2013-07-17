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

	public ArrayList<LiDataTableItem> getColum(int index) {
		return cols.get(index);
	}

	public int getColumCount() {
		return cols.size();
	}

	public List<ArrayList<LiDataTableItem>> getColumns() {
		return cols;
	}

	public ParameterizedInputSequence getPIS() {
		return pis.clone();
	}
}
