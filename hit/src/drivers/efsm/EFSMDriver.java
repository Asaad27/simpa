package drivers.efsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import tools.loggers.LogManager;
import automata.Automata;
import automata.State;
import automata.efsm.EFSM;
import automata.efsm.EFSMTransition;
import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedInputSequence;
import automata.efsm.ParameterizedOutput;
import drivers.Driver;

public abstract class EFSMDriver extends Driver{
	protected EFSM automata;
	protected State currentState;
	
	public enum Types {STRING, NUMERIC, NOMINAL};

	public EFSMDriver(EFSM automata){
		super();
		type = DriverType.EFSM;
		this.automata = automata;
	}
	
	public ParameterizedOutput execute(ParameterizedInput pi) {
		ParameterizedOutput po = null;
		if (!pi.isEpsilonSymbol()){
			numberOfAtomicRequest++;
			EFSMTransition currentTrans = null;
			for(EFSMTransition t : (List<EFSMTransition>)automata.getTransitions()){
				if (t.getFrom().equals(currentState) && t.getInput().equals(pi.getInputSymbol())){
					if (t.getOutputParameters(pi.getParameters()) != null){
						currentTrans = t;
						break;
					}
				}
			}
			if (currentTrans != null){
				po = new ParameterizedOutput(currentTrans.getOutput(), currentTrans.getOutputParameters(pi.getParameters()));
				currentState = currentTrans.getTo();					
			}else{
				po = new ParameterizedOutput();
			}
			LogManager.logRequest(pi, po);
		}
		return po;
	}
	
	public List<String> getInputSymbols(){
		List<String> is = new ArrayList<String>();
		for(EFSMTransition t : automata.getTransitions()){
			if (!is.contains(t.getInput())) is.add(t.getInput());
		}
		Collections.sort(is);
		return is;
	}
	
	public List<String> getOutputSymbols(){
		List<String> os = new ArrayList<String>();
		for(EFSMTransition t : automata.getTransitions()){
			if (!os.contains(t.getOutput())) os.add(t.getOutput());
		}
		Collections.sort(os);
		return os;		
	}
	
	@Override
	public String getSystemName(){
		return automata.getName();
	}
	
	public abstract HashMap<String, List<ArrayList<Parameter>>> getDefaultParamValues(); 
	
	public abstract TreeMap<String, List<String>> getParameterNames();
	
	@Override
	public void reset(){
		super.reset();
		if (automata != null){
			automata.reset();
			currentState = automata.getInitialState();
		}		
	}	
	
	public TreeMap<String, List<Parameter>> getInitState(){
		TreeMap<String, List<Parameter>>res = new TreeMap<String, List<Parameter>>();
		List<String> s = getInputSymbols();
		s.addAll(getOutputSymbols());
		for(int i=0; i<s.size(); i++){
			List<Parameter> init = new ArrayList<Parameter>();
			init.add(new Parameter("init", Types.NOMINAL));
			res.put(s.get(i), init);
		}
		return res;
	}
	
	@Override
	public ParameterizedInputSequence getCounterExample(Automata a){
		return null;		
	}
	
	@Override
	public boolean isCounterExample(Object ce, Object conjecture) {
		return false;
	}
}
