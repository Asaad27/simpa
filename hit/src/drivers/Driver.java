package drivers;

import java.util.List;

import tools.loggers.LogManager;
import automata.Automata;

public abstract class Driver {
	public int numberOfRequest = 0;
	public int numberOfAtomicRequest = 0;
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

	public abstract Object getCounterExample(Automata a);

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
	
	public void reset() {
		if (addtolog) {
			LogManager.logReset();
		}
		numberOfRequest++;
	}
}
