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
 *     Nicolas BREMOND
 ********************************************************************************/
package drivers.mealy;

import java.util.ArrayList;
import java.util.List;

import automata.mealy.InputSequence;
import examples.mealy.ZQPaper0709;

public class ZQPaper0709Driver extends AutomatonMealyDriver {

	public ZQPaper0709Driver() {
		super(ZQPaper0709.getAutomata());
	}

	/**
	 * This is a legacy method, it is unused I left it here because those CE
	 * might be important.
	 */
	protected List<InputSequence> getForcedCE() {
		List<InputSequence> seq = new ArrayList<InputSequence>();

		InputSequence ce = new InputSequence();
		ce.addInput("a");
		ce.addInput("b");
		ce.addInput("a");
		ce.addInput("a");
		seq.add(ce);

		ce = new InputSequence();
		ce.addInput("a");
		ce.addInput("a");
		ce.addInput("c");
		ce.addInput("c");
		seq.add(ce);

		return seq;
	}
}
