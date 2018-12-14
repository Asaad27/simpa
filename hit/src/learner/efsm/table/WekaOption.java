package learner.efsm.table;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JLabel;

import options.BooleanOption;
import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import tools.loggers.LogManager;

public class WekaOption extends BooleanOption {
	/**
	 * Do not use this field, use instead {@link #isAvailable()}
	 */
	static private Boolean availability = null;
	/**
	 * Do not use this field, use instead {@link #getAvailabilityMessage()}
	 */
	static private String availabilityMessage = null;

	/**
	 * check presence and version of Weka
	 * 
	 * @return true if weka is available
	 */
	public static boolean isAvailable() {
		if (availability != null)
			return availability;
		try {
			availability = weka.core.Version.MAJOR >= 3;
			if (!availability)
				availabilityMessage = "Warning : Weka version >= 3 needed. Please update Weka.";
		} catch (Exception e) {
			availabilityMessage = "Warning : Unable to use Weka. Check the buildpath.";
			availability = false;
		}
		if (!availability)
			LogManager.logError(availabilityMessage);
		return availability;
	}

	public static String getAvailabilityMessage() {
		isAvailable();
		return availabilityMessage;
	}

	private final BooleanOption forceJ48 = new BooleanOption(
			"use J48 algorithm", "use-J48",
			"Force the use of J48 algorithm instead of M5P for numeric classes",
			new ArrayList<>(), new ArrayList<>(), false);

	WekaOption() {
		super("use weka", "weka", "select the library used for data mining",
				null, new ArrayList<>(), false);
		assert !isEnabled();
		setSubTreeIfTrue(Arrays.asList(forceJ48));
	}

	@Override
	protected void createMainComponent() {
		if (isAvailable()) {
			super.createMainComponent();
			return;
		}
		JLabel label = new JLabel(
				"weka is not available (" + getAvailabilityMessage() + ").");
		mainComponent = label;
	}

	@Override
	public boolean isEnabled() {
		assert isAvailable() || !super.isEnabled();
		return super.isEnabled();
	}

	@Override
	protected void makeArgumentDescriptors(String argument) {
		assert argument.equals("weka");
		enableArgumentDescriptor = new ArgumentDescriptor(AcceptedValues.NONE,
				"--weka", this);
		disableArgumentDescriptor = new ArgumentDescriptor(AcceptedValues.NONE,
				"--freeARFF", this);
	}

	@Override
	public void setEnabled(Boolean v) {
		if (isAvailable())
			super.setEnabled(v);
		else {
			if (v)
				LogManager.logError(
						"Weka is not available and cannot be selected ("
								+ getAvailabilityMessage() + ").");
			super.setEnabled(false);
		}
	}

	public boolean forceJ48() {
		return isEnabled() && forceJ48.isEnabled();
	}
}
