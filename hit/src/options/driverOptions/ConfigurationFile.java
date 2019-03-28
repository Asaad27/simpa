package options.driverOptions;

import options.FileOption;

public class ConfigurationFile extends FileOption {
	private static final String defaultDescription = "XML file describing the driver.";

	public ConfigurationFile() {
		this(defaultDescription);
	}

	public ConfigurationFile(String detailedDescription) {
		super("--DConfig", detailedDescription, null,
				FileSelectionMode.FILES_ONLY, FileExistance.MUST_EXIST);
	}

	@Override
	public String getHelpByArgument(ArgumentDescriptor arg) {
		return defaultDescription;
	}
}
