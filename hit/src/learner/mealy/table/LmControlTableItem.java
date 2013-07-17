package learner.mealy.table;

import main.Options;
import automata.mealy.Mealy;

public class LmControlTableItem {
	private String outputSymbol;

	public LmControlTableItem(String outputSymbol) {
		this.outputSymbol = outputSymbol;
	}

	public String getOutputSymbol() {
		return outputSymbol;
	}

	public boolean isOmegaSymbol() {
		return outputSymbol.length() == 0;
	}

	@Override
	public String toString() {
		return outputSymbol.equals(Mealy.OMEGA) ? Options.SYMBOL_OMEGA_UP
				: outputSymbol;
	}
}
