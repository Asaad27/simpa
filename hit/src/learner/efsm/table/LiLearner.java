package learner.efsm.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import learner.Learner;
import learner.efsm.LiConjecture;
import main.simpa.Options;
import tools.loggers.LogManager;
import automata.State;
import automata.efsm.EFSMTransition;
import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedInputSequence;
import automata.efsm.ParameterizedOutput;
import automata.efsm.ParameterizedOutputSequence;
import drivers.Driver;
import drivers.efsm.EFSMDriver;
import drivers.efsm.EFSMDriver.Types;
import tools.Utils;

public class LiLearner extends Learner {
	private EFSMDriver driver;
	private LiControlTable cTable;
	private LiDataTable dTable;
	private Map<String, List<ArrayList<Parameter>>> defaultParamValues;
	private HashMap<String, ArrayList<String>> ndvUsed = new HashMap<String, ArrayList<String>>();
	private static boolean UNIQUE_NDV = true;
	private static boolean MARK_USED_NDV = true;
	
	
	public LiLearner(Driver driver) {
		this.driver = (EFSMDriver) driver;
		defaultParamValues = this.driver.getDefaultParamValues();

		this.cTable = new LiControlTable(driver.getInputSymbols(),
				defaultParamValues);
		this.dTable = new LiDataTable(driver.getInputSymbols(),
				defaultParamValues);
	}

	private void completeTable() {
		for (int i = 0; i < cTable.getCountOfRowsInS(); i++)
			fillTablesForRow(cTable.getRowInS(i), dTable.getRowInS(i));
		for (int i = 0; i < cTable.getCountOfRowsInR(); i++)
			fillTablesForRow(cTable.getRowInR(i), dTable.getRowInR(i));
	}
	
	/* TODO séparer inputs de colonne et inputs de ligne */
	@SuppressWarnings("unchecked")
	private void fillTablesForRow(LiControlTableRow ctr, LiDataTableRow dtr) {
		ParameterizedInputSequence querie = ctr.getPIS();
		querie.removeEmptyInput();
		
		for (int i = 0; i < ctr.getColumnCount(); i++) {	
			if (ctr.getColumn(i).isEmpty()) {	
				ArrayList<ParameterizedInputSequence> qlist = Utils.generatePermutations(ctr.getColumPIS(i), 0, defaultParamValues);
				for (int l = 0; l < qlist.size(); l++) {
					driver.reset();
					for (int m = 0; m < qlist.get(l).getLength(); m++) {
						querie.addParameterizedInput(qlist.get(l).getSymbol(m), qlist.get(l).getParameter(m));
					}
					ParameterizedInputSequence pis = new ParameterizedInputSequence();
					ParameterizedOutputSequence pos = new ParameterizedOutputSequence();
					
					for (int j = 0; j < querie.sequence.size(); j++) {
						if (UNIQUE_NDV) {
							ParameterizedInput pi = querie.sequence.get(j).clone();
							for (int k = 0; k < pi.getParameters().size(); k++) {
								if (pi.isNdv(k)) {
										System.out.println("Requesting NDV for " + pi.getInputSymbol());
										pi.setParameterValue(k,
															findNdvInPos(dTable.getNdv(pi
																		.getNdvIndexForVar(k)), pos, pi
																		.getParameters().get(k),
																		pi.getInputSymbol()
																	)
															);
								}
							}
							pis.addParameterizedInput(pi);
							ParameterizedOutput po = driver.execute(pi);
							pos.addParameterizedOuput(po);
						} else {
							ParameterizedInput pi = querie.sequence.get(j).clone();
							for (int k = 0; k < pi.getParameters().size(); k++) {
								if (pi.isNdv(k)) {
									pi.setParameterValue(
											k,
											findNdvInPos(dTable.getNdv(pi
													.getNdvIndexForVar(k)), pos, pi
													.getParameters().get(k), pi.getInputSymbol()));
								}
							}
							pis.addParameterizedInput(pi);
							ParameterizedOutput po = driver.execute(pi);
							pos.addParameterizedOuput(po);
						}
					}

					LiControlTableItem cti = new LiControlTableItem(
							querie.getLastParameters(), pos.getLastSymbol());
					ctr.addAtColumn(i, cti);

					TreeMap<String, List<Parameter>> automataState = driver
							.getInitState();
					ParameterizedInputSequence currentPis = new ParameterizedInputSequence();
					ParameterizedOutputSequence currentPos = new ParameterizedOutputSequence();
					for (int j = 0; j < pis.sequence.size() - 1; j++) {
						currentPis.addParameterizedInput(pis.sequence.get(j)
								.clone());
						currentPos.addParameterizedOuput(pos.sequence.get(j)
								.clone());
						LiDataTableItem dti = new LiDataTableItem(
								currentPis.getLastParameters(),
								(TreeMap<String, List<Parameter>>) automataState
										.clone(), currentPos
										.getLastParameters(), currentPos
										.getLastSymbol());
						dTable.addAtCorrespondingPlace(dti, currentPis);

						automataState.put(pis.sequence.get(j).getInputSymbol(),
								pis.sequence.get(j).getParameters());
						if (!pos.sequence.get(j).isOmegaSymbol())
							automataState.put(pos.sequence.get(j)
									.getOutputSymbol(), pos.sequence.get(j)
									.getParameters());
					}
					dtr.getColum(i).add(
							new LiDataTableItem(pis.getLastParameters(),
									automataState, pos.getLastParameters(), pos
											.getLastSymbol()));
					for (int m = 0; m < qlist.get(l).getLength(); m++) {
						querie.removeLastParameterizedInput();
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param ndv
	 * @param pos
	 * @param parameter
	 * @param pKey
	 * @return
	 */
	private Parameter findNdvInPos(NDV ndv, ParameterizedOutputSequence pos,
			Parameter parameter, String pKey) {
		Parameter pNdv = parameter;
		ParameterizedInputSequence pis = ndv.pis.clone();
		pis.removeEmptyInput();
		String pNdv_val;
		
		if (!this.ndvUsed.containsKey(pKey)) {
			this.ndvUsed.put(pKey, new ArrayList<String>());
		}
		if (!UNIQUE_NDV) {
			pis.removeEmptyInput();
			if (pis.sequence.size() <= pos.sequence.size()) {
				if (ndv.paramIndex < pos.getLastParameters().size()) {
					pNdv = pos.getLastParameters().get(ndv.paramIndex);
					pNdv.setNdv(ndv.indexNdv);
					return pNdv;
				}
			}
			return pNdv;
		}
		
		for (int i = pis.sequence.size() - 1; (i < pos.sequence.size() && i >= 0); i--) {
			List<Parameter> outputParameters = pos.sequence.get(i).getParameters();
			if (ndv.paramIndex < outputParameters.size()) {
				pNdv_val = outputParameters.get(ndv.paramIndex).value;
				if (!this.ndvUsed.get(pKey).contains(pNdv_val)) {
					pNdv = outputParameters.get(ndv.paramIndex);
					pNdv.setNdv(ndv.indexNdv);
					if (MARK_USED_NDV)
						this.ndvUsed.get(pKey).add(pNdv.value);
					return pNdv;
				}
			}
		}
		
		for (int i = pis.sequence.size(); (i < pos.sequence.size()); i++) {
			if (ndv.paramIndex < pos.sequence.get(i).getParameters().size()) {
				pNdv_val = pos.sequence.get(i).getParameters().get(ndv.paramIndex).value;
				if (!this.ndvUsed.get(pKey).contains(pNdv_val)) {
					pNdv = pos.sequence.get(i).getParameters().get(ndv.paramIndex);
					pNdv.setNdv(ndv.indexNdv);
					if (MARK_USED_NDV)
						this.ndvUsed.get(pKey).add(pNdv.value);
					return pNdv;
				}
			}
		}
		
		return pNdv;
		
	}
	

	@Override
	public LiConjecture createConjecture() {
		LogManager.logConsole("Building the raw conjecture");
		LiConjecture c = new LiConjecture(driver);
		for (int i = 0; i < cTable.getCountOfRowsInS(); i++) {
			c.addState(new State("S" + i, cTable.getRowInS(i).isEpsilon()));
		}
		List<LiControlTableRow> allRows = cTable.getAllRows();
		Collections.sort(allRows, new Comparator<LiControlTableRow>() {
			@Override
			public int compare(LiControlTableRow o1, LiControlTableRow o2) {
				return o1.getPIS().sequence.size()
						- o2.getPIS().sequence.size();
			}
		});
		for (LiControlTableRow ctr : allRows) {
			if (ctr.isEpsilon()) {
				continue;
			}
			int iFrom = cTable.getFromState(ctr);
			int iTo = cTable.getToState(ctr);
			State from = c.getState(iFrom);
			State to = c.getState(iTo);
			String inputSymbol = ctr.getLastPI().getInputSymbol();
			ArrayList<LiControlTableItem> allControlItems = cTable
					.getRowInS(iFrom).getColumn(
							cTable.getInputSymbols().indexOf(inputSymbol));
			ArrayList<LiDataTableItem> allDataItems = dTable.getRowInS(
					iFrom).getColum(
					cTable.getInputSymbols().indexOf(inputSymbol));
			for (int i = 0; i < cTable.R.size(); i++) {
				if (cTable.getRowInS(iFrom).isEquivalentTo(
						cTable.getRowInR(i))) {
					allDataItems.addAll(dTable.getRowInR(i).getColum(
							cTable.getInputSymbols().indexOf(inputSymbol)));
				}
			}

			for (LiControlTableItem cti : allControlItems) {
				if (!cti.isOmegaSymbol()) {
					if (ctr.getLastPI().getParamHash()
							.equals(cti.getParamHash())) {
						ArrayList<LiDataTableItem> correspondingDataTableItems = new ArrayList<LiDataTableItem>();
						for (LiDataTableItem dti : allDataItems) {
							if (cti.getOutputSymbol().equals(
									dti.getOutputSymbol())
									&& !correspondingDataTableItems
											.contains(dti))
								correspondingDataTableItems.add(dti);
						}
						for (NDV n : dTable.ndvList) {
							ParameterizedInputSequence tmpis = n.getPIS();
							if (ctr.getPIS()
									.toString()
									.equals(tmpis.removeEmptyInput()
											.toString())) {
								for (LiDataTableItem tmp : correspondingDataTableItems) {
									tmp.getOutputParameters().set(
											n.paramIndex,
											new Parameter("Ndv"
													+ n.indexNdv,
													Types.STRING));
								}
							}
						}
						if (!correspondingDataTableItems.isEmpty())
							c.addTransition(new EFSMTransition(c, from, to,
									inputSymbol, cti.getOutputSymbol(),
									correspondingDataTableItems));
					}
				}
			}
		}
		for (int i = c.getTransitionCount() - 1; i >= 0; i--) {
			for (int j = i - 1; j >= 0; j--) {
				EFSMTransition t1 = c.getTransition(i);
				EFSMTransition t2 = c.getTransition(j);
				if (t1.getInput().equals(t2.getInput())
						&& t1.getOutput().equals(t2.getOutput())
						&& (t1.getFrom().equals(t2.getFrom()))
						&& (t1.getTo().equals(t2.getTo()))) {
					c.removeTransition(i);
					break;
				}
			}
		}

		expandInitParams(c.getTransitions());

		LogManager.logInfo("Raw conjecture have " + c.getStateCount()
				+ " states and " + c.getTransitionCount() + " transitions : ");
		for (EFSMTransition t : c.getTransitions()) {
			LogManager.logTransition(t.toString());
			for (LiDataTableItem dti : t.getParamsData()) {
				LogManager.logData(dti.toString());
			}
		}
		LogManager.logLine();

		c.exportToRawDot();
		c.exportToDot();
		c.exportToAslan();
		c.exportToXML();
		return c;
	}

	private void expandInitParams(List<EFSMTransition> list) {
		List<Integer> arity = new ArrayList<Integer>();
		if (!list.isEmpty() && !list.get(0).getParamsData().isEmpty()) {
			for (Map.Entry<String, List<Parameter>> entry : list.get(0)
					.getParamsData(0).getAutomataState().entrySet()) {
				arity.add(entry.getValue().size());
			}
			for (EFSMTransition t : list) {
				for (int i = 0; i < t.getParamsDataCount(); i++) {
					int j = 0;
					for (Map.Entry<String, List<Parameter>> entry : t
							.getParamsData(i).getAutomataState().entrySet()) {
						if (entry.getValue().size() > arity.get(j))
							arity.set(j, entry.getValue().size());
						j++;
					}
				}
			}
			for (EFSMTransition t : list) {
				for (LiDataTableItem dti : t.getParamsData()) {
					int j = 0;
					for (Map.Entry<String, List<Parameter>> entry : dti
							.getAutomataState().entrySet()) {
						for (int k = entry.getValue().size(); k < arity.get(j); k++)
							entry.getValue().add(
									new Parameter(Parameter.PARAMETER_INIT_VALUE));
						j++;
					}
				}
			}
		}
	}

	private void handleDisputedRow(NDF disputed) {
		for (int i = 0; i < disputed.parameters.size(); i++) {
			ParameterizedInputSequence pis = disputed.getPIS();
			pis.addParameterizedInput(new ParameterizedInput(disputed
					.getInputSymbol(), disputed.parameters.get(i)));
			boolean alreadyExists = false;
			for (int j = 0; j < cTable.getCountOfRowsInR(); j++) {
				if (pis.isSame(cTable.getRowInR(j).getPIS())) {
					alreadyExists = true;
					break;
				}
			}
			if (!alreadyExists) {
				cTable.addRowInR(new LiControlTableRow(pis, cTable.E));
				dTable.addRowInR(new LiDataTableRow(pis, cTable
						.getInputSymbols()));
				completeTable();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void handleNBP(NBP nbp) {
		//Iterates on every line of the table
		final List<LiControlTableRow> allRows = cTable.getAllRows();
		for (LiControlTableRow ctr : allRows) {
			//Looks for a cell in which the input parameters are different from 
			//those concerned by the NBP
			boolean paramExists = false;
			for (int j = 0; j < ctr.getSizeOfColumn(nbp.iInputSymbol); j++) {
				if (nbp.getParamHash()
						.equals(ctr.getItemInColumn(nbp.iInputSymbol, j)
								.getParamHash())) {
					paramExists = true;
					break;
				}
			}
			if (!paramExists) {
				driver.reset();
				
				//constructs the PIS that will be used to balance the cell
				ParameterizedInputSequence query = ctr.getPIS();
				query.addParameterizedInput(
						new ParameterizedInput(
								ctr.getColumPIS(nbp.iInputSymbol).getLastSymbol(),//TODO:it assumes that E will never contains sequences (is that true ?) (*)
								nbp.params)
				);
				query.removeEmptyInput();

				//Sends the sequence and store the results in a POS
				//Before sending, search a velue to give to each NDV
				ParameterizedInputSequence pis = new ParameterizedInputSequence();
				ParameterizedOutputSequence pos = new ParameterizedOutputSequence();
				for (ParameterizedInput queryPI : query.sequence) {
					ParameterizedInput api = queryPI.clone();
					for (int k = 0; k < api.getParameters().size(); k++) {
						if (api.isNdv(k)) {
							System.out.println("Requesting NDV for " + api.getInputSymbol());
							String pKey = UNIQUE_NDV ? api.getInputSymbol() : api.getParameterValue(0);
							api.setParameterValue(
									k,
									findNdvInPos(
											dTable.getNdv(api.getNdvIndexForVar(k)),
											pos,
											api.getParameters().get(k),
											pKey));
						}
					}
					pis.addParameterizedInput(api);
					ParameterizedOutput po = driver.execute(api);
					pos.addParameterizedOuput(po);
				}
					
				//Completes the control table with the new item
				LiControlTableItem ctiNBP = new LiControlTableItem(
						pis.getLastParameters(), pos.getLastSymbol());
				for (int j = 0; j < nbp.params.size(); j++) {
					ctiNBP.setNdv(j, nbp.params.get(j).getNdv());
				}
				ctr.addAtColumn(nbp.iInputSymbol, ctiNBP);
				
				//Completes the data table
				TreeMap<String, List<Parameter>> automataState = driver
						.getInitState();
				ParameterizedInputSequence currentPis = new ParameterizedInputSequence();
				ParameterizedOutputSequence currentPos = new ParameterizedOutputSequence();
				pis.removeEmptyInput();
				for (int j = 0; j < pis.sequence.size(); j++) {
					ParameterizedInput tmpNdv = pis.sequence.get(j).clone();
					currentPis.addParameterizedInput(tmpNdv);
					currentPos.addParameterizedOuput(pos.sequence.get(j).clone());
					LiDataTableItem dti = new LiDataTableItem(
							currentPis.getLastParameters(),
							(TreeMap<String, List<Parameter>>) automataState.clone(),
							currentPos.getLastParameters(),
							currentPos.getLastSymbol());
					dTable.addAtCorrespondingPlace(dti, currentPis);

					automataState.put(pis.sequence.get(j).getInputSymbol(),
							pis.sequence.get(j).getParameters());
					if (!pos.sequence.get(j).isOmegaSymbol())
						automataState.put(
								pos.sequence.get(j).getOutputSymbol(),
								pos.sequence.get(j).getParameters());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void handleNDV(NDV ndv) {
		/* Let us find all rows starting with the NDV given as parameter */
		List<LiControlTableRow> ctrs = cTable.getRowStartsWith(ndv.pis);
		/* Iteration on rows containing NDV */
		for (LiControlTableRow ctr : ctrs) {
			/* Iteration on input parameters */
			for (int i = 0; i < driver.getInputSymbols().size(); i++) {
				/* Iteration on default values for the current parameter */
				for (int l = 0; l < defaultParamValues.get(
						cTable.getInputSymbol(i)).size(); l++) {
					for (int k = 0; k < defaultParamValues
							.get(cTable.getInputSymbol(i)).get(l).size(); k++) {
						ArrayList<Parameter> ndvParam = driver
								.getDefaultParamValues()
								.get(cTable.getInputSymbol(i)).get(l);
						if (ndvParam.get(k).type == ndv.type) {
							driver.reset();
							ParameterizedInputSequence pis = ndv.getPIS();
							ParameterizedOutputSequence pos = new ParameterizedOutputSequence();
							for (ParameterizedInput pi : pis.sequence) {
								pos.sequence.add(driver.execute(pi));
							}
							String ndvVal = "0";
							try{
								ndvVal = pos.getLastParameters().get(
									ndv.paramIndex).value;
								LogManager.logInfo("Ndv value is : " + ndvVal);
							}catch(Exception e){															}
							ndvParam.get(k).value = ndvVal;
							ParameterizedInput ndvpi = new ParameterizedInput(
									driver.getInputSymbols().get(i), ndvParam);
							ndvpi.setNdvIndexForVar(k, ndv.indexNdv);
							ParameterizedOutput po = driver.execute(ndvpi);

							pis.addParameterizedInput(ndvpi);
							pos.addParameterizedOuput(po);

							LiControlTableItem ctiNDV = new LiControlTableItem(
									pis.getLastParameters(),
									pos.getLastSymbol());
							ctiNDV.setNdv(k, dTable.getCountOfNdv() - 1);
							ctr.addAtColumn(i, ctiNDV);

							TreeMap<String, List<Parameter>> automataState = driver
									.getInitState();
							ParameterizedInputSequence currentPis = new ParameterizedInputSequence();
							ParameterizedOutputSequence currentPos = new ParameterizedOutputSequence();
							pis.removeEmptyInput();
							for (int j = 0; j < pis.sequence.size(); j++) {
								currentPis.addParameterizedInput(pis.sequence
										.get(j).clone());
								currentPos.addParameterizedOuput(pos.sequence
										.get(j).clone());
								LiDataTableItem dti = new LiDataTableItem(
										currentPis.getLastParameters(),
										(TreeMap<String, List<Parameter>>) automataState
												.clone(), currentPos
												.getLastParameters(),
										currentPos.getLastSymbol());
								dTable.addAtCorrespondingPlace(dti, currentPis);

								automataState.put(pis.sequence.get(j)
										.getInputSymbol(), pis.sequence.get(j)
										.getParameters());
								if (!pos.sequence.get(j).isOmegaSymbol())
									automataState.put(pos.sequence.get(j)
											.getOutputSymbol(), pos.sequence
											.get(j).getParameters());
							}
						}
					}
				}
			}
		}
	}

	
	private void handleNonClosed(int iRow) {
		ParameterizedInputSequence origPis = cTable.getRowInR(iRow).getPIS();
		cTable.addRowInS(cTable.removeRowInR(iRow));
		dTable.addRowInS(dTable.removeRowInR(iRow));
		for (int i = 0; i < cTable.getInputSymbolsCount(); i++) {
			for (int j = 0; j < defaultParamValues
					.get(cTable.getInputSymbol(i)).size(); j++) {
				ParameterizedInputSequence pis = origPis.clone();
				pis.addParameterizedInput(new ParameterizedInput(cTable
						.getInputSymbol(i), defaultParamValues.get(
						cTable.getInputSymbol(i)).get(j)));
				LiControlTableRow newControlRow = new LiControlTableRow(pis,
						cTable.E);
				cTable.addRowInR(newControlRow);
				LiDataTableRow newDataRow = new LiDataTableRow(pis,
						cTable.getInputSymbols());
				dTable.addRowInR(newDataRow);
			}
		}
		completeTable();
	}


	@Override
	public void learn() {
		LogManager.logConsole("Inferring the system");
		boolean finished = false;
		boolean contrex = true;
		NDV ndv;
		NBP nbp;
		NDF ndf;
		ParameterizedInputSequence ce = null;
		completeTable();
		LogManager.logControlTable(cTable);
		LogManager.logDataTable(dTable);
		
		while (contrex) {
			contrex = false;
			finished = false;
			while (!finished) {
				finished = true;
	
				while ((ndv = dTable.findNDV()) != null) {
					finished = false;
					LogManager.logStep(LogManager.STEPNDV, ndv);
					handleNDV(ndv);
					LogManager.logControlTable(cTable);
					LogManager.logDataTable(dTable);
				}
				while ((nbp = cTable.getNotBalancedParameter()) != null) {
					finished = false;
					LogManager.logStep(LogManager.STEPNBP, nbp);
					handleNBP(nbp);
					LogManager.logControlTable(cTable);
					LogManager.logDataTable(dTable);
				}
				int alreadyNonClosed = 0;
				for (int nonClosedRow : cTable.getNonClosedRows()) {
					int nonClosedRowRealIndex = nonClosedRow - alreadyNonClosed;
					finished = false;
					int seems = -1;
					if (Options.REUSE_OP_IFNEEDED
							&& ((seems = seemsEquivalent(nonClosedRowRealIndex)) != -1)
							&& !cTable.getRowInR(nonClosedRowRealIndex)
									.seems()) {
						LogManager.logStep(LogManager.STEPOTHER, "Row "
								+ cTable.getRowInR(nonClosedRowRealIndex).getPIS()
								+ " in R seems to be equivalent to row " 
								+ cTable.getRowInS(seems).getPIS()
								+ " in S");
						handleSeemsEquivalent(nonClosedRowRealIndex, seems);
					} else if (!cTable.isClosedRow(nonClosedRowRealIndex)){
						//bugfix : this condition prevents adding to S multiple non-closed rows 
						//representing the same state in the same instance of this loop
						//TODO : simplify this loop ? (with cTable.getFirstNonClosedRow for example)
						LogManager.logStep(LogManager.STEPNCR,
								cTable.R.get(nonClosedRowRealIndex).getPIS());
						handleNonClosed(nonClosedRowRealIndex);
						alreadyNonClosed++;
					}
					LogManager.logControlTable(cTable);
					LogManager.logDataTable(dTable);
				}
				while ((ndf = cTable.getDisputedItem()) != null) {
					finished = false;
					LogManager.logStep(LogManager.STEPNDF, ndf);
					handleDisputedRow(ndf);
					LogManager.logControlTable(cTable);
					LogManager.logDataTable(dTable);
				}
			}
			LiConjecture conjecture = createConjecture();
			if (!driver.isCounterExample(ce, conjecture))
				ce = driver.getCounterExample(conjecture);
			else
				LogManager.logInfo("Previous counter example : " + ce
						+ " is still a counter example for the new conjecture");
			if (ce != null) {
				contrex = true;
				System.out.println("[LEARNER] GETTING COUNTER-EXAMPLE: " + ce + " " + ce.getLength() + " " + ce.getLastSymbol());
				finished = false;
				int suffixLength = 1;
				do {
					System.out.println("[LEARNER] Let's go for column " + ce.getIthSuffix(suffixLength));
					LogManager.logControlTable(cTable);
					cTable.addColumnInE(ce.getIthSuffix(suffixLength));
					dTable.addColumnInAllRows();
					LogManager.logControlTable(cTable);
					completeTable();
					if (!cTable.getNonClosedRows().isEmpty()) {
						System.out.println("[LEARNER] Breaking");
						break;
					}
					suffixLength++;
				} while (suffixLength <= ce.getLength());
				LogManager.logControlTable(cTable);
				LogManager.logDataTable(dTable);
			}
		}
	}


	private void handleSeemsEquivalent(int nonClosedRow, int seems) {
		LiControlTableRow ctr = cTable.getRowInR(nonClosedRow);
		ctr.seemsTo(seems);
		List<Parameter> previousOutputValues = new ArrayList<Parameter>();
		ParameterizedInputSequence tmp = ctr.getPIS();
		for (int z = 0; z < ctr.getPIS().sequence.size(); z++) {
			previousOutputValues
					.addAll(dTable.getFixedOutputParametersFor(tmp));
			if (!previousOutputValues.isEmpty())
				LogManager.logInfo("New parameter values : "
						+ previousOutputValues.toString());
			for (int y = 0; y < previousOutputValues.size(); y++) {
				for (int i = 0; i < driver.getInputSymbols().size(); i++) {
					for (int l = 0; l < defaultParamValues.get(
							cTable.getInputSymbol(i)).size(); l++) {
						for (int k = 0; k < defaultParamValues
								.get(cTable.getInputSymbol(i)).get(l).size(); k++) {
							if (previousOutputValues.get(y).type == defaultParamValues
									.get(cTable.getInputSymbol(i)).get(l)
									.get(k).type) {
								driver.reset();
								ParameterizedInputSequence pis = ctr.getPIS();
								LogManager.logInfo(pis.toString());
								ParameterizedOutputSequence pos = new ParameterizedOutputSequence();
								for (int m = 0; m < pis.sequence.size(); m++)
									pos.sequence.add(driver
											.execute(pis.sequence.get(m)));

								ArrayList<Parameter> def = new ArrayList<Parameter>();
								for (Parameter p : defaultParamValues.get(
										cTable.getInputSymbol(i)).get(l)) {
									def.add(p.clone());
								}

								ParameterizedInput seemsPi = new ParameterizedInput(
										driver.getInputSymbols().get(i), def);
								seemsPi.getParameters().get(k).value = previousOutputValues
										.get(y).value;

								pis.addParameterizedInput(seemsPi);
								ParameterizedOutput po = driver
										.execute(seemsPi);
								pos.addParameterizedOuput(po);

								LiControlTableItem cti = new LiControlTableItem(
										pis.getLastParameters(),
										pos.getLastSymbol());
								ctr.addAtColumn(i, cti);

								TreeMap<String, List<Parameter>> automataState = driver
										.getInitState();
								ParameterizedInputSequence currentPis = new ParameterizedInputSequence();
								ParameterizedOutputSequence currentPos = new ParameterizedOutputSequence();
								pis.removeEmptyInput();
								for (int j = 0; j < pis.sequence.size(); j++) {
									currentPis
											.addParameterizedInput(pis.sequence
													.get(j).clone());
									currentPos
											.addParameterizedOuput(pos.sequence
													.get(j).clone());
									@SuppressWarnings("unchecked")
									LiDataTableItem dti = new LiDataTableItem(
											currentPis.getLastParameters(),
											(TreeMap<String, List<Parameter>>) automataState
													.clone(), currentPos
													.getLastParameters(),
											currentPos.getLastSymbol());
									dTable.addAtCorrespondingPlace(dti,
											currentPis);

									automataState.put(pis.sequence.get(j)
											.getInputSymbol(), pis.sequence
											.get(j).getParameters());
									if (!pos.sequence.get(j).isOmegaSymbol())
										automataState.put(pos.sequence.get(j)
												.getOutputSymbol(),
												pos.sequence.get(j)
														.getParameters());
								}
							}
						}
					}
				}
			}
			previousOutputValues.clear();
			tmp.removeLastParameterizedInput();
			if (ctr.isEquivalentTo(cTable.getRowInS(seems)))
				break;
		}
	}

	/**
	 * Research the row in S that is the most similar to the non-closed row given in parameter
	 * @param nonClosedRowIndex the index of the non-closed row
	 * @return the index of the most similar row in S, or -1 if no row is similar enough
	 */
	private int seemsEquivalent(int nonClosedRowIndex) {
		List<Double> stats = new ArrayList<Double>();
		LiControlTableRow nonClosedRow = cTable.getRowInR(nonClosedRowIndex);
		double max = 0;
		//for each row of S ...
		for(LiControlTableRow rowOfS : cTable.S){
		//for (int i = 0; i < cTable.getCountOfRowsInS(); i++) {
			double diff = 0.0;
			max = 0;
			//for each column of the table ...
			for (int j = 0; j < cTable.getColsCount(); j++) {
				//... we count a difference for each output symbol in the j^th
				//cell of the non-closed row that is not in the j^th cell of the 
				//row of S.
				for (int k = 0; k < nonClosedRow.getSizeOfColumn(j); k++) {
					max++;
					if (rowOfS.getSizeOfColumn(j) <= k){
						diff++;
					} else {
						String outputSymbolRowInS = rowOfS.getColumn(j).get(k).getOutputSymbol();
						String outputSymbolNonClosedRow = nonClosedRow.getColumn(j).get(k).getOutputSymbol();
						if (!outputSymbolRowInS.equals(outputSymbolNonClosedRow)) {
							diff++;
						}
					}
				}
			}
			//store the "difference ratio" between the non-closed row and the row of S
			stats.add(diff / max);
		}
		
		//select the index of the row of S that is the closest to the non-closed row
		int indexMin = 0;
		for (int i = 1; i < stats.size(); i++) {
			if (stats.get(i) < stats.get(indexMin)) {
				indexMin = i;
			}
		}
		
		return (stats.get(indexMin) <= 1 / max ? indexMin : -1);
	}
}
