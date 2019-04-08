/********************************************************************************
 * Copyright (c) 2019 Institut Polytechnique de Grenoble 
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
package drivers.mealy.real.mqtt;

import java.util.ArrayList;
import java.util.List;

/**
 * a class representing the options for building one {@link MQTTClient}.
 * 
 * @author Nicolas BREMOND
 *
 */
class MQTTClientDescriptor {
	/**
	 * The id to use when connecting to the broker. Will be automatically filled
	 * if empty.
	 */
	String id = "";
	boolean connect = false;
	boolean disconnect = false;
	Boolean close = false;

	class Publish {
		String topic;
		String message;
		Boolean retain;
	}

	List<Publish> connectWithWill = new ArrayList<>();
	List<Publish> publish = new ArrayList<>();
	List<String> deleteRetain = new ArrayList<>();

	List<String> subscribe = new ArrayList<>();
	List<String> unsubscribe = new ArrayList<>();

}
