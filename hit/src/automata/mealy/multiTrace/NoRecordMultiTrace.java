package automata.mealy.multiTrace;

import learner.mealy.LmTrace;

/**
 * A {@link MultiTrace} which do not record symbols applied but only the number
 * of resets
 * 
 * @author Nicolas BREMOND
 *
 */
public class NoRecordMultiTrace implements MultiTrace {
	int resetNumber = 0;
	boolean isAfterRecordedReset = false;

	@Override
	public void recordIO(String input, String output) {
		isAfterRecordedReset = false;
	}

	@Override
	public void recordReset() {
		resetNumber++;
		isAfterRecordedReset = true;
	}

	@Override
	public int getResetNumber() {
		return resetNumber;
	}

	@Override
	public boolean isAfterRecordedReset() {
		return isAfterRecordedReset;
	}

	@Override
	public void recordTrace(LmTrace trace) {
		if (trace.size() > 0)
			isAfterRecordedReset = false;
	}

	@Override
	public boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}
}
