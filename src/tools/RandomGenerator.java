/********************************************************************************
 * Copyright (c) 2019 Institut Polytechnique de Grenoble 
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

import java.util.List;
import java.util.Random;
import java.util.Set;

import options.PercentageOption;

public interface RandomGenerator {

	/**
	 * get the seed used at the last call to {@link #initRandom()}.
	 * 
	 * @return the seed which can be used to produce the same sequence of
	 *         random.
	 */
	public long getSeed();

	public Random getRand();

	public boolean randBoolWithPercent(PercentageOption percent);

	// the following method were taken from tools.Utils

	public boolean randBoolWithPercent(int p);

	public int randIntBetween(int a, int b);

	public <T> T randIn(List<T> l);

	public <T> T randIn(T l[]);

	public long randLong();

	public int randInt(int max);

	public <T> T randIn(Set<T> s);

	public String randString();

	public String randAlphaNumString(int size);

}
