package tools.loggers;

import java.util.Map;

public interface ILogger {
	public void logEnd();
	public void logReset();
	public void logError(String s);
	public void logException(String s, Exception e);
	public void logInfo(String s);
	public void logStart();
	public void logStat(String s);
	public void logData(String data);
	public void logTransition(String trans);
	public void logLine();
	public void logImage(String path);
	public void logConcrete(String data);
	public void logParameters(Map<String, Integer> params);
}
