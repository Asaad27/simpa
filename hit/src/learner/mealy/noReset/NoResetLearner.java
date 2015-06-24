package learner.mealy.noReset;

import learner.Learner;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import learner.mealy.noReset.dataManager.DataManager;
import learner.mealy.noReset.dataManager.FullyKnownTrace;
import learner.mealy.noReset.dataManager.FullyQualifiedState;
import drivers.mealy.MealyDriver;

import java.util.ArrayList;
import java.util.Set;

import tools.Utils;
import tools.loggers.LogManager;

public class NoResetLearner extends Learner {
	private MealyDriver driver;
	private DataManager dataManager;
	protected ArrayList<ArrayList<String>> W;
	private int n;//the maximum number of states

	public NoResetLearner(MealyDriver d){
		driver = d;
	}
	
	public void learn(){
		LogManager.logInfo("Inferring the system");
		LogManager.logConsole("Inferring the system");

		n = 3;//TODO find how this parameter is obtained
		//TODO getW;
		W = new ArrayList<ArrayList<String>>();//Characterization set
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
		if (check())
			LogManager.logInfo("The computed conjecture seems to be coherent with the driver");
		else
			LogManager.logInfo("The computed conjecture is not correct");
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
	
	private boolean check(){
		LogManager.logStep(LogManager.STEPOTHER, "checking the computed conjecture");
		NoResetMealyDriver generatedDriver = new NoResetMealyDriver(dataManager.getConjecture());
		generatedDriver.stopLog();
		DataManager generated = new DataManager(generatedDriver, W);
		int knownPos = localize(generated, W);
		FullyQualifiedState knownState = dataManager.getFullyQualifiedState(generated.getC(knownPos).getWResponses());
		while(knownPos != generated.traceSize()){
			boolean updated = false;
			for (FullyKnownTrace v : knownState.getVerifiedTrace())
			if (generated.getSubtrace(knownPos, knownPos + v.getTrace().size()).equals(v.getTrace())){
				knownState = v.getEnd();
				knownPos += v.getTrace().size();
				updated = true;
				break;
			}
			assert updated : "V is not well defined" ;
		}
		FullyQualifiedState currentState = knownState;
		int i = 0;
		while (currentState != dataManager.getC(dataManager.traceSize())){
			if (dataManager.getC(i) == currentState){
				generated.apply(dataManager.getSubtrace(i, i+1).getInputsProjection());
				currentState = dataManager.getC(i+1);
			}
			assert currentState != null;
			i++;
		}
		//Now the two automata are in same state.
		//We can do a random walk
		
		int max_try = driver.getInputSymbols().size() * n * 10;
		dataManager = null;//we use directly the driver for the walk so dataManager is not up to date;
		driver.stopLog();
		for (int j = 0; j < max_try; j++){
			int rand = Utils.randInt(driver.getInputSymbols().size());
			String input = driver.getInputSymbols().get(rand);
			if (!driver.execute(input).equals(generated.apply(input)))
				return false;
		}
		
		return true;
	}
}
