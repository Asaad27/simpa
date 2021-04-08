/********************************************************************************
 * Copyright (c) 2017,2019 Institut Polytechnique de Grenoble 
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ConjectureNotConnexException extends Exception {

	private static final long serialVersionUID = 9158571872132308078L;
	private List<FullyQualifiedState> reachedStates;
	private Set<FullyQualifiedState> notFullyKnownStates;

	public ConjectureNotConnexException(
			List<FullyQualifiedState> reachedStates,
			Set<FullyQualifiedState> notFullyKnownStates) {
		this.reachedStates = reachedStates;
		this.notFullyKnownStates = notFullyKnownStates;
	}

	public ConjectureNotConnexException(Collection<FullyQualifiedState> reachedStates) {
		this.reachedStates = new ArrayList<>(reachedStates);
	}

	public void setNotFullyKnownStates(Set<FullyQualifiedState> notFullyKnownStates) {
		this.notFullyKnownStates = notFullyKnownStates;
	}

	public String toString() {
		return "The infered automata seems to be not totaly connex : we reached "
				+ reachedStates + " but not " + notFullyKnownStates;
	}

}
