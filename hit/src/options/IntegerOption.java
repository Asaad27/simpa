package options;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import options.OptionTree.ArgumentDescriptor.AcceptedValues;

public class IntegerOption extends OptionTree {
	SpinnerNumberModel spinnerModel;
	JSpinner spinner;
	Integer value;
	Integer maximum;
	Integer minimum;

	private final ArgumentDescriptor argument;

	public IntegerOption(String name, String argument, String description,
			int defaultValue) {
		assert argument.startsWith("-");
		this.argument = new ArgumentDescriptor(AcceptedValues.ONE, argument,
				this);
		this.description = description;
		setValue(defaultValue);
		setMinimum(0);
	}

	private final List<OptionTree> subOptions = new ArrayList<>();// Sub options
																	// are not
																	// supported
																	// by
																	// IntegerOption
																	// at this
																	// time.

	@Override
	protected void createMainComponent() {
		JPanel labelledSpinner = new JPanel();
		labelledSpinner
				.setLayout(new BoxLayout(labelledSpinner, BoxLayout.X_AXIS));
		labelledSpinner.add(new JLabel(description));
		spinnerModel = new SpinnerNumberModel(0, null, null, 1);
		spinner = new JSpinner(spinnerModel);
		setValue(value);
		setMaximum(maximum);
		setMinimum(minimum);
		spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				setValue(spinnerModel.getNumber().intValue());
			}
		});
		labelledSpinner.add(spinner);
		mainConponent = labelledSpinner;
	}

	public void setMaximum(Integer maximum) {
		assert maximum == null || minimum == null || maximum >= minimum;
		if (maximum != null && value > maximum)
			setValue(maximum);
		this.maximum = maximum;
		if (mainConponent != null) {
			spinnerModel.setMaximum(maximum);
		}
	}

	public void setMinimum(Integer minimum) {
		assert maximum == null || minimum == null || maximum >= minimum;
		if (minimum != null && value < minimum)
			setValue(minimum);
		this.minimum = minimum;
		if (mainConponent != null) {
			spinnerModel.setMaximum(maximum);
		}
	}

	public void setValue(Integer value) {
		if (minimum != null && value < minimum)
			value = minimum;
		if (maximum != null && value > maximum)
			value = maximum;
		this.value = value;
		if (mainConponent != null) {
			if (!spinnerModel.getValue().equals(value))
				spinner.setValue(value);
		}
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
		if (arg.values.size() == 0)
			return false;
		try {
			Integer parsed = new Integer(arg.values.get(0));
			setValue(parsed);
			if (value != parsed) {
				parsingErrorStream.println(
						"error : the value provided is not in the accepted range.");
				return false;
			}
		} catch (NumberFormatException e) {
			parsingErrorStream.println("error : unable to parse "
					+ arg.values.get(0) + " as an Integer");
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
	protected String getSelectedArgument() {
		return argument.name + "=" + value;
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

	public int getValue() {
		return value;
	}

}
