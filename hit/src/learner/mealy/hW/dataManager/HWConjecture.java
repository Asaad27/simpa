package learner.mealy.hW.dataManager;

import java.util.List;

import automata.State;
import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.distinctionStruct.Characterization;
import drivers.Driver;
import learner.mealy.CeExposedUnknownStateException;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import tools.loggers.LogManager;

public class HWConjecture extends LmConjecture {
	private static final long serialVersionUID = 1829831676465011460L;

	final SimplifiedDataManager dataManager;

	public HWConjecture(Driver d, SimplifiedDataManager dm) {
		super(d);
		dataManager = dm;
	}

	@Override
	public State searchInitialState(List<LmTrace> appliedSequences)
			throws CeExposedUnknownStateException {
		State s = getInitialState();
		if (s != null)
			return s;
		Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> characterization = dataManager
				.getInitialCharacterization();
		for (GenericInputSequence w : characterization.unknownPrints()) {
			dataManager.reset();
			GenericOutputSequence r = dataManager.apply(w);
			if (Options.getLogLevel() != LogLevel.LOW)
				LogManager.logInfo(
						"characterizing initial state with sequence ", w);
			characterization.addPrint(w, r);
		}
		if (!dataManager.hasState(characterization))
			throw new CeExposedUnknownStateException(characterization);
		dataManager.getInitialCharacterization();// update initial state in
													// dataManager
		dataManager.reset();
		assert (dataManager.getCurrentState() != null);
		return dataManager.getCurrentState().getState();
	}
}
