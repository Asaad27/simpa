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

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import options.valueHolders.ListHolder;
import options.valueHolders.Stringifyable;
import options.valueHolders.ValueChangeHandler;
import options.valueHolders.ValueHolder;

/**
 * Generic class for an option which can be provided several times.
 * 
 * @author Nicolas BREMOND
 *
 * @param <T>
 *            the type of element provided in each option
 */
public abstract class ListOption<T, H extends ValueHolder<T, ?> & Stringifyable>
		extends OptionTree {

	private ArgumentDescriptor argument;
	final ListHolder<T, H> value;

	public ListOption(String argument, String name, String description) {
		this.argument = new ArgumentDescriptor(
				ArgumentDescriptor.AcceptedValues.SEVERAL, argument, this);
		value = new ListHolder<T, H>(name, description) {

			@Override
			protected H createNewElement() {
				return createSimpleHolder();
			}

			@Override
			protected String getAddButtonText() {
				String optionTreeText = ListOption.this.getAddButtonText();
				if (optionTreeText != null)
					return optionTreeText;
				return super.getAddButtonText();
			}
		};
		value.addHandler(new ValueChangeHandler() {

			@Override
			public void valueChanged() {
				validateSelectedTree();
			}
		});
	}

	protected String getAddButtonText() {
		return null;
	}

	protected abstract H createSimpleHolder();

	/**
	 * Get the value represented by this option. The list cannot be modified.
	 * 
	 * @return the list of value represented by this option.
	 */
	public List<T> getValues() {
		return Collections.unmodifiableList(value.getValue());
	}

	@Override
	protected void createMainComponent() {
		mainComponent = value.getComponent();
	}

	@Override
	protected List<OptionTree> getSelectedChildren() {
		return getChildren();
	}

	@Override
	protected boolean isActivatedByArg(ArgumentValue arg) {
		return argument.name.equals(arg.getDescriptor().name);
	}

	@Override
	protected boolean setValueFromArg(ArgumentValue arg,
			PrintStream parsingErrorStream) {
		value.clear();
		try {
			for (String stringValue : arg.getValues()) {
				value.addElement(stringValue);
			}
		} catch (ParseException e) {
			parsingErrorStream.println(e.getMessage());
		}
		return true;
	}

	@Override
	protected void setValueFromSelectedChildren(
			List<OptionTree> selectedChildren) {
		assert selectedChildren.equals(getChildren());
	}

	@Override
	protected ArgumentValue getSelectedArgument() {
		ArgumentValue av = new ArgumentValue(argument);
		for (String s : value.getValueAsStrings(false))
			av.addValue(s);
		return av;
	}

	@Override
	protected List<ArgumentDescriptor> getAcceptedArguments() {
		return Arrays.asList(argument);
	}

	@Override
	public String getHelpByArgument(ArgumentDescriptor arg) {
		assert arg == argument;
		return createSimpleHolder().getDescription();
	}

	@Override
	public ListHolder<T, H> getValueHolder() {
		return value;
	}

}
