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
package options;

import javax.swing.JLabel;
import javax.swing.UIManager;

public abstract class OptionValidator {
	public enum CriticalityLevel {
		NOTHING, WARNING, ERROR,
	}

	private CriticalityLevel criticality = CriticalityLevel.NOTHING;
	private String message = "";

	public OptionValidator() {
	}

	public CriticalityLevel getCriticality() {
		return criticality;
	}

	protected void setCriticality(CriticalityLevel criticality) {
		this.criticality = criticality;
	}

	protected void setMessage(String message) {
		this.message = message;
	}

	protected void clear() {
		setMessage("");
		setCriticality(CriticalityLevel.NOTHING);
	}

	public String getMessage() {
		return message;
	}

	public abstract void check();

	public JLabel createComponent() {
		if (criticality.compareTo(CriticalityLevel.ERROR) >= 0)
			return new JLabel(getMessage(),
					UIManager.getIcon("OptionPane.errorIcon"), JLabel.LEFT);
		if (criticality.compareTo(CriticalityLevel.WARNING) >= 0)
			return new JLabel(getMessage(),
					UIManager.getIcon("OptionPane.warningIcon"), JLabel.LEFT);
		return new JLabel(getMessage());
	}

}
