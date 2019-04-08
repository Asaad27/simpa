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
package options.automataOptions;

import java.lang.reflect.InvocationTargetException;

import drivers.Driver;
import options.OneArgChoiceOptionItem;

public class DriverChoiceItem<T extends Driver<?, ?>>
		extends OneArgChoiceOptionItem {
	Class<? extends T> driverClass;

	public DriverChoiceItem(String displayName, String argument,
			DriverChoice<?> parent, Class<? extends T> driverClass) {
		super(displayName, argument, parent);
		this.driverClass = driverClass;
		assert parent.driverBaseType.isAssignableFrom(driverClass);
	}

	public DriverChoiceItem(DriverChoice<?> parent,
			Class<? extends T> driverClass) {
		this(driverClass.getSimpleName(), driverClass.getName(), parent,
				driverClass);
	}

	/**
	 * this function can be overridden to construct complex drivers.
	 * 
	 * @return a driver.
	 */
	public T createDriver() {
		try {
			return driverClass.getConstructor().newInstance();
		} catch (InstantiationException | IllegalArgumentException
				| IllegalAccessException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			System.out.println("Unable to instanciate the driver : "
					+ driverClass.getName());
			return null;
		}

	}

}
