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
 *     Nicolas BREMOND
 ********************************************************************************/
package main.simpa;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import tools.Stats;
import tools.Utils;
import tools.loggers.LogManager;

public class SIMPAStats {
	public final static String name = "SIMPAStats";

	private static void welcome() {
		System.out.println(name + " - "
				+ new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
		System.out.println();
	}

	public static void main(String[] args) throws IOException {
		//Options.STAT = true;
		welcome();
		String dir = Options.OUTDIR;

		try {
			Stats stat = new Stats("global.csv");
			stat.setHeaders(Utils.createArrayList("State", "Requests",
					"Duration", "Transitions"));
			int[] states = { 50, 100, 200, 300, 500, 750, 1000, 2000, 3000, 5000, 7500, 10000 };
			for (int i : states) {
				Options.MINSTATES = i;
				Options.MAXSTATES = i;

				System.out.println("|+] State = " + i);

				SIMPATestMealy.main(args);

				stat.addRecord(Utils.createArrayList(
						String.valueOf(i),
						String.valueOf(Utils.meanOfCSVField(Options.DIRTEST
								+ File.separator + "stats.csv", 5)),
						String.valueOf(Utils.meanOfCSVField(Options.DIRTEST
								+ File.separator + "stats.csv", 6)),
						String.valueOf(Utils.meanOfCSVField(Options.DIRTEST
								+ File.separator + "stats.csv", 7))));

				Options.OUTDIR = dir;
			}
			stat.close();
		} catch (Exception e) {
			LogManager.logException("Unexpected error at test", e);
		}
	}
}
