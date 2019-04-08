/********************************************************************************
 * Copyright (c) 2015,2019 Institut Polytechnique de Grenoble 
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
package tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import tools.loggers.LogManager;

public class GNUPlot{
   private static String GNUPLOT = "gnuplot";
   
   public static void makeGraph(String instructions)
   {    		
	   try {
		   Runtime rt = Runtime.getRuntime();
		   Process p = rt.exec(GNUPLOT);
		   p.getOutputStream().write(instructions.getBytes());
		   p.getOutputStream().close();
		    try {
	            final BufferedReader reader = new BufferedReader(
	                    new InputStreamReader(p.getErrorStream()));
	            String line = null;
	            while ((line = reader.readLine()) != null) {
					if (line.matches(
							"Warning: empty x range \\[[0-9.]+:[0-9.]+\\], adjusting to \\[[0-9.]+:[0-9.]+\\]"))
						continue;
	                System.out.println(line);
	            }
	            reader.close();
	        } catch (final Exception e) {
	            e.printStackTrace();
	        }
		   p.waitFor();         
	   }
	   catch (Exception e) {
		   LogManager.logException("Warning: creating GNUPlot graph", e);
		   return;
	   }
   }
}

