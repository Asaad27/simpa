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
 ********************************************************************************/
package learner.mealy.table;

import main.simpa.Options;
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
