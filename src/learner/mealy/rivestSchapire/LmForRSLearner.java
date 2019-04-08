/********************************************************************************
 * Copyright (c) 2018,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 ********************************************************************************/
package learner.mealy.rivestSchapire;

import java.util.ArrayList;
import java.util.List;

import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import learner.mealy.LmTrace;
import learner.mealy.table.LmControlTableRow;
import learner.mealy.table.LmLearner;
import learner.mealy.table.LmOptions;
import options.RandomOption;
import tools.RandomGenerator;
import tools.loggers.LogManager;

public class LmForRSLearner extends LmLearner {
	private final RandomGenerator rand;
	StateDriver driver;
	KnownTracesTree knownTraces = new KnownTracesTree();

	public LmForRSLearner(StateDriver driver, LmOptions options,
			RandomOption rand) {
		super(driver, options);
		this.driver = driver;
		this.rand = rand.getRand();
	}

	@Override
	protected void resetDriver() {
		super.resetDriver();
		List<LmControlTableRow> differentRows = new ArrayList<>();
		for (int i = 0; i < cTable.getCountOfRowsInS(); i++) {
			LmControlTableRow newRaw = cTable.getRowInS(i);
			boolean isEqual = false;
			for (LmControlTableRow taken : differentRows) {
				if (taken.isEquivalentTo(newRaw)) {
					isEqual = true;
					break;
				}
			}
			if (!isEqual)
				differentRows.add(newRaw);
		}
		if (differentRows.size() > driver.learner.n) {
			while (true) {
				int i = rand.randInt(differentRows.size());
				int j = rand.randInt(differentRows.size() - 1);
				if (j >= i)
					j++;
				LmControlTableRow rowI = cTable.getRowInS(i);
				LmControlTableRow rowJ = cTable.getRowInS(j);

				for (int k = 0; k < rowI.getColumnCount(); k++) {
					if (!rowI.getColumn(k).getOutputSymbol()
							.equals(rowJ.getColumn(k).getOutputSymbol())) {
						InputSequence seq = ((rand.randInt(2) == 0) ? rowI
								: rowJ).getIS();
						seq.addInputSequence(cTable.getColSuffix(k));
						RivestSchapireStatsEntry stats = driver.learner.stats;
						try {
							applyOnDriver(seq);
						} catch (KnownTracesTree.InconsistencyException e) {
							LogManager
									.logConsole("Non-determinisn on h exhibithed by probabilistic method");
							stats.increaseSucceededProbabilisticSearch();
							throw e;
						}
						stats.increaseFailedProbabilisticSearch();
						super.resetDriver();
						break;
					}
				}
			}
		}
	}

	@Override
	protected OutputSequence applyOnDriver(InputSequence inSeq) {
		assert driver.isAfterReset();
		OutputSequence outSeq = super.applyOnDriver(inSeq);
		knownTraces.tryAndInsert(new LmTrace(inSeq, outSeq));
		return outSeq;
	}

	@Override
	protected void handleNewCounterExample(LmTrace ce) {
		knownTraces.tryAndInsert(ce);
	}

}
