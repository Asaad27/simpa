package drivers;

import java.util.List;

import tools.loggers.LogManager;

public abstract class Driver<I, O> {
	private int numberOfRequest = 0;
	private int numberOfAtomicRequest = 0;
	private long start = 0;
	public long duration = 0;

	public enum DriverType {
		NONE, EFSM, MEALY, DFA, SCAN
	};

	public DriverType type = DriverType.NONE;
	protected boolean addtolog = true;

	public Driver() {
		start = System.nanoTime();
	}

	public void stopLog() {
		addtolog = false;
	}

	public void startLog() {
		addtolog = true;
	}

	public abstract List<String> getInputSymbols();

	public abstract String getSystemName();

	public int getNumberOfRequest() {
		return numberOfRequest;
	}

	public int getNumberOfAtomicRequest() {
		return numberOfAtomicRequest;
	}


	public abstract boolean isCounterExample(Object ce, Object conjecture);

	public void logStats() {
		LogManager.logLine();
		duration = System.nanoTime() - start;
		LogManager.logStat("Duration : " + ((float) duration / 1000000000)
				+ " seconds");
		LogManager.logStat("Number of requests : " + numberOfRequest);
		LogManager.logStat("Average request length : "
				+ ((float) numberOfAtomicRequest / numberOfRequest) + "\n");
		LogManager.logLine();
	}
	
	protected abstract void logRequest(I input, O output);

	public final O execute(I input) {
		O output = execute_implem(input);
		numberOfAtomicRequest++;
		if (addtolog)
			logRequest(input, output);
		return output;
	}

	protected abstract O execute_implem(I input);

	public final void reset() {
		reset_implem();
		if (addtolog) {
			LogManager.logReset();
		}
		numberOfRequest++;
	}

	protected abstract void reset_implem();

}
