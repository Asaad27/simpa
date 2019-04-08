/********************************************************************************
 * Copyright (c) 2018,2019 Institut Polytechnique de Grenoble 
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
package tools;

import java.util.Random;

import main.simpa.Options;
import options.valueHolders.SeedHolder;

/**
 * This class is a temporary class for random utilities.
 * 
 * Anywhere this class is used, it should be replaced by a
 * {@link options.RandomOption} integrated into a main
 * {@link options.OptionTree} in order to let user choose the seed.
 * 
 * @author Nicolas BREMOND
 *
 */
public class StandaloneRandom extends SeedHolder {
	static Random seedProvider = null;

	static void setSeed(long s) {
		new StandaloneRandom();
		seedProvider.setSeed(s);
	}

	public StandaloneRandom() {
		super("");
		if (seedProvider == null) {
			seedProvider = new Random();
			seedProvider.setSeed(Options.SEED);
		}
		setValue(seedProvider.nextLong());
		initRandom();
	}
}
