/********************************************************************************
 * Copyright (c) 2015,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Maxime MEIGNAN
 ********************************************************************************/
package detection;

import automata.efsm.ParameterizedInput;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Class that caracterizes a reflection
 */
public class Reflection {

	/**
	 * The input sequence that triggers the reflection
	 */
	public final List<ParameterizedInput> path;
	/**
	 * The index of the parameterized input concerned by the reflection
	 */
	public final int inputElementIndex;
	/**
	 * The index of the input parameter concerned by the reflection
	 */
	public final int inputElementParamIndex;
	/**
	 * The index of the output parameter concerned by the reflection
	 */
	public final int outputElementParamIndex;
	/**
	 * The output symbol where we expect to find thr reflected value
	 */
	public final String expectedOutputSymbol;
	/**
	 * True if the reflection has been confirmed, or tested for XSS
	 */
	public boolean hasBeenTested = false;

	public Reflection(List<ParameterizedInput> path, int inputElementIndex, int inputElementParamIndex, int outputElementParamIndex, String outputSymbol) {
		this.path = path;
		this.inputElementIndex = inputElementIndex;
		this.inputElementParamIndex = inputElementParamIndex;
		this.outputElementParamIndex = outputElementParamIndex;
		this.expectedOutputSymbol = outputSymbol;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + Objects.hashCode(this.path);
		hash = 31 * hash + this.inputElementIndex;
		hash = 31 * hash + this.inputElementParamIndex;
		hash = 31 * hash + this.outputElementParamIndex;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Reflection other = (Reflection) obj;
		if (!Objects.equals(this.path, other.path)) {
			return false;
		}
		if (this.inputElementIndex != other.inputElementIndex) {
			return false;
		}
		if (this.inputElementParamIndex != other.inputElementParamIndex) {
			return false;
		}
		if (this.outputElementParamIndex != other.outputElementParamIndex) {
			return false;
		}
		return true;
	}

	@Override
	public Reflection clone() {
		List<ParameterizedInput> pathClone = new LinkedList<>();
		for (ParameterizedInput pi : path) {
			pathClone.add(pi.clone());
		}
		return new Reflection(pathClone, inputElementIndex, inputElementParamIndex, outputElementParamIndex, expectedOutputSymbol);
	}

	@Override
	public String toString() {
		return "{" + "path=" + path + ", expectedOutputSymbol=" + expectedOutputSymbol + '}';
	}

}
