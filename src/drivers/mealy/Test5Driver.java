/********************************************************************************
 * Copyright (c) 2017,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Lingxiao WANG
 *     Nicolas BREMOND
 ********************************************************************************/
package drivers.mealy;

 import examples.mealy.Test5Mealy;

public class Test5Driver extends AutomatonMealyDriver {

	public Test5Driver() {
		super(Test5Mealy.getAutomata());
	}
}
