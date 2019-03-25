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

	private final BooleanOption forceJ48;

	WekaOption() {
		super("use weka library", "E_weka",
				"Select the library used for data mining.", null,
				new ArrayList<>(), false);
		forceJ48 = new BooleanOption("use J48 algorithm", "E_use_J48",
				"Force the use of J48 algorithm instead of M5P for numeric classes.",
				new ArrayList<>(), new ArrayList<>(), false) {
			@Override
			public String getDisableHelp() {
				return "Do not use J48 algorithm and use M5P instead for numeric classes.";
			}

			@Override
			protected void makeArgumentDescriptors(String argument) {
				super.makeArgumentDescriptors(argument);
				disableArgumentDescriptor = new ArgumentDescriptor(
						AcceptedValues.NONE, "--E_use_M5P", this);
			}
		};
		assert !isEnabled();
		setSubTreeIfTrue(Arrays.asList(forceJ48));
	}

	@Override
	public String getEnableHelp() {
		if (isAvailable())
			return "Use weka library (instead of free ARFF library).";
		else
			return "Use weka library (but it is not available on this system).";
	}

	@Override
	public String getDisableHelp() {
		return "Use free ARFF library instead of weka.";
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
		assert argument.equals("E_weka");
		super.makeArgumentDescriptors(argument);
		disableArgumentDescriptor = new ArgumentDescriptor(AcceptedValues.NONE,
				"--E_freeARFF", this);
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
