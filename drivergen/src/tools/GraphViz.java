package tools;

import java.io.File;
import java.io.IOException;

import main.drivergen.Options;
import tools.loggers.LogManager;

public class GraphViz {
	private static String DOT = "dot";

	public static File dotToFile(String filename) {
		File img = null;
		if (Options.GRAPHVIZ) {
			try {
				File input = new File(filename);
				img = new File(Utils.changeExtension(filename, "svg"));
				Runtime rt = Runtime.getRuntime();
				String[] args = { DOT, "-Tsvg", input.getAbsolutePath(), "-o",
						img.getAbsolutePath() };
				Process p = rt.exec(args);
				p.waitFor();
			} catch (Exception e) {
				LogManager.logException("Warning: converting dot to file", e);
				return null;
			}
		}
		return img;
	}

	private static int checkExe(String exe, String arguments) {
		Runtime rt = Runtime.getRuntime();
		Process p;
		try {
			String[] args = { exe, arguments };
			p = rt.exec(args);
			p.waitFor();
			return p.exitValue();
		} catch (IOException e) {
			return 1;
		} catch (Exception e) {
			return 2;
		}
	}

	public static int check() {
		if (checkExe(DOT, "-V") != 0) {
			if (Utils.isWindows()) {
				File programFile = null;
				String[] possibleVersions = null;
				if (Utils.is64bit())
					programFile = new File(System.getenv("ProgramFiles(X86)"));
				else
					programFile = new File(System.getenv("ProgramFiles"));
				possibleVersions = programFile.list();
				if (possibleVersions != null && possibleVersions.length > 0) {
					int iLastVersion = -1;
					for (int i = 0; i < possibleVersions.length; i++) {
						if (possibleVersions[i].startsWith("Graphviz ")) {
							if (iLastVersion == -1)
								iLastVersion = i;
							else {
								float v1 = Float.parseFloat(possibleVersions[i]
										.substring(9));
								float v2 = Float
										.parseFloat(possibleVersions[iLastVersion]
												.substring(9));
								if (v1 > v2)
									iLastVersion = i;
							}
						}
					}
					DOT = programFile.getAbsolutePath() + File.separator
							+ possibleVersions[iLastVersion] + File.separator
							+ "bin" + File.separator + "dot.exe";
					return checkExe(DOT, "-V");
				} else {
					return 3;
				}
			} else {
				DOT = Utils.exec("which dot");
				return checkExe(DOT, "-V");
			}
		}
		return 0;
	}
}
