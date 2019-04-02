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
package automata.mealy.multiTrace;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import learner.mealy.LmTrace;

public interface MultiTraceTest {
	abstract MultiTrace create();

	@Test
	default public void testIsAfterReset() {
		MultiTrace mt = create();
		mt.recordReset();
		assertTrue(mt.isAfterRecordedReset());
		mt.recordIO("", "");
		assertFalse(mt.isAfterRecordedReset());
		mt.recordReset();
		assertTrue(mt.isAfterRecordedReset());
		mt.recordTrace(new LmTrace());
		assertTrue(mt.isAfterRecordedReset());
		mt.recordTrace(new LmTrace("", ""));
		assertFalse(mt.isAfterRecordedReset());
	}

	@Test
	default public void testdoNotStartAfterReset() {
		MultiTrace mt = create();
		assertFalse(mt.isAfterRecordedReset());
	}

	@Test
	default public void testResetNb() {
		MultiTrace mt = create();
		assertEquals(0, mt.getResetNumber());
		mt.recordReset();
		assertEquals(1, mt.getResetNumber());
		mt.recordIO("", "");
		assertEquals(1, mt.getResetNumber());
		mt.recordReset();
		assertEquals(2, mt.getResetNumber());
	}

	@Test
	default public void testRepeatedReset() {
		MultiTrace mt = create();
		mt.recordReset();
		mt.recordReset();
		assertEquals(2, mt.getResetNumber());
		mt.recordIO("", "");
		mt.recordReset();
		mt.recordReset();
		assertEquals(4, mt.getResetNumber());
	}

}
