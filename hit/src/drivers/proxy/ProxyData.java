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
package drivers.proxy;

public class ProxyData {
	String request = null;
	String response = null;
	int ready = OK;

	static int NOT_READY = -1;
	static int OK = 0;

	public ProxyData(int msg) {
		this.request = null;
		this.response = null;
		ready = NOT_READY;
	}

	public ProxyData(String req, String resp) {
		this.request = req;
		this.response = resp;
		ready = OK;
	}

	public String getRequest() {
		return request;
	}

	public String getResponse() {
		return response;
	}
}
