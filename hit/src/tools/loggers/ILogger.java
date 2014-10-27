package tools.loggers;

import java.util.Map;

import learner.efsm.table.LiControlTable;
import learner.efsm.table.LiDataTable;
import learner.efsm.tree.ZXObservationNode;
import learner.mealy.table.LmControlTable;
import learner.mealy.tree.ZObservationNode;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedOutput;

public interface ILogger {
	public void logControlTable(LiControlTable ct);

	public void logControlTable(LmControlTable ct);

	public void logDataTable(LiDataTable dt);

	public void logEnd();

	public void logReset();

	public void logError(String s);

	public void logException(String s, Exception e);
	
	public void logFatalError(String s);

	public void logInfo(String s);

	public void logRequest(ParameterizedInput pi, ParameterizedOutput po);

	public void logRequest(String input, String ouput);

	public void logStart();

	public void logStat(String s);

	public void logStep(int step, Object o);

	public void logData(String data);

	public void logTransition(String trans);

	public void logLine();

	public void logImage(String path);

	public void logConcrete(String data);

	public void logParameters(Map<String, Integer> params);

	public void logObservationTree(ZObservationNode root);

	public void logXObservationTree(ZXObservationNode root);
}
