/********************************************************************************
 * Copyright (c) 2011,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 *     Nicolas BREMOND
 ********************************************************************************/
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

	/**
	 * Get the list of input symbols which can be applied to the System Under
	 * Inference.
	 * 
	 * Learning algorithms currently suppose that SUI are complete and thus,
	 * this value returned by this method should not depend on the state of the
	 * SUI.
	 * 
	 * @return the list of input which can be applied on SUI.
	 */
	public abstract List<String> getInputSymbols();

	/**
	 * Get the name of the System Under Inference.
	 * 
	 * @return the name of the system.
	 */
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

	/**
	 * Execute an input on the System Under Inference and return the output
	 * produced by the system.
	 * 
	 * @param input
	 *            the input to apply on SUI
	 * @return the output produced by SUI
	 */
	protected abstract O execute_implem(I input);

	public final void reset() {
		reset_implem();
		if (addtolog) {
			LogManager.logReset();
		}
		numberOfRequest++;
	}

	/**
	 * Reset the System Under Inference to its initial state. A driver used with
	 * no-reset algorithm will not use this method and thus, the implementation
	 * can simply be {@code throw new RuntimeException();}.
	 */
	protected abstract void reset_implem();

}
