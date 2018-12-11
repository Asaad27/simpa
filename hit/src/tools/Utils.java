package tools;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import automata.efsm.Parameter;
import automata.efsm.ParameterizedInputSequence;
import main.simpa.Options;
import tools.loggers.LogManager;

public class Utils {
	// Use Random instead of SecureRandom,
	// because for simulation needs, we do not need a crypto-strong generator.
	// Instead, we need good control of the seed.
	private static Random rand = new Random();
	
	public static void setSeed(long seed){
		rand.setSeed(seed);
	}

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
		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase().replace(' ', '_');
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
	
	public static List<HTTPData> generateCombinationOfSet(HashMap<String, ArrayList<String>> data){
		List<HTTPData> comb = new ArrayList<HTTPData>();
		List<String> nameList = new ArrayList<String>();
		List<ArrayList<String>> dataList = new ArrayList<ArrayList<String>>();
		
		for (String k : data.keySet()){
			nameList.add(k);
			dataList.add(data.get(k));			
		}
		
		generateCombinationOfSetRec(comb, nameList, dataList, new ArrayList<Integer>(data.size()));
		return comb;
	}

	private static void generateCombinationOfSetRec(List<HTTPData> comb,
			List<String> nameList, List<ArrayList<String>> dataList, ArrayList<Integer> list) {
		if (list.size() == nameList.size()){
			HTTPData d = new HTTPData();
			for(int i=0; i< list.size(); i++){
				d.add(nameList.get(i), dataList.get(i).get(list.get(i)));
			}
			comb.add(d);
		}else{
			for(int i=0; i<dataList.get(list.size()).size(); i++){
				list.add(i);
				generateCombinationOfSetRec(comb, nameList, dataList, list);
				list.remove(list.size()-1);
			}
		}		
	}	

	public static void copyFile(File in, File out) throws IOException {
		FileInputStream fin = new FileInputStream(in);
		FileOutputStream fou = new FileOutputStream(out);
		FileChannel inChannel = fin.getChannel();
		FileChannel outChannel = fou.getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
			fin.close();
			fou.close();
		}
	}
	
	public static void copyDir(final Path source, final Path target) throws IOException {
		Files.walkFileTree(source, new SimpleFileVisitor<Path>(){
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.copy(file, target.resolve(source.relativize(file)));
				return FileVisitResult.CONTINUE;
				
			}
			public FileVisitResult preVisitDirectory (Path dir, BasicFileAttributes attrs) throws IOException{
				Files.copy(dir, target.resolve(source.relativize(dir)));
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * @see #downloadFile(URL, boolean)
	 * @param url
	 *            the URL of the file to download.
	 * @return the downloaded file.
	 * @throws IOException
	 */
	public static File downloadWithCache(URL url) throws IOException {
		return downloadFile(url, true);
	}

	/**
	 * Download a file from URL or get it from the cache. There is no checking
	 * for modifications at this time.
	 * 
	 * @param url
	 *            the URL of the file to download.
	 * @param fromCache
	 *            indicate if the file should be reused from cache, if already
	 *            downloaded.
	 * @return the downloaded file.
	 * @throws IOException
	 */
	public static File downloadFile(URL url, boolean fromCache)
			throws IOException {
		File downloadCacheDir = new File(getSIMPACacheDirectory(), "Download");
		File endFile = new File(downloadCacheDir,
				url.getHost() + File.separator + url.getFile());
		if (endFile.exists() && fromCache)
			return endFile;
		URLConnection connection = url.openConnection();
		connection.connect();
		long size = connection.getContentLengthLong();
		endFile.getParentFile().mkdirs();
		System.out.print("Downloading " + url);
		System.out.flush();
		java.io.BufferedInputStream in = new java.io.BufferedInputStream(
				connection.getInputStream());
		java.io.FileOutputStream fos = new java.io.FileOutputStream(endFile);
		java.io.BufferedOutputStream bufferedFile = new BufferedOutputStream(
				fos);
		byte data[] = new byte[1024];
		int read;
		long pos = 0;
		while ((read = in.read(data, 0, 1024)) >= 0) {
			pos += read;
			bufferedFile.write(data, 0, read);
			System.out.print("\r");
			if (size != -1) {
				System.out.print((pos * 100 / size) + "% ");
			}
			System.out.print("Downloading " + url);
			System.out.flush();
		}
		System.out.println();
		bufferedFile.close();
		in.close();

		return endFile;
	}

	/**
	 * Get path of a cache directory. Directory will be created if it does not
	 * exists.
	 * 
	 * @return a directory trying to match a standard cache format.
	 */
	public static File getSIMPACacheDirectory() {
		String system = System.getProperty("deployment.user.cachedir");
		if (system != null)
			return new File(system + File.separator + "SIMPA");
		File nonStandardPath = null;
		if (isUnix()) {
			String home = System.getProperty("user.home");
			if (home != null)
				nonStandardPath = new File(home + File.separator + ".cache"
						+ File.separator + "SIMPA");
		}
		if (nonStandardPath == null) {
			nonStandardPath = new File(Options.OUTDIR + "SIMPA-cache");
		}
		System.err.println("cannot find standard cache path. Using '"
				+ nonStandardPath.getAbsolutePath() + "' instead.");
		nonStandardPath.mkdirs();
		nonStandardPath.deleteOnExit();
		return nonStandardPath;
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
	
	public static long randLong() {
		return rand.nextLong();
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

	/**
	 * change a list of string into a single string where all elements are
	 * separated with spaces (and spaces in elements are escaped)
	 * 
	 * @param arguments a non-empty list of string
	 * @return a string which can be transformed into {@code arguments} using
	 *         {@link #stringToList(String)}
	 */
	public static String listToString(List<String> arguments) {
		assert !arguments.isEmpty();
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < arguments.size(); i++) {
			String arg = arguments.get(i);
			s.append(arg.replaceAll("\\\\", "\\\\\\\\").replaceAll(" ",
					"\\\\ "));
			if (i + 1 != arguments.size())
				s.append(" ");
		}
		return s.toString();
	}

	/**
	 * split a string into a list of string using spaces as separators. escaped
	 * space are transformed into normal spaces.
	 * 
	 * @param args a string to split.
	 * @return a list of string which can be concatenated again using
	 *         {@link #listToString(List)}
	 */
	public static List<String> stringToList(String args) {
		List<String> r = new ArrayList<>();
		boolean backslashSeen = false;
		StringBuilder currentArg = new StringBuilder();
		for (char c : args.toCharArray()) {
			if (backslashSeen) {
				assert c == ' '
						|| c == '\\' : "only '\\' and ' ' characters can be escaped at this level";
				currentArg.append(c);
				backslashSeen = false;
			} else if (c == '\\') {
				backslashSeen = true;
			} else if (c == ' ') {
				r.add(currentArg.toString());
				currentArg = new StringBuilder();
			} else {
				currentArg.append(c);
				backslashSeen = false;
			}

		}
		r.add(currentArg.toString());
		return r;
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
	
	public static String randAlphaNumString(int size) {
		String charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder randomString = new StringBuilder();
		for (int i = 0; i < size; i++) {
			int index = rand.nextInt(charset.length());
			randomString.append(charset.charAt(index));
		};
		return randomString.toString();
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
	
	public static boolean isCSRFtoken(String ndv){
		return ndv.matches("^[a-zA-Z0-9+/\\.]{16,}(={0,2})?$");
	}
	
	public static ArrayList<ParameterizedInputSequence> generatePermutations(
										ParameterizedInputSequence querie, 
										int index, 
										Map<String, List<ArrayList<Parameter>>> defaultParamValues) {
		ArrayList<ParameterizedInputSequence> qlist = new ArrayList<ParameterizedInputSequence>();
		if (index == querie.getLength() - 1) {
			// System.out.println("Length: " + querie.getLength() + " ; last symbol: " + querie.getLastSymbol());
			for (int i = 0; i < defaultParamValues.get(querie.getLastSymbol()).size(); i++) {
				ParameterizedInputSequence pis = new ParameterizedInputSequence();
				pis.addParameterizedInput(querie.getLastSymbol(), defaultParamValues.get(querie.getLastSymbol()).get(i));
				qlist.add(pis);
			}
			return qlist;
		}
		ArrayList<ParameterizedInputSequence> sub_qlist = generatePermutations(querie, index + 1, defaultParamValues);
		for (int i = 0; i < defaultParamValues.get(querie.getSymbol(index)).size(); i++) {
			for (int j = 0 ; j < sub_qlist.size(); j++) { 
				ParameterizedInputSequence pis = new ParameterizedInputSequence();
				pis.addParameterizedInput(querie.getSymbol(index), defaultParamValues.get(querie.getSymbol(index)).get(i));
				for (int k = 0; k < sub_qlist.get(j).getLength(); k++) {
					pis.addParameterizedInput(sub_qlist.get(j).getSymbol(k), sub_qlist.get(j).getParameter(k)); 
				}
				qlist.add(pis);
				// System.out.println("Adding: " + pis);
			}
		}
		return qlist;	
	}
}
