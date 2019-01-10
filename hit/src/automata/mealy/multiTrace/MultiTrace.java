package automata.mealy.multiTrace;

import learner.mealy.LmTrace;

/**
 * A structure to record traces with reset. The recording might not start by (or
 * after) a reset.
 * 
 * @see LmTrace
 * @author Nicolas BREMOND
 *
 */
public interface MultiTrace {
	/**
	 * Record a single execution
	 * 
	 * @param input
	 *            the input applied
	 * @param output
	 *            the output observed
	 */
	public abstract void recordIO(String input, String output);

	/**
	 * Record a trace. The trace can be empty. No reset is recorded before the
	 * trace, the trace will only be appended.
	 * 
	 * @param trace
	 *            the trace to record
	 */
	default public void recordTrace(LmTrace trace) {
		for (int i = 0; i < trace.size(); i++)
			recordIO(trace.getInput(i), trace.getOutput(i));
	}

	/**
	 * Record a reset. Note that no reset is recorded at the start of the
	 * MultiTrace if this method is not call first.
	 */
	public abstract void recordReset();

	/**
	 * 
	 * Get the number of reset recorded.
	 * 
	 * @return the number of reset recorded.
	 */
	public abstract int getResetNumber();

	/**
	 * Indicate whether the last record was a reset or a symbol. It return false
	 * on an empty object if no reset has been recorded first.
	 * 
	 * @return {@code true} if last record was a reset, {@code false} otherwise.
	 */
	public abstract boolean isAfterRecordedReset();

}
