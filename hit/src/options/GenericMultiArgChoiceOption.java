package options;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GenericMultiArgChoiceOption<T extends MultiArgChoiceOptionItem>
		extends GenericChoiceOption<T> {
	protected String optionName = "";

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
	protected String getSelectedArgument() {
		return getSelectedItem().argument.name;
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
		return "set value of option " + optionName + " to "
				+ getItemFromArg(new ArgumentValue(arg));
	}
}