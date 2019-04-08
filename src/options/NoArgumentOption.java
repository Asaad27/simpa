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
