package learner.efsm.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedInputSequence;
import main.simpa.SIMPA;

public class LiControlTable {
	public List<String> inputSymbols;
	private Map<String, List<ArrayList<Parameter>>> defaultParamValues;
	public List<LiControlTableRow> S;
	public List<LiControlTableRow> R;
	public List<ParameterizedInputSequence> E;
	private List<NDF> NdfList;

	public LiControlTable(List<String> inputSymbols,
			Map<String, List<ArrayList<Parameter>>> defaultParamValues2) {
		this.inputSymbols = inputSymbols;
		this.defaultParamValues = defaultParamValues2;
		NdfList = new ArrayList<NDF>();
		initialize();
	}

	public int getColsCount() {
		return E.size();
	}

	public ParameterizedInputSequence getColSuffix(int i) {
		return E.get(i);
	}

	public void addColumnInE(ParameterizedInputSequence col) {
		boolean exists = false;
		for (ParameterizedInputSequence is : E) {
			if (is.hasSameSymbolSequence(col))
				exists = true;
		}
		if (!exists) {
			E.add(col);
			for (LiControlTableRow row : S)
				row.addColumn();
			for (LiControlTableRow row : R)
				row.addColumn();
		}
	}

	private void checkForDuplicatedRow(LiControlTableRow row){
		for (LiControlTableRow rowCurrent : getAllRows()) {
			if (row.getPIS().equals(rowCurrent.getPIS())){
				throw new AssertionError("Duplicate row in R");
			}
		}	
	}
	
	public void addRowInR(LiControlTableRow row) {
		if(SIMPA.DEFENSIVE_CODE) checkForDuplicatedRow(row);
		R.add(row);
	}

	public void addRowInS(LiControlTableRow row) {
		if(SIMPA.DEFENSIVE_CODE) checkForDuplicatedRow(row);
		S.add(row);
	}

	public List<LiControlTableRow> getAllRows() {
		List<LiControlTableRow> allRows = new ArrayList<LiControlTableRow>();
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

	/**
	 * Returns a disputed row if any.
	 * Disputed row :
	 * A row s \in S is disputed if there exists an x in E such that more than one output symbol
	 * are included in the cell indexed by s & x   -- i.e. C(s,x).
	 * 
	 * Resolved row : A disputed row s is resolved if for each output symbol y involved in C(s,x), there 
	 * is a row in R __union S__(this part has been forgotten in Karim's thesis)
	 * indexed by s.x(x.paramValues), and (x.paramValues, y) is in C(s,x).
	 * 
	 * @return A NDF object if the disputed row has not been discovered before
	 */
	public NDF getDisputedItem() {
		//Iterates over rows of S
		for (LiControlTableRow ctr : S) {
			
			//Iterates over cell of the row
			for (int i = 0; i < ctr.getColumnCount(); i++) {
				int sizeOfColumnI = ctr.getSizeOfColumn(i);
				
				//Iterates over every couple of item of the cell
				for (int j = 0; j < sizeOfColumnI; j++) {
					String symbolJ = ctr.getItemInColumn(i, j).getOutputSymbol();
					for (int k = j + 1; k < sizeOfColumnI; k++) {
						String symbolK = ctr.getItemInColumn(i, k).getOutputSymbol();
						
						//if they have different output symbols
						if (!symbolJ.equals(symbolK)) {
							
							//We store the different parameters combinations found in the cell 
							List<ArrayList<Parameter>> parameters = new ArrayList<>();
							for (int l = 0; l < sizeOfColumnI; l++) {
								parameters.add((ArrayList<Parameter>) ctr
										.getItemInColumn(i, l).getParameters());
							}
							//and create a NDF object with it
							NDF ndf = new NDF(ctr.getPIS().removeEmptyInput(),
									inputSymbols.get(i), parameters);
				
							//and return it, if it has not been discovered before
							if (!NdfList.contains(ndf)) {
								NdfList.add(ndf);
								return ndf.clone();
							}
						}
					}
				}
			}
		}
		return null;
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
			if (!rowIsClosed)
				ncr.add(i);
		}
		return ncr;
	}
	
	/**
	 * Checks if the the control table is closed.
	 * @return the index of the first non-closed row if any; returns -1 otherwise.
	 */
	public int getFirstNonClosedRow(){
		for (int i = 0; i < R.size(); i++) {
			if (!isClosedRow(i))
				return i;
		}
		return -1;
	}
	
	public boolean isClosedRow(int rowIndexInR){
		return R.get(rowIndexInR).isEquivalentToARowAmong(S);
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

	public NBP getNotBalancedParameter() {
		final List<LiControlTableRow> allRows = getAllRows();
		for (int i = 0; i < inputSymbols.size(); i++) {
			for (int z = 0; z < allRows.size(); z++) {
				for (int y = 0; y < allRows.get(z).getSizeOfColumn(i); y++) {
					List<Parameter> parametersA = allRows.get(z)
							.getItemInColumn(i, y).getParameters();
					for (int x = 0; x < allRows.size(); x++) {
						if (x == z)
							continue;
						boolean found = false;
						for (int w = 0; w < allRows.get(x).getSizeOfColumn(i); w++) {
							List<Parameter> parametersB = allRows.get(x)
									.getItemInColumn(i, w).getParameters();
							if (parametersA.equals(parametersB)) {
								found = true;
								break;
							}
						}
						if (!found) {
							NBP nbp = new NBP(parametersA, i, getInputSymbol(i));
							for (int k = 0; k < allRows.get(z).getSizeOfColumn(
									i); k++) {
								boolean foundReal = true;
								for (int l = 0; l < allRows.get(z)
										.getItemInColumn(i, k).getParameters()
										.size(); l++) {
									if (!allRows.get(z).getItemInColumn(i, k)
											.getParameter(l)
											.equals(nbp.params.get(l))) {
										foundReal = false;
										break;
									}
								}
								if (foundReal) {
									for (int l = 0; l < allRows.get(z)
											.getItemInColumn(i, k)
											.getParameters().size(); l++) {
										nbp.setNdvIndex(l, allRows.get(z)
												.getItemInColumn(i, k)
												.getParameterNDVIndex(l));
									}
								}
							}
							return nbp;
						}
					}
				}
			}
		}
		return null;
	}

	public LiControlTableRow getRowInR(int iRow) {
		return R.get(iRow);
	}

	public LiControlTableRow getRowInS(int iRow) {
		return S.get(iRow);
	}

	
	/**
	 * Look for rows in control table that start with the pis
	 * input sequence
	 * 
	 * @param 	prefix prefix to look for
	 * @return	A list of rows of Data table that start
	 * 			with pis
	 */
	public List<LiControlTableRow> getRowsStartsWith(
			ParameterizedInputSequence prefix) {
		final List<LiControlTableRow> allRows = getAllRows();
		List<LiControlTableRow> ctrs = new ArrayList<>();
		for (LiControlTableRow ctr : allRows) {
			if (ctr.getPIS().startsWith(prefix))
				ctrs.add(ctr);
		}
		return ctrs;
	}

	/**
	 * Return the row matching a given prefix
	 * @param prefix The prefix to look for
	 * @param matchExpected Indicates if we expect to find a matching row
	 * @return The corresponding row
	 */
	public LiControlTableRow getRow(ParameterizedInputSequence prefix, boolean matchExpected){
		final List<LiControlTableRow> allRows = getAllRows();
		for (LiControlTableRow ctr : allRows) {
			if (ctr.getPIS().equals(prefix))
				return ctr;
		}
		if(matchExpected){
			throw new AssertionError("No corresponding row was found.");
		} else {
			return null;
		}
	}
	
	/**
	 * @see LiControlTable#getRow(automata.efsm.ParameterizedInputSequence, boolean)
	 */
	public LiControlTableRow getRow(ParameterizedInputSequence prefix){
		return getRow(prefix,true);
	}
	
	/**
	 * Check the existence of a row indexed by the given prefix
	 * @param prefix The prefix to look for in the table
	 * @return true if a row was found, false otherwise
	 */
	public boolean rowExists(ParameterizedInputSequence prefix) {
		return getRow(prefix, false) != null;
	}
	
	public int getFromState(LiControlTableRow ctr) {
		final List<LiControlTableRow> allRows = getAllRows();
		ParameterizedInputSequence pis = ctr.getPIS();
		pis.removeLastParameterizedInput();
		if (pis.sequence.isEmpty())
			pis.addEmptyParameterizedInput();
		for (LiControlTableRow c : allRows) {
			if (pis.isSame(c.getPIS()))
				return getToState(c);
		}
		return 0;
	}

	public int getToState(LiControlTableRow ctr) {
		for (int i = 0; i < S.size(); i++) {
			if (ctr.isEquivalentTo(S.get(i)))
				return i;
		}
		return 0;
	}

	private void initialize() {
		R = new ArrayList<LiControlTableRow>();
		E = new ArrayList<ParameterizedInputSequence>();

		for (int i = 0; i < inputSymbols.size(); i++) {
			ParameterizedInputSequence seq = new ParameterizedInputSequence();
			seq.addParameterizedInput(new ParameterizedInput(inputSymbols
					.get(i)));
			E.add(seq);
		}

		S = new ArrayList<LiControlTableRow>();
		ParameterizedInputSequence empty = new ParameterizedInputSequence();
		empty.addEmptyParameterizedInput();
		S.add(new LiControlTableRow(empty, E));

		ParameterizedInputSequence pis;
		for (int i = 0; i < inputSymbols.size(); i++) {
			List<ArrayList<Parameter>> params = defaultParamValues
					.get(inputSymbols.get(i));
			if (params == null) {
				throw new RuntimeException(
						"Unable to find default parameter values for input symbol \""
								+ inputSymbols.get(i) + "\"");
			} else {
				for (int l = 0; l < params.size(); l++) {
					pis = new ParameterizedInputSequence();
					pis.addParameterizedInput(inputSymbols.get(i),
							defaultParamValues.get(inputSymbols.get(i)).get(l));
					R.add(new LiControlTableRow(pis, E));
				}
			}
		}
	}

	private boolean paramEquals(List<Parameter> A, List<Parameter> B) {
		if (A.size() != B.size()) {
			return false;
		}
		for (int i = 0; i < A.size(); i++) {
			if(!A.get(i).equals(B.get(i))){
				return false;
			}
		}
		return true;
	}

	public LiControlTableRow removeRowInR(int iRow) {
		return R.remove(iRow);
	}

	/**
	 * Returns the item corresponding to the PIS given in argument.
	 * TODO : Verify is it is possible that E can contain sequences of size > 1
	 * @param pis
	 * @return 
	 */
	public LiControlTableItem getItem(ParameterizedInputSequence pis) {
		ParameterizedInputSequence prefix = pis.clone();
		ParameterizedInput lastPI = prefix.removeLastParameterizedInput();
		if(prefix.sequence.isEmpty()){
			prefix.addEmptyParameterizedInput();
		}
		String lastSymbol = lastPI.getInputSymbol();
		
		/* Computes the column number of the cell we have to look into*/
		int j = inputSymbols.indexOf(lastSymbol);
		if (j == -1) {
			throw new AssertionError("This should be impossible, since E contains at least all the alphabet");
		}
		
		/* Look for the right row in the table */
		LiControlTableRow ctr = getRow(prefix);
		ArrayList<LiControlTableItem> cellContent = ctr.getColumn(j);
		for (LiControlTableItem cti : cellContent) {
			boolean paramsHaveSameValues = true;
			for (int i = 0; i < cti.getParameters().size(); i++) {
				if (!cti.getParameter(i).value.equals(lastPI.getParameterValue(i))) {
					paramsHaveSameValues = false;
					break;
				}
			}
			if (paramsHaveSameValues) {
				return cti;
			}
		}

		return null;
	}
	
	void addAtCorrespondingPlace(LiControlTableItem cti, ParameterizedInputSequence pis) {
		//Compute the column index from the last symbol
		String lastSymbol = pis.getLastSymbol();
		int columnIndex = inputSymbols.indexOf(lastSymbol);
		
		//Compute the PIS that indexes the row
		ParameterizedInputSequence prefix = pis.clone();
		prefix.removeLastParameterizedInput();
		if (prefix.sequence.isEmpty())
			prefix.addEmptyParameterizedInput();

		//Look for the matching row
		LiControlTableRow ctr = getRow(prefix);

		//check if the item already exists
		ArrayList<LiControlTableItem> cellContent = ctr.getColumn(columnIndex);
		for (LiControlTableItem existingCti : cellContent) {
			if (existingCti.getParameters().equals(cti.getParameters())) {
				if (!existingCti.getOutputSymbol().equals(cti.getOutputSymbol())){
					throw new AssertionError("The system seems undeterministic, or the driver the inference cannot work");
				}
				return;
			}
		}

		//if not found, we add it
		cellContent.add(cti);
	}
}
