package learner.mealy.hW;

import automata.mealy.GenericInputSequence;
import automata.mealy.GenericInputSequence.GenericOutputSequence;
import automata.mealy.distinctionStruct.Characterization;

public class CeExposedUnknownStateException extends Exception {
	private static final long serialVersionUID = 4415414641251919120L;

	public final Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> characterization;

	public CeExposedUnknownStateException(
			Characterization<? extends GenericInputSequence, ? extends GenericOutputSequence> characterization) {
		this.characterization = characterization;
	}
}
