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
 *     Nicolas BREMOND
 *     Roland GROZ
 *     Lingxiao WANG
 ********************************************************************************/
package drivers.mealy;

import automata.State;
import automata.mealy.multiTrace.MultiTrace;
import learner.mealy.LmConjecture;

import java.util.*;

public abstract class CompleteMealyDriver extends PartialMealyDriver {


	//	public List<String> getUndefinedInputs() {
//		return getInputSymbols().stream()
//				.filter(not(this::isDefined))
//				.collect(toList());
//	}

	public CompleteMealyDriver(String name) {
		super(name);
		type = DriverType.MEALY;
	}

	@Override
	public List<String> getDefinedInputs() {
		return getInputSymbols();
	}

	public abstract List<String> getInputSymbols();


}
