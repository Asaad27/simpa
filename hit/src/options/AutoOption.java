package options;

import options.valueHolders.SingleValueAutoHolder;
import options.valueHolders.SingleValueHolder;
import options.valueHolders.ValueHolder;

public class AutoOption<T, B extends ValueHolder<T> & SingleValueHolder>
		extends SingleValueArgumentOption<T, SingleValueAutoHolder<T, B>> {

	private AutoValueValidator<T, B> autoValueValidator;

	public AutoOption(String argument, B holder) {
		super(argument, new SingleValueAutoHolder<T, B>(holder));
		autoValueValidator = new AutoValueValidator<T, B>();
	}

	public boolean useAutoValue() {
		return value.useAutoValue();
	}

	public void setValueAuto(T v) {
		value.setValue(v);
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

class AutoValueValidator<T, H extends ValueHolder<T>> extends OptionValidator {

	public AutoValueValidator() {
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