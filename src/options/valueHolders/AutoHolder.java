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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

public abstract class AutoHolder<T, H extends ValueHolder<T, T>>
		extends SingleTypeValueHolder<T> {

	final H baseHolder;
	final BooleanHolder useAuto;

	/**
	 * In the case were autoValue is allowed, this method indicate whether the
	 * user wants to use the value he set or the auto value.
	 */
	public boolean useAutoValue() {
		assert useAuto != null;
		return useAuto.getValue();
	}

	/**
	 * a String describing the use of default value. e.g. "use size of set as
	 * value for this option". A {@code null} String indicate that this option
	 * do not provide auto value.
	 */
	String autoValueLabel = null;

	public AutoHolder(H baseHolder) {
		super(baseHolder.getName(), baseHolder.getDescription(),
				baseHolder.getValue());
		this.baseHolder = baseHolder;
		useAuto = new BooleanHolder(
				"use automatic value for " + baseHolder.getName(),
				"If enabled, SIMPA will try to automatically choose a value.");
		setValue(baseHolder.getValue());
		useAuto.setValue(true);

		useAuto.addHandler(new ValueChangeHandler() {

			@Override
			public void valueChanged() {
				updateWithValue();
			}
		});
		baseHolder.addHandler(new ValueChangeHandler() {

			@Override
			public void valueChanged() {
				if (!useAutoValue()) {
					setValue(baseHolder.getValue());
				} else {
					updateWithValue();
				}
			}
		});
		addHandler(new ValueChangeHandler() {

			@Override
			public void valueChanged() {
				baseHolder.setValue(getValue());
				assert baseHolder.getValue().equals(
						getValue()) : "the value set is not compatible with base holder";
				setValue(baseHolder.getValue());
			}
		});
		updateWithValue();
	}

	@Override
	protected JComponent createMainComponent() {
		JPanel mainComponent = new JPanel();
		mainComponent.setLayout(new BoxLayout(mainComponent, BoxLayout.Y_AXIS));
		JPanel firstLineComponent = new JPanel();
		firstLineComponent
				.setLayout(new BoxLayout(firstLineComponent, BoxLayout.X_AXIS));
		firstLineComponent.add(useAuto.getComponent());
		firstLineComponent.add(Box.createGlue());

		JPanel secondLineComponent = new JPanel();
		secondLineComponent.setLayout(
				new BoxLayout(secondLineComponent, BoxLayout.X_AXIS));
		secondLineComponent.add(Box.createHorizontalStrut(20));
		secondLineComponent.add(baseHolder.getComponent());
		secondLineComponent.add(Box.createGlue());

		mainComponent.add(firstLineComponent);
		mainComponent.add(secondLineComponent);
		return mainComponent;
	}

	@Override
	protected void updateWithValue() {
		assert getValue() != null;
		assert baseHolder != null;
		assert useAuto != null;
		baseHolder.setValue(getValue());
		baseHolder.setEnabled(!useAutoValue());
	}

	public void setValueNonAuto(T v) {
		useAuto.setValue(false);
		super.setValue(v);
		assert getValue().equals(v);
	}
}
