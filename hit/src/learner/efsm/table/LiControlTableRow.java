package learner.efsm.table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import automata.efsm.EFSM;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedInputSequence;
import java.util.Collection;

public class LiControlTableRow {
	private ParameterizedInputSequence pis;
	private List<ArrayList<LiControlTableItem>> cols;
	private List<ParameterizedInputSequence> E;
	private int equiv;

	public LiControlTableRow(ParameterizedInputSequence pis,
			List<ParameterizedInputSequence> E) {
		this.pis = pis;
		this.E = E;
		this.equiv = -1;
		cols = new ArrayList<ArrayList<LiControlTableItem>>();
		for (int i = 0; i < E.size(); i++) {
			cols.add(new ArrayList<LiControlTableItem>());
		}
	}

	public void addColumn(ParameterizedInputSequence col) {
		cols.add(new ArrayList<LiControlTableItem>());
	}

	public void seemsTo(int rowInS) {
		equiv = rowInS;
	}

	public boolean seems() {
		return equiv != -1;
	}

	public void addAtColumn(int iColumn, LiControlTableItem cti) {
		String sCti = cti.toString();
		boolean exists = false;
		for (int i = 0; i < cols.get(iColumn).size(); i++) {
			if (sCti.equals(cols.get(iColumn).get(i).toString())) {
				exists = true;
				break;
			}
		}
		if (!exists)
			cols.get(iColumn).add(cti);
	}

	public ArrayList<LiControlTableItem> getColumn(int index) {
		return cols.get(index);
	}

	public int getColumnCount() {
		return cols.size();
	}

	public LiControlTableItem getItemInColumn(int iColumn, int iItem) {
		return cols.get(iColumn).get(iItem);
	}

	public ParameterizedInput getLastPI() {
		return pis.sequence.get(pis.sequence.size() - 1);
	}

	public ParameterizedInputSequence getColumPIS(int i) {
		return E.get(i);
	}

	public ParameterizedInputSequence getPIS() {
		return pis.clone();
	}

	public int getSizeOfColumn(int iColumn) {
		return cols.get(iColumn).size();
	}

	public boolean isEpsilon() {
		return ((pis.sequence.size() == 1) && (pis.getLastSymbol()
				.equals(EFSM.EPSILON)));
	}
	
	public boolean isEquivalentTo(LiControlTableRow aRow) {
		return isEquivalentToSet(aRow);
	}

	public boolean isEquivalentToSet(LiControlTableRow aRow) {
		HashSet<String> setISa = new HashSet<String>();
		HashSet<String> setISb = new HashSet<String>();
		for (int i = 0; i < getColumnCount(); i++) {
			setISa.clear();
			setISb.clear();
			for (int j = 0; j < getSizeOfColumn(i); j++){
				setISa.add(getItemInColumn(i, j).getOutputSymbol());
			}
			for (int j = 0; j < aRow.getSizeOfColumn(i); j++){
				setISb.add(aRow.getItemInColumn(i, j).getOutputSymbol());
			}
			if (!setISa.equals(setISb))
				return false;
		}
		return true;
	}
	
	public boolean isEquivalentToAll(LiControlTableRow aRow) {
		ArrayList<String> setISa = new ArrayList<String>();
		ArrayList<String> setISb = new ArrayList<String>();
		for (int i = 0; i < getColumnCount(); i++) {
			setISa.clear();
			setISb.clear();
			for (int j = 0; j < getSizeOfColumn(i); j++){
				setISa.add(getItemInColumn(i, j).getOutputSymbol());
			}
			for (int j = 0; j < aRow.getSizeOfColumn(i); j++){
				setISb.add(aRow.getItemInColumn(i, j).getOutputSymbol());
			}
			if (!setISa.equals(setISb))
				return false;
		}
		return true;
	}
	
	public boolean isEquivalentToNumber(LiControlTableRow aRow) {
		for (int i = 0; i < getColumnCount(); i++) {
			int a=0, b=0;
			for (int j = 0; j < getSizeOfColumn(i); j++){
				if (getItemInColumn(i, j).getOutputSymbol().equals("miss")) a++;
			}
			for (int j = 0; j < aRow.getSizeOfColumn(i); j++){
				if (aRow.getItemInColumn(i, j).getOutputSymbol().equals("miss")) b++;
			}
			if (a != b)
				return false;
		}
		return true;
	}
	
	public boolean isEquivalentToARowAmong(Collection<LiControlTableRow> aCollectionOfRows){
		for(LiControlTableRow aRow : aCollectionOfRows){
			if (isEquivalentTo(aRow)){
				return true;
			}
		}
		return false;
	}
	
}
