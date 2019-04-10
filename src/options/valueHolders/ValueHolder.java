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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/**
 * This class is a base to store the value of a part of configuration of SIMPA.
 * 
 * The goal of this class is to provide a uniform base to represent values in
 * graphical interface.
 * 
 * @author Nicolas BREMOND
 *
 * @param <UserType>
 *            the type of value which seems to be stored in this class
 * @param <InnerType>
 *            the type of value actually stored in this class (can be more
 *            complex than UserType, but is the same type most of the time
 *            {@link SingleTypeValueHolder})
 */
public abstract class ValueHolder<UserType, InnerType> {
	/**
	 * The default value or {@code null} if there is no value defined by
	 * default.
	 */
	private UserType defaultValue;
	/**
	 * The current value stored.
	 */
	private InnerType value;
	/**
	 * The name to display for the representation of this value.
	 */
	private String name;
	/**
	 * The description of this value.
	 */
	private String description;
	/**
	 * The graphical component representing this value. Can be {@code null}
	 * before the graphical interface is used.
	 */
	JComponent valueComponent;

	/**
	 * @See {@link #addHandler(ValueChangeHandler)}
	 */
	private final List<ValueChangeHandler> changeHandlers = new ArrayList<>();

	ValueHolder(String name, String description) {
		this.name = name;
		this.description = description;
	}

	ValueHolder(String name, String description, UserType startValue) {
		this(name, description);
		value = UserToInnerType(startValue, null);
		assert InnerToUser(value).equals(startValue);
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Transforms a value represented as InnerType to the type used by user of
	 * this class.
	 * 
	 * @param i
	 *            the inner representation of a value.
	 * @return the user representation of the value provided.
	 */
	protected abstract UserType InnerToUser(InnerType i);

	/**
	 * Transform a value represented as user type to a value of inner type.
	 * 
	 * @param u
	 *            the value to transform.
	 * @param previousValue
	 *            a hint about the previous value. If not {@code null}, this can
	 *            be used to build the new value.
	 * @return the inner representation of the given value.
	 */
	protected abstract InnerType UserToInnerType(UserType u,
			InnerType previousValue);

	/**
	 * Set the current value of this object.
	 * 
	 * The value will be stored as {@code InnerType} and {@link #getValue()}
	 * might return a different value if the methods
	 * {@link #InnerToUser(InnerType)} and
	 * {@link #UserToInnerType(UserType, InnerType)} are not precise enough.
	 * 
	 * @param v
	 *            the value to store.
	 */
	public final void setValue(UserType v) {
		if (value == null && v == null)
			return;
		if (value != null && InnerToUser(value).equals(v))
			return;
		value = UserToInnerType(v, value);
		assert InnerToUser(value).equals(v);
		callChangeHandlers();
		updateWithValue();
	}

	/**
	 * Call handlers to indicate that the value as changed.
	 * 
	 * Children classes should call this method if and only if the value is a
	 * mutable object which is modified. This method is already called by
	 * {@link #setValue(UserType)}.
	 */
	protected final void callChangeHandlers() {
		for (ValueChangeHandler handler : changeHandlers) {
			handler.valueChanged();
		}
	}

	/**
	 * Classes extending this class should implement this method to update their
	 * representation of value with the value stored in this base class. This
	 * method is called when the value changed and also after constructing the
	 * graphical component.
	 */
	abstract protected void updateWithValue();

	/**
	 * set whether user is allowed to change this value or not (change at any
	 * time through graphical interface)
	 * 
	 * @param b
	 */
	final void setEnabled(boolean b) {
		if (valueComponent != null) {
			valueComponent.setEnabled(b);
			for (Component comp : valueComponent.getComponents())
				comp.setEnabled(b);
		}

	}

	/**
	 * add a handler to be called when the value change. It is not defined yet
	 * if it can be called also when value doesn't change (especially for
	 * initialization of object)
	 * 
	 * @param h
	 *            the handler to record
	 */
	public void addHandler(ValueChangeHandler h) {
		changeHandlers.add(h);
	}

	/**
	 * only create components, value will be set by {@link #updateWithValue()}
	 */
	protected abstract JComponent createMainComponent();

	public JComponent getComponent() {
		if (valueComponent == null) {
			valueComponent = createMainComponent();
			valueComponent.setToolTipText(description);
			updateWithValue();
		}
		return valueComponent;
	}

	/**
	 * Get the current value stored.
	 * 
	 * @return the current value stored.
	 */
	public UserType getValue() {
		return InnerToUser(value);
	}

	/**
	 * get the value stored as InnerType.
	 * 
	 * @return
	 */
	protected InnerType getInnerValue() {
		return value;
	}

	/**
	 * Get the default value. This value is used if the user does not provide
	 * any value for this object. This is mostly used for CLI as graphical
	 * interface will always have a representation of any value.
	 * 
	 * @return a value to use if the user does not provide any or {@code null}
	 *         if there is no default value.
	 */
	public UserType getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Set or delete default value.
	 * 
	 * @param def
	 *            the value to use if none is provided by user or {@code null}
	 *            to remove default value.
	 * @see #getDefaultValue() getDefaultValue() for more informations.
	 */
	public void setDefaultValue(UserType def) {
		defaultValue = def;
	}

}
