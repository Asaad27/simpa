package options.valueHolders;

import options.ParseException;

public class SingleValueAutoHolder<T, H extends ValueHolder<T> & Stringifyable>
		extends AutoHolder<T, H> implements Stringifyable {

	public SingleValueAutoHolder(H baseHolder) {
		super(baseHolder);
	}

	@Override
	public void setValueFromString(String strValue) throws ParseException {
		if (strValue.equals("auto") || strValue.isEmpty()) {
			useAuto.setValue(true);
		} else {
			useAuto.setValue(false);
			baseHolder.setValueFromString(strValue);
		}
	}

	@Override
	public String getValueAsString(boolean debug) {
		if (useAutoValue() && !debug)
			return "auto";
		return baseHolder.getValueAsString(debug);
	}
}
