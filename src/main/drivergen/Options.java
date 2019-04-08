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
 *     Karim HOSSEN
 *     Maxime MEIGNAN
 ********************************************************************************/
package main.drivergen;

import tools.GraphViz;

public class Options {
	public static final String VERSION = "1.0";
	public static final String NAME = "TIC";
	public static boolean CSS = false;
	public static boolean JS = false;
	public static int TIMEOUT = 10000;
	public static long LIMIT_TIME = Long.MAX_VALUE;
	
	public static boolean GRAPHVIZ = GraphViz.check() == 0;
	public static String OUTDIR = System.getProperty("user.dir");
	public static boolean LOG = false;
	public static boolean OPEN_LOG = false;
	
	public static String INPUT = "";

}
