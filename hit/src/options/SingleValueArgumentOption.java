package options;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import options.valueHolders.Stringifyable;
import options.valueHolders.ValueHolder;

public abstract class SingleValueArgumentOption<T, H extends ValueHolder<T, T> & Stringifyable>
		extends OptionTree {

	final H value;

	protected final ArgumentDescriptor argument;

	protected SingleValueArgumentOption(String argument, H holder) {
		assert argument.startsWith("-");
		this.value = holder;
		this.argument = new ArgumentDescriptor(AcceptedValues.ONE, argument,
				this);
		this.description = holder.getDescription();
	}

	private final List<OptionTree> subOptions = new ArrayList<>();// Sub options
																	// are not
																	// supported
																	// at this
																	// time.

	@Override
	protected void createMainComponent() {
		mainComponent = value.getComponent();
	}

	@Override
	protected List<OptionTree> getSelectedChildren() {
		return subOptions;
	}

	@Override
	protected boolean isActivatedByArg(ArgumentValue arg) {
		return arg.getName().equals(argument.name);
	}

	@Override
	protected boolean setValueFromArg(ArgumentValue arg,
			PrintStream parsingErrorStream) {
		assert isActivatedByArg(arg);
		if (arg.getValues().size() == 0)
			return false;
		try {
			value.setValueFromString(arg.getValues().get(0));
		} catch (ParseException e) {
			parsingErrorStream.println("Error : " + e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	protected void setValueFromSelectedChildren(
			List<OptionTree> selectedChildren) {
		assert selectedChildren == subOptions;
	}

	@Override
	protected ArgumentValue getSelectedArgument() {
		ArgumentValue argValue = new ArgumentValue(argument);
		argValue.addValue(value.getValueAsString(false));
		return argValue;
	}

	@Override
	protected ArgumentValue getDebugArgument() {
		ArgumentValue argValue = new ArgumentValue(argument);
		argValue.addValue(value.getValueAsString(true));
		return argValue;
	}

	@Override
	protected List<ArgumentDescriptor> getAcceptedArguments() {
		List<ArgumentDescriptor> args = new ArrayList<>();
		args.add(argument);
		return args;
	}

	@Override
	public String getHelpByArgument(ArgumentDescriptor arg) {
		assert arg == argument;
		return description;
	}

	public T getValue() {
		return value.getValue();
	}

	@Override
	public H getValueHolder() {
		return value;
	}

	public void setDefaultValue(T def) {
		value.setDefaultValue(def);
	}

	@Override
	protected ArgumentValue getDefaultValue() {
		if (value.getDefaultValue() == null)
			return null;
		ArgumentValue argValue = new ArgumentValue(argument);
		T current = value.getValue();
		value.setValue(value.getDefaultValue());
		argValue.addValue(value.getValueAsString(false));
		value.setValue(current);
		return argValue;
	}

}
