/********************************************************************************
 * Copyright (c) 2012,2019 Institut Polytechnique de Grenoble 
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
package drivers.mealy.real;

import gov.nist.javax.sip.header.ProxyAuthenticate;
import gov.nist.javax.sip.header.WWWAuthenticate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;

import tools.DigestClientAuthenticationMethod;
import tools.UDPSend;
import tools.Utils;
import tools.loggers.LogManager;

public class SIPDriverIPTelTest {
}
