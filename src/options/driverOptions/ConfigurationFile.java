/********************************************************************************
 * Copyright (c) 2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 ********************************************************************************/
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
