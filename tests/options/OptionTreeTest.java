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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import tools.NullStream;
import options.automataOptions.AutomataChoice;

public class OptionTreeTest {
	/**
	 * assert that a tree can parse option and show output if an error occurs.
	 * 
	 * @param tree
	 *            the tree to test
	 * @param arguments
	 *            the arguments to parse.
	 */
	static void testParse(OptionTree tree, String arguments) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		boolean result = tree.parseArguments(arguments, new PrintStream(out));
		if (!result)
			System.out.println(out.toString());
		assertTrue(result, out.toString());
	}

	@Nested
	class ArgumentValue {
		AutomataChoice t = new AutomataChoice();

		String base = "--Mealy --algo=MLm --OT_shortest ";
		String sampleDriver = "drivers.mealy.transparent.RandomMealyDriver";
		String driver = "--Driver";

		void checkValue() {
			assertEquals(t.mealy, t.getSelectedItem());
		}

		@Test
		/**
		 * check that string used for test are correct.
		 */
		void checkTestStrings() {
			// argument must be needed
			assertFalse(t.parseArguments(base, new NullStream()));

			// argument and its value are valid
			t = new AutomataChoice();
			testParse(t, base + " " + driver + "=" + sampleDriver);
			checkValue();
		}

		@Test
		void testMissingValue() {
			assertFalse(
					t.parseArguments(base + " " + driver, new NullStream()));
		}

		@Test
		void testMissingArgument() {
			assertFalse(t.parseArguments(base + " " + sampleDriver,
					new NullStream()));
		}

		@Test
		void testValueWithSpace() {
			testParse(t, base + " " + driver + " " + sampleDriver);
			checkValue();
		}

		@Test
		void testValueWithEqual() {
			testParse(t, driver + "=" + sampleDriver + " " + base);
			checkValue();
		}
	}

	@Nested
	class FullTreeTest extends main.simpa.SIMPA {
		@Test
		void testHelpAssertions() {
			// this test is used to run assertions in the help method (for
			// instance checking that two options with same argument also have
			// the same help text).
			boolean ea = false;
			assert ea = true;
			assumeTrue(ea);
			getOptions().printAllHelp(new NullStream());
			getOptions().printHelp(new NullStream());
		}

		@Test
		void testExamples() {
			for (Sample s : samples) {
				testParse(getOptions(), s.cmd + " ");
			}
		}

		@Test
		void testInitialGuiOptions() {
			testParse(getOptions(), initialGuiOptions);
		}
	}

}
