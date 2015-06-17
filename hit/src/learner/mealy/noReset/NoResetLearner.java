package learner.mealy.noReset;

import learner.Learner;
import learner.mealy.LmConjecture;
import learner.mealy.noReset.dataManager.FullyQualifiedState;
import drivers.mealy.MealyDriver;

import java.util.ArrayList;
import java.util.AbstractSet;
import java.util.HashSet;

import tools.loggers.LogManager;

public class NoResetLearner extends Learner {
	private LmConjecture T;
	private MealyDriver driver;

	public NoResetLearner (MealyDriver d){
		driver = d;
	}
	
	public void learn(){
		LogManager.logConsole("Inferring the system");
		GlobalTrace trace = new GlobalTrace(driver);
		T = new LmConjecture(driver);
		
		//TODO getW;
		ArrayList<String> W = new ArrayList<String>();//Characterization set
		W.add("a");
		W.add("b");
		StringBuilder logW = new StringBuilder("Used characterization set : [");
		for (String w : W){
			logW.append(w + ", ");
		}
		logW.append("]");
		LogManager.logInfo(logW.toString());
		
		AbstractSet<FullyQualifiedState> Q = new HashSet<FullyQualifiedState>();
		
		
	}
	
	public LmConjecture createConjecture() {
		return T;
	}
	
	/**
	 * 
	 * @param trace omega the global trace of the automata, will be completed \in (IO)*
	 * @param inputSequences a subset of the characterization state \subset W \subset I*
	 * @return the position of the fully identified state in the GlobalTrace
	 */
	private int Localizer(GlobalTrace trace,	ArrayList<String> inputSequences){
		LogManager.logInfo("Localize");
		//TODO
		trace.apply("a");
		trace.apply("a");
		trace.apply("a");
		trace.apply("a");
		trace.apply("a");
		trace.apply("b");
		return trace.size()-2;
	}
}
