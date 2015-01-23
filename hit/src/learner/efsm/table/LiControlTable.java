package learner.efsm.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedInputSequence;

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
			if (is.equals(col))
				exists = true;
		}
		if (!exists) {
			E.add(col);
			for (LiControlTableRow row : S)
				row.addColumn(col);
			for (LiControlTableRow row : R)
				row.addColumn(col);
		}
	}

	public void addRowInR(LiControlTableRow row) {
		R.add(row);
	}

	public void addRowInS(LiControlTableRow row) {
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

	public NDF getDisputedItem() {
		for (LiControlTableRow ctr : S) {
			for (int i = 0; i < ctr.getColumCount(); i++) {
				for (int j = 0; j < ctr.getSizeOfColumn(i); j++) {
					for (int k = j + 1; k < ctr.getSizeOfColumn(i); k++) {
						if (!ctr.getItemInColumn(i, j)
								.getOutputSymbol()
								.equals(ctr.getItemInColumn(i, k)
										.getOutputSymbol())) {
							List<ArrayList<Parameter>> parameters = new ArrayList<ArrayList<Parameter>>();
							for (int l = 0; l < ctr.getSizeOfColumn(i); l++) {
								parameters.add((ArrayList<Parameter>) ctr
										.getItemInColumn(i, l).getParameters());
							}
							NDF ndf = new NDF(ctr.getPIS().removeEmptyInput(),
									inputSymbols.get(i), parameters);
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
							if (paramEquals(parametersA, parametersB)) {
								found = true;
								break;
							}
						}
						if (!found) {
							NBP nbp = new NBP(parametersA, i);
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
	 * Look for rows in data table that start with the pis
	 * input sequence
	 * 
	 * @param 	pis prefix to look for
	 * @return	A list of rows of Data table that start
	 * 			with pis
	 */
	public List<LiControlTableRow> getRowStartsWith(
			ParameterizedInputSequence pis) {
		final List<LiControlTableRow> allRows = getAllRows();
		List<LiControlTableRow> ctrs = new ArrayList<LiControlTableRow>();
		for (LiControlTableRow ctr : allRows) {
			if (ctr.getPIS().startsWith(pis))
				ctrs.add(ctr);
		}
		return ctrs;
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
		StringBuffer hashA = new StringBuffer();
		for (int i = 0; i < A.size(); i++) {
			if (A.get(i).isNDV()) {
				hashA.append("(ndv" + A.get(i).ndv + ")");
			} else {
				hashA.append("(" + A.get(i).value + ")");
			}
		}
		StringBuffer hashB = new StringBuffer();
		for (int i = 0; i < B.size(); i++) {
			if (B.get(i).isNDV()) {
				hashB.append("(ndv" + B.get(i).ndv + ")");
			} else {
				hashB.append("(" + B.get(i).value + ")");
			}
		}
		return hashA.toString().equals(hashB.toString());
	}

	public LiControlTableRow removeRowInR(int iRow) {
		return R.remove(iRow);
	}
}
