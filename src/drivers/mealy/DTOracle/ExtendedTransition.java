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
package drivers.mealy.DTOracle;

public class ExtendedTransition<T extends Test> {
	final public String input;
	final public String output;
	final public ExtendedState<T> from;
	final public ExtendedState<T> to;

	public ExtendedTransition(String input, String output,
			ExtendedState<T> from, ExtendedState<T> to) {
		assert input != null && output != null && from != null && to != null;
		this.input = input;
		this.output = output;
		this.from = from;
		this.to = to;
	}

}
