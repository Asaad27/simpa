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
 *     Nicolas BREMOND
 ********************************************************************************/
package drivers.efsm.real;

import tools.HTTPRequest;
import tools.HTTPResponse;
import tools.TCPSend;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedOutput;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import drivers.efsm.EFSMDriver;

/*
 * Driver - Help
 * 
 * The driver class must extend driver.web.WebDriver.
 * The others classes is reserved for testing or other algorithms (not used in SPaCIoS)
 * 
 * For an example of a complete class see WGStoredXSSDriver.java
 * 
 * The contructor defines the ip address and port to access the system
 * Then it can calls initConnection to be able to connect to the system (Basic auth for Webgoat)
 * 
 * getOutputSymbols / getInputSymbols functions returns a list of symbols (String)
 * 
 * Each symbols have at least one parameter, the default values is specified by the getDefaultParamValues function.
 * And their name is defined in the getParameterNames function.
 * 
 * abstractToConcrete take ParameterizedInput (one symbol and its parameter) as input and return the corresponding HTTP request
 * 
 * concreteToAbstract take HTTPResponse as input and return the corresponding abstract response (one symbol and its parameter)
 *  
 */

public abstract class HighWebDriver extends EFSMDriver {
	public String systemHost;
	public int systemPort;

	public WebClient webClient;
	public HtmlPage currentPage;

	public HighWebDriver() {
		super(null);
		webClient = new WebClient();
		currentPage = null;
	}

	public HTTPResponse executeWeb(HTTPRequest req) {
		return new HTTPResponse(TCPSend.Send(systemHost, systemPort, req));
	}

	public ParameterizedOutput execute_implem(ParameterizedInput pi) {
		ParameterizedOutput po = concreteToAbstract(abstractToConcrete(pi));
		return po;
	}

	public abstract HtmlPage abstractToConcrete(ParameterizedInput pi);

	public abstract ParameterizedOutput concreteToAbstract(HtmlPage resp);
}
