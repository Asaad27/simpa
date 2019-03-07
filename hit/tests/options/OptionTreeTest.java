package options;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import tools.NullStream;
import options.automataOptions.AutomataChoice;

public class OptionTreeTest {

	@Nested
	class ArgumentValue {
		AutomataChoice t = new AutomataChoice();

		String base = "--mealy --lm --shortestCE ";
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

}
