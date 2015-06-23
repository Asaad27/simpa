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
		ArrayList<ArrayList<String>> WResponses = localize_intern(dataManager, inputSequences);
		FullyQualifiedState s = dataManager.getFullyQualifiedState(WResponses);
		dataManager.setC(dataManager.traceSize()-WResponses.get(WResponses.size()-1).size(), s);
		return dataManager.traceSize() - WResponses.get(inputSequences.size()-1).size();

	}
	
	private ArrayList<ArrayList<String>> localize_intern(DataManager dataManager, ArrayList<ArrayList<String>> inputSequences){
		if (inputSequences.size() == 1){
			ArrayList<ArrayList<String>> WResponses = new ArrayList<ArrayList<String>>();
			WResponses.add(dataManager.apply(inputSequences.get(0)));
			return WResponses;
		}
		LogManager.logInfo("Localizer : Localize with " + inputSequences);
		int n = 3;//TODO find how this parameter is obtained
		
		ArrayList<ArrayList<String>> Z1 = new ArrayList<ArrayList<String>>(inputSequences);
		Z1.remove(Z1.size()-1);
		ArrayList<ArrayList<ArrayList<String>>> localizerResponses = new ArrayList<ArrayList<ArrayList<String>>>();
		LogManager.logInfo("Localizer : Applying " + (2*n-1) + " times localize(" + Z1 + ")");
		for (int i = 0; i < 2*n - 1; i++){
			localizerResponses.add(localize_intern(dataManager, Z1));
		}
		
		int j = n;
		boolean isLoop = false;
		while (!isLoop){
			j--;
			assert (j>=0) : "no loop was found";
			isLoop = true;
			for (int m = 0; m < n-1; m++){
				if (!localizerResponses.get(j+m).equals(localizerResponses.get(n+m))){
					isLoop = false;
					LogManager.logInfo("it's not a loop : ["+(j+m)+"] = " + localizerResponses.get(j+m) +
							" ≠ [" + (n+m) + "=" + localizerResponses.get(n+m));
					break;
				}
			}
		}
		LogManager.logInfo("Localizer : Found a loop of size " + (n-j));
		LogManager.logInfo("Localizer : We know that applying localize_intern(" + Z1 + ") will produce " + localizerResponses.get(j+n-1));
		
		ArrayList<ArrayList<String>> WResponses = localizerResponses.get(j+n-1);
		ArrayList<ArrayList<String>> Z2 = new ArrayList<ArrayList<String>>(Z1);
		Z2.remove(Z2.size()-1);
		Z2.add(inputSequences.get(inputSequences.size()-1));
		ArrayList<ArrayList<String>> Z2Responses = localize_intern(dataManager, Z2);
		WResponses.add(Z2Responses.get(Z2Responses.size()-1));
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < inputSequences.size(); i++){
			s.append(new LmTrace(inputSequences.get(i),WResponses.get(i)) + ", ");
		}
		LogManager.logInfo("Localizer : Before " + inputSequences.get(inputSequences.size()-1) + " we were in " + s);
		assert WResponses.size() == inputSequences.size();
		return WResponses;
	}
}
