/********************************************************************************
 * Copyright (c) 2018,2019 Institut Polytechnique de Grenoble 
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
package drivers.mealy.real.mqtt;

public abstract class MQTTOperation {
	private String input = null;

	protected String usedTopic;
	protected String usedMessage;

	public String createInput(boolean showTopic, boolean showMessage) {
		assert input == null;
		input = createInput_intern(showTopic, showMessage);
		return input;
	}

	protected abstract String createInput_intern(boolean showTopic,
			boolean showMessage);

	public String getInput() {
		return input;
	}

	public abstract String execute();
}
