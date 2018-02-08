package learner.mealy.hW.dataManager;

import learner.mealy.LmTrace;

public class InconsistancyWithConjectureException extends RuntimeException {

	private static final long serialVersionUID = 8658553725594257279L;
	private LmTrace conjecture;
	private LmTrace driver;

	public InconsistancyWithConjectureException(LmTrace conjecture,
			LmTrace driver) {
		this.conjecture = conjecture;
		this.driver = driver;
	}

	public String toString() {
		return "Inconsistancy between trace and conjecture. We expected "
				+ conjecture + " and we get " + driver;
	}

}
