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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import options.OptionTree.ArgumentDescriptor.AcceptedValues;
import options.valueHolders.ValueHolder;

public class TextOption extends OptionTree {
	private JTextField field;
	final private ArgumentDescriptor argument;
	/**
	 * the value of this option.
	 */
	private String text = "";
	/**
	 * the help for {@link #getHelpByArgument(ArgumentDescriptor)}
	 */
	final String help;
	/**
	 * the default text for this option or {@code null} if this option is
	 * mandatory.
	 */
	final String defaultText;

	/**
	 * create a new text option without a default value.
	 * 
	 * @param arg
	 *            the CLI argument.
	 * @param description
	 *            the {@link OptionTree#description description} for this
	 *            option.
	 * @param help
	 *            the text to display for help.
	 */
	public TextOption(String arg, String description, String help) {
		this(arg, null, description, help);
	}

	/**
	 * create a new text option with a default value.
	 * 
	 * @param arg
	 *            the CLI argument.
	 * @param defaultText
	 *            the default text if option is not provided.
	 * @param description
	 *            the {@link OptionTree#description description} for this
	 *            option.
	 * @param help
	 *            the text to display for help.
	 */
	public TextOption(String arg, String defaultText, String description,
			String help) {
		this.argument = new ArgumentDescriptor(AcceptedValues.ONE, arg, this);
		this.defaultText = defaultText;
		this.help = help;
		this.description = description;
		if (defaultText != null)
			text = defaultText;
	}

	/**
	 * get the value of this option.
	 * 
	 * @return the value of this option.
	 */
	public String getText() {
		assert field == null || field.getText().equals(text);
		return text;
	}

	/**
	 * set the value of this option (also update GUI).
	 * 
	 * @param v
	 *            the new value of this option.
	 */
	public void setValue(String v) {
		assert v != null;
		text = v;
		if (field != null) {
			if (!field.getText().equals(text)) {
				field.setText(text);
				field.validate();
			}
		}
		validateSelectedTree();
	}

	@Override
	protected void createMainComponent() {
		field = new JTextField(50);
		JPanel pane = new JPanel(new GridBagLayout());
		mainComponent = pane;
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.gridx = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		pane.add(new JLabel(description), c);
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_START;
		pane.add(field, c);

		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				setValue(field.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				setValue(field.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				setValue(field.getText());
			}
		});
		setValue(text);
	}

	@Override
	protected List<OptionTree> getSelectedChildren() {
		return getChildren();
	}

	@Override
	protected boolean isActivatedByArg(ArgumentValue arg) {
		return arg.getDescriptor().name.equals(argument.name);
	}

	@Override
	protected boolean setValueFromArg(ArgumentValue arg,
			PrintStream parsingErrorStream) {
		if (arg.getValues().isEmpty()) {
			parsingErrorStream.println(arg.getName() + " needs a value");
			return false;
		}
		setValue(arg.getValues().get(0));
		return true;
	}

	@Override
	protected void setValueFromSelectedChildren(
			List<OptionTree> selectedChildren) {
	}

	@Override
	protected ArgumentValue getSelectedArgument() {
		ArgumentValue arg = new ArgumentValue(argument);
		arg.addValue(text);
		return arg;
	}

	@Override
	protected ArgumentValue getDefaultValue() {
		if (defaultText == null)
			return null;
		ArgumentValue argV = new ArgumentValue(argument);
		argV.addValue(defaultText);
		return argV;
	}

	@Override
	protected List<ArgumentDescriptor> getAcceptedArguments() {
		return Arrays.asList(argument);
	}

	@Override
	public String getHelpByArgument(ArgumentDescriptor arg) {
		assert arg.equals(argument);
		return help;
	}

	@Override
	public ValueHolder<?, ?> getValueHolder() {
		// TODO update when value will be hold with a value holder
		return null;
	}

}
