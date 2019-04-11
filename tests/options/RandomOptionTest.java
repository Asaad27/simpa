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
package options;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import options.valueHolders.SeedHolder;

public class RandomOptionTest extends RandomOption {

	public RandomOptionTest() {
		super("--testSeed", "testing purpose");
	}

	@Test
	public void testHasDefaultValue() {
		OptionTreeTest.testParse(this, "");
	}

	@Test
	public void testDefaultValueIsAuto() {
		RandomOptionTest r1 = new RandomOptionTest();
		RandomOptionTest r2 = new RandomOptionTest();
		OptionTreeTest.testParse(r1, "");
		OptionTreeTest.testParse(r2, "");
		testSeedsAreDifferents(r1, r2);
	}

	@Test
	public void testInitialValueIsAuto() {
		RandomOptionTest r1 = new RandomOptionTest();
		RandomOptionTest r2 = new RandomOptionTest();
		testSeedsAreDifferents(r1, r2);
	}

	@Test
	public void testSeedIsInitialized() {
		RandomOptionTest r1 = new RandomOptionTest();
		RandomOptionTest r2 = new RandomOptionTest();
		OptionTreeTest.testParse(r1, "--testSeed=" + SeedHolder.AUTO_VALUE);
		OptionTreeTest.testParse(r2, "--testSeed=" + SeedHolder.AUTO_VALUE);
		testSeedsAreDifferents(r1, r2);
	}

	public static void testSeedsAreDifferents(RandomOption r1,
			RandomOption r2) {
		r1.getValueHolder().initRandom();
		r2.getValueHolder().initRandom();
		String errormsg = "In extremely rare cases, this test might fail with a false positive";
		assertNotEquals(r1.getRand().getSeed(), r2.getRand().getSeed(),
				errormsg);
		assertNotEquals(r1.getRand().randLong(), r2.getRand().randLong(),
				errormsg);
	}

	@Test
	public void testSeedIsUsed() {
		long seed = 58623547;
		OptionTreeTest.testParse(this, "--testSeed=" + seed);
		getValueHolder().initRandom();
		assertEquals(seed, getRand().getSeed());
	}

	@Test
	public void testSeedIsReproductible() {
		OptionTreeTest.testParse(this, "--testSeed=" + SeedHolder.AUTO_VALUE);
		getValueHolder().initRandom();
		long seed = getRand().getSeed();
		long firstLong = getRand().randLong();

		OptionTreeTest.testParse(this, "--testSeed=" + seed);
		getValueHolder().initRandom();
		assertEquals(seed, getRand().getSeed());
		assertEquals(firstLong, getRand().randLong());
		getValueHolder().setValue(seed + 1);
		testSeedsAreDifferents(this, new RandomOptionTest());

		getValueHolder().setValue(seed);
		getValueHolder().initRandom();
		assertEquals(seed, getRand().getSeed());
		assertEquals(firstLong, getRand().randLong());
	}
}
