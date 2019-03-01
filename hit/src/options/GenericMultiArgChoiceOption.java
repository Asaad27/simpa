package options;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import options.valueHolders.ValueHolder;

public class GenericMultiArgChoiceOption<T extends MultiArgChoiceOptionItem>
		extends GenericChoiceOption<T> {

	public GenericMultiArgChoiceOption(String optionName) {
		super(optionName);
	}

	@Override
	protected boolean isActivatedByArg(ArgumentValue arg) {
		for (T choice : choices) {
			if (choice.argument.name.equals(arg.getName()))
				return true;
		}
		return false;
	}

	protected T getItemFromArg(ArgumentValue arg) {
		for (T choice : choices) {
			if (choice.argument.name.equals(arg.getName())) {
				selectChoice(choice);
				return choice;
			}
		}
		assert false;
		return null;
	}

	@Override
	protected boolean setValueFromArg(ArgumentValue arg,
			PrintStream parsingErrorStream) {
		T item = getItemFromArg(arg);
		if (item == null)
			return false;

		selectChoice(item);
		return true;

	}

	@Override
	protected ArgumentValue getDefaultValue() {
		if (defaultItem == null)
			return null;
		return new ArgumentValue(defaultItem.argument);
	}

	@Override
	protected ArgumentValue getSelectedArgument() {
		return new ArgumentValue(getSelectedItem().argument);
	}

	@Override
	protected List<ArgumentDescriptor> getAcceptedArguments() {
		List<ArgumentDescriptor> args = new ArrayList<>();
		for (T choice : choices) {
			args.add(choice.argument);
			assert choice.argument.name.startsWith("-");
		}
		return args;
	}

	@Override
	public String getHelpByArgument(ArgumentDescriptor arg) {
		return "Set " + getName() + " to "
				+ getItemFromArg(new ArgumentValue(arg)) + ".";
	}

	@Override
	public ValueHolder<?, ?> getValueHolder() {
		// TODO update when value will be hold with a value holder
		return null;
	}
}
