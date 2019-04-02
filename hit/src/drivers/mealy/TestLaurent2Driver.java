/********************************************************************************
 * Copyright (c) 2014,2019 Institut Polytechnique de Grenoble 
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
import examples.mealy.TestLaurent2;

public class TestLaurent2Driver extends AutomatonMealyDriver {

	public TestLaurent2Driver() {
		super(TestLaurent2.getAutomata());
	}
	
	/**
	 * This is a legacy method, it is unused I left it here because those CE
	 * might be important.
	 */
	protected List<InputSequence> getForcedCE() {
		List<InputSequence> ces = new ArrayList<InputSequence>();
		InputSequence seq1 = new InputSequence();
		seq1.addInput("c");
		seq1.addInput("c");
		seq1.addInput("c");
		seq1.addInput("b");
		seq1.addInput("b");
		ces.add(seq1);
		return ces;
	}
}
