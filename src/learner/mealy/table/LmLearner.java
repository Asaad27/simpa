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
 *     Roland GROZ
 *     Nicolas BREMOND
 ********************************************************************************/
package learner.mealy.table;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import learner.Learner;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.loggers.LogManager;
import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import automata.mealy.multiTrace.SimpleMultiTrace;
import drivers.mealy.MealyDriver;

public class LmLearner extends Learner {
	private MealyDriver driver;
	protected LmControlTable cTable;
	private LmStatsEntry stats;
	private LmOptions options;

	public LmLearner(MealyDriver driver, LmOptions options) {
		this.options = options;
		this.driver = driver;
		this.cTable = new LmControlTable(driver.getInputSymbols());
		stats = new LmStatsEntry(this.driver, options);
		options.oracle.updateWithDriver(this.driver);
	}

	private void completeTable() {
		for (int i = 0; i < cTable.getCountOfRowsInS(); i++)
			fillTablesForRow(cTable.getRowInS(i));
		for (int i = 0; i < cTable.getCountOfRowsInR(); i++)
			fillTablesForRow(cTable.getRowInR(i));
	}

	protected void resetDriver() {
		driver.reset();
	}

	protected OutputSequence applyOnDriver(InputSequence inSeq) {
		return driver.execute(inSeq);
	}

	/**
	 * this is overriden for Rivest&Schapire learner to detect inconsistencies
	 * on h
	 * 
	 * @param trace
	 *            the trace applied on driver
	 */
	protected void handleNewCounterExample(LmTrace trace) {
	}

	private void fillTablesForRow(LmControlTableRow ctr) {
		InputSequence querie = null;
		for (int i = 0; i < ctr.getColumnCount(); i++) {
			if (ctr.getColumn(i).getOutputSymbol() == null) {
				resetDriver();
				querie = ctr.getIS();
				querie.addInputSequence(ctr.getInputSequence(i));
				InputSequence is = new InputSequence();
				is.addInputSequence(querie);
				OutputSequence os = applyOnDriver(is);
				LmControlTableItem cti = new LmControlTableItem(os
						.getIthSuffix(ctr.getColSuffixSize(i)).toString());
				ctr.setAtColumn(i, cti);
			}
		}
	}

	@Override
	public LmConjecture createConjecture() {
		if (addtolog)
			LogManager.logConsole("Building the conjecture");
		LmConjecture c = new LmConjecture(driver);
		for (int i = 0; i < cTable.getCountOfRowsInS(); i++) {
			c.addState(new State("S" + i, cTable.getRowInS(i).isEpsilon()));
		}
		List<LmControlTableRow> allRows = cTable.getAllRows();
		Collections.sort(allRows, new Comparator<LmControlTableRow>() {
			@Override
			public int compare(LmControlTableRow o1, LmControlTableRow o2) {
				return o1.getIS().sequence.size() - o2.getIS().sequence.size();
			}
		});
		for (LmControlTableRow ctr : allRows) {
			if (!ctr.isEpsilon()) {
				int iFrom = cTable.getFromState(ctr);
				int iTo = cTable.getToState(ctr);
				State from = c.getState(iFrom);
				State to = c.getState(iTo);
				String inputSymbol = ctr.getLastPI();
				LmControlTableItem controlItem = cTable.getRowInS(iFrom)
						.getColumn(
								cTable.getInputSymbols().indexOf(inputSymbol));

				if (!controlItem.isOmegaSymbol()) {
					c.addTransition(new MealyTransition(c, from, to,
							inputSymbol, controlItem.getOutputSymbol()));
				}
			}
		}
/*		for (int i = c.getTransitionCount() - 1; i >= 0; i--) {
			for (int j = i - 1; j >= 0; j--) {
				MealyTransition t1 = c.getTransition(i);
				MealyTransition t2 = c.getTransition(j);
				if (t1.getInput().equals(t2.getInput())
						&& t1.getOutput().equals(t2.getOutput())
						&& (t1.getFrom().equals(t2.getFrom()))
						&& (t1.getTo().equals(t2.getTo()))) {
					c.removeTransition(i);
					break;
				}
			}
		}*/

		if (Options.getLogLevel() == LogLevel.ALL) {
			LogManager.logInfo("Conjecture has " + c.getStateCount()
					+ " states and " + c.getTransitionCount()
					+ " transitions : ");
			for (MealyTransition t : c.getTransitions()) {
				LogManager.logTransition(t.toString());
			}
			LogManager.logConsole("Exporting conjecture");
			c.exportToDot();
		}
		LogManager.logLine();

		return c;
	}

	private void handleNonClosed(int iRow) {
		InputSequence origPis = cTable.getRowInR(iRow).getIS();
		cTable.addRowInS(cTable.removeRowInR(iRow));
		for (int i = 0; i < cTable.getInputSymbolsCount(); i++) {
			InputSequence pis = origPis.clone();
			pis.addInput(new String(cTable.getInputSymbol(i)));
			LmControlTableRow newControlRow = new LmControlTableRow(pis,
					cTable.E);
			cTable.addRowInR(newControlRow);
		}
		completeTable();
	}

	@Override
	public void learn() {
		LogManager.logConsole("Inferring the system");
		long startTime = System.nanoTime();
// RG: changed Karim's use of finished that repeated CE search after final conjecture.
//		boolean finished = false;
		boolean potentialNewNonClosedRows = true;
		InputSequence ce = null;
		completeTable();
		LogManager.logControlTable(cTable);
		while (true /*!finished*/) {
			potentialNewNonClosedRows = true;
//			finished = true;
			while (potentialNewNonClosedRows) {
				potentialNewNonClosedRows = false;
				int alreadyNonClosed = 0;
				for (int nonClosedRow : cTable.getNonClosedRows()) {
					potentialNewNonClosedRows=true;
//					finished = false;
					if (Options.getLogLevel() != LogLevel.LOW)
						LogManager.logStep(LogManager.STEPNCR,
								cTable.R.get(nonClosedRow).getIS());
					handleNonClosed(nonClosedRow - (alreadyNonClosed++));
					if (Options.getLogLevel() == LogLevel.ALL)
						LogManager.logControlTable(cTable);
				}
			}
			stopLog();
			LmConjecture conj = createConjecture();
			startLog();
			if (!driver.isCounterExample(ce, conj)) {
				SimpleMultiTrace appliedSequences = new SimpleMultiTrace();
				if (driver.getCounterExample_noThrow(options.oracle, conj, null,
						appliedSequences, false, stats.getOracle())) {
					LmTrace ceTrace = appliedSequences.getLastTrace();
					handleNewCounterExample(ceTrace);
					ce = ceTrace.getInputsProjection();
				} else {
					ce = null;
				}
			} else
				LogManager.logInfo("Previous counter example : " + ce
						+ " is still a counter example for the new conjecture");
			if (ce != null) {
//				finished = false;
				int suffixLength = 1;
				do {
					cTable.addColumnInE(ce.getIthSuffix(suffixLength));
					completeTable();
					if (!cTable.getNonClosedRows().isEmpty())
						break;
					suffixLength++;
				} while (suffixLength <= ce.getLength());
				if (cTable.getNonClosedRows().isEmpty())
// RG: this should not happen with Lm. Test could be converted to assert once we are sure.
					LogManager.logInfo("Counter example failed to exhibit new state");
				if (Options.getLogLevel() == LogLevel.ALL)
					LogManager.logControlTable(cTable);
			}
			else
//				finished = true;
				break;
		}
		float duration = (float) ((System.nanoTime() - startTime)
				/ 1000000000.);
		stats.finalUpdate(createConjecture(), duration,
//<<<<<<< HEAD:src/learner/mealy/table/LmLearner.java
				driver.getNumberOfAtomicRequest(), driver.getNumberOfRequest());
//=======
//				driver.numberOfAtomicRequest - lastOracleLength,
//				driver.numberOfRequest - lastOracleResets);
//>>>>>>> 4cc71680... add fixes to build stats and display graphs:hit/src/learner/mealy/table/LmLearner.java
	}

	@Override
	public LmStatsEntry getStats() {
		return stats;
	}
}
