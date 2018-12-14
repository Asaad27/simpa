package learner.mealy.rivestSchapire;

import java.util.List;

import automata.State;
import automata.mealy.OutputSequence;
import drivers.mealy.MealyDriver;
import learner.Learner;
import learner.mealy.CeExposedUnknownStateException;
import learner.mealy.LmConjecture;
import learner.mealy.LmTrace;
import learner.mealy.table.LmLearner;
import options.learnerOptions.OracleOption;
import stats.StatsEntry_OraclePart;
import tools.loggers.LogManager;

class StateDriver extends MealyDriver {
	class ThreadEndException extends RuntimeException{
		private static final long serialVersionUID = -2529130613268413483L;

	}
	private MealyDriver realDriver;
	protected OutputSequence homingSequenceResponse;
	private Learner stateLearner;
	protected RivestSchapireLearner learner;
	protected Thread thread;
	private boolean resetDone;
	protected boolean paused;
	private String prefix;
	private int globalTraceLengthBeforeLastCE = 0;

	/**
	 * should be invoke only if we are in  the initial state of this driver (i.e reset() has no effect)
	 */
	StateDriver(MealyDriver realDriver, OutputSequence response, RivestSchapireLearner learner){
		super(realDriver.getSystemName() + " for state " + response);
		homingSequenceResponse = response;
		StringBuilder prefixBuilder = new StringBuilder();
		prefixBuilder.append("[");
		for (String o : homingSequenceResponse.sequence)
			prefixBuilder.append(o + ", ");
		if (homingSequenceResponse.getLength()==0)
			prefixBuilder.append("empty ");
		prefixBuilder.setCharAt(prefixBuilder.length()-1, ']');
		prefixBuilder.append(", ");
		prefix = prefixBuilder.toString();
		this.realDriver = realDriver;
		this.learner = learner;
		resetDone = true;
		stateLearner = (learner.hIsGiven)
				? new LmLearner(this, learner.options.lmOptions)
				: new LmForRSLearner(this, learner.options.lmOptions,
						learner.options.seedForProbabilistic);
		paused = true;
		class R implements Runnable{
			private Learner learner;
			private StateDriver d;
			R(Learner l, StateDriver s){
				learner = l;
				d = s;
			}
			public void run(){
				d.learner.lock.lock();
				try{
					LogManager.logInfo("thread started");
					learner.learn();
					d.getLearner().finishedLearner = d;
					LogManager.logInfo(d.homingSequenceResponse + " learner has finish");
				}catch(ThreadEndException e){
					LogManager.logInfo(d.homingSequenceResponse + " interrupted");
				} catch (KnownTracesTree.InconsistencyException e) {
					d.learner.threadThrown = e;
				}catch(Throwable e){
					LogManager.logInfo("Exception caught in thread " + homingSequenceResponse);
					LogManager.logException("in thread "+homingSequenceResponse, new Exception(e));
					d.learner.threadThrown = e;
				}finally {
					d.learner.lock.unlock();
				}
			}
		}
		thread = new Thread(new R(stateLearner, this));
		thread.start();
	}

	protected void computeStep(){
		thread.notify();
	}

	public String execute(String i){
		resetDone = false;
		return realDriver.execute(i);
	}

	public List<String> getInputSymbols(){
		return realDriver.getInputSymbols();
	}

	@Override
	public Integer getStateCount() {
		return realDriver.getStateCount();
	}

	//	//this let us to have a global dictionary for used CE.
	//	public InputSequence getRandomCounterExemple(Mealy c){
	//		return realDriver.getRandomCounterExemple(c); // this do not work because returned CE start from initial state of realDriver
	//	}

	public void reset(){
		LogManager.logInfo("reset call for state " + homingSequenceResponse);
		if (resetDone){
			LogManager.logInfo("    already in state after " + homingSequenceResponse);
			return;
		}
		paused = true;
		learner.resetCall();
		learner.lock.unlock();//We let the next thread or main thread take the hand.
		while (paused && learner.finishedLearner == null && learner.threadThrown == null){
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					throw new RuntimeException("the thread is not supposed to be interrupted");
				}
			}
		}
		learner.lock.lock();//we take the hand so main thread will stop notifying
		if (learner.finishedLearner != null || learner.threadThrown != null)
			throw new ThreadEndException();
		resetDone = true;
		return;
	}

	@Override
	public boolean getCounterExample(OracleOption options,
			LmConjecture conjecture, State conjectureStartingState,
			List<LmTrace> appliedSequences, Boolean forbidReset,
			StatsEntry_OraclePart oracleStats)
			throws CeExposedUnknownStateException {
		learner.stats.counterExampleCalled();
		return super.getCounterExample(options, conjecture,
				conjectureStartingState, appliedSequences, forbidReset,
				oracleStats);
	}

	public RivestSchapireLearner getLearner() {
		return learner;
	}

	public Learner getStateLearner() {
		return stateLearner;
	}

	protected void unpause(){
		paused = false;
		synchronized ((this)) {
			notify();
		}
	}

	/**
	 * this method supposed that the inference is finished, i.e. learner.finishedLearner != null || learner.threadThrown != null
	 * otherwise the thread may wait again and join will not work
	 */
	public void killThread(){
		try {
			synchronized (this) {
				notify();
			}
			thread.join();
		} catch (InterruptedException e) {
		}
	}

	public String getPrefix() {
		return prefix;
	}

	public boolean isAfterReset() {
		return resetDone;
	}

	public int getGlobalTraceLengthBeforeLastCE() {
		return globalTraceLengthBeforeLastCE;
	}
}
