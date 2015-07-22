package learner.mealy.rivestShapire;

import java.util.List;

import tools.loggers.LogManager;
import learner.Learner;
import learner.mealy.table.LmLearner;
import automata.mealy.OutputSequence;
import drivers.mealy.MealyDriver;

class StateDriver extends MealyDriver {
	class ThreadEndException extends RuntimeException{
		private static final long serialVersionUID = -2529130613268413483L;
		
	}
	private MealyDriver realDriver;
	protected OutputSequence homingSequenceResponse;
	private Learner stateLearner;
	private RivestShapireLearner learner;
	protected Thread thread;
	private boolean resetDone;
	protected boolean paused;
	private String prefix;

	/**
	 * should be invoke only if we are in  the initial state of this driver (i.e reset() has no effect)
	 */
	StateDriver(MealyDriver realDriver, OutputSequence response, RivestShapireLearner learner){
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

	public void reset(){
		LogManager.logInfo("reset call for state " + homingSequenceResponse);
		if (resetDone){
			LogManager.logInfo("    already in state after " + homingSequenceResponse);
			return;
		}
		paused = true;
		learner.resetCall();
		while (paused && learner.finishedLearner == null){
			Thread.yield();
		}
		if (learner.finishedLearner != null)
			throw new ThreadEndException();
		return;
//		StateDriver next = learner.home();
//		if (next == this){
//			LogManager.logInfo("    staying in same state (" + homingSequenceResponse + ") keep hand");
//			return;
//		}
//		LogManager.logInfo("    moved in an other state (" + next.homingSequenceResponse + ") give hand");
//		synchronized(next.thread){
//			LogManager.logInfo("    " + homingSequenceResponse + " notify " +  next.homingSequenceResponse);
//			next.thread.notify();
//			next.thread.interrupt();
//		}
//		try {
//			synchronized(thread){
//				LogManager.logInfo("    " + homingSequenceResponse + " will now wait");
//				thread.wait();
//				LogManager.logInfo("    " + homingSequenceResponse + " is no longer waiting");
//
//			}
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			//e.printStackTrace();
//			//throw new RuntimeException(e);
//			LogManager.logInfo(homingSequenceResponse+" interrupted while waiting");
//		}
	}

	public RivestShapireLearner getLearner() {
		return learner;
	}

	public Learner getStateLearner() {
		return stateLearner;
	}
	
	protected void unpause(){
		paused = false;
	}
	
	public void killThread(){
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//thread.stop();
	}

	public String getPrefix() {
		return prefix;
	}
}
