package learner.mealy.rivestSchapire;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import automata.Automata;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.OutputSequence;
import drivers.mealy.MealyDriver;
import drivers.mealy.MealyDriver.UnableToComputeException;
import learner.Learner;
import learner.mealy.LmTrace;
import stats.StatsEntry;
import tools.loggers.LogManager;

public class RivestSchapireLearner extends Learner {
	private InputSequence homingSequence;
	private MealyDriver driver;
	private Map<OutputSequence,StateDriver> drivers;
	protected StateDriver finishedLearner;
	protected Throwable threadThrown = null;
	private Automata conjecture = null;
	private RivestSchapireStatsEntry stats;
	protected Lock lock = new ReentrantLock();//When a learner is computing, it take the lock. When the lock is free, the main thread try to notify a stateDriver

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
		try {
			homingSequence = driver.getHomingSequence();
		} catch (UnableToComputeException e) {
			throw new RuntimeException(e);
		}
		LogManager.logStep(LogManager.STEPOTHER,"Inferring the system");
		LogManager.logConsole("Inferring the system (global)");
		stats = new RivestSchapireStatsEntry(driver, homingSequence);
		//StateDriver first = home();
		//first.unpause();
		long start = System.nanoTime();
		resetCall();
		while (finishedLearner == null){
			if (threadThrown != null){
				LogManager.setPrefix("");
				for (StateDriver s : drivers.values())
					s.killThread();
				throw new RuntimeException(threadThrown);
			}
			lock.lock();//if we can lock that mean that no thread is computing. So we try to notify the one which is waiting.
			for (StateDriver s : drivers.values())
				if (!s.paused)
					synchronized (s) {
						s.notify();
					}
			lock.unlock();
			Thread.yield();
		}
		LogManager.setPrefix("");
		LogManager.logStep(LogManager.STEPOTHER,"killing threads");
		for (StateDriver s : drivers.values())
			s.killThread();
		stats.setDuration(((float)(System.nanoTime() - start))/ 1000000000);
		stats.setTraceLength(driver.numberOfAtomicRequest);
		stats.setLearnerNumber(drivers.size());
		createConjecture().exportToDot();
		stats.updateWithConjecture(createConjecture());
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
		stats.increaseresetCallNb();
		Runtime runtime = Runtime.getRuntime();
	    runtime.gc();
	    stats.updateMemory((int) (runtime.totalMemory() - runtime.freeMemory()));
	    LogManager.setPrefix("");
		StateDriver next = home();
		LogManager.logInfo("giving hand to " + next.homingSequenceResponse);
		LogManager.setPrefix(next.getPrefix());
		next.unpause();
	}

	public StatsEntry getStats(){
		return stats;
	}
}
