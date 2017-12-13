package drivers;

/**
 * interface to indicate that the driver is able to generate all automaton for a
 * given number of states, inputs and outputs.
 * 
 */

public interface ExhaustiveGenerator {
	public class TooBigSeedException extends RuntimeException {
		private static final long serialVersionUID = 8349347884318216393L;
		long seed;

		public TooBigSeedException(long seed) {
			super("seed " + seed + " is too big to genreate an automata.");
			this.seed = seed;
		}
	};

	public class EndOfLoopException extends RuntimeException {
		private static final long serialVersionUID = -7568966387909496709L;

		public EndOfLoopException() {
			super("lopping over all automata is done");

		}
	};
}
