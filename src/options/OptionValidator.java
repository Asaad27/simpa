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

/**
 * This class enables developers to check the value of the options they use. The
 * actual test should be written in the method {@link #check()} and they can
 * provide messages to explain to the user why the value is not correct.
 * 
 * If the test needs to know which driver is used, have a look to
 * {@link options.automataOptions.PostDriverValidator}
 * 
 * @author Nicolas BREMOND
 *
 */
public abstract class OptionValidator {
	/**
	 * An enumeration of possible level of errors found in options.
	 * 
	 * The level will define the display of messages in graphical interface (see
	 * {@link OptionValidator#createComponent()}) and define whether the
	 * inference can be made or not with these options. Levels above
	 * {@link #WARNING} abort inference.
	 * 
	 * @author Nicolas BREMOND
	 */
	public enum CriticalityLevel {
		/**
		 * There is no problem, but a message can still be displayed.
		 */
		NOTHING,
		/**
		 * There is an issue, but we can still infer with these options.
		 */
		WARNING,
		/**
		 * The value of this option can not be used to start inference.
		 */
		ERROR,

	}

	private CriticalityLevel criticality = CriticalityLevel.NOTHING;
	private String message = "";

	/**
	 * Get the {@link CriticalityLevel criticality} computed in last
	 * {@link #check()}.
	 * 
	 * @return the {@link CriticalityLevel criticality} computed in last
	 *         {@link #check()}.
	 */
	public CriticalityLevel getCriticality() {
		return criticality;
	}

	/**
	 * Set the {@link CriticalityLevel criticality} of the value of option.
	 * 
	 * @param criticality
	 *            the {@link CriticalityLevel criticality} of the issue found by
	 *            {@link #check()}.
	 */
	protected void setCriticality(CriticalityLevel criticality) {
		this.criticality = criticality;
	}

	/**
	 * Set the message explaining what is the issue to the user of SIMPA.
	 * 
	 * @param message
	 *            a text to the user of SIMPA explaining what is the issue with
	 *            the chosen value.
	 */
	protected void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Reset criticality and remove message.
	 */
	protected void clear() {
		setMessage("");
		setCriticality(CriticalityLevel.NOTHING);
	}

	/**
	 * Get the message computed in last {@link #check()}.
	 * 
	 * @return the message computed in last {@link #check()}.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * This method will be automatically called before starting inference and
	 * when the value of option is changed.
	 * 
	 * Developers of sub-classes should write their test in this method and call
	 * {@link #setCriticality(CriticalityLevel)} and {@link #setMessage(String)}
	 * to set the result of their test (NB. it can be useful to start this
	 * method to a call to {@link #clear()}).
	 */
	public abstract void check();

	/**
	 * Create the graphical component to display the message to the user of
	 * SIMPA.
	 * 
	 * @return a graphical component containing the {@link #getMessage()
	 *         message}.
	 */
	public final JLabel createComponent() {
		if (criticality.compareTo(CriticalityLevel.ERROR) >= 0)
			return new JLabel(getMessage(),
					UIManager.getIcon("OptionPane.errorIcon"), JLabel.LEFT);
		if (criticality.compareTo(CriticalityLevel.WARNING) >= 0)
			return new JLabel(getMessage(),
					UIManager.getIcon("OptionPane.warningIcon"), JLabel.LEFT);
		return new JLabel(getMessage());
	}

}
