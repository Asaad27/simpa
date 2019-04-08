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
package options;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import options.valueHolders.ValueHolder;

/**
 * This class represent a choice option like {@link MultiArgChoiceOption} but in
 * this class, there is only one argument for this option in command line
 * instead of one argument per choice.
 * 
 * @author Nicolas BREMOND
 *
 */
public class GenericOneArgChoiceOption<T extends OneArgChoiceOptionItem>
		extends GenericChoiceOption<T> {
	ArgumentDescriptor argumentDescriptor;

	public GenericOneArgChoiceOption(String argument, String optionName,
			String description) {
		super(optionName);
		this.description = description;
		assert argument.startsWith("-");
		argumentDescriptor = makeArgumentDescriptor(argument);
	}

	protected ArgumentDescriptor makeArgumentDescriptor(String argument) {
		return new ArgumentDescriptor(ArgumentDescriptor.AcceptedValues.ONE,
				argument, this);
	}

	@Override
	protected boolean isActivatedByArg(ArgumentValue arg) {
		return arg.getName().equals(argumentDescriptor.name);
	}

	/**
	 * This method allow to parse additional choice items.
	 * 
	 * @param arg
	 *            the argument to parse
	 * @return a ChoiceItem or null
	 */
	protected T selectExtraChoice(ArgumentValue arg) {
		return null;
	}

	@Override
	protected boolean setValueFromArg(ArgumentValue arg,
			PrintStream parsingErrorStream) {
		assert argumentDescriptor.acceptedValues == AcceptedValues.ONE;
		if (arg.getValues().size() == 0) {
			parsingErrorStream
					.println("Error : argument " + argumentDescriptor.name
							+ " is expecting one value but none is provided.");
			return false;
		}
		if (arg.getValues().size() > 1)
			parsingErrorStream.println("Warning : argument "
					+ argumentDescriptor.name
					+ " is expecting only one value but more than one are provided."
					+ " Only the first is used.");
		String value = arg.getValues().get(0);
		T selectedChoice = null;
		for (T choice : choices) {
			if (choice.argValue.equals(value))
				selectedChoice = choice;
		}
		if (selectedChoice == null) {
			selectedChoice = selectExtraChoice(arg);
		}
		if (selectedChoice == null) {
			parsingErrorStream
					.println("Error : value '" + value + "' of argument "
							+ argumentDescriptor.name + " cannot be parsed.");
			return false;
		}
		selectChoice(selectedChoice);
		return true;

	}

	@Override
	protected ArgumentValue getDefaultValue() {
		if (defaultItem == null)
			return null;
		ArgumentValue arg = new ArgumentValue(argumentDescriptor);
		arg.addValue(defaultItem.argValue);
		return arg;
	}

	@Override
	protected ArgumentValue getSelectedArgument() {
		ArgumentValue argValue = new ArgumentValue(argumentDescriptor);
		argValue.addValue(getSelectedItem().argValue);
		return argValue;
	}

	@Override
	protected List<ArgumentDescriptor> getAcceptedArguments() {
		List<ArgumentDescriptor> list = new ArrayList<>();
		list.add(argumentDescriptor);
		return list;
	}

	@Override
	public String getHelpByArgument(ArgumentDescriptor arg) {
		assert description.endsWith(".");
		if (getDefaultValue() != null)
			return description.substring(0, description.length() - 1)
					+ " (default : " + getDefaultValue().toStringWithValues()
					+ ").";
		return description;
	}

	@Override
	public ValueHolder<?, ?> getValueHolder() {
		// TODO update when value will be handled with a valueHolder
		return null;
	}

	@Override
	public List<SampleArgumentValue> getSampleArgumentValues(
			ArgumentDescriptor arg) {
		assert arg == argumentDescriptor;
		List<SampleArgumentValue> r = new ArrayList<>();
		for (T choice : choices) {
			r.add(new SampleArgumentValue(arg, choice.argValue, true,
					choice.displayName));
		}
		return r;
	}

}
