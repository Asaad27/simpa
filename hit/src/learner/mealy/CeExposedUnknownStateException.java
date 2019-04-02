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
package learner.mealy;

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
