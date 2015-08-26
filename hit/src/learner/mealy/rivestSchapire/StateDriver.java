package learner.mealy.rivestSchapire;

import java.util.List;

import tools.loggers.LogManager;
import learner.Learner;
import learner.mealy.table.LmLearner;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.OutputSequence;
import drivers.mealy.MealyDriver;

class StateDriver extends MealyDriver {
	class ThreadEndException extends RuntimeException{
		private static final long serialVersionUID = -2529130613268413483L;

	}
	private MealyDriver realDriver;
	protected OutputSequence homingSequenceResponse;
	private Learner stateLearner;
	private RivestSchapireLearner learner;
	protected Thread thread;
	private boolean resetDone;
	protected boolean paused;
	private String prefix;

	/**
	 * should be invoke only if we are in  the initial state of this driver (i.e reset() has no effect)
	 */
	StateDriver(MealyDriver realDriver, OutputSequence response, RivestSchapireLearner learner){
		super(realDriver.getSystemName() + " for state " + response);
		homingSequenceResponse = response;
		StringBuilder prefixBuilder = new StringBuilder();
		prefixBuilder.append("[");
		for (String o : homingSequenceResponse.sequence)
			prefixBuilder.append(o+"\t");
		prefixBuilder.setCharAt(prefixBuilder.length()-1, ']');
		prefixBuilder.append("  \t");
		prefix = prefixBuilder.toString();
		this.realDriver = realDriver;
		this.learner = learner;
		resetDone = true;
		stateLearner = new LmLearner(this);
		paused = true;
		class R implements Runnable{
			private Learner learner;
			private StateDriver d;
			R(Learner l, StateDriver s){
				learner = l;
				d = s;
			}
			public void run(){
				try{
					LogManager.logInfo("thread started");
					learner.learn();
					d.getLearner().finishedLearner = d;
					LogManager.logInfo(d.homingSequenceResponse + " learner has finish");
				}catch(ThreadEndException e){
					LogManager.logInfo(d.homingSequenceResponse + " interrupted");
				}catch(Throwable e){
					LogManager.logInfo("Exception caught in thread " + homingSequenceResponse);
					LogManager.logException("in thread "+homingSequenceResponse, new Exception(e));
					d.learner.threadThrown = e;
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

	public InputSequence getShortestCounterExemple(Mealy m){
		LogManager.logInfo("reset the driver in order to get the initial state");
		reset();
		return realDriver.getShortestCounterExemple(null,m,m.getInitialState());
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
		while (paused && learner.finishedLearner == null && learner.threadThrown == null){
			Thread.yield();
		}
		if (learner.finishedLearner != null || learner.threadThrown != null)
			throw new ThreadEndException();
		return;
	}

	public RivestSchapireLearner getLearner() {
		return learner;
	}

	public Learner getStateLearner() {
		return stateLearner;
	}

	protected void unpause(){
		paused = false;
	}

	public void killThread(){
		try {
			thread.join();
		} catch (InterruptedException e) {
		}
	}

	public String getPrefix() {
		return prefix;
	}
}
