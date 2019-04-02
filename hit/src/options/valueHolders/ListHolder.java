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

import java.util.ArrayList;
import java.util.List;

public abstract class ListHolder<E, H extends ValueHolder<E, ?> & Stringifyable>
		extends SequenceHolder<E, List<E>, H> implements Stringifyable {

	public ListHolder(String name, String description) {
		this(name, description, ',', '\\');
	}

	public ListHolder(String name, String description, char separator,
			char escape) {
		super(name, description, new ArrayList<>(), separator, escape);
	}

	@Override
	public void clear() {
		setValue(new ArrayList<>());
	}

	@Override
	public void addElementFromString(E element) {
		List<E> newValue = new ArrayList<>(getValue());
		newValue.add(element);
		setValue(newValue);
	}

	@Override
	protected List<E> InnerToUser(List<H> inner) {
		return holdersTypeToList(inner);
	}

	@Override
	protected List<H> UserToInnerType(List<E> user, List<H> previousValue) {
		return listToHolder(user, previousValue);
	}

}
