/********************************************************************************
 * Copyright (c) 2015,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 *     Roland GROZ
 ********************************************************************************/
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
import main.simpa.Options;
import main.simpa.Options.LogLevel;
import stats.StatsEntry;
import stats.attribute.Attribute;
import tools.loggers.LogManager;

public class RivestSchapireLearner extends Learner {
	private InputSequence homingSequence;
	private MealyDriver driver;
	private Map<OutputSequence,StateDriver> drivers;
	protected StateDriver finishedLearner;
	protected Throwable threadThrown = null;
	private Automata conjecture = null;
	protected RivestSchapireStatsEntry stats;
	protected Lock lock = new ReentrantLock();//When a learner is computing, it take the lock. When the lock is free, the main thread try to notify a stateDriver
	protected int n=-1;
	protected boolean hIsGiven=true;
	protected RivestSchapireOptions options;
	
	public RivestSchapireLearner(MealyDriver driver,
			RivestSchapireOptions options) {
		this.driver = driver;
		options.updateWithDriver(driver);
		this.options = options;
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
		long start = System.nanoTime();
		if (options.probabilisticRS()) {
			n = options.getStateNumberBound();
			System.out.println(n);
			hIsGiven = false;
			homingSequence = new InputSequence();
		} else {
			LogManager.logStep(LogManager.STEPOTHER,
					"Computing homing sequence");
			try {
				homingSequence = driver.getHomingSequence();
			} catch (UnableToComputeException e) {
				throw new RuntimeException(e);
			}
			hIsGiven = true;
		}
		LogManager.logStep(LogManager.STEPOTHER, "Inferring the system");
		LogManager.logConsole("Inferring the system (global)");
		stats = new RivestSchapireStatsEntry(driver, hIsGiven, options);

		boolean hIsCorrect = hIsGiven;
		do {
			hIsCorrect = true;
			try {
				learn(homingSequence);
			} catch (KnownTracesTree.InconsistencyException e) {
				hIsCorrect = false;
				homingSequence.addInputSequence(e.seen.getInputsProjection());
		for (StateDriver s : drivers.values())
			s.killThread();
		System.out.println("extended\n\n\n\n");
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		} while (!hIsCorrect);
		stats.setDuration(((float) (System.nanoTime() - start)) / 1000000000);
		assert finishedLearner.getGlobalTraceLengthBeforeLastCE() != 0;
		stats.setTraceLength(
				finishedLearner.getGlobalTraceLengthBeforeLastCE());
		stats.setLearnerNumber(drivers.size());
		if (Options.getLogLevel() == LogLevel.ALL)
			createConjecture().exportToDot();
		stats.updateWithConjecture(createConjecture());
		stats.updateWithHomingSequence(homingSequence);
		stats.setOracle(finishedLearner.getStateLearner().getStats()
				.get(Attribute.ORACLE_USED));
	}

	protected void learn(InputSequence homingSequence) throws Throwable {
		if (Options.getLogLevel() != LogLevel.LOW)
			LogManager.logConsole(
					"learning with homing sequence " + homingSequence);
		LogManager.logInfo("learning with homing sequence h=" + homingSequence);
		drivers = new HashMap<OutputSequence, StateDriver>();
		resetCall();
		while (finishedLearner == null){
			if (threadThrown != null){
				LogManager.setPrefix("");
				for (StateDriver s : drivers.values())
					s.killThread();
				Throwable e = threadThrown;
				threadThrown = null;
				throw e;
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
		// The garbage collection was removed because it really slow down the
		// algorithm.
		// runtime.gc();
		stats.updateMemory((int) (runtime.totalMemory() - runtime.freeMemory()));
	    LogManager.setPrefix("");
		StateDriver next = home();
		LogManager.logInfo("giving hand to " + next.homingSequenceResponse);
		LogManager.setPrefix(next.getPrefix());
		next.unpause();
	}

	@Override
	public StatsEntry getStats(){
		return stats;
	}
}
