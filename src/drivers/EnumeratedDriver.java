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
package drivers;

/**
 * An interface for driver enumerating all possibles automata matching given
 * requirements.
 * 
 * @author Nicolas BREMOND
 *
 */
public interface EnumeratedDriver {
	/**
	 * Get the seed used to build the automata.
	 * 
	 * @return the seed used to build the automata.
	 */
	long getSeed();
}
