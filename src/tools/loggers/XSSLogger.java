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
 *     Maxime MEIGNAN
 *     Nicolas BREMOND
 ********************************************************************************/
package tools.loggers;

import automata.efsm.ParameterizedInput;
import com.gargoylesoftware.htmlunit.WebRequest;
import detection.Reflection;
import drivers.efsm.real.GenericDriver;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.simpa.Options;

public class XSSLogger {

	private File file;
	private File dir;
	private DateFormat dateFormat;
	private DateFormat filenameFormat;
	private Writer writer;
	private String lineReturn;

	public XSSLogger() {
		lineReturn = System.getProperty("line.separator");
		dir = Options.getXSSLogDir();
		filenameFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
		dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		try {
			if (!dir.isDirectory()) {
				if (!dir.mkdirs()) {
					throw new IOException("unable to create "
							+ dir.getAbsolutePath() + " directory");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logStart() {
		try {
			file = new File(dir, filenameFormat.format(new Date()) + ".txt");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(dateFormat.format(new Date()) + lineReturn
					+ "XSS analysis results" + lineReturn + lineReturn);
			writer.flush();
		} catch (IOException ex) {
			Logger.getLogger(XSSLogger.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void logEnd() {
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logFoundReflection(Reflection r, GenericDriver driver) {
		Reflection reflection = r.clone();
		printLine("Reflection found ! Used sequence :");
		int i = 0;
		for (ParameterizedInput pi : reflection.path) {
			if(i == reflection.inputElementIndex){
				pi.getParameters().get(reflection.inputElementParamIndex).value = "REFLECTION_INPUT";
			}
			WebRequest request = driver.parameterizedInputToRequest(pi);
			printLine(request.getHttpMethod().toString() + " " + request.getUrl().toString() + " " + request.getRequestParameters(), 1);
			i++;
		}
		printLine();
	}
	
	public void logFoundXSS(Reflection r, GenericDriver driver) {
		Reflection reflection = r.clone();
		printLine("XSS found ! Used sequence :");
		for (ParameterizedInput pi : reflection.path) {
			WebRequest request = driver.parameterizedInputToRequest(pi);
			printLine(request.getHttpMethod().toString() + " " + request.getUrl().toString() + " " + request.getRequestParameters(), 1);
		}
		printLine();
	}

	private void printLine(String line, int indent) {
		try {
			String resultLine = line + lineReturn;
			for (int i = 0; i < indent; i++) {
				resultLine = "\t" + resultLine; 
			}
			writer.write(line + lineReturn);
			writer.flush();
		} catch (IOException ex) {
			Logger.getLogger(XSSLogger.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private void printLine(String line) {
		printLine(line, 0);
	}
	
	private void printLine(){
		printLine("");
	}
}
