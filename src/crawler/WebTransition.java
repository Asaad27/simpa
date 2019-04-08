/********************************************************************************
 * Copyright (c) 2012,2019 Institut Polytechnique de Grenoble 
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
package crawler;

public class WebTransition {
	private int from;
	private int to;
	private WebInput by;

	public WebTransition(int from, int to, WebInput by) {
		super();
		this.from = from;
		this.to = to;
		this.by = by;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public WebInput getBy() {
		return by;
	}
	
	public void setBy(WebInput i) {
		this.by = i;
	}

	public String toString() {
		return from + " to " + to + " by input_" + by;
	}
}
