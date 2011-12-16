package learner;

import learner.efsm.table.LiLearner;
import learner.mealy.table.LmLearner;
import learner.mealy.tree.SigmaLearner;
import main.Options;
import automata.Automata;
import drivers.Driver;

public abstract class Learner {
	protected boolean addtolog = true;
	
	public void stopLog(){
		addtolog = false;
	}
	
	public void startLog(){
		addtolog = true;
	}
	
	protected abstract void completeDataStructure();
	
	public abstract Automata createConjecture();
	
	public abstract void learn();
	
	public static Learner getLearnerFor(Driver driver) throws Exception{
		switch(driver.type){
		case EFSM:
			return new LiLearner(driver);
		case MEALY:
			if (Options.TREEINFERENCE) return new SigmaLearner(driver);
			return new LmLearner(driver);
		default:
			return null;
		}
	}	
}
