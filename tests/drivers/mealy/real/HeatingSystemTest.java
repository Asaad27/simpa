/********************************************************************************
 * Copyright (c) 2018,2019 Institut Polytechnique de Grenoble 
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
package drivers.mealy.real;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import automata.mealy.InputSequence;
import tools.StandaloneRandom;
import tools.Utils;
import tools.loggers.LogManager;

/**
 * This driver is an interface to an arduino-based heating manager. The original
 * manager is too complex but it can be simplified (reducing counters) to be
 * able to infer it.
 * 
 * @author Nicolas BREMOND
 *
 */
public class HeatingSystemTest {
}
