package tools.loggers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LogManager {
	private static DateFormat tfm = new SimpleDateFormat("[HH:mm:ss:SSS] ");


	static ArrayList<ILogger> loggers = new ArrayList<ILogger>();
	
	public static void addLogger(ILogger logger){
		loggers.add(logger);
	}
	
	public static void end() {
		logConsole("End");
		for (ILogger l : loggers) l.logEnd();
		//System.exit(0);
	}
	
	public static void logConsole(String s){
		System.out.flush();
		System.out.println(tfm.format(new Date()) + s);
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
		for (ILogger l : loggers) l.logInfo(s);
	}

	public static void start() {
		for (ILogger l : loggers) l.logStart();		
	}
	
	public static void logReset() {
		for (ILogger l : loggers) l.logReset();		
	}
	
	public static void logStat(String s) {
		for (ILogger l : loggers) l.logStat(s);		
	}
	
	public static void logLine() {
		for (ILogger l : loggers) l.logLine();		
	}
	
	public static void logTransition(String trans) {
		for (ILogger l : loggers) l.logTransition(trans);		
	}

	public static void logData(String data) {
		for (ILogger l : loggers) l.logData(data);		
	}

	public static void logImage(String path) {
		for (ILogger l : loggers) l.logImage(path);
	}

	public static void logConcrete(String data) {
		for( ILogger l : loggers) l.logConcrete(data);
	}

	public static void clear() {
		loggers.clear();
	}
}