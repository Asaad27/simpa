package options;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public abstract class NoArgumentOption extends OptionTree {

	@Override
	protected boolean isActivatedByArg(ArgumentValue arg) {
		return false;
	}

	@Override
	protected boolean setValueFromArg(ArgumentValue arg,
			PrintStream parsingErrorStream) {
		assert false : "this method should not be called";
		return false;
	}

	@Override
	protected ArgumentValue getSelectedArgument() {
		return null;
	}

	@Override
	protected List<ArgumentDescriptor> getAcceptedArguments() {
		return new ArrayList<>();
	}

	@Override
	public String getHelpByArgument(ArgumentDescriptor arg) {
		assert false : "this method should not be called";
		return null;
	}

}
