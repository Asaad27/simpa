package options.automataOptions;

import java.lang.reflect.InvocationTargetException;

import drivers.Driver;
import options.OneArgChoiceOptionItem;

public class DriverChoiceItem<T extends Driver> extends OneArgChoiceOptionItem {
	Class<? extends T> driverClass;

	public DriverChoiceItem(String displayName, String argument,
			DriverChoice<?> parent, Class<? extends T> driverClass) {
		super(displayName, argument, parent);
		this.driverClass = driverClass;
		assert parent.driverBaseType.isAssignableFrom(driverClass);
	}

	public DriverChoiceItem(DriverChoice<?> parent,
			Class<? extends T> driverClass) {
		this(driverClass.getSimpleName(), driverClass.getName(), parent,
				driverClass);
	}

	/**
	 * this function can be overridden to construct complex drivers.
	 * 
	 * @return a driver.
	 */
	public T createDriver() {
		try {
			return driverClass.getConstructor().newInstance();
		} catch (InstantiationException | IllegalArgumentException
				| IllegalAccessException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			System.out.println("Unable to instanciate the driver : "
					+ driverClass.getName());
			return null;
		}

	}

}
