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
 ********************************************************************************/
package tools;

/**
 * Get this interface from the nist-sip IM
 * 
 * @author olivier deruelle
 */
public interface ClientAuthenticationMethod {

	/**
	 * Initialize the Client authentication method. This has to be done outside
	 * the constructor.
	 * 
	 * @throws Exception
	 *             if the parameters are not correct.
	 */
	public void initialize(String realm, String userName, String uri,
			String nonce, String password, String method, String cnonce,
			String algorithm) throws Exception;

	/**
	 * generate the response
	 * 
	 * @returns null if the parameters given in the initialization are not
	 *          correct.
	 */
	public String generateResponse();

}
