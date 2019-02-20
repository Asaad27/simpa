package options.valueHolders;

import java.io.PrintStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import options.ParseException;

public abstract class NumberHolder<T extends Number & Comparable<T>>
		extends ValueHolder<T> implements Stringifyable {
	SpinnerNumberModel spinnerModel;
	JSpinner spinner;
	T maximum;
	T minimum;
	String description;

	/**
	 * construct a Number option;
	 * 
	 * @param descriptionv
	 *            an help String
	 * @param defaultValue
	 *            the initial value of this option.
	 */
	public NumberHolder(String name, String description, T defaultValue) {
		super(name, description, defaultValue);
		this.description = description;
		assert defaultValue != null;
		setValue(defaultValue);
		minimum = toType(0);
		updateWithValue();
	}

	@Override
	protected JComponent createMainComponent() {
		JPanel labelledSpinner = new JPanel();
		labelledSpinner
				.setLayout(new BoxLayout(labelledSpinner, BoxLayout.X_AXIS));
		labelledSpinner.add(new JLabel(description));
		spinnerModel = new SpinnerNumberModel(toType(1), null, null, toType(0));
		spinner = new JSpinner(spinnerModel);
		spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				setValue(toType(spinnerModel.getNumber()));
			}
		});
		labelledSpinner.add(spinner);
		labelledSpinner.add(Box.createGlue());
		return labelledSpinner;
	}

	public void setMaximum(T maximum) {
		this.maximum = maximum;
		assert maximum == null || minimum == null
				|| maximum.compareTo(minimum) >= 0;
		updateWithValue();
	}

	@Override
	protected void updateWithValue() {
		assert getValue() != null;
		assert maximum == null || minimum == null
				|| maximum.compareTo(minimum) >= 0;
		if (minimum != null && getValue().compareTo(minimum) < 0)
			setValue(minimum);
		if (maximum != null && getValue().compareTo(maximum) > 0)
			setValue(maximum);
		if (spinnerModel != null) {
			if (!spinnerModel.getValue().equals(getValue()))
				spinner.setValue(getValue());
			spinnerModel.setMaximum(maximum);
			spinnerModel.setMaximum(maximum);
		}
	}

	public void setMinimum(T minimum) {
		this.minimum = minimum;
		assert maximum == null || minimum == null
				|| maximum.compareTo(minimum) >= 0;
		updateWithValue();
	}

	protected boolean parseString(String strValue,
			PrintStream parsingErrorStream) {

		try {
			T parsed = parse(strValue);
			setValue(parsed);
			if (getValue() != parsed) {
				parsingErrorStream.println(
						"error : the value provided is not in the accepted range.");
				return false;
			}
		} catch (NumberFormatException e) {
			parsingErrorStream.println(
					"error : unable to parse " + strValue + " as an Integer");
			return false;
		}
		return true;
	}

	protected abstract T parse(String s);

	protected abstract T toType(int v);

	protected abstract T toType(Number v);

	@Override
	public void setValueFromString(String strValue) throws ParseException {
		try {
			setValue(parse(strValue));
		} catch (NumberFormatException e) {
			throw new ParseException(e);
		}
		assert getValue() != null;
	}

	@Override
	public String getValueAsString(boolean debug) {
		assert getValue() != null;
		return getValue().toString();
	}
}
