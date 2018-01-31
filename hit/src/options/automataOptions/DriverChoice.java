package options.automataOptions;

import java.lang.reflect.Constructor;

import drivers.Driver;
import options.GenericOneArgChoiceOption;

public class DriverChoice<T extends Driver>
		extends GenericOneArgChoiceOption<DriverChoiceItem<T>> {
	Class<T> driverBaseType;

	protected DriverChoiceItem<T> extraChoice = null;

	public DriverChoice(Class<T> baseType) {
		super("--driver");
		driverBaseType = baseType;
	}

	@Override
	protected DriverChoiceItem<T> selectExtraChoice(ArgumentValue arg) {
		assert arg.values.size() > 0;
		String className = arg.values.get(0);
		for (DriverChoiceItem<T> driverChoice : choices) {
			if (driverChoice.driverClass.getName().equals(className))
				return driverChoice;
		}

		try {
			Class<?> c = Class.forName(className);
			Class<? extends T> driverClass;
			if (driverBaseType.isAssignableFrom(c)) {

				driverClass = c.asSubclass(driverBaseType);
			} else {
				System.out.println("Error : class " + className + " is not a "
						+ driverBaseType.getSimpleName() + ".");
				return null;
			}
			Constructor<?> noArgumentConstructor = null;
			Constructor<?>[] constructors = c.getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				if (constructor.getParameterTypes().length == 0)
					noArgumentConstructor = constructor;
			}
			if (noArgumentConstructor == null) {
				System.out.println("Error : class " + className
						+ " cannot be instanciate without argument."
						+ " To make this class available in options, you need to write and add an item in "
						+ this.getClass().getName() + " .");
			}
			assert extraChoice == null : "The extra choice should not be overwiten";
			if (extraChoice == null)
				extraChoice = new DriverChoiceItem<T>(
						"driver selected from command line argument",
						driverClass.getName(), this, driverClass);
			addChoice(extraChoice);
			extraChoice.argValue = className;
			extraChoice.driverClass = driverClass;
			return extraChoice;
		} catch (ClassNotFoundException e) {
			System.out.println("Unable to find the driver."
					+ "Please check the system name (" + className + ")");
			return null;
		}
	}

	public T createDriver() {
		return getSelectedItem().createDriver();
	}
}
