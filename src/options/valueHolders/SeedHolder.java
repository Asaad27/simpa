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
package options.valueHolders;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import options.PercentageOption;
import tools.RandomGenerator;
import tools.loggers.LogManager;

public class SeedHolder extends SingleValueAutoHolder<Long, LongHolder>
		implements RandomGenerator {

	/**
	 * @param seedUse
	 *            a text describing what the seed is used for (not describing
	 *            the seed itself).
	 */
	public SeedHolder(String seedUse) {
		super(new LongHolder("Seed for " + seedUse,
				"Seed used to initialize a random source."
						+ " It can be manualy choosen to re-run a previous inference.",
				(long) 1));
		updateWithValue();
		baseHolder.setMinimum(Long.MIN_VALUE);
		baseHolder.setMaximum(Long.MAX_VALUE);
	}

	/**
	 * This seed is used to create seeds of each {@link RandomOption}. It might
	 * be used for deep debugging.
	 */
	static public final long MAIN_SEED;
	/**
	 * The {@link Random} source of seed for each {@link RandomOption}. It is
	 * initialize with {@link #MAIN_SEED}.
	 */
	static private final Random seedGenerator;
	static {
		MAIN_SEED = new Random().nextLong();
		seedGenerator = new Random();
		seedGenerator.setSeed(MAIN_SEED);
	}

	Random rand = null;

	/**
	 * initialize the Random. This must be called before starting to use the
	 * Random in order to set and record the seed.
	 */
	public void initRandom() {
		if (useAutoValue())
			setValue(seedGenerator.nextLong());
		rand = new Random();
		rand.setSeed(getSeed());
		LogManager.logInfo("Seed for " + getName() + " set to " + getSeed());
	}

	/**
	 * get the seed used at the last call to {@link #initRandom()}.
	 * 
	 * @return the seed which can be used to produce the same sequence of
	 *         random.
	 */
	@Override
	public long getSeed() {
		return getValue();
	}

	public RandomGenerator getRandomGenerator() {
		return this;
	}

	@Override
	public Random getRand() {
		assert rand != null : "rand must be initialized with a call to init()";
		return rand;
	}

	@Override
	public boolean randBoolWithPercent(PercentageOption percent) {
		return randBoolWithPercent(percent.getIntValue());
	}

	// the following method were taken from tools.Utils

	@Override
	public boolean randBoolWithPercent(int p) {
		return rand.nextInt(100) < p;
	}

	@Override
	public int randIntBetween(int a, int b) {
		if (a == b)
			return a;
		else if (a > b) {
			a -= b;
			b += a;
			a = b - a;
		}
		return rand.nextInt(b - a + 1) + a;
	}

	@Override
	public <T> T randIn(List<T> l) {
		if (l.isEmpty())
			return null;
		else
			return l.get(rand.nextInt(l.size()));
	}

	@Override
	public <T> T randIn(T l[]) {
		if (l.length == 0)
			return null;
		else
			return l[rand.nextInt(l.length)];
	}

	@Override
	public long randLong() {
		return rand.nextLong();
	}

	@Override
	public int randInt(int max) {
		return rand.nextInt(max);
	}

	@Override
	public <T> T randIn(Set<T> s) {
		List<T> l = new ArrayList<>(s);
		return randIn(l);
	}

	@Override
	public String randString() {
		return "random" + randInt(1000);
	}

	@Override
	public String randAlphaNumString(int size) {
		String charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder randomString = new StringBuilder();
		for (int i = 0; i < size; i++) {
			int index = rand.nextInt(charset.length());
			randomString.append(charset.charAt(index));
		}
		return randomString.toString();
	}

}
