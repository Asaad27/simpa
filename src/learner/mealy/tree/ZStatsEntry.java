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
package learner.mealy.tree;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import automata.mealy.MealyTransition;
import drivers.mealy.CompleteMealyDriver;
import learner.mealy.LmConjecture;
import options.learnerOptions.OracleOption;
import options.valueHolders.SeedHolder;
import stats.GraphGenerator;
import stats.StatsEntry;
import stats.StatsEntry_OraclePart;
import stats.StatsSet;
import stats.attribute.Attribute;

public class ZStatsEntry extends StatsEntry {
	public static final Attribute<Integer> TRACE_LENGTH = Attribute.TRACE_LENGTH;
	public static final Attribute<Integer> INPUT_SYMBOLS = Attribute.INPUT_SYMBOLS;
	public static final Attribute<Integer> OUTPUT_SYMBOLS = Attribute.OUTPUT_SYMBOLS;
	public static final Attribute<Integer> STATE_NUMBER = Attribute.STATE_NUMBER;
	public static final Attribute<Integer> LOOP_RATIO = Attribute.LOOP_RATIO;
	public static final Attribute<String> AUTOMATA = Attribute.AUTOMATA;
	public static final Attribute<Float> DURATION = Attribute.DURATION;
	public static final Attribute<Long> SEED = Attribute.SEED;
	public final static Attribute<Integer> ASKED_COUNTER_EXAMPLE = Attribute.ASKED_COUNTER_EXAMPLE;
	public final static Attribute<String> ORACLE_USED = Attribute.ORACLE_USED;
	public final static Attribute<Integer> ORACLE_TRACE_LENGTH = Attribute.ORACLE_TRACE_LENGTH;
	public final static Attribute<Float> ORACLE_DURATION = Attribute.ORACLE_DURATION;
	public final static Attribute<Integer> RESET_NB = Attribute.RESET_CALL_NB;
	public final static Attribute<Integer> ORACLE_RESET_NB = Attribute.ORACLE_RESET_NB;

	private static Attribute<?>[] attributes = new Attribute<?>[]{
			TRACE_LENGTH,
			INPUT_SYMBOLS,
			OUTPUT_SYMBOLS,
			STATE_NUMBER,
			LOOP_RATIO,
			AUTOMATA,
			DURATION,
			SEED,
			ASKED_COUNTER_EXAMPLE,
			ORACLE_USED,
			ORACLE_TRACE_LENGTH,
			ORACLE_DURATION,
			RESET_NB,
			ORACLE_RESET_NB,
	};
	
	public static String getCSVHeader_s() {
		return makeCSVHeader(attributes);
	}

	/**
	 * a static version of {@link#getAttributes}
	 * 
	 * @return the attributes of this class;
	 */
	public static Attribute<?>[] getAttributes_s() {
		return attributes;
	}

	protected Attribute<?>[] getAttributesIntern() {
		return attributes;
	}

	private int traceLength = 0;
	private int inputSymbols;
	private int outputSymbols;
	private int statesNumber;
	private int loopTransitionPercentage;
	private String automata;
	private float duration;
	private long seed;
	private int resetNb;

	private final StatsEntry_OraclePart oracle;

	public StatsEntry_OraclePart getOracle() {
		return oracle;
	}

	/**
	 * rebuild a StatsEntry object from a CSV line
	 * 
	 * @param line
	 *            the line to parse
	 */
	public ZStatsEntry(String line) {
		StringTokenizer st = new StringTokenizer(line, ",");
		traceLength = Integer.parseInt(st.nextToken());
		inputSymbols = Integer.parseInt(st.nextToken());
		outputSymbols = Integer.parseInt(st.nextToken());
		statesNumber = Integer.parseInt(st.nextToken());
		loopTransitionPercentage = Integer.parseInt(st.nextToken());
		automata = st.nextToken();
		duration = Float.parseFloat(st.nextToken());
		seed = Long.parseLong(st.nextToken());
		st.nextToken();// CE_NB
		st.nextToken();// ORACLE_USED
		st.nextToken();// ORACLE_TRACE_LENGTH
		st.nextToken();// ORACLE_DURATION
		resetNb = Integer.parseInt(st.nextToken());
		st.nextToken();//ORACLE_RESET_NB

		st = new StringTokenizer(line, ",");
		oracle = new StatsEntry_OraclePart(st, getAttributes());
	}

	public ZStatsEntry(CompleteMealyDriver d, OracleOption oracleOptions) {
		this.inputSymbols = d.getInputSymbols().size();
		this.automata = d.getSystemName();
		this.seed = SeedHolder.getMainSeed();
		oracle = new StatsEntry_OraclePart(oracleOptions);
	}

	/**
	 * Record data available after inference for statistics.
	 * 
	 * @param conjecture
	 *            the conjecture built.
	 * @param duration
	 *            the total duration of inference (duration of last oracle will
	 *            be removed);
	 * @param traceLength
	 *            the total trace length of inference (trace length of last
	 *            oracle will be removed)
	 * @param resets
	 *            the total number of reset for inference. (reset used by last
	 *            oracle will be removed).
	 */
	public void finalUpdate(LmConjecture conjecture, float duration,
			int traceLength, int resets) {
		updateWithConjecture(conjecture);
		this.duration = duration - oracle.getLastDuration();
		this.traceLength = traceLength - oracle.getLastTraceLength();
		this.resetNb = resets - oracle.getLastResetNb();
	}

	public void updateWithConjecture(LmConjecture conjecture) {
		statesNumber = conjecture.getStateCount();
		int loopTransitions = 0;
		Set<String> outputSymbols = new HashSet<>();
		for (MealyTransition t : conjecture.getTransitions()) {
			outputSymbols.add(t.getOutput());
			if (t.getTo() == t.getFrom())
				loopTransitions++;
		}
		loopTransitionPercentage = ((100 * loopTransitions)
				/ conjecture.getTransitionCount());
		this.outputSymbols = outputSymbols.size();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Comparable<T>> T getStaticAttribute(Attribute<T> a) {
		if (a == TRACE_LENGTH)
			return (T) Integer.valueOf(traceLength);
		if (a == INPUT_SYMBOLS)
			return (T) Integer.valueOf(inputSymbols);
		if (a == OUTPUT_SYMBOLS)
			return (T) Integer.valueOf(outputSymbols);
		if (a == STATE_NUMBER)
			return (T) Integer.valueOf(statesNumber);
		if (a == LOOP_RATIO)
			return (T) Integer.valueOf(loopTransitionPercentage);
		if (a == AUTOMATA)
			return (T) automata;
		if (a == DURATION)
			return (T) Float.valueOf(duration);
		if (a == SEED)
			return (T) Long.valueOf(seed);
		if (a == ASKED_COUNTER_EXAMPLE)
			return (T) Integer.valueOf(getOracle().getAskedCE());
		if (a == ORACLE_USED)
			return (T) getOracle().getName();
		if (a == ORACLE_TRACE_LENGTH)
			return (T) Integer.valueOf(getOracle().getTraceLength());
		if (a == ORACLE_DURATION)
			return (T) Float.valueOf(getOracle().getDuration());
		if (a == RESET_NB)
			return (T) Integer.valueOf(resetNb);
		if (a == ORACLE_RESET_NB)
			return (T) Integer.valueOf(oracle.getResetNb());
		throw new RuntimeException("unspecified attribute for this stats\n(no "+a.getName()+" in "+this.getClass()+")");

	}

	public <T extends Comparable<T>> Float getFloatValue(Attribute<T> a) {
		if (
				a == TRACE_LENGTH ||
				a == INPUT_SYMBOLS ||
				a == OUTPUT_SYMBOLS ||
				a == STATE_NUMBER ||
				a == LOOP_RATIO ||
				a == ASKED_COUNTER_EXAMPLE||
				a == RESET_NB ||
				a == ORACLE_RESET_NB ||
				a == ORACLE_TRACE_LENGTH)
			return ((Integer) get(a)).floatValue();
		if (a == SEED)
			return ((Long) get(a)).floatValue();
		if (a == DURATION || a == ORACLE_DURATION)
			return (Float) get(a);
		throw new RuntimeException(
				a.getName() + " is not available or cannot be cast to float");

	}

	@Override
	public GraphGenerator getDefaultsGraphGenerator() {
		return new GraphGenerator() {
			@Override
			public void generate(StatsSet s) {
			}
		};
	}
}
