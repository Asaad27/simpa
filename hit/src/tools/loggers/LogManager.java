package tools.loggers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import tools.GraphViz;

import learner.efsm.table.LiControlTable;
import learner.efsm.table.LiDataTable;
import learner.efsm.tree.ZXObservationNode;
import learner.mealy.table.LmControlTable;
import learner.mealy.tree.ZObservationNode;
import main.simpa.Options;
import automata.State;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedOutput;
import detection.Reflection;
import drivers.efsm.real.GenericDriver;
import tools.Utils;

public class LogManager {
	private static DateFormat tfm = new SimpleDateFormat("[HH:mm:ss:SSS] ");

	public static final int STEPOTHER = -1;
	public static final int STEPNDV = 0;
	public static final int STEPNBP = 1;
	public static final int STEPNCR = 2;
	public static final int STEPNDF = 3;

	static ArrayList<ILogger> loggers = new ArrayList<ILogger>();
	static XSSLogger xssLogger;
	
	private static String prefix = "";

	public static void addLogger(ILogger logger) {
		loggers.add(logger);
	}
	
	public static void delLogger(ILogger logger){
		loggers.remove(logger);
	}
	
	public static void clearsLoggers(){
		loggers.clear();
	}

	public static void end() {
		logConsole("End");
		for (ILogger l : loggers)
			l.logEnd();
		if(Options.XSS_DETECTION){
			xssLogger.logEnd();
		}
		// System.exit(0);
	}
	
	public static void logFatalError(String s) {
		for (ILogger l : loggers)
			l.logFatalError(prefix+s);
	}

	public static void logControlTable(LmControlTable ct) {
		for (ILogger l : loggers)
			l.logControlTable(ct);
	}

	public static void logControlTable(LiControlTable ct) {
		for (ILogger l : loggers)
			l.logControlTable(ct);
	}

	public static void logDataTable(LiDataTable dt) {
		for (ILogger l : loggers)
			l.logDataTable(dt);
	}

	public static void logConsole(String s) {
		System.out.println(prefix + s);
		System.out.flush();
	}

	public static void logError(String s) {
		System.err.flush();
		System.err.println(tfm.format(new Date()) + prefix + s);
		for (ILogger l : loggers)
			l.logError(prefix + s);
	}

	public static void logException(String s, Exception e) {
		System.err.flush();
		System.err.println(tfm.format(new Date()) + prefix + s);
		e.printStackTrace(System.err);
	}

	public static void logInfo(String s) {
		for (ILogger l : loggers)
			l.logInfo(prefix + s);
	}

	public static void logInfo(Object... objects) {
		if (!loggers.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (Object o : objects)
				sb.append(o.toString());
			logInfo(sb.toString());
		}
	}

	public static void logWarning(String s) {
		System.err.println(prefixMultiLines("Warning : ", s));
		for (ILogger l : loggers)
			l.logWarning(prefix + s);
	}

	public static void logRequest(ParameterizedInput pi, ParameterizedOutput po) {
		for (ILogger l : loggers)
			l.logRequest(pi, po);
	}

	public static void logRequest(String input, String output, int n) {
		for (ILogger l : loggers)
			l.logRequest(input, output, n);
	}

	public static void logRequest(String input, String output, int n,
			State before, State after) {
		for (ILogger l : loggers)
			l.logRequest(input, output, n, before, after);
	}

	public static void start() {
		for (ILogger l : loggers)
			l.logStart();
		if(Options.XSS_DETECTION){
			xssLogger = new XSSLogger();
			xssLogger.logStart();
		}
	}

	public static void logReset() {
		for (ILogger l : loggers)
			l.logReset();
	}

	public static void logStat(String s) {
		for (ILogger l : loggers)
			l.logStat(prefix + s);
	}

	public static void logLine() {
		for (ILogger l : loggers)
			l.logLine();
	}

	public static void logStep(int step, Object o) {
		for (ILogger l : loggers)
			l.logStep(step, o);
	}

	public static void logTransition(String trans) {
		for (ILogger l : loggers)
			l.logTransition(prefix + trans);
	}

	public static void logData(String data) {
		for (ILogger l : loggers)
			l.logData(prefix + data);
	}

	public static void logImage(String path) {
		for (ILogger l : loggers)
			l.logImage(path);
	}

	public static void logDot(String dot, String name) {
		Writer writer = null;
		File file = null;
		File dir = Options.getDotDir();
		try {
			if (!dir.isDirectory() && !dir.mkdirs())
				throw new IOException("unable to create " + dir.getName()
						+ " directory");

			file = new File(dir.getPath() + File.separatorChar + name + ".dot");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("digraph G {\n");
			writer.write(dot);
			writer.write("}\n");
			writer.close();
			LogManager.logInfo("Conjecture has been exported to "
					+ file.getName());
			File imagePath = GraphViz.dotToFile(file.getPath());
			if (imagePath != null)
				logImage(imagePath.getPath());
		} catch (IOException e) {
			LogManager.logException("Error writing dot file", e);
		}
	}

	public static void logConcrete(String data) {
		for (ILogger l : loggers)
			l.logConcrete(prefix + data);
	}

	public static void logSymbolsParameters(Map<String, Integer> params) {
		for (ILogger l : loggers)
			l.logParameters(params);
	}

	public static void logObservationTree(ZObservationNode root) {
		for (ILogger l : loggers)
			if (GraphViz.IS_AVAILABLE)
				l.logObservationTree(root);
	}

	public static void logXObservationTree(ZXObservationNode root) {
		for (ILogger l : loggers)
			if (GraphViz.IS_AVAILABLE)
				l.logXObservationTree(root);
	}
	
	/**
	 * Logs a reflection found in the web application.
	 * Print the input sequence triggering the reflection
	 * @param r The data representing the reflection
	 * @param driver The GenericDriver used to translate abstract symbols into concrete requests
	 */
	public static void logFoundReflection(Reflection r, GenericDriver driver){
		xssLogger.logFoundReflection(r, driver);
	}

	/**
	 * Logs a XSS found in the web application. Print the input sequence
	 * used to introduce the payload and observe the result
	 *
	 * @param r The data representing the sequence used
	 * @param driver The GenericDriver used to translate abstract symbols into
	 * concrete requests
	 */
	public static void logFoundXSS(Reflection r, GenericDriver driver) {
		xssLogger.logFoundXSS(r, driver);
	}

	public static void setPrefix(String s){
		prefix = s;
	}
	
	public static String getPrefix(){
		return prefix;
	}

	public static String prefixMultiLines(String prefix, String message) {
		String otherLinePrefix = Utils.space(prefix.length());
		return prefix + message.replace("\n", "\n" + otherLinePrefix);
	}
}
