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
package automata;

import java.io.Serializable;

public abstract class Transition implements Serializable {
	private static final long serialVersionUID = -7954062378513113845L;

	protected State from;
	protected State to;
	protected String input;

	public Transition(State from, State to, String input) {
		this.from = from;
		this.to = to;
		this.input = input;
	}

	public State getFrom() {
		return from;
	}

	public State getTo() {
		return to;
	}

	public String getInput() {
		return input;
	}

	public String toDot() {
		return from + " -> " + to + "[label=\"" + input + "\"];";
	}

	@Override
	public String toString() {
		return from + " to " + to + " by " + input;
	}
}
