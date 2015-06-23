package learner.mealy.noReset;

import learner.Learner;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import learner.mealy.noReset.dataManager.DataManager;
import learner.mealy.noReset.dataManager.FullyQualifiedState;
import drivers.mealy.MealyDriver;

import java.util.ArrayList;
import java.util.Set;

import tools.loggers.LogManager;

public class NoResetLearner extends Learner {
	private MealyDriver driver;
	private DataManager dataManager;

	public NoResetLearner(MealyDriver d){
		driver = d;
	}
	
	public void learn(){
		LogManager.logInfo("Inferring the system");
		LogManager.logConsole("Inferring the system");
		
		//TODO getW;
		ArrayList<ArrayList<String>> W = new ArrayList<ArrayList<String>>();//Characterization set
		W.add(new ArrayList<String>());
		W.add(new ArrayList<String>());
		W.get(0).add("a");
		W.get(1).add("b");
		StringBuilder logW = new StringBuilder("Used characterization set : [");
		for (ArrayList<String> w : W){
			for (String wi : w)
				logW.append(" " + wi);
			logW.append(",");
		}
		logW.append("]");
		LogManager.logInfo(logW.toString());
		
		//GlobalTrace trace = new GlobalTrace(driver);
		dataManager = new DataManager(driver, W);
		
		//start of the algorithm
		localize(dataManager, W);
		
		while (!dataManager.isFullyKnown()){
			LogManager.logLine();
			int qualifiedStatePos;
			LmTrace sigma;
			if (dataManager.getC(dataManager.traceSize()) != null){
				FullyQualifiedState q = dataManager.getC(dataManager.traceSize());
				LogManager.logInfo("We already know the curent state (q = " + q + ")");	
				ArrayList<String> alpha = dataManager.getShortestAlpha(q);
				dataManager.apply(alpha);
				dataManager.updateCKVT();//to get the new state, should be automated in 
				assert dataManager.getC(dataManager.traceSize()) != null;
				qualifiedStatePos = dataManager.traceSize();
				FullyQualifiedState quallifiedState = dataManager.getC(qualifiedStatePos);
				Set<String> X = dataManager.getxNotInR(quallifiedState);
				String x=X.iterator().next(); //here we CHOOSE to take the first
				LogManager.logInfo("We choose x = " + x + " in " + X);		
				String o = dataManager.apply(x);
				sigma = new LmTrace(x,o);
				LogManager.logInfo("So sigma = " + sigma);	
				assert dataManager.getC(dataManager.traceSize()) == null : "we are trying to quallify this state, that should not be already done.";
			}else{
				LogManager.logInfo("We don't know the curent state");	
				qualifiedStatePos = dataManager.traceSize()-1;
				while (dataManager.getC(qualifiedStatePos) == null)
					qualifiedStatePos --;
				LogManager.logInfo("last quallified state is " + dataManager.getC(qualifiedStatePos));
				sigma = dataManager.getSubtrace(qualifiedStatePos,dataManager.traceSize());
				LogManager.logInfo("We got sigma = "+ sigma);
			}
			FullyQualifiedState q = dataManager.getC(qualifiedStatePos);
			ArrayList<ArrayList<String>> allowed_W = dataManager.getwNotInK(q, sigma);
			ArrayList<String> w = allowed_W.get(0); //here we CHOOSE to take the first.
			LogManager.logInfo("We choose w = " + w + " in " + allowed_W);		
			int newStatePos = dataManager.traceSize();
			dataManager.apply(w);
			LogManager.logInfo("We found that " + q + " followed by " + sigma + "give " +dataManager.getSubtrace(newStatePos, dataManager.traceSize()));
			dataManager.addPartiallyKnownTrace(q, sigma, dataManager.getSubtrace(newStatePos, dataManager.traceSize()));
			dataManager.updateCKVT();
			if (dataManager.getC(dataManager.traceSize()) == null){
				localize(dataManager, W);
				dataManager.updateCKVT();
			}
		}
		LogManager.logConsole(dataManager.readableTrace());
		dataManager.getConjecture().exportToDot();
	}
	
	public LmConjecture createConjecture() {
		LmConjecture c = dataManager.getConjecture();
		LogManager.logInfo("Conjecture have " + c.getStateCount()
				+ " states and " + c.getTransitionCount() + " transitions : ");
		return c;
	}
	
	/**
	 * 
	 * @param trace omega the global trace of the automata, will be completed \in (IO)*
	 * @param inputSequences a subset of the characterization state \subset W \subset I*
	 * @return the position of the fully identified state in the GlobalTrace
	 */
	private int localize(DataManager dataManager, ArrayList<ArrayList<String>> inputSequences){
		LogManager.logInfo("Localizing...");
		//TODO
		dataManager.apply("a");
		dataManager.apply("a");
		dataManager.apply("a");
		dataManager.apply("a");
		dataManager.apply("a");
		dataManager.apply("b");
		ArrayList<ArrayList<String>> WResponses = new ArrayList<ArrayList<String>>();
		WResponses.add(new ArrayList<String>());
		WResponses.add(new ArrayList<String>());
		WResponses.get(0).add("0");
		WResponses.get(1).add("0");
		FullyQualifiedState s = dataManager.getFullyQualifiedState(WResponses);
		dataManager.setC(dataManager.traceSize()-1, s);
		LogManager.logInfo("Localized ! We are b/0 after " + s);
		return dataManager.traceSize()-1;
	}
}
