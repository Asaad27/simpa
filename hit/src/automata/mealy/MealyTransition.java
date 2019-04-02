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
package automata.mealy;

import automata.State;
import automata.Transition;

public class MealyTransition extends Transition {
	private static final long serialVersionUID = 6719440643049324689L;

	protected String output;
	protected Mealy automata;

	public MealyTransition(Mealy automata, State s1, State s2, String input,
			String output) {
		super(s1, s2, input);
		this.output = output;
		this.automata = automata;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String symbol) {
		output = symbol;
	}
	
	public boolean isLoop(){
		return from.equals(to);
	}

	@Override
	public String toString() {
		return from + " to " + to + " by " + input + "/" + output;
	}

	public String toDot() {
		return from + " -> " + to + " [label="
				+ tools.GraphViz.id2DotAuto(input + " / " + output) + "];";
	}

	public String getName() {
		return from + "-" + to;
	}
	
	public int hashCode(){
		return (from + input).hashCode();
	}

	public boolean equals(MealyTransition t){
		return output.equals(t.output) && input.equals(t.input) &&
				from.equals(t.from) && to.equals(t.to);
	}
}
