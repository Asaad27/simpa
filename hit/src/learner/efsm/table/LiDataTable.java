package learner.efsm.table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedInputSequence;
import java.util.LinkedList;
import detection.XSSDetector;
import main.simpa.Options;

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

	/* XSS detector (may be moved to another place) */
	private XSSDetector xssDetector = null;
	
	/**
	 * Constructor for data table from input symbols, default values
	 */
	public LiDataTable(List<String> inputSymbols,
			Map<String, List<ArrayList<Parameter>>> defaultParamValues) {
		this.inputSymbols = inputSymbols;
		this.defaultParamValues = defaultParamValues;
		this.ndvList = new ArrayList<NDV>();
		if(Options.XSS_DETECTION){
			ArrayList<String> ignoredValues = new ArrayList<>();
			ignoredValues.add(Parameter.PARAMETER_INIT_VALUE);
			ignoredValues.add(Parameter.PARAMETER_NO_VALUE);
			ignoredValues.add("");
			this.xssDetector = new XSSDetector(ignoredValues);
		}
		initialize();
	}

	/* TODO: considère qu'il n'y a qu'un seul input dans la colonne => à changer */
	public void addAtCorrespondingPlace(LiDataTableItem dti,
			ParameterizedInputSequence currentPis) {
		//May be moved to another place
		if(Options.XSS_DETECTION){
			xssDetector.recordItem(dti, pis);
		}
		String lastSymbol = currentPis.getLastSymbol();
		ParameterizedInputSequence prefix = currentPis.clone();
		prefix.removeLastParameterizedInput();
		if (prefix.sequence.isEmpty())
			prefix.addEmptyParameterizedInput();
		final List<LiDataTableRow> allRows = getAllRows();
		for (LiDataTableRow dtr : allRows) {
			if (dtr.getPIS().equals(prefix)) {
				int index = inputSymbols.indexOf(lastSymbol);
				String dtiStr = dti.toString();
				boolean exist = false;
				for (LiDataTableItem existingDti : dtr.getColumn(index)) {
					if (existingDti.toString().equals(dtiStr)) {
						exist = true;
						break;
					}
				}
				if (!exist) {
					dtr.getColumn(index).add(dti);
				}
				break;
			}
		}
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
					LinkedList<Parameter> params = new LinkedList<>();
					/* Look for NDV index in box (i, j) of table */
					int ndvIndex = newNDVForItem(dtr.getColumn(j), params,
							ndvList.size());
					/* If a NDV was found */
					if (ndvIndex != -1) {
						/* Get input sequence */
						ParameterizedInputSequence pi = dtr.getPIS();
						/* Pop output parameter that is NDV */
						Parameter last = params.removeLast();
						/* Add column parameter in input sequence */
						pi.addParameterizedInput(inputSymbols.get(j), params);
						/* Creation of a new NDV */
						NDV aNdv = new NDV(pi.removeEmptyInput(), last.type,
								ndvIndex, ndvList.size());
						if (!ndvList.contains(aNdv)) {
							ndvList.add(aNdv);
							return aNdv.clone();
						}
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
	 * @param item		Content of a cell of the data table (consist of a list of (PIS, POS))
	 * @param params	List of parameters where to write the sequence where a NDV was found
	 * @param index		Index of the last NDV already found
	 * 
	 * @return			The index of the NDV input parameter in params, -1 if not found
	 */
	public int newNDVForItem(List<LiDataTableItem> item,
			List<Parameter> params, int index) {
		List<List<Parameter>> oldParams = new ArrayList<>();
		int currentIndex = 0;
		for (int i = 0; i < item.size(); i++) {
			for (int j = i + 1; j < item.size(); j++) {
				/* If the inputs have the same values */
				if ((item.get(i).getInputParametersValues().equals(item.get(j)
						.getInputParametersValues()))
						/* and if the initial state was the same */
						&& (item.get(i).getAutomataStateValues().equals(item
								.get(j).getAutomataStateValues()))) {
					/* Iteration on output values */
					for (int k = 0; k < item.get(i).getOutputParameters()
							.size(); k++) {
						/* If k in bounds */
						if (k < item.get(j).getOutputParameters().size()
								/* and if the values of this output for i and j
								 * are different
								 */
								&& !(item.get(i).getOutputParameters().get(k).value
										.equals(item.get(j)
												.getOutputParameters().get(k).value))) {
							boolean exists = false;
							/* Iteration on parameters to see if already exists in
							 * the list */
							for (List<Parameter> old : oldParams) {
								if (old.equals(item.get(i).getInputParameters())) {
									exists = true;
									break;
								}
							}
							/* If not, it is a new NDV that we found */
							if (!exists) {
								/* Index is the number of previously found NDV 
								 * So if index == current_index, we already found
								 * this parameter before */
								if (currentIndex == index) {
									/* add all input parameters */
									params.addAll(item.get(i)
											.getInputParameters());
									/* Add output parameter that is NDV */
									params.add(item.get(i)
											.getOutputParameters().get(k));
									return k;
								} else {
									oldParams.add(item.get(i).getInputParameters());
									currentIndex++;
								}
							}
						}
					}
				}
			}
		}
		/* No NDV found, return -1 */
		return -1;
	}

	public LiDataTableRow removeRowInR(int iRow) {
		return R.remove(iRow);
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