/********************************************************************************
 * Copyright (c) 2019 Institut Polytechnique de Grenoble 
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
package options.driverOptions;

import options.IntegerOption;

public class TimeoutOption extends IntegerOption {
	public TimeoutOption(int default_ms) {
		super("--Dtimeout", "timeout to decide quiescence output (ms)",
				"When an input is sent to the System Under Learning, we do not know if we will get an output or not."
						+ " After this timeout (in ms), we consider that we will not have an answer.",
				default_ms);
	}

	/**
	 * To be compliant with future change of format, use {@link #getValue_ms() }
	 * instead.
	 * 
	 * @deprecated because it does not specified the unit of timeout. Use
	 *             {@link #getValue_ms()} instead.
	 */
	@Override
	@Deprecated
	public Integer getValue() {
		return super.getValue();
	}

	/**
	 * Get the timeout selected in milliseconds.
	 * 
	 * @return the timeout selected as milliseconds
	 */
	public int getValue_ms() {
		return super.getValue();
	}
}
