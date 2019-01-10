package automata.mealy.multiTrace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import learner.mealy.LmTrace;

/**
 * A simple implementation of {@link MultiTrace}.
 * 
 * @author Nicolas BREMOND
 *
 */
public class SimpleMultiTrace implements MultiTrace, Iterable<LmTrace> {
	private final List<LmTrace> traces;
	private LmTrace lastTrace;

	public SimpleMultiTrace() {
		traces = new ArrayList<>();
		lastTrace = new LmTrace();
		traces.add(lastTrace);
	}

	@Override
	public void recordIO(String input, String output) {
		lastTrace.append(input, output);
	}

	@Override
	public void recordReset() {
		lastTrace = new LmTrace();
		traces.add(lastTrace);
	}

	@Override
	public Iterator<LmTrace> iterator() {
		return Collections.unmodifiableList(traces).iterator();
	}

	@Override
	public int getResetNumber() {
		return traces.size() - 1;
	}

	@Override
	public boolean isAfterRecordedReset() {
		return traces.size() > 1 && lastTrace.size() == 0;
	}

	public LmTrace getLastTrace() {
		return lastTrace;
	}

	@Override
	public void recordTrace(LmTrace trace) {
		lastTrace.append(trace);
	}

}
