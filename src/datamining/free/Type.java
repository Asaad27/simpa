/********************************************************************************
 * Copyright (c) 2013,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Maxime PEYRARD
 *     Emmanuel PERRIER
 *     Karim HOSSEN
 ********************************************************************************/
package datamining.free;

public class Type {
	String type;
	
	public Type() {
		type = "STRING";
	}
	
	public Type(String t) {
		this.type = new String(t);
	}
	
	public String get() {
		return type;
	}
	
	public String toString() {
		return type;
	}
}
