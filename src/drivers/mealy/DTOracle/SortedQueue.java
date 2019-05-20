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
package drivers.mealy.DTOracle;

import java.util.ArrayList;

public class SortedQueue<T extends Test> {
	// TODO optimize
	private ArrayList<T> testPoints = new ArrayList<T>();

	void insert(T t) {
		testPoints.add(t);
	}

	T pollCheapest() {
		if (testPoints.isEmpty())
			return null;
		T highest = testPoints.remove(testPoints.size() - 1);
		for (int i = 0; i < testPoints.size(); i++) {
			if (testPoints.get(i).getCost() < highest.getCost()) {
				highest = testPoints.set(i, highest);
			}
		}
		return highest;
	}

	Integer cheapestCost() {
		if (testPoints.isEmpty())
			return null;
		int highest = testPoints.get(0).getCost();
		for (int i = 1; i < testPoints.size(); i++) {
			int depth = testPoints.get(i).getCost();
			if (depth < highest)
				highest = depth;
		}
		return highest;
	}

	public boolean isEmpty() {
		return testPoints.isEmpty();
	}

	public void clear() {
		testPoints.clear();
	}
}
