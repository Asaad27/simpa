package options.valueHolders;

import options.ParseException;

public interface Stringifyable {

	void setValueFromString(String strValue) throws ParseException;

	String getValueAsString(boolean debug);

}
