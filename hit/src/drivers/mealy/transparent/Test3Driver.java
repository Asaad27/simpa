/********************************************************************************
 * Copyright (c) 2016,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Lingxiao WANG
 ********************************************************************************/
package drivers.mealy.transparent;

import examples.mealy.Test3Mealy;

public class Test3Driver extends TransparentMealyDriver {

	public Test3Driver() {
		super(Test3Mealy.getAutomata());
	}
}
