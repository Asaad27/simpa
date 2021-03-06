/********************************************************************************
 * Copyright (c) 2015,2019 Institut Polytechnique de Grenoble 
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
package learner.mealy.localizerBased;

import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import automata.mealy.InputSequence;
import automata.mealy.MealyTransition;
import drivers.mealy.MealyDriver;
import learner.mealy.LmConjecture;
import stats.GraphGenerator;
import stats.StatsEntry;
import stats.attribute.Attribute;

public class LocalizerBasedStatsEntry extends StatsEntry {
	public static final Attribute<Integer>W_SIZE = Attribute.W_SIZE;
	public static final Attribute<Integer>W1_LENGTH = Attribute.W1_LENGTH;
	public static final Attribute<Integer>LOCALIZER_CALL_NB = Attribute.LOCALIZER_CALL_NB;
	public static final Attribute<Integer>LOCALIZER_SEQUENCE_LENGTH = Attribute.LOCALIZER_SEQUENCE_LENGTH;
	public static final Attribute<Integer>TRACE_LENGTH = Attribute.TRACE_LENGTH;
	public static final Attribute<Integer>INPUT_SYMBOLS = Attribute.INPUT_SYMBOLS;
	public static final Attribute<Integer>OUTPUT_SYMBOLS = Attribute.OUTPUT_SYMBOLS;
	public static final Attribute<Integer>STATE_NUMBER = Attribute.STATE_NUMBER;
	public static final Attribute<Integer>STATE_NUMBER_BOUND = Attribute.STATE_NUMBER_BOUND;
	public static final Attribute<Integer>STATE_BOUND_OFFSET = Attribute.STATE_BOUND_OFFSET;
	public static final Attribute<Integer>LOOP_RATIO = Attribute.LOOP_RATIO;
	public static final Attribute<String> AUTOMATA = Attribute.AUTOMATA;
	public static final Attribute<Float> DURATION = Attribute.DURATION;
	public static final Attribute<Integer>MEMORY = Attribute.MEMORY;
	public static final Attribute<Boolean>WITH_SPEEDUP = Attribute.WITH_SPEEDUP;
	
	private static Attribute<?>[] attributes = new Attribute<?>[]{
			W_SIZE,
			W1_LENGTH,
			LOCALIZER_CALL_NB,
			LOCALIZER_SEQUENCE_LENGTH,
			TRACE_LENGTH,
			INPUT_SYMBOLS,
			OUTPUT_SYMBOLS,
			STATE_NUMBER,
			STATE_NUMBER_BOUND,
			STATE_BOUND_OFFSET,
			LOOP_RATIO,
			AUTOMATA,
			DURATION,
			MEMORY,
			WITH_SPEEDUP,
	};
	
	public static String getCSVHeader_s(){
		return makeCSVHeader(attributes);
	}
	
	/**
	 * a static version of {@link#getAttributes}
	 * @return the attributes of this class;
	 */
	public static Attribute<?>[] getAttributes_s() {
		return attributes;
	}	
	
	@Override
	protected Attribute<?>[] getAttributesIntern() {
		return attributes;
	}

	private int WSize;
	private int w1Length;
	private int localizeCallNb = 0;
	private int localizeSequenceLength;
	private int traceLength = 0;
	private int inputSymbols;
	private int outputSymbols;
	private int statesNumber;
	private int n;
	private int loopTransitionPercentage;
	private String automata;
	private float duration;
	private int memory = 0;
	private boolean with_speedup;
	
	/**
	 * rebuild a LocalizerBasedStats object from a CSV line
	 * @param line the line to parse
	 */
	public LocalizerBasedStatsEntry(String line){
		StringTokenizer st = new StringTokenizer(line, ",");
		WSize = Integer.parseInt(st.nextToken());
		w1Length = Integer.parseInt(st.nextToken());
		localizeCallNb = Integer.parseInt(st.nextToken());
		localizeSequenceLength = Integer.parseInt(st.nextToken());
		traceLength = Integer.parseInt(st.nextToken());
		inputSymbols = Integer.parseInt(st.nextToken());
		outputSymbols = Integer.parseInt(st.nextToken());
		statesNumber = Integer.parseInt(st.nextToken());
		n = Integer.parseInt(st.nextToken());
		loopTransitionPercentage = Integer.parseInt(st.nextToken());
		automata = st.nextToken();
		duration = Float.parseFloat(st.nextToken());
		memory = Integer.parseUnsignedInt(st.nextToken());
		with_speedup = Boolean.parseBoolean(st.nextToken());
	}

	public LocalizerBasedStatsEntry(List<InputSequence> W, MealyDriver d,
			int n, LocalizerBasedOptions options) {
		WSize = W.size();
		w1Length = W.get(0).getLength();
		this.inputSymbols = d.getInputSymbols().size();
		this.n = n;
		this.automata = d.getSystemName();
		this.with_speedup = options.useSpeedUp();
	}

	protected void setLocalizeSequenceLength(int length){
		localizeSequenceLength = length;
	}

	protected void increaseLocalizeCallNb(){
		localizeCallNb ++;
	}

	protected void setTraceLength(int traceLength) {
		this.traceLength = traceLength;
	}

	protected void setStatesNumber(int statesNumber) {
		this.statesNumber = statesNumber;
	}

	public void updateWithConjecture(LmConjecture conjecture) {
		statesNumber = conjecture.getStateCount();
		int loopTransitions=0;
		HashSet<String> outputSymbols = new HashSet<>();
		for (MealyTransition t : conjecture.getTransitions()) {
			outputSymbols.add(t.getOutput());
			if (t.getTo() == t.getFrom())
				loopTransitions++;
		}
		this.outputSymbols = outputSymbols.size();
		loopTransitionPercentage = ((100*loopTransitions)/conjecture.getTransitionCount());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Comparable<T>> T getStaticAttribute(Attribute<T> a) {
		if (a == W_SIZE)
			return (T) Integer.valueOf(WSize);
		if (a == W1_LENGTH)
			return (T) Integer.valueOf(w1Length);
		if (a == LOCALIZER_CALL_NB)
			return (T) Integer.valueOf(localizeCallNb);
		if (a == LOCALIZER_SEQUENCE_LENGTH)
			return (T) Integer.valueOf(localizeSequenceLength);
		if (a == TRACE_LENGTH)
			return (T) Integer.valueOf(traceLength);
		if (a == INPUT_SYMBOLS)
			return (T) Integer.valueOf(inputSymbols);
		if (a == OUTPUT_SYMBOLS)
			return (T) Integer.valueOf(outputSymbols);
		if (a == STATE_NUMBER)
			return (T) Integer.valueOf(statesNumber);
		if (a == STATE_NUMBER_BOUND)
			return (T) Integer.valueOf(n);
		if (a == STATE_BOUND_OFFSET)
			return (T) Integer.valueOf(n - statesNumber);
		if (a == LOOP_RATIO)
			return (T) Integer.valueOf(loopTransitionPercentage);
		if (a == AUTOMATA)
			return (T) automata;
		if (a == DURATION)
			return (T) Float.valueOf(duration);
		if (a == MEMORY)
			return (T) Integer.valueOf(memory);
		if (a == WITH_SPEEDUP)
			return (T) Boolean.valueOf(with_speedup);
		throw new RuntimeException("unspecified attribute for this stats\n(no "+a.getName()+" in "+this.getClass()+")");

	}

	@Override
	public <T extends Comparable<T>> Float getFloatValue(Attribute<T> a) {
		if (a == W_SIZE || 
				a == W1_LENGTH ||
				a == LOCALIZER_CALL_NB ||
				a == LOCALIZER_SEQUENCE_LENGTH ||
				a == TRACE_LENGTH ||
				a == INPUT_SYMBOLS ||
				a == OUTPUT_SYMBOLS ||
				a == STATE_NUMBER ||
				a == STATE_NUMBER_BOUND ||
				a == STATE_BOUND_OFFSET ||
				a == MEMORY ||
				a == LOOP_RATIO)
			return ((Integer) get(a)).floatValue();
		if (a == DURATION)
			return (Float) get(a);
		throw new RuntimeException(a.getName() + " is not available or cannot be cast to float");

	}
	@Override
	public GraphGenerator getDefaultsGraphGenerator() {
		return new LocalizerBasedGraphGenerator();
	}

	public void setDuration(float duration) {
		this.duration = duration;
	}

	public void updateMemory(int currentMemory) {
		if (currentMemory > memory)
			memory = currentMemory;
	}

}
