package learner.mealy.noReset;

import learner.Learner;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import learner.mealy.noReset.dataManager.DataManager;
import learner.mealy.noReset.dataManager.FullyKnownTrace;
import learner.mealy.noReset.dataManager.FullyQualifiedState;
import automata.mealy.InputSequence;
import automata.mealy.OutputSequence;
import main.simpa.Options;
import drivers.mealy.MealyDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import tools.Utils;
import tools.loggers.LogManager;

public class NoResetLearner extends Learner {
	private MealyDriver driver;
	private DataManager dataManager;
	protected ArrayList<InputSequence> W;
	private int n;//the maximum number of states

	public NoResetLearner(MealyDriver d){
		driver = d;
	}
	
	public void learn(){
		LogManager.logInfo("Inferring the system");
		LogManager.logConsole("Inferring the system");

		n = 4;//Options.MAXSTATES;//TODO find how this parameter is obtained
		//TODO getW;
		W = new ArrayList<InputSequence>();//Characterization set
		W.add(new InputSequence());
		W.add(new InputSequence());
		W.get(0).addInput("INVITE");
		W.get(1).addInput("BYE");
		W.get(1).addInput("INVITE");
		StringBuilder logW = new StringBuilder("Used characterization set : [");
		for (InputSequence w : W){
			logW.append(w + ", ");
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
				InputSequence alpha = dataManager.getShortestAlpha(q);
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
			List<InputSequence> allowed_W = dataManager.getwNotInK(q, sigma);
			InputSequence w = allowed_W.get(0); //here we CHOOSE to take the first.
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
		if (checkRandomWalk()){
			LogManager.logConsole("The computed conjecture seems to be coherent with the driver");
			LogManager.logInfo("The computed conjecture seems to be coherent with the driver");
		}else{
			LogManager.logConsole("The computed conjecture is not correct");
			LogManager.logInfo("The computed conjecture is not correct");
		}
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
	private int localize(DataManager dataManager, List<InputSequence> inputSequences){
		LogManager.logInfo("Localizing...");
		List<OutputSequence> WResponses = localize_intern(dataManager, inputSequences);
		FullyQualifiedState s = dataManager.getFullyQualifiedState(WResponses);
		dataManager.setC(dataManager.traceSize()-WResponses.get(WResponses.size()-1).getLength(), s);
		return dataManager.traceSize() - WResponses.get(inputSequences.size()-1).getLength();

	}
	
	private List<OutputSequence> localize_intern(DataManager dataManager, List<InputSequence> inputSequences){
		if (inputSequences.size() == 1){
			List<OutputSequence> WResponses = new ArrayList<OutputSequence>();
			WResponses.add(dataManager.apply(inputSequences.get(0)));
			return WResponses;
		}
		LogManager.logInfo("Localizer : Localize with " + inputSequences);
		
		ArrayList<InputSequence> Z1 = new ArrayList<InputSequence>(inputSequences);
		Z1.remove(Z1.size()-1);
		ArrayList<List<OutputSequence>> localizerResponses = new ArrayList<List<OutputSequence>>();
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
					LogManager.logInfo("Tried size " + (n-j) +" : it's not a loop : ["+(j+m)+"] = (" + Z1 + " → " + localizerResponses.get(j+m) +
							") ≠ [" + (n+m) + "] = (" + Z1 + " → " + localizerResponses.get(n+m) + ")");
					break;
				}
			}
		}
		LogManager.logInfo("Localizer : Found a loop of size " + (n-j));
		LogManager.logInfo("Localizer : We know that applying localize_intern(" + Z1 + ") will produce " + localizerResponses.get(j+n-1));
		
		List<OutputSequence> WResponses = localizerResponses.get(j+n-1);
		List<InputSequence> Z2 = new ArrayList<InputSequence>(Z1);
		Z2.remove(Z2.size()-1);
		Z2.add(inputSequences.get(inputSequences.size()-1));
		List<OutputSequence> Z2Responses = localize_intern(dataManager, Z2);
		WResponses.add(Z2Responses.get(Z2Responses.size()-1));
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < inputSequences.size(); i++){
			s.append(new LmTrace(inputSequences.get(i),WResponses.get(i)) + ", ");
		}
		LogManager.logInfo("Localizer : Before " + inputSequences.get(inputSequences.size()-1) + " we were in " + s);
		assert WResponses.size() == inputSequences.size();
		return WResponses;
	}
	
	private boolean checkRandomWalk(){
		LogManager.logStep(LogManager.STEPOTHER, "checking the computed conjecture");
		NoResetMealyDriver generatedDriver = new NoResetMealyDriver(dataManager.getConjecture());
		generatedDriver.stopLog();
		generatedDriver.setCurrentState(dataManager.getC(dataManager.traceSize()).getState());
		
		//Now the two automata are in same state.
		//We can do a random walk
		
		int max_try = driver.getInputSymbols().size() * n * 10;
		dataManager = null;//we use directly the driver for the walk so dataManager is not up to date;
		driver.stopLog();
		for (int j = 0; j < max_try; j++){
			int rand = Utils.randInt(driver.getInputSymbols().size());
			String input = driver.getInputSymbols().get(rand);
			if (!driver.execute(input).equals(generatedDriver.execute(input)))
				return false;
		}
		
		return true;
	}
}
