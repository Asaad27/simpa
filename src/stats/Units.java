/********************************************************************************
 * Copyright (c) 2015,2019 Institut Polytechnique de Grenoble 
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
package stats;

public class Units {
	public final static Units PERCENT = new Units("%");
	public final static Units SYMBOLS = new Units("symbols");
	public final static Units SEQUENCES = new Units("sequences");
	public final static Units STATES = new Units("states");
	public final static Units TRANSITIONS = new Units("transitions");
	public final static Units NODES = new Units("nodes");
	public final static Units FUNCTION_CALL = new Units("calls");
	public final static Units SECONDS = new Units("s");
	public final static Units NO_UNITS = new Units("");
	public final static Units BOOLEAN = new Units("");
	public static final Units BYTE = new Units("B");
	public static final Units LEARNER = new Units("LEARNER");
	private String symbol;

	public Units(String symbol){
		this.symbol = symbol; 
	}

	public String getSymbol(){
		return symbol;
	}

	public String toString(){
		return getSymbol();
	}
}
