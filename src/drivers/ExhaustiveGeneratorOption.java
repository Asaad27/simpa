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
package drivers;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import options.AutoLongOption;
import options.LongOption;
import options.OptionCategory;
import options.automataOptions.DriverChoice;
import options.automataOptions.DriverChoiceItem;

public abstract class ExhaustiveGeneratorOption<T extends Driver<?, ?> & EnumeratedDriver>
		extends DriverChoiceItem<T> implements Iterable<T> {
	/**
	 * The seed used to create a specific driver. It should not be specified
	 * ({@link LongOption#useAutoValue()} to iterate over all possible drivers.
	 */
	public final AutoLongOption seed = new AutoLongOption(
			"--Sexhaustive-generator-pos",
			"a number representing the automaton built",
			"A number used to represent an automata in the generation list."
					+ " The automatic value let the enum mode select all seeds.",
			(long) 0) {
		@Override
		protected List<ArgumentDescriptor> getHelpArguments() {
			return Collections.emptyList();
		}
	};

	public ExhaustiveGeneratorOption(
			DriverChoice<? extends Driver<?, ?>> parent,
			Class<? extends T> driverClass) {
		super("Exhaustive generator (for stats)", "exhaustive_generator",
				parent, driverClass);
		subTrees.add(seed);
		seed.setCategory(OptionCategory.STATS);
	}

	/**
	 * If seed is not imposed (i.e. {@link LongOption#useAutoValue()
	 * seed.useAutoValue()} is {@code true}) this store the iterator over all
	 * drivers. the iterator is given by the overloaded {@link #iterator()}
	 * method.
	 */
	Iterator<T> iterator;

	@Override
	public final T createDriver() {
		T next;
		if (seed.useAutoValue()) {
			if (iterator == null)
				iterator = iterator();
			next = iterator.next();
		} else {
			next = createDriver(seed.getValue());
			assert next.getSeed() == seed.getValue();
			if (next == null)
				throw new NoSuchElementException(
						"invalid seed for exhaustive driver generation");
		}
		seed.setValueAuto(next.getSeed());
		return next;
	}

	/**
	 * Create a driver for a generated automaton having a given seed. This
	 * method can be overridden for better performances.
	 * 
	 * @param wantedSeed
	 *            the seed used to create the driver.
	 * @return a driver with its seed equals to the given seed.
	 */
	protected T createDriver(long wantedSeed) {
		Iterator<T> it = iterator();
		while (it.hasNext()) {
			T next = it.next();
			if (next.getSeed() == wantedSeed)
				return next;
		}
		return null;
	}

	/**
	 * indicate whether another driver is available or not. As some
	 * implementations may modify the current driver during this call, this
	 * method should only be called when the driver returned by
	 * {@link #createDriver()} is not used anymore.
	 * 
	 * This method should be called in enumeration mode where no seed is not
	 * imposed.
	 *
	 * @return {@code true} if another driver can be built.
	 */
	public boolean hasNext() {
		assert !seed.useAutoValue();
		if (iterator == null)
			iterator = iterator();
		return iterator.hasNext();
	}

}
