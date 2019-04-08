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
package drivers.mealy.transparent;

import examples.mealy.LockerMealy;
import examples.mealy.LockerMealy.OnError;
import examples.mealy.LockerMealy.OutputPolicy;

public class EasyRandomLockerMealyDriver extends TransparentMealyDriver {
	public EasyRandomLockerMealyDriver(){
		super(LockerMealy.getRandomLockerMealy(OnError.STAY_IN_PLACE,
				OutputPolicy.UNLOCK_GOOD_BAD, null));
		automata.exportToDot();
	}
}
