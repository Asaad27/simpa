package options.automataOptions;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import drivers.Driver;
import options.GenericOneArgChoiceOption;
import options.OptionCategory;
import options.OptionTree;

public class DriverChoice<T extends Driver<?, ?>>
		extends GenericOneArgChoiceOption<DriverChoiceItem<? extends T>> {
	Class<T> driverBaseType;

	protected DriverChoiceItem<T> extraChoice = null;

	public DriverChoice(Class<T> baseType, String optionName) {
		super("--Driver", optionName, "Select the driver to infer."
				+ " There are some pre-defined values but you can use the full name of a java class.");
		driverBaseType = baseType;
		setCategory(OptionCategory.INFERENCE);
	}

	@Override
	protected DriverChoiceItem<? extends T> selectExtraChoice(
			ArgumentValue arg) {
		assert arg.getValues().size() > 0;
		String className = arg.getValues().get(0);
		for (DriverChoiceItem<? extends T> driverChoice : choices) {
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

	@Override
	protected void addChoice(DriverChoiceItem<? extends T> choice) {
		for (OptionTree option : choice.subTrees)
			option.setCategoryIfUndef(OptionCategory.DRIVER);
		super.addChoice(choice);
	}

	@Override
	public List<SampleArgumentValue> getSampleArgumentValues(
			ArgumentDescriptor arg) {
		List<SampleArgumentValue> result = new ArrayList<>();
		super.getSampleArgumentValues(arg);
		result.add(new SampleArgumentValue(arg, "a class name", false,
				"use the driver written in the specified class"));
		for (SampleArgumentValue sample : super.getSampleArgumentValues(arg)) {
			assert (sample.argument == arg);
			if (sample.value.endsWith(sample.help) && sample.real) {
				sample = new SampleArgumentValue(arg, sample.value,
						sample.real);
			}
			result.add(sample);
		}
		return result;
	}
}
