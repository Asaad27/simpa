/********************************************************************************
 * Copyright (c) 2011,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 ********************************************************************************/
package tools;

import java.util.HashMap;

public class CookieManager {
	public HashMap<String, String> cookies;

	public CookieManager() {
		cookies = new HashMap<String, String>();
	}

	public String get(String name) {
		return cookies.get(name);
	}

	public String set(String name, String value) {
		return cookies.put(name, value);
	}

	public String getCookieLine() {
		StringBuffer b = new StringBuffer();
		for (String key : cookies.keySet()) {
			b.append(key + "=" + cookies.get(key) + "; ");
		}
		if (!cookies.isEmpty())
			return b.substring(0, b.length() - 2);
		else
			return b.toString();
	}

	public void updateCookies(String line) {
		if (line != null) {
			String[] infos = line.trim().split("; ");
			String[] cookie;
			for (String info : infos) {
				cookie = info.split("=");
				if (!cookie[0].toLowerCase().equals("httponly")) {
					cookies.put(cookie[0].trim(), cookie[1].trim());
				}
			}
		}
	}

	public void reset() {
		cookies.clear();
	}

	public boolean isEmpty() {
		return cookies.size() == 0;
	}
}
