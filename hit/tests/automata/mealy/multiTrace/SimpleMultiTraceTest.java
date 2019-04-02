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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;

import org.junit.jupiter.api.Test;

import learner.mealy.LmTrace;

public class SimpleMultiTraceTest extends SimpleMultiTrace {
	static class contractTest implements MultiTraceTest {

		@Override
		public MultiTrace create() {
			return new SimpleMultiTrace();
		}
	}

	@Test
	public void testTraceRecording() {
		LmTrace trace1 = new LmTrace("a", "b");
		trace1.append("c", "d");
		LmTrace trace2 = new LmTrace("e", "f");
		trace2.append("g", "h");
		recordIO("a", "b");
		assertEquals(getLastTrace(), new LmTrace("a", "b"));
		recordIO("c", "d");
		recordReset();
		recordTrace(trace2);
		recordReset();
		recordTrace(new LmTrace());
		recordReset();

		Iterator<LmTrace> it = iterator();
		assertEquals(it.next(), trace1);
		assertEquals(it.next(), trace2);
		assertEquals(it.next(), new LmTrace());
		assertEquals(it.next(), new LmTrace());

	}

}
