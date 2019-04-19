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

import examples.mealy.RandomMealy;
import stats.GraphGenerator;
import stats.StatsEntry;
import stats.StatsSet;
import stats.attribute.Attribute;
import tools.StandaloneRandom;

/**
 * https://hal.inria.fr/hal-01678991
 * 
 * @author Nicolas BREMOND
 *
 */
public class Article_2017PetrenkoStatEntry extends ExternalData {

	public static StatsSet getSet() {
		StatsSet set = new StatsSet();
		String randomName = new RandomMealy(new StandaloneRandom(), true)
				.getName();
	//	@formatter:off
		set.add(new Article_2017PetrenkoStatEntry(randomName,   2,  1, 2, 2, (float) 0.01));
		set.add(new Article_2017PetrenkoStatEntry(randomName,   7,  2, 2, 2, (float) 0.01));
		set.add(new Article_2017PetrenkoStatEntry(randomName,  18,  3, 2, 2, (float) 0.01));
		set.add(new Article_2017PetrenkoStatEntry(randomName,  30,  4, 2, 2, (float) 0.01));
		set.add(new Article_2017PetrenkoStatEntry(randomName,  43,  5, 2, 2, (float) 0.02));
		set.add(new Article_2017PetrenkoStatEntry(randomName,  57,  6, 2, 2, (float) 0.05));
		set.add(new Article_2017PetrenkoStatEntry(randomName,  69,  7, 2, 2, (float) 0.13));
		set.add(new Article_2017PetrenkoStatEntry(randomName,  83,  8, 2, 2, (float) 0.32));
		set.add(new Article_2017PetrenkoStatEntry(randomName, 107,  9, 2, 2, (float) 2.4));
		set.add(new Article_2017PetrenkoStatEntry(randomName, 119, 10, 2, 2, (float) 9.0));
		set.add(new Article_2017PetrenkoStatEntry(randomName, 146, 11, 2, 2, 161));
	//	@formatter:on
		return set;
	}

	public Article_2017PetrenkoStatEntry(String automata, int traceLength,
			int statesNumber, int inputNumber, int outputNumber,
			float duration) {
		super();
		this.automata = automata;
		this.traceLength = traceLength;
		this.statesNumber = statesNumber;
		this.inputNumber = inputNumber;
		this.outputNumber = outputNumber;
		this.duration = duration;
	}

	final String automata;
	final int traceLength;
	final int statesNumber;
	final int inputNumber;
	final int outputNumber;
	final float duration;

	@Override
	protected Attribute<?>[] getAttributesIntern() {
		return new Attribute<?>[] { Attribute.TRACE_LENGTH,
				Attribute.STATE_NUMBER, Attribute.INPUT_SYMBOLS,
				Attribute.AUTOMATA, Attribute.DURATION };
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
		if (a == Attribute.OUTPUT_SYMBOLS)
			return (T) Integer.valueOf(outputNumber);
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
