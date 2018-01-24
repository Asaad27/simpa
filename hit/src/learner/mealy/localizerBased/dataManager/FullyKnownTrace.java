package learner.mealy.localizerBased.dataManager;

import learner.mealy.LmTrace;

public class FullyKnownTrace {
	private FullyQualifiedState start;
	private FullyQualifiedState end;
	private LmTrace trace;
	
	public FullyKnownTrace(FullyQualifiedState start, LmTrace trace, FullyQualifiedState end){
		this.start = start;
		this.trace = trace;
		this.end = end;
	}
	
	public FullyQualifiedState getStart(){
		return start;
	}
	
	public FullyQualifiedState getEnd(){
		return end;
	}
	
	public LmTrace getTrace(){
		return trace;//a secure way is to return trace.clone(). Should we use it ?
	}
	
	public String toString(){
		return start.toString() + 
				" followed by " + trace.toString() +
				" → " + end.toString();
	}
}
