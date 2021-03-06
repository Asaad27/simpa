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
 *     Maxime PEYRARD
 *     Emmanuel PERRIER
 *     Nicolas BREMOND
 ********************************************************************************/
package tools;

import java.io.File;
import java.io.IOException;

import tools.loggers.LogManager;

public class GraphViz{
   private static String DOT = "dot";
	public static final boolean IS_AVAILABLE;

	static {
		IS_AVAILABLE = check() == 0;
		if (!IS_AVAILABLE)
			LogManager.logWarning(
					"Unable to find GraphViz dot. Check your environment.");
	}
   
   public static File dotToFile(String filename)
   {
	   File img = null;
		if (IS_AVAILABLE) {
		   try {
			   File input = new File(filename);
			   img = new File(Utils.changeExtension(filename, "svg"));
			   Runtime rt = Runtime.getRuntime();  
			   String[] args = {DOT, "-Tsvg", input.getAbsolutePath(), "-o", img.getAbsolutePath()};
			   Process p = rt.exec(args);         
			   p.waitFor();         
		   }
		   catch (Exception e) {
			   LogManager.logException("Warning: converting dot to file", e);
			   return null;
		   }
	   }
	   return img;
   }
   
	public static int check() {
		Runtime rt = Runtime.getRuntime();
		Process p;
		try {
			String[] args = { DOT, "-V" };
			p = rt.exec(args);
			p.waitFor();
			return p.exitValue();
		} catch (IOException e) {
			try {
				DOT = Utils.exec("which dot");
				String[] newargs = { DOT, "-V" };
	            if (DOT != null){
	            	p = rt.exec(newargs);
	    			p.waitFor();
	    			return p.exitValue();
	            }
	            return 1;
			}catch (Exception f) {
				return 1;
			}
		} catch (Exception e) {
			return 2;
		}
	}

	/**
	 * encode an identifier to dot format (i.e. with a syntax accepted by the
	 * grammar) This version choose between using {@link #id2DotHtml(String)
	 * id2DotHtml} or {@link #id2DotSimpleText(String) id2SimpleText}
	 * 
	 * @param id
	 *            the identifier to encode
	 * @return a string ready to put in the dot file
	 */
	public static String id2DotAuto(String id) {
		if (id.replace("<", "").length() == id.replace(">", "").length()
				&& id.contains("<"))// this test for detecting HTML contents can
									// be largely improved
			return id2DotHtml(id);
		else
			return id2DotSimpleText(id);
	}

	/**
	 * @see #id2DotAuto(String)
	 */
	public static String id2DotHtml(String id) {
		return "<" + id + ">";
	}

	/**
	 * @see #id2DotAuto(String)
	 */
	public static String id2DotSimpleText(String id) {
		return "\"" + id.replace("\"", "\\\"") + "\"";
	}

}

