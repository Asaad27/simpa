package learner.efsm.table;

import java.util.ArrayList;

import options.BooleanOption;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import tools.loggers.LogManager;

public class WekaOption extends BooleanOption {
	/**
	 * Do not use this field, use instead {@link #isAvailable()}
	 */
	static private Boolean availability = null;

	public static boolean isAvailable() {
		if (availability != null)
			return availability;
		try {
			availability = weka.core.Version.MAJOR >= 3;
			if (!availability)
				LogManager.logError(
						"Warning : Weka version >= 3 needed. Please update Weka.");
		} catch (Exception e) {
			LogManager.logError(
					"Warning : Unable to use Weka. Check the buildpath.");
			availability = false;
		}
		return availability;
	}

	WekaOption() {
		super("use weka", "weka", "select the library used for data mining",
				new ArrayList<>(), new ArrayList<>(), false);
		assert !isEnabled();

	}

	@Override
	protected void makeArgumentDescriptors(String argument) {
		assert argument.equals("weka");
		enableArgumentDescriptor = new ArgumentDescriptor(AcceptedValues.NONE,
				"--weka", this);
		disableArgumentDescriptor = new ArgumentDescriptor(AcceptedValues.NONE,
				"--freeARFF", this);
	}

}
