package options.valueHolders;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

public abstract class ValueHolder<T> {
	private T value;
	private String name;
	private String description;
	JComponent valueComponent;

	private final List<ValueChangeHandler> changeHandlers = new ArrayList<>();

	ValueHolder(String name, String description) {
		this.name = name;
		this.description = description;
	}

	ValueHolder(String name, String description, T startValue) {
		this(name, description);
		value = startValue;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public final void setValue(T v) {
		if (value == null && v == null)
			return;
		if (value != null && value.equals(v))
			return;
		value = v;
		for (ValueChangeHandler handler : changeHandlers) {
			handler.valueChanged();
		}
		updateWithValue();
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

	public T getValue() {
		return value;
	}

}
