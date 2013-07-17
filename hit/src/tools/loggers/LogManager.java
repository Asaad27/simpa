package tools.loggers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import learner.efsm.table.LiControlTable;
import learner.efsm.table.LiDataTable;
import learner.mealy.table.LmControlTable;
import learner.mealy.tree.ObservationNode;
import main.Options;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedOutput;

public class LogManager {
	private static DateFormat tfm = new SimpleDateFormat("[HH:mm:ss:SSS] ");

	public static final int STEPOTHER = -1;
	public static final int STEPNDV = 0;
	public static final int STEPNBP = 1;
	public static final int STEPNCR = 2;
	public static final int STEPNDF = 3;

	static ArrayList<ILogger> loggers = new ArrayList<ILogger>();

	public static void addLogger(ILogger logger) {
		loggers.add(logger);
	}

	public static void end() {
		logConsole("End");
		for (ILogger l : loggers)
			l.logEnd();
		// System.exit(0);
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
		if (!Options.TEST) {
			System.out.flush();
			System.out.println(tfm.format(new Date()) + s);
		}
	}

	public static void logError(String s) {
		System.err.flush();
		System.err.println(tfm.format(new Date()) + s);
	}

	public static void logException(String s, Exception e) {
		System.err.flush();
		System.err.println(tfm.format(new Date()) + s);
		e.printStackTrace(System.err);
		System.exit(1);
	}

	public static void logInfo(String s) {
		for (ILogger l : loggers)
			l.logInfo(s);
	}

	public static void logRequest(ParameterizedInput pi, ParameterizedOutput po) {
		for (ILogger l : loggers)
			l.logRequest(pi, po);
	}

	public static void logRequest(String input, String output) {
		for (ILogger l : loggers)
			l.logRequest(input, output);
	}

	public static void start() {
		for (ILogger l : loggers)
			l.logStart();
	}

	public static void logReset() {
		for (ILogger l : loggers)
			l.logReset();
	}

	public static void logStat(String s) {
		for (ILogger l : loggers)
			l.logStat(s);
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
			l.logTransition(trans);
	}

	public static void logData(String data) {
		for (ILogger l : loggers)
			l.logData(data);
	}

	public static void logImage(String path) {
		for (ILogger l : loggers)
			l.logImage(path);
	}

	public static void logConcrete(String data) {
		for (ILogger l : loggers)
			l.logConcrete(data);
	}

	public static void clear() {
		loggers.clear();
	}

	public static void logSymbolsParameters(Map<String, Integer> params) {
		for (ILogger l : loggers)
			l.logParameters(params);
	}

	public static void logObservationTree(ObservationNode root) {
		for (ILogger l : loggers)
			l.logObservationTree(root);
	}
}