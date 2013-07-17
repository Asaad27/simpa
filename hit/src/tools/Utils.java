package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import main.Options;
import tools.loggers.LogManager;

public class Utils {
	private static Random rand = new Random();

	public static boolean isWindows() {
		return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
	}

	public static boolean isMac() {
		return (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0);
	}

	public static boolean isUnix() {
		String OS = System.getProperty("os.name").toLowerCase();
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS
				.indexOf("aix") > 0);
	}

	public static boolean isSolaris() {
		return (System.getProperty("os.name").toLowerCase().indexOf("sunos") >= 0);
	}

	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?");
	}

	public static boolean is64bit() {
		return (System.getenv("ProgramW6432") != null);
	}

	public static int getStatusCode(String response) {
		String[] resp = response.split("\n");
		String[] status = resp[0].trim().split(" ");
		return Integer.parseInt(status[1]);
	}

	public static String nextSymbols(String current) {
		for (int i = current.length() - 1; i >= 0; i--) {
			if (current.charAt(i) == 'z') {
				current = resetCharAt(current, i);
				if (i == 0)
					return "a" + current;
			} else {
				current = incCharAt(current, i);
				break;
			}
		}
		return current;
	}

	public static String decapitalize(String s) {
		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}

	public static String resetCharAt(String s, int pos) {
		StringBuffer buf = new StringBuffer(s);
		buf.setCharAt(pos, 'a');
		return buf.toString();
	}

	public static String incCharAt(String s, int pos) {
		StringBuffer buf = new StringBuffer(s);
		buf.setCharAt(pos, (char) (buf.charAt(pos) + 1));
		return buf.toString();
	}

	public static String capitalize(String s) {
		if (s.length() == 0)
			return s;
		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	}

	public static String exec(String cmd) {
		String output = null;
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader input = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			output = input.readLine();
			input.close();
		} catch (Exception e) {
		}
		return output;
	}

	public static String escapeTags(String original) {
		if (original == null)
			return "";
		StringBuffer out = new StringBuffer("");
		char[] chars = original.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			boolean found = true;
			switch (chars[i]) {
			case 60:
				out.append("&lt;");
				break; // <
			case 62:
				out.append("&gt;");
				break; // >
			case 34:
				out.append("&quot;");
				break; // "
			default:
				found = false;
				break;
			}
			if (!found)
				out.append(chars[i]);

		}
		return out.toString();

	}

	public static boolean randBoolWithPercent(int p) {
		return rand.nextInt(100) < p;
	}

	public static void copyFile(File in, File out) throws IOException {
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

	public static int randIntBetween(int a, int b) {
		if (a == b)
			return a;
		else if (a > b) {
			a -= b;
			b += a;
			a = b - a;
		}
		return rand.nextInt(b - a + 1) + a;
	}

	public static <T> T randIn(List<T> l) {
		if (l.isEmpty())
			return null;
		else
			return l.get(rand.nextInt(l.size()));
	}

	public static String filter(Object s) {
		return s.toString().replaceAll(Options.SYMBOL_AND, "&")
				.replaceAll(Options.SYMBOL_NOT_EQUAL, "!=")
				.replaceAll(Options.SYMBOL_OR, "|").replaceAll(" saved", " ");
	}

	public static <T> String joinAndClean(List<T> l, String sep) {
		String res = null;
		if (!l.isEmpty())
			res = filter(l.get(0).toString());
		for (int i = 1; i < l.size(); i++)
			res += sep + filter(l.get(i).toString());
		return res;
	}

	public static <T> T randIn(T l[]) {
		if (l.length == 0)
			return null;
		else
			return l[rand.nextInt(l.length)];
	}

	public static int randInt(int max) {
		return rand.nextInt(max);
	}

	@SuppressWarnings("unchecked")
	public static <T> T randIn(Set<T> l) {
		if (l.size() > 0)
			return (T) l.toArray()[rand.nextInt(l.size())];
		else
			return null;
	}

	public static void browse(File log) {
		String os = System.getProperty("os.name").toLowerCase();
		Runtime rt = Runtime.getRuntime();
		try {
			if (os.indexOf("win") >= 0) {
				rt.exec("rundll32 url.dll,FileProtocolHandler "
						+ log.getAbsolutePath());
			} else if (os.indexOf("mac") >= 0) {
				rt.exec("open " + log.getAbsolutePath());
			} else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
				String[] browsers = { "epiphany", "firefox", "mozilla",
						"konqueror", "netscape", "opera", "links", "lynx" };
				StringBuffer cmd = new StringBuffer();
				for (int i = 0; i < browsers.length; i++)
					cmd.append((i == 0 ? "" : " || ") + browsers[i] + " \""
							+ log.getAbsolutePath() + "\" ");
				rt.exec(new String[] { "sh", "-c", cmd.toString() });
			}
		} catch (Exception e) {
			LogManager.logException("Unable to start the browser", e);
		}
	}

	public static boolean deleteDir(File path) {
		boolean resultat = true;
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					resultat &= deleteDir(files[i]);
				} else {
					resultat &= files[i].delete();
				}
			}
		}
		resultat &= path.delete();
		return (resultat);
	}

	public static boolean createDir(File dir) {
		if (dir != null && !dir.isDirectory()) {
			dir.mkdirs();
		}
		return dir.isDirectory();
	}

	public static boolean cleanDir(File dir) {
		return deleteDir(dir) && createDir(dir);
	}

	public static String changeExtension(String originalName,
			String newExtension) {
		int lastDot = originalName.lastIndexOf(".");
		if (lastDot != -1) {
			return originalName.substring(0, lastDot) + "." + newExtension;
		} else {
			return originalName + "." + newExtension;
		}
	}

	@SafeVarargs
	public static <T> ArrayList<T> createArrayList(T... elements) {
		ArrayList<T> list = new ArrayList<T>();
		for (T element : elements) {
			list.add(element);
		}
		return list;
	}

	public static final String escapeHTML(String source) {
		return source.replaceAll("<", "&lt;").replaceAll(">", "&gt;")
				.replaceAll("\n", "<br/>").replaceAll(" ", "&nbsp;");
	}

	public static String removeExtension(String s) {
		String separator = "" + File.separatorChar;
		String filename;
		int lastSeparatorIndex = s.lastIndexOf(separator);
		if (lastSeparatorIndex == -1) {
			filename = s;
		} else {
			filename = s.substring(lastSeparatorIndex + 1);
		}
		int extensionIndex = filename.lastIndexOf(".");
		if (extensionIndex == -1)
			return filename;
		return filename.substring(0, extensionIndex);
	}

	public static String makePath(String absolutePath) {
		if (!absolutePath.endsWith(File.separator))
			absolutePath += File.separator;
		return absolutePath;
	}

	public static float meanOfCSVField(String filename, int i) {
		try {
			float mean = 0;
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String strLine = "";
			StringTokenizer st = null;
			int lineNumber = 0;
			br.readLine();
			while ((strLine = br.readLine()) != null) {
				st = new StringTokenizer(strLine, ",");
				int token = 0;
				while (token < i) {
					token++;
					st.nextToken();
				}
				String c = (st.hasMoreTokens() ? st.nextToken() : null);
				if (c != null)
					mean += Float.parseFloat(c);
				lineNumber++;
			}
			br.close();
			return mean / lineNumber;
		} catch (Exception e) {
			return -1;
		}
	}

	public static float percentOfCSVField(String filename, int i, String value) {
		try {
			float nb = 0;
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String strLine = "";
			StringTokenizer st = null;
			int lineNumber = 0;
			br.readLine();
			while ((strLine = br.readLine()) != null) {
				st = new StringTokenizer(strLine, ",");
				int token = 0;
				while (token < i) {
					token++;
					st.nextToken();
				}
				String c = (st.hasMoreTokens() ? st.nextToken() : null);
				if (c != null && c.equals(value))
					nb++;
				lineNumber++;
			}
			br.close();
			return 100 * nb / lineNumber;
		} catch (Exception e) {
			return -1;
		}
	}

	public static String space(int length) {
		String s = "";
		for (int i = 0; i < length; i++)
			s += " ";
		return s;
	}

	public static String fileContentOf(String filename) {
		String content = "";
		try {

			BufferedReader br = new BufferedReader(new FileReader(filename));
			String strLine = "";
			while ((strLine = br.readLine()) != null) {
				content += strLine + "\n";
			}
			br.close();
		} catch (Exception e) {
		}
		return content;
	}

	public static String randString() {
		return "random" + Utils.randInt(1000);
	}

	public static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	public static void saveToFile(String s, String filename) {
		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(s);
			out.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}
