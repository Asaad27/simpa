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
package options.valueHolders;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

import options.ParseException;

public class BooleanHolder extends SingleTypeValueHolder<Boolean>
		implements Stringifyable {

	private JCheckBox checkBox;

	public BooleanHolder(String name, String description) {
		super(name, description, false);
		updateWithValue();
	}

	@Override
	protected JComponent createMainComponent() {
		assert checkBox == null;
		checkBox = new JCheckBox(getName());
		checkBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setValue(checkBox.isSelected());

			}
		});
		return checkBox;
	}

	@Override
	protected void updateWithValue() {
		assert getValue() != null;
		if (checkBox != null)
			checkBox.setSelected(getValue());
	}

	@Override
	public void setValueFromString(String s) throws ParseException {
		Boolean b = Boolean.valueOf(s);
		setValue(b);
	}

	@Override
	public String getValueAsString(boolean forDebug) {
		return getValue().toString();
	}

}
