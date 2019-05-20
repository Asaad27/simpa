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
package drivers.mealy.DTOracle;

import static org.junit.Assume.assumeFalse;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FakeTest implements drivers.mealy.DTOracle.Test {
	final int depth;

	public FakeTest(int depth) {
		this.depth = depth;
	}

	@Override
	public int getCost() {
		return depth;
	}

}

class SortedQueueTest extends SortedQueue<FakeTest> {

	@Test
	void testUniqStart() {
		insert(new FakeTest(2));
		insert(new FakeTest(4));
		insert(new FakeTest(5));
		insert(new FakeTest(3));
		assertUniq(2);
	}

	@Test
	void testUniqEnd() {
		insert(new FakeTest(2));
		insert(new FakeTest(0));
		insert(new FakeTest(5));
		insert(new FakeTest(-8));
		assertUniq(-8);
	}

	@Test
	void testUniqMiddle() {
		insert(new FakeTest(2));
		insert(new FakeTest(0));
		insert(new FakeTest(1));
		insert(new FakeTest(3));
		assertUniq(0);
	}

	@Test
	void testUniqAlone() {
		insert(new FakeTest(3));
		assertUniq(3);
	}

	void assertUniq(int value) {
		assertEquals(Integer.valueOf(value), cheapestCost());
		assertEquals(value, pollCheapest().depth);
		assertNotEquals(Integer.valueOf(value), cheapestCost());
	}

	@Test
	void testIsEmpty() {
		assertTrue(isEmpty());
		insert(new FakeTest(2));
		assertFalse(isEmpty());
		insert(new FakeTest(2));
		assertFalse(isEmpty());
		pollCheapest();
		assertFalse(isEmpty());
		pollCheapest();
		assertTrue(isEmpty());
		clear();
		assertTrue(isEmpty());
		insert(new FakeTest(2));
		assertFalse(isEmpty());
		clear();
		assertTrue(isEmpty());
	}

	@Test
	void testOrdered() {
		insert(new FakeTest(5));
		insert(new FakeTest(5));
		insert(new FakeTest(3));
		insert(new FakeTest(5));
		assertOrdered();
	}

	@Test
	void testOrdered2() {
		insert(new FakeTest(-1));
		insert(new FakeTest(5));
		insert(new FakeTest(3));
		insert(new FakeTest(7));
		insert(new FakeTest(7));
		assertOrdered();
	}

	void assertOrdered() {
		assumeFalse(isEmpty());
		Integer depth = cheapestCost();
		while (!isEmpty()) {
			assertTrue(cheapestCost() >= depth);
			depth = cheapestCost();
			FakeTest p = pollCheapest();
			assertTrue(p.depth == depth);
		}
	}

}
