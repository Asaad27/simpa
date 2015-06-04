package learner.efsm.table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedInputSequence;
import drivers.efsm.EFSMDriver;
import java.util.TreeMap;

public class LiDataTable {
	
	/* List of input symbols, ie.vocabulary */
	public List<String> inputSymbols;
	/* Default parameter values for input symbols */
	private Map<String, List<ArrayList<Parameter>>> defaultParamValues;
	/* Firsts lines of Data table (cf Angluin algo) */
	public List<LiDataTableRow> S;
	/* Final lines of Data table (cf Angluin algo) */
	public List<LiDataTableRow> R;
	/* List of parameters declared non deterministic */
	public List<NDV> ndvList;

	/* Constructor for data table from input symbols, 
	 * default values
	 */
	public LiDataTable(List<String> inputSymbols,
			Map<String, List<ArrayList<Parameter>>> defaultParamValues) {
		this.inputSymbols = inputSymbols;
		this.defaultParamValues = defaultParamValues;
		this.ndvList = new ArrayList<NDV>();
		initialize();
	}

	/* TODO: considère qu'il n'y a qu'un seul input dans la colonne => à changer */
	void addAtCorrespondingPlace(LiDataTableItem dti, ParameterizedInputSequence pis) {
		
		String lastSymbol = pis.getLastSymbol();
		int columnIndex = inputSymbols.indexOf(lastSymbol);
		
		
		ParameterizedInputSequence prefix = pis.clone();
		prefix.removeLastParameterizedInput();
		if (prefix.sequence.isEmpty())
			prefix.addEmptyParameterizedInput();
		
		
		LiDataTableRow dtr = getRow(prefix);
		
		String dtiStr = dti.toString();
		for (LiDataTableItem existingDti : dtr.getColumn(columnIndex)) {
			if (existingDti.toString().equals(dtiStr)) {
				return;
			}
		}
		dtr.getColumn(columnIndex).add(dti);	
	}

	
	public void addRowInR(LiDataTableRow row) {
		R.add(row);
	}

	
	public void addRowInS(LiDataTableRow row) {
		S.add(row);
	}


	/**
	 * Look for a NDV in any row of the table
	 * 
	 * @TODO 	better search ; NDV should be associated w/ input symbols (?) 
	 * 
	 * @return 	a NDV object corresponding to the first non-deterministic parameter
	 *			discovered (ignoring already known ones),  null if no new NDV found
	 */
	public NDV findNDV() {
		final List<LiDataTableRow> allRows = getAllRows();
		/* Iteration on table rows */
		for (LiDataTableRow dtr : allRows) {
			/* Iteration on table columns */
			for (int j = 0; j < dtr.getColumnCount(); j++) {
				/* if the cell is not empty */
				if (!dtr.getColumn(j).isEmpty()) {
					/* Look for NDV index in cell (i, j) of table */
					NDV ndv = searchNDVinCell(dtr, j);
					/* If a NDV was found */
					if (ndv != null) {
						ndvList.add(ndv);
						return ndv.clone();
					}
				}
			}
		}
		return null;
	}

	/**
	 * Getter for all rows in the data table
	 * 
	 * @return	A lit containing S entries AND R entries
	 */
	public List<LiDataTableRow> getAllRows() {
		List<LiDataTableRow> allRows = new ArrayList<LiDataTableRow>();
		allRows.addAll(S);
		allRows.addAll(R);
		return allRows;
	}

	public int getCountOfNdv() {
		return ndvList.size();
	}

	public int getCountOfRowsInR() {
		return R.size();
	}

	public int getCountOfRowsInS() {
		return S.size();
	}

	public NDV getNdv(int iNdv) {
		return ndvList.get(iNdv);
	}

	public LiDataTableRow getRowInR(int iRow) {
		return R.get(iRow);
	}

	public LiDataTableRow getRowInS(int iRow) {
		return S.get(iRow);
	}

	/**
	 * @see LiControlTable.#getRow(automata.efsm.ParameterizedInputSequence)  
	 * @param prefix
	 * @return 
	 */
	public LiDataTableRow getRow(ParameterizedInputSequence prefix){
			final List<LiDataTableRow> allRows = getAllRows();
		for (LiDataTableRow ctr : allRows) {
			if (ctr.getPIS().equals(prefix))
				return ctr;
		}
		throw new AssertionError("No corresponding row was found.");
	}
	
	private void initialize() {
		S = new ArrayList<LiDataTableRow>();
		R = new ArrayList<LiDataTableRow>();
		ParameterizedInputSequence empty = new ParameterizedInputSequence();
		empty.addEmptyParameterizedInput();
		S.add(new LiDataTableRow(empty, inputSymbols));
		ParameterizedInputSequence pis = null;
		for (int i = 0; i < inputSymbols.size(); i++) {
			for (int l = 0; l < defaultParamValues.get(inputSymbols.get(i))
					.size(); l++) {
				pis = new ParameterizedInputSequence();
				pis.addParameterizedInput(inputSymbols.get(i),
						defaultParamValues.get(inputSymbols.get(i)).get(l));
				R.add(new LiDataTableRow(pis, inputSymbols));
			}
		}
	}

	
	/**
	 *  Find a new NDV in a given box of data table 
	 * 
	 * 
	 * @param dtr			The DataTableRow in where to look for a NDV
	 * @param indexCell		The index of the cell in where to look for a NDV
	 * @return				A NDV object if found, null otherwise
	 */
	public NDV searchNDVinCell(LiDataTableRow dtr, int indexCell) {
		List<List<Parameter>> oldParams = new ArrayList<>();
		ArrayList<LiDataTableItem> cellContent = dtr.getColumn(indexCell);
		for (int i = 0; i < cellContent.size(); i++) {
			LiDataTableItem itemI = cellContent.get(i);
			for (int j = i + 1; j < cellContent.size(); j++) {
				LiDataTableItem itemJ = cellContent.get(j);
				/* If the inputs have the same values */
				if ((itemI.getInputParametersValues().equals(
						itemJ.getInputParametersValues()))
						/* and if the initial state was the same */
						&& (itemI.getAutomataStateValues().equals(
								itemJ.getAutomataStateValues()))) {
					/* Iteration on output values */
					for (int k = 0; k < itemI.getOutputParameters().size(); k++) {
						/* If k in bounds */
						if (k < itemJ.getOutputParameters().size()
								/* and if the values of this output for i and j
								 * are different
								 */
								&& !(itemI.getOutputParameters().get(k).value
										.equals(itemJ.getOutputParameters().get(k).value))) {
							boolean exists = false;
							
							/* Iteration on parameters to see if already exists in
							 * the list */
							for (List<Parameter> old : oldParams) {
								if (old.equals(itemI.getInputParameters())) {
									exists = true;
									break;
								}
							}
							
							/* If not, it is a new NDV that we found */
							if (!exists) {
									ParameterizedInputSequence pis = dtr.getPIS();
									pis.addParameterizedInput(inputSymbols.get(indexCell), itemI.getInputParameters());
									EFSMDriver.Types type = itemI.getOutputParameters().get(k).type;
									NDV newndv = new NDV(pis, type, k, ndvList.size(), itemI.getOutputSymbol());
								if (!ndvList.contains(newndv)) {
									return newndv;
								} else {
									oldParams.add(itemI.getInputParameters());
								}
							}
						}
					}
				}
			}
		}
		/* No NDV found, return -1 */
		return null;
	}
		
	public LiDataTableRow removeRowInR(int iRow) {
		return R.remove(iRow);
	}

	public List<Parameter> getOutputParametersFor(ParameterizedInputSequence pis, TreeMap<String, List<Parameter>> automataState) {
		ParameterizedInputSequence prefix = pis.clone();
		ParameterizedInput lastPI = prefix.removeLastParameterizedInput();
		if (prefix.sequence.isEmpty()) {
			prefix.addEmptyParameterizedInput();
		}
		String lastSymbol = lastPI.getInputSymbol();

		/* Computes the column number of the cell we have to look into*/
		int j = inputSymbols.indexOf(lastSymbol);
		if (j == -1) {
			throw new AssertionError("This should be impossible, since E contains at least all the alphabet");
		}

		/* Look for the right row in the table */
		List<LiDataTableRow> allRows = getAllRows();
		for (LiDataTableRow dtr : allRows) {
			if (dtr.getPIS().equals(prefix)) {
				ArrayList<LiDataTableItem> cellContent = dtr.getColumn(j);
				for (LiDataTableItem dti : cellContent) {
					boolean paramsHaveSameValues = true;
					for (int i = 0; i < dti.getInputParameters().size(); i++) {
						if (!dti.getInputParameters().get(i).value.equals(lastPI.getParameterValue(i))) {
							paramsHaveSameValues = false;
							break;
						}
						for (Map.Entry<String, List<Parameter>> entrySet : automataState.entrySet()) {
							String key = entrySet.getKey();
							if (!dti.getAutomataState().get(key).equals(automataState.get(key))) {
								paramsHaveSameValues = false;
								break;
							}
						}

					}
					if (paramsHaveSameValues) {
						return dti.getOutputParameters();
					}
				}
			}
		}
		return null;
	}

	public Set<Parameter> getFixedOutputParametersFor(
			ParameterizedInputSequence pis) {
		Set<Parameter> params = new HashSet<Parameter>();
		Set<String> values = new HashSet<String>();
		pis = pis.clone();
		ParameterizedInput pi = pis.removeLastParameterizedInput();
		if (pis.sequence.isEmpty())
			pis.addEmptyParameterizedInput();
		LiDataTableRow dtr = null;
		for (LiDataTableRow d : S) {
			if (d.getPIS().isSame(pis)) {
				dtr = d;
				break;
			}
		}
		pis.addParameterizedInput(pi);
		pis.removeEmptyInput();
		for (LiDataTableItem dti : dtr.getColumn(inputSymbols.indexOf(pi
				.getInputSymbol()))) {
			boolean isEqual = true;
			for (int i = 0; i < dti.getInputParameters().size(); i++) {
				if (!dti.getInputParameters().get(i)
						.equals(pi.getParameters().get(i))) {
					isEqual = false;
					break;
				}
			}
			if (isEqual) {
				NDV ndv = null;
				for (NDV n : ndvList) {
					if (n.getPIS().isSame(pis))
						ndv = n;
				}
				for (int i = 0; i < dti.getOutputParameters().size(); i++) {
					if (ndv == null || ndv.paramIndex != i) {
						if (!values
								.contains(dti.getOutputParameters().get(i).value)) {
							params.add(dti.getOutputParameters().get(i));
							values.add(dti.getOutputParameters().get(i).value);
						}
					}
				}
			}
		}
		return params;
	}

	
	public void addColumnInAllRows() {
		for (LiDataTableRow row : R) {
			row.addColumn();
		}
		for (LiDataTableRow row : S) {
			row.addColumn();
		}
	}
	
}