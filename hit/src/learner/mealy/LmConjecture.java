package learner.mealy;

import java.util.List;

import drivers.Driver;

public class LmConjecture extends automata.mealy.Mealy {
	private static final long serialVersionUID = -6920082057724492261L;
	private List<String> inputSymbols;

	public LmConjecture(Driver d) {
		super(d.getSystemName());
		this.inputSymbols = d.getInputSymbols();
	}

	public List<String> getInputSymbols() {
		return inputSymbols;
	}

}
