package learner.mealy.rivestShapire;

import java.util.HashMap;
import java.util.Map;

import learner.Learner;
import learner.mealy.LmTrace;
import tools.loggers.LogManager;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.OutputSequence;
import drivers.mealy.MealyDriver;

public class RivestShapireLearner extends Learner {
	private InputSequence homingSequence;
	private MealyDriver driver;
	private Map<OutputSequence,StateDriver> drivers;
	protected StateDriver finishedLearner;

	public RivestShapireLearner(MealyDriver driver) {
		this.driver = driver;
		drivers = new HashMap<OutputSequence,StateDriver>();
	}

	@Override
	public Mealy createConjecture() {
		return (Mealy) finishedLearner.getStateLearner().createConjecture();
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
