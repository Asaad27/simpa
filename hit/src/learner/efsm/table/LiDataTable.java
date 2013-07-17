package learner.efsm.table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedInputSequence;

public class LiDataTable {
	public List<String> inputSymbols;
	private Map<String, List<ArrayList<Parameter>>> defaultParamValues;
	public List<LiDataTableRow> S;
	public List<LiDataTableRow> R;
	public List<NDV> ndvList;

	public LiDataTable(List<String> inputSymbols,
			Map<String, List<ArrayList<Parameter>>> defaultParamValues) {
		this.inputSymbols = inputSymbols;
		this.defaultParamValues = defaultParamValues;
		this.ndvList = new ArrayList<NDV>();
		initialize();
	}

	public void addAtCorrespondingPlace(LiDataTableItem dti,
			ParameterizedInputSequence currentPis) {
		String lastSymbol = currentPis.getLastSymbol();
		ParameterizedInputSequence prefix = currentPis.clone();
		prefix.removeLastParameterizedInput();
		if (prefix.sequence.isEmpty())
			prefix.addEmptyParameterizedInput();
		final List<LiDataTableRow> allRows = getAllRows();
		for (LiDataTableRow dtr : allRows) {
			if (dtr.getPIS().getHash().equals(prefix.getHash())) {
				int index = inputSymbols.indexOf(lastSymbol);
				String dtiStr = dti.toString();
				boolean exist = false;
				for (LiDataTableItem existingDti : dtr.getColum(index)) {
					if (existingDti.toString().equals(dtiStr)) {
						exist = true;
						break;
					}
				}
				if (!exist) {
					dtr.getColum(index).add(dti);
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

	public NDV findNDV() {
		final List<LiDataTableRow> allRows = getAllRows();
		for (LiDataTableRow dtr : allRows) {
			for (int j = 0; j < dtr.getColumCount(); j++) {
				if (!dtr.getColum(j).isEmpty()) {
					List<Parameter> params = new ArrayList<Parameter>();
					int ndvIndex = newNDVForItem(dtr.getColum(j), params,
							ndvList.size());
					if (ndvIndex != -1) {
						ParameterizedInputSequence pi = dtr.getPIS();
						Parameter last = params.remove(params.size() - 1);
						pi.addParameterizedInput(inputSymbols.get(j), params);
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

	public void initialize() {
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

	public int newNDVForItem(List<LiDataTableItem> item,
			List<Parameter> params, int index) {
		List<ArrayList<Parameter>> oldParams = new ArrayList<ArrayList<Parameter>>();
		int currentIndex = 0;
		for (int i = 0; i < item.size(); i++) {
			for (int j = i + 1; j < item.size(); j++) {
				if ((item.get(i).getInputParametersValues().equals(item.get(j)
						.getInputParametersValues()))
						&& (item.get(i).getAutomataStateValues().equals(item
								.get(j).getAutomataStateValues()))) {
					for (int k = 0; k < item.get(i).getOutputParameters()
							.size(); k++) {
						if (item.get(j).getOutputParameters().size() > k
								&& !(item.get(i).getOutputParameters().get(k).value
										.equals(item.get(j)
												.getOutputParameters().get(k).value))) {
							boolean exists = false;
							for (ArrayList<Parameter> old : oldParams) {
								if (old.equals(item.get(i).getInputParameters())) {
									exists = true;
									break;
								}
							}
							if (!exists) {
								if (currentIndex == index) {
									params.addAll(item.get(i)
											.getInputParameters());
									params.add(item.get(i)
											.getOutputParameters().get(k));
									return k;
								} else {
									oldParams.add((ArrayList<Parameter>) item
											.get(i).getInputParameters());
									currentIndex++;
								}
							}
						}
					}
				}
			}
		}
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
		for (LiDataTableItem dti : dtr.getColum(inputSymbols.indexOf(pi
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
}