package options.automataOptions;

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
			return driverClass.newInstance();
		} catch (InstantiationException e) {
			System.out.println("Unable to instantiate the driver : "
					+ driverClass.getName());
			return null;
		} catch (IllegalAccessException e) {
			System.out.println(
					"Unable to access the driver : " + driverClass.getName());
			return null;
		}

	}

}
