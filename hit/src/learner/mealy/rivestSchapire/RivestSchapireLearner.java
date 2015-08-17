package learner.mealy.rivestSchapire;

import java.util.HashMap;
import java.util.Map;

import automata.Automata;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.OutputSequence;
import drivers.mealy.MealyDriver;
import learner.Learner;
import learner.mealy.LmTrace;
import tools.loggers.LogManager;

public class RivestSchapireLearner extends Learner {
	private InputSequence homingSequence;
	private MealyDriver driver;
	private Map<OutputSequence,StateDriver> drivers;
	protected StateDriver finishedLearner;
	private Automata conjecture = null;

	public RivestSchapireLearner(MealyDriver driver) {
		this.driver = driver;
		drivers = new HashMap<OutputSequence,StateDriver>();
	}

	@Override
	public Mealy createConjecture() {
		if (conjecture == null){
			conjecture = finishedLearner.getStateLearner().createConjecture();
			conjecture.invalideateInitialsStates();
		}
		return (Mealy) conjecture;
	}

	@Override
	public void learn() {
		driver.reset();
		LogManager.logStep(LogManager.STEPOTHER, "Computing homing sequence");
		homingSequence = driver.getHomingSequence();
		LogManager.logStep(LogManager.STEPOTHER,"Inferring the system");
		LogManager.logConsole("Inferring the system (global)");
		//StateDriver first = home();
		//first.unpause();
		resetCall();
		while (finishedLearner == null)
			Thread.yield();
		LogManager.setPrefix("");
		LogManager.logStep(LogManager.STEPOTHER,"killing threads");
		for (StateDriver s : drivers.values())
			s.killThread();
		createConjecture().exportToDot();
	}
	
	protected StateDriver home(){
		OutputSequence output = new OutputSequence();
		for (String i : homingSequence.sequence)
			output.addOutput(driver.execute(i));
		return getOrCreateStateDriver(output);
	}
//	protected void endStep();
	protected StateDriver getOrCreateStateDriver(OutputSequence homingResponse){
		StateDriver sd = drivers.get(homingResponse);
		if (sd == null){
			LogManager.logInfo("new state found : " + new LmTrace(homingSequence, homingResponse));
			sd = new StateDriver(driver, homingResponse, this);
			drivers.put(homingResponse, sd);
		}
		return sd;
	}

	public void resetCall() {
		LogManager.setPrefix("");
		StateDriver next = home();
		LogManager.logInfo("giving hand to " + next.homingSequenceResponse);
		LogManager.setPrefix(next.getPrefix());
		next.unpause();
	}

}
