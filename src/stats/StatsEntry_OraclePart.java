/********************************************************************************
 * Copyright (c) 2018,2019 Institut Polytechnique de Grenoble 
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
package stats;

import java.util.StringTokenizer;

import options.MultiArgChoiceOptionItem;
import options.learnerOptions.OracleOption;
import stats.attribute.Attribute;

public class StatsEntry_OraclePart {

	private int askedCE = 0;
	private String oracleUsed = "Unknown";
	private int traceLength = 0;
	private int resetNb = 0;
	private float duration = 0;

	private int lastTraceLength = 0;
	private int lastResetNb = 0;
	private float lastDuration = 0;

	public StatsEntry_OraclePart(StringTokenizer st,
			Attribute<?>[] csvAttributes) {
		for (int i = 0; i < csvAttributes.length; i++) {
			assert st.hasMoreTokens();
			String token = st.nextToken();
			if (csvAttributes[i] == Attribute.ASKED_COUNTER_EXAMPLE)
				askedCE = Integer.parseInt(token);
			if (csvAttributes[i] == Attribute.ORACLE_USED)
				oracleUsed = token;
			if (csvAttributes[i] == Attribute.ORACLE_DURATION)
				duration = Float.parseFloat(token);
			if (csvAttributes[i] == Attribute.ORACLE_TRACE_LENGTH)
				traceLength = Integer.parseInt(token);
		}
		assert !st.hasMoreTokens();
	}

	public StatsEntry_OraclePart(OracleOption oracle) {
		MultiArgChoiceOptionItem mode = oracle.getSelectedItem();
		if (mode == oracle.interactive)
			oracleUsed = "interactive";
		else if (mode == oracle.shortest)
			oracleUsed = "shortest";
		else if (mode == oracle.mrBean)
			oracleUsed = "MrBean";
		else if (mode == oracle.distinctionTreeBased)
			oracleUsed = "distinctionTree";
		else
			throw new RuntimeException("must be implemented");
	}

	/**
	 * Record a call to oracle. The duration and trace length of last oracle are
	 * not added to the total.
	 * 
	 * @param thisTraceLength
	 *            the length of trace used by this call to oracle
	 * @param thisDuration
	 *            the duration of this call to oracle
	 */
	public void addOracleCall(int thisTraceLength, int thisResetNumber,
			float thisDuration) {
		askedCE++;
		traceLength += lastTraceLength;
		resetNb += lastResetNb;
		duration += lastDuration;
		lastTraceLength = thisTraceLength;
		lastResetNb = thisResetNumber;
		lastDuration = thisDuration;
	}

	public int getAskedCE() {
		return askedCE;
	}

	public String getName() {
		return oracleUsed;
	}

	public int getTraceLength() {
		return traceLength;
	}

	public int getResetNb() {
		return resetNb;
	}

	public float getDuration() {
		return duration;
	}

	public int getLastTraceLength() {
		return lastTraceLength;
	}

	public float getLastDuration() {
		return lastDuration;
	}

	public int getLastResetNb() {
		return lastResetNb;
	}

}
