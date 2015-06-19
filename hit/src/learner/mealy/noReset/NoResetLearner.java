package learner.mealy.noReset;

import learner.Learner;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import learner.mealy.noReset.dataManager.DataManager;
import learner.mealy.noReset.dataManager.FullyQualifiedState;
import drivers.mealy.MealyDriver;

import java.util.ArrayList;

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
		
		GlobalTrace trace = new GlobalTrace(driver);
		dataManager = new DataManager(driver, W, trace);
		
		//start of the algorithm
		localize(trace, W);
		
		while (!dataManager.isFullyKnown()){
			LogManager.logLine();
			int qualifiedStatePos;
			LmTrace sigma;
			if (trace.getC(trace.size()) != null){
				FullyQualifiedState q = trace.getC(trace.size());
				LogManager.logInfo("We already know the curent state (q = " + q + ")");	
				ArrayList<String> alpha = dataManager.getShortestAlpha(q);
				trace.apply(alpha);
				dataManager.updateC();//to get the new state, should be automated in trace
				assert trace.getC(trace.size()) != null;
				qualifiedStatePos = trace.size();
				FullyQualifiedState quallifiedState = trace.getC(qualifiedStatePos);
				ArrayList<String> X = dataManager.getxNotInR(quallifiedState);
				String x=X.get(0); //here we CHOOSE to take the first
				LogManager.logInfo("We choose x = " + x + " in " + X);		
				String o = trace.apply(x);
				sigma = new LmTrace(x,o);
				LogManager.logInfo("So sigma = " + sigma);	
				assert trace.getC(trace.size()) == null : "we are trying to quallify this state, that should not be already done.";
			}else{
				LogManager.logInfo("We don't know the curent state");	
				qualifiedStatePos = trace.size()-1;
				while (trace.getC(qualifiedStatePos) == null)
					qualifiedStatePos --;
				LogManager.logInfo("last quallified state is " + trace.getC(qualifiedStatePos));
				sigma = trace.subtrace(qualifiedStatePos,trace.size());
				LogManager.logInfo("We got sigma = "+ sigma);
			}
			FullyQualifiedState q = trace.getC(qualifiedStatePos);
			ArrayList<ArrayList<String>> allowed_W = dataManager.getwNotInK(q, sigma);
			ArrayList<String> w = allowed_W.get(0); //here we CHOOSE to take the first.
			LogManager.logInfo("We choose w = " + w + " in " + allowed_W);		
			int newStatePos = trace.size();
			trace.apply(w);
			//trace.apply("b");
			LogManager.logInfo("We found that " + q + " followed by " + sigma + "give " +trace.subtrace(newStatePos, trace.size()));
			dataManager.addPartiallyKnownTrace(q, sigma, trace.subtrace(newStatePos, trace.size()));
			dataManager.updateC();
			if (trace.getC(trace.size()) == null){
				localize(trace, W);
				dataManager.updateC();
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
	private int localize(GlobalTrace trace, ArrayList<ArrayList<String>> inputSequences){
		LogManager.logInfo("Localizing...");
		//TODO
		trace.apply("a");
		trace.apply("a");
		trace.apply("a");
		trace.apply("a");
		trace.apply("a");
		trace.apply("b");
		ArrayList<ArrayList<String>> WResponses = new ArrayList<ArrayList<String>>();
		WResponses.add(new ArrayList<String>());
		WResponses.add(new ArrayList<String>());
		WResponses.get(0).add("0");
		WResponses.get(1).add("0");
		FullyQualifiedState s = DataManager.instance.getFullyQualifiedState(WResponses);
		trace.setC(trace.size()-1, s);
		LogManager.logInfo("Localized ! We are b/0 after " + s);
		return trace.size()-1;
	}
}
