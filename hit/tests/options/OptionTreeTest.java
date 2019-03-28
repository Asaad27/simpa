package options;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import tools.NullStream;
import options.automataOptions.AutomataChoice;

public class OptionTreeTest {

	@Nested
	class ArgumentValue {
		AutomataChoice t = new AutomataChoice();

		String base = "--mealy --algo=MLm --shortestCE ";
		String sampleDriver = "drivers.mealy.transparent.RandomMealyDriver";
		String driver = "--driver";

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
			assertTrue(
					t.parseArguments(base + " " + driver + "=" + sampleDriver,
							new NullStream()));
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
			assertTrue(
					t.parseArguments(base + " " + driver + " " + sampleDriver,
							new NullStream()));
			checkValue();
		}

		@Test
		void testValueWithEqual() {
			assertTrue(
					t.parseArguments(driver + "=" + sampleDriver + " " + base,
							new NullStream()));
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
	}

}
