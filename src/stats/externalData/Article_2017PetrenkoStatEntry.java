/********************************************************************************
 * Copyright (c) 2019 Institut Polytechnique de Grenoble 
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
package stats.externalData;

import stats.GraphGenerator;
import stats.StatsEntry;
import stats.StatsSet;
import stats.attribute.Attribute;

/**
 * From https://link.springer.com/article/10.1007/s10817-018-9486-0#Sec21
 * 
 * doi : https://doi.org/10.1007/s10817-018-9486-0
 * 
 * @author Nicolas BREMOND
 *
 */
public class Article_2017PetrenkoStatEntry extends StatsEntry {

	public static StatsSet getSet()
	{
		StatsSet set = new StatsSet();
		// TODO
		return set;
	}

	public Article_2017PetrenkoStatEntry(String automata, int traceLength,
			int statesNumber, int inputNumber, float duration) {
		super();
		this.automata = automata;
		this.traceLength = traceLength;
		this.statesNumber = statesNumber;
		this.inputNumber = inputNumber;
		this.duration = duration;
	}


	final String automata;
	final int traceLength;
	final int statesNumber;
	final int inputNumber;
	final float duration;
	@Override
	protected Attribute<?>[] getAttributesIntern() {
		return new Attribute<?>[] { Attribute.TRACE_LENGTH,
				Attribute.STATE_NUMBER, Attribute.INPUT_SYMBOLS,
				Attribute.AUTOMATA,
				Attribute.DURATION };
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Comparable<T>> T getStaticAttribute(Attribute<T> a) {
		if (a == Attribute.TRACE_LENGTH)
			return (T) Integer.valueOf(traceLength);
		if (a == Attribute.AUTOMATA)
			return (T) automata;
		if (a == Attribute.INPUT_SYMBOLS)
			return (T) Integer.valueOf(inputNumber);
		if (a == Attribute.STATE_NUMBER)
			return (T) Integer.valueOf(statesNumber);
		if (a == Attribute.DURATION)
			return (T) Float.valueOf(duration);
		throw new RuntimeException("invalid attribute " + a);
	}

	@Override
	public <T extends Comparable<T>> Float getFloatValue(Attribute<T> a) {
		if (a == Attribute.AUTOMATA)
			throw new RuntimeException();
		if (a == Attribute.DURATION)
			return (Float) getStaticAttribute(a);
		return ((Integer) getStaticAttribute(a)).floatValue();
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