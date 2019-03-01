package options.valueHolders;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

public abstract class ValueHolder<UserType, InnerType> {
	/**
	 * The default value or {@code null} if there is no value defined by
	 * default.
	 */
	private UserType defaultValue;
	private InnerType value;
	private String name;
	private String description;
	JComponent valueComponent;

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

	protected abstract UserType InnerToUser(InnerType i);

	protected abstract InnerType UserToInnerType(UserType u,
			InnerType previousValue);

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

	protected final void callChangeHandlers() {
		for (ValueChangeHandler handler : changeHandlers) {
			handler.valueChanged();
		}
	}

	/**
	 * called when the value is changed
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

	public UserType getValue() {
		return InnerToUser(value);
	}

	protected InnerType getInnerValue() {
		return value;
	}

	public UserType getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(UserType def) {
		defaultValue = def;
	}

}
