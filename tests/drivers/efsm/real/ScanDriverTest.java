/********************************************************************************
 * Copyright (c) 2014,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 *     Maxime MEIGNAN
 *     Nicolas BREMOND
 ********************************************************************************/
package drivers.efsm.real;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;

import main.drivergen.Options;

import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.BasicCredentialsProvider;

import tools.HTTPData;
import tools.loggers.LogManager;
import automata.efsm.Parameter;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import crawler.WebInput;
import crawler.WebInput.Type;
import drivers.efsm.EFSMDriverTest;

public class ScanDriverTest {

}
