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
 ********************************************************************************/
package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import tools.loggers.LogManager;

public class Stats {
	private BufferedWriter writer = null;
	private File f;
	private int recordCount;
	private String filename = null;

	public Stats(String filename) {
		try {
			recordCount = 0;
			this.filename = filename;
			f = new File(filename);
			f.getAbsoluteFile().getParentFile().mkdirs();
			writer = new BufferedWriter(new FileWriter(f));
		} catch (IOException e) {
			LogManager.logException("Unable to create stats file (" + filename
					+ ")", e);
			writer = null;
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setHeaders(List<String> headers) {
		headers.add(0, "TestID");
		addHeaders(headers);
	}

	public void addHeaders(List<String> headers) {
		if (writer != null) {
			try {
				for (int i = 0; i < headers.size(); i++) {
					writer.write(headers.get(i));
					if (i < headers.size() - 1)
						writer.write(",");
				}
				writer.write("\n");
				writer.flush();
			} catch (IOException e) {
				LogManager.logException("Unable to write in stats file", e);
			}
		}
	}

	public void addRecord(List<String> records) {
		records.add(0, String.valueOf(++recordCount));
		addHeaders(records);
	}

	public void close() {
		try {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		} catch (IOException e) {
		}
	}
}