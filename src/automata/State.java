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
 ********************************************************************************/
package automata;

import java.io.Serializable;

/**
 * This class represent a state of an automaton.
 */
public class State implements Serializable {
	private static final long serialVersionUID = 3191363945864393433L;

	private final String name;
	private Boolean initial;
	private Boolean flag;

	public State(String name, Boolean initial) {
		this.name = name;
		this.initial = initial;
		this.flag = false;
	}

	/**
	 * @deprecated by Nicolas BREMOND because states does not always have a name
	 *             with a number after one letter (for instance states from dot
	 *             files).
	 */
	@Deprecated
	public int getId() {
		return Integer.parseInt(name.substring(1));
	}

	public void cleanMark() {
		flag = false;
	}

	public void mark() {
		flag = true;
	}

	public boolean isMarked() {
		return flag;
	}

	/**
	 * @return the name of this state
	 */
	public String getName() {
		return name;
	}

	/**
	 * Indicate whether this state is marked as initial
	 * 
	 * @return true if this state is marked as initial.
	 */
	public Boolean isInitial() {
		return initial;
	}

	public boolean equals(Object comp) {
		if (this == comp)
			return true;
		if (!(comp instanceof State))
			return false;
		State to = (State) comp;
		return ((name.equals(to.name)) && (initial.equals(to.initial)));
	}

	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * change whether this state is marked as initial or not.
	 * 
	 * @param isInitial
	 *            the new status of this state.
	 * @warning it is really unlikely that you should use this method. see
	 *          {@link automata.Automata#setInitialState(State)} instead.
	 * @see automata.Automata#setInitialState(State)
	 */
	protected void setInitial(boolean isInitial){
		initial = isInitial;
	}
}
