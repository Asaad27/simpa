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
package learner.mealy.hW.dataManager;

public class LocalizedHZXWSequence {
	public final HZXWSequence sequence;
	public int transferPosition = 0;
	public FullyQualifiedState endOfTransferState = null;

	public LocalizedHZXWSequence(HZXWSequence sequence) {
		this.sequence = sequence;
	}

	public boolean equals(Object o) {
		if (o instanceof LocalizedHZXWSequence)
			return equals((LocalizedHZXWSequence) o);
		return false;
	}

	public boolean equals(LocalizedHZXWSequence o) {
		assert endOfTransferState == null
				|| endOfTransferState.equals(o.endOfTransferState);
		return sequence.equals(o.sequence)
				&& (transferPosition == o.transferPosition);
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}
}
