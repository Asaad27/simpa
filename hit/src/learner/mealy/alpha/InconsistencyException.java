package learner.mealy.alpha;

import java.util.List;

public class InconsistencyException extends Exception{
	
	public int index;
	public List<StateLabel> localLabelling;
	
	public InconsistencyException(){
		super();
	}
	
	public InconsistencyException(int index,List<StateLabel> localLabelling){
		super();
		this.index=index;
		this.localLabelling=localLabelling;
		
	}

}
