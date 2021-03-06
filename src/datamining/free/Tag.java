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

public class Tag {
	String m_attribute;
	String m_value;
	
	public Tag() {
		m_attribute = "";
		m_value = "";
	}
	
	public Tag(String attribute, String value) {
		m_attribute = new String(attribute);
		m_value = new String(filterStr(value));
	}
	
	public String getValue() {
		return m_value;
	}
	
	public String getAttribute() {
		return m_attribute;
	}
	
	public void setValue(String value) {
		m_value = new String(filterStr(value));
	}
	
	public void setAttribute(String attribute) {
		m_attribute = new String(filterStr(attribute));
	}
	
	private String filterStr(String str) {
		String [] t_str = str.split("'");
		if (t_str.length > 1)
			return t_str[1];
		else
			return t_str[0];
	}
	
	public String toString() {
		String str = "";
		str += "("+ m_attribute + " = " + filterStr(m_value)+ ")";
		return str;
	}
}
