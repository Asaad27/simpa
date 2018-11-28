package drivers;

import java.util.Iterator;
import java.util.NoSuchElementException;

import options.LongOption;
import options.automataOptions.DriverChoice;
import options.automataOptions.DriverChoiceItem;

public abstract class ExhaustiveGeneratorOption<T extends Driver & EnumeratedDriver>
		extends DriverChoiceItem<T> implements Iterable<T> {
	/**
	 * The seed used to create a specific driver. It should not be specified
	 * ({@link LongOption#useAutoValue()} to iterate over all possible drivers.
	 */
	public final LongOption seed = new LongOption(
			"--seed-for-exhaustive-generator",
			"seed used to build the automaton in driver",
			"let the enum mode select all seeds");

	public ExhaustiveGeneratorOption(DriverChoice<? extends Driver> parent,
			Class<? extends T> driverClass) {
		super("Exhaustive generator", "--exhaustive-generator", parent,
				driverClass);
		subTrees.add(seed);
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
		seed.setValue(next.getSeed());
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
