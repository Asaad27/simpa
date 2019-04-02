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
package learner.mealy.hW.dataManager;

import automata.State;
import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.distinctionStruct.Characterization;
import automata.mealy.multiTrace.MultiTrace;
import drivers.mealy.MealyDriver;
import learner.mealy.CeExposedUnknownStateException;
import learner.mealy.LmConjecture;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.loggers.LogManager;

public class HWConjecture extends LmConjecture {
	private static final long serialVersionUID = 1829831676465011460L;

	final SimplifiedDataManager dataManager;

	public HWConjecture(MealyDriver d, SimplifiedDataManager dm) {
		super(d);
		dataManager = dm;
	}

	@Override
	public State searchInitialState(MultiTrace appliedSequences)
			throws CeExposedUnknownStateException {
		State s = getInitialState();
		if (s != null)
			return s;
		Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> characterization = dataManager
				.getInitialCharacterization();
		for (GenericInputSequence w : characterization.unknownPrints()) {
			driver.reset();
			appliedSequences.recordReset();
			GenericOutputSequence r = driver.execute(w);
			appliedSequences.recordTrace(w.buildTrace(r));
			if (Options.getLogLevel() != LogLevel.LOW)
				LogManager.logInfo(
						"characterizing initial state with sequence ", w);
			characterization.addPrint(w, r);
		}
		if (!dataManager.hasState(characterization))
			throw new CeExposedUnknownStateException(characterization);
		dataManager.getInitialCharacterization();// update initial state in
													// dataManager
		assert (dataManager.getInitialState() != null);
		return dataManager.getInitialState().getState();
	}
}
