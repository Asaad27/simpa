package options.valueHolders;

import options.ParseException;

public interface SingleValueHolder {

	void setValueFromString(String strValue) throws ParseException;

	String getValueAsString(boolean debug);

}
