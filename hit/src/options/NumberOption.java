package options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import options.OptionTree.ArgumentDescriptor.AcceptedValues;

public abstract class NumberOption<T extends Number & Comparable<T>>
		extends OptionTree {
	SpinnerNumberModel spinnerModel;
	JSpinner spinner;
	T value;
	T maximum;
	T minimum;

	/**
	 * In the case were autoValue is allowed, this method indicate whether the
	 * user wants to use the value he set or the auto value.
	 */
	public boolean useAutoValue() {
		if (!autoValueIsAllowed())
			return false;
		if (autoValueCheckBox != null)
			return autoValueCheckBox.isSelected();
		return useAutoValue;
	}

	/**
	 * This field hold the value for {@link #useAutoValue()} when there is no
	 * graphical component. It is better to use the method
	 * {@link #useAutoValue()} instead of this field.
	 */
	private boolean useAutoValue;
	/**
	 * a String describing the use of default value. e.g. "use size of set as
	 * value for this option". A {@code null} String indicate that this option
	 * do not provide auto value.
	 */
	String autoValueLabel = null;

	/**
	 * indicate if the user is allowed to not provide a value for this option.
	 * 
	 * @return true if this option can not be provided
	 */
	protected boolean autoValueIsAllowed() {
		return autoValueLabel != null;
	}

	JCheckBox autoValueCheckBox = null;

	AutoValueValidator<T> autoValueValidator;

	private final ArgumentDescriptor argument;

	private NumberOption(String argument, String description) {
		assert argument.startsWith("-");
		this.argument = new ArgumentDescriptor(AcceptedValues.ONE, argument,
				this);
		this.description = description;
		setMinimum(toType(0));
		autoValueValidator = new AutoValueValidator<T>(this);
		addValidator(autoValueValidator);
	}

	/**
	 * constructs an Integer option without auto value;
	 * 
	 * @param argument
	 *            the CLI argument to set the value this option
	 * @param description
	 *            an help String
	 * @param defaultValue
	 *            the initial value of this option.
	 */
	public NumberOption(String argument, String description, T defaultValue) {
		this(argument, description);
		assert defaultValue != null;
		setValue(defaultValue);
		useAutoValue = false;
		assert !autoValueIsAllowed();
	}

	/**
	 * constructs an Integer option with auto value allowed (user can leave the
	 * field undecided and it will be computed later);
	 * 
	 * @param argument
	 *            the CLI argument to set the value this option
	 * @param description
	 *            an help String
	 * @param autoValueLabel
	 *            a text for {@link #autoValueLabel}.
	 */
	public NumberOption(String argument, String description,
			String autoValueLabel) {
		this(argument, description);
		assert autoValueLabel != null;
		this.autoValueLabel = autoValueLabel;
		this.useAutoValue = true;
		assert autoValueIsAllowed();
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
		spinnerModel = new SpinnerNumberModel(toType(1), null, null, toType(0));
		spinner = new JSpinner(spinnerModel);
		setValue(value);
		setMaximum(maximum);
		setMinimum(minimum);
		spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				setValue(toType(spinnerModel.getNumber()));
			}
		});
		labelledSpinner.add(spinner);
		labelledSpinner.add(Box.createGlue());
		if (autoValueIsAllowed()) {
			JPanel mainConponent = new JPanel();
			this.mainConponent = mainConponent;
			mainConponent
					.setLayout(new BoxLayout(mainConponent, BoxLayout.Y_AXIS));
			mainConponent.add(labelledSpinner);
			JPanel checkBoxComponent = new JPanel();
			checkBoxComponent.setLayout(
					new BoxLayout(checkBoxComponent, BoxLayout.X_AXIS));
			autoValueCheckBox = new JCheckBox(autoValueLabel);
			autoValueCheckBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (autoValueCheckBox.isSelected()) {
						spinner.setEnabled(false);
						useAutoValue = true;
					} else {
						spinner.setEnabled(true);
						useAutoValue = false;
						if (value == null)
							value = toType(spinnerModel.getNumber());
						else
							setValue(value);
						autoValueValidator.clearError();
					}
				}
			});
			if (useAutoValue) {
				autoValueCheckBox.setSelected(true);
				spinner.setEnabled(false);
			}
			checkBoxComponent.add(Box.createHorizontalStrut(20));
			checkBoxComponent.add(autoValueCheckBox);
			checkBoxComponent.add(Box.createGlue());
			mainConponent.add(checkBoxComponent);
		} else
			mainConponent = labelledSpinner;
	}

	public void setMaximum(T maximum) {
		this.maximum = maximum;
		if (value == null)
			return;
		assert maximum == null || minimum == null
				|| maximum.compareTo(minimum) >= 0;
		if (maximum != null && value.compareTo(maximum) > 0)
			setValue(maximum);
		if (mainConponent != null) {
			spinnerModel.setMaximum(maximum);
		}
	}

	public void setMinimum(T minimum) {
		this.minimum = minimum;
		if (value == null)
			return;
		assert maximum == null || minimum == null
				|| maximum.compareTo(minimum) >= 0;
		if (minimum != null && value.compareTo(minimum) < 0)
			setValue(minimum);
		if (mainConponent != null) {
			spinnerModel.setMaximum(maximum);
		}
	}

	public void setValue(T value) {
		if (value == null)
			return;
		if (minimum != null && value.compareTo(minimum) < 0)
			value = minimum;
		if (maximum != null && value.compareTo(maximum) > 0)
			value = maximum;
		this.value = value;
		if (spinnerModel != null) {
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
		if (arg.getValues().size() == 0)
			return false;
		if (autoValueIsAllowed() && arg.getValues().get(0).equals("auto")) {
			useAutoValue = true;
			return true;
		}
		try {
			T parsed = parse(arg.getValues().get(0));
			setValue(parsed);
			if (value != parsed) {
				parsingErrorStream.println(
						"error : the value provided is not in the accepted range.");
				return false;
			}
		} catch (NumberFormatException e) {
			parsingErrorStream.println("error : unable to parse "
					+ arg.getValues().get(0) + " as an Integer");
			return false;
		}
		useAutoValue = false;
		return true;
	}

	protected abstract T parse(String s);

	protected abstract T toType(int v);

	protected abstract T toType(Number v);

	@Override
	protected void setValueFromSelectedChildren(
			List<OptionTree> selectedChildren) {
		assert selectedChildren == subOptions;
	}

	@Override
	protected ArgumentValue getSelectedArgument() {
		ArgumentValue argValue = new ArgumentValue(argument);
		if (useAutoValue())
			argValue.addValue("auto");
		else
			argValue.addValue(value.toString());
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
		assert autoValueIsAllowed() || value != null;
		return value;
	}

	public void setAutoValueError(String string) {
		autoValueValidator.setError(string);
		validateSelectedTree();
	}

	public void clearAutoValueError() {
		autoValueValidator.clearError();
		validateSelectedTree();
	}

}

class AutoValueValidator<T extends Number & Comparable<T>>
		extends OptionValidator {

	public AutoValueValidator(NumberOption<T> parent) {
		super(parent);
	}

	public void clearError() {
		setCriticality(CriticalityLevel.NOTHING);
		setMessage("");
	}

	public void setError(String string) {
		setCriticality(CriticalityLevel.WARNING);
		setMessage(string);

	}

	@Override
	public void check() {
	}

}
