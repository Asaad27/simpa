package learner.mealy.hW.dataManager;

import automata.mealy.GenericInputSequence;

public abstract class GenericHNDException extends RuntimeException {
	private static final long serialVersionUID = 4287088919594039959L;

	public abstract GenericInputSequence getNewH();

}
